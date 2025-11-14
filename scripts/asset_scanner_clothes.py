# ============================================================
#  🧩 asset_scanner_clothes.py
#  ────────────────────────────────────────────────
#  - clothes 카테고리만 스캔
#  - clothes/male/[longsleeve,shorts,shortsleeves,tshirt]/walk/color.png
#  - male은 무시, longsleeve부터 subcategory
#  - walk 폴더만 스캔 (run/jump은 {animation} 패턴)
#  - common 중 light/dark만 store(3 CR), 나머지는 gacha(0 CR)
# ============================================================

import pandas as pd
from pathlib import Path
import logging

# =============== 설정 ===============
ASSET_ROOT = Path(r"C:\Users\SSAFY\Desktop\S13P31A405\AssetsStore\spritesheets\clothes")
CSV_FILE = "output/boutique_items_clothes.csv"
LOG_FILE = "log/asset_scan_clothes.log"

# 희귀도 키워드
RARITY_KEYWORDS = {
    "legendary": ["zombie", "platinum", "fur_gold"],
    "epic": ["bronze", "bright", "lavender", "fur_copper"],
    "rare": ["amber", "blue", "green", "pink", "purple", "olive", "taupe"],
    "common": ["black", "brown", "grey", "gray", "white", "tan", "light", "dark"],
}

# ✅ 상점 판매 가능 색상 (common 중에서)
STORE_COLORS = ["light", "dark"]

# ====================================

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[
        logging.FileHandler(LOG_FILE, encoding="utf-8"),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)


def assign_rarity(color: str) -> str:
    """색상명 기반 rarity 자동 지정"""
    lower = color.lower()
    for rarity, keywords in RARITY_KEYWORDS.items():
        for kw in keywords:
            if kw in lower:
                return rarity
    return "common"


def get_price_and_method(color: str, rarity: str):
    """
    색상과 희귀도에 따른 가격과 획득 방법 결정
    - common + light/dark → 3 CR, store
    - 그 외 → 0 CR, gacha
    """
    lower_color = color.lower()

    if rarity == "common" and any(store_color in lower_color for store_color in STORE_COLORS):
        return 3, "store"
    else:
        return 0, "gacha"


def parse_clothes_path(png_path: Path):
    """
    clothes 경로 파싱
    예: male/longsleeve/walk/red.png → subcategory='longsleeve', style=None, color='red'
        male/shorts/walk/blue.png → subcategory='shorts', style=None, color='blue'

    ✅ male은 무시하고 longsleeve부터 subcategory
    """
    parts = png_path.parts

    # clothes/male/longsleeve/walk/red.png
    if len(parts) < 4:
        return None

    # male(parts[0]) 무시
    subcategory = parts[1]  # longsleeve, shorts, shortsleeves, tshirt
    # walk(parts[2]) 무시
    color = png_path.stem  # red, blue, etc

    category = 'clothes'
    style = None
    base_path = f"\\AssetsStore\\spritesheets\\{category}\\male\\{subcategory}\\{{animation}}\\{color}.png"
    asset_key = f"male_{subcategory}_{color}"
    name = f"{subcategory.replace('_', ' ').title()} {color.title()}"
    rarity = assign_rarity(color)

    # ✅ 가격과 획득 방법 결정
    price_cr, obtain_method = get_price_and_method(color, rarity)

    return {
        'category': category,
        'subcategory': subcategory,
        'style': style,
        'color': color,
        'name': name,
        'asset_key': asset_key,
        'base_path': base_path,
        'rarity': rarity,
        'price_cr': price_cr,
        'obtain_method': obtain_method,
    }


def scan_clothes():
    """clothes 카테고리 스캔 (walk 폴더만)"""
    logger.info(f"🔍 Clothes 스캔 시작: {ASSET_ROOT}")

    if not ASSET_ROOT.exists():
        logger.error(f"❌ 폴더 없음: {ASSET_ROOT}")
        return pd.DataFrame()

    items = []
    seen_keys = set()

    # walk 폴더만 찾기
    walk_paths = list(ASSET_ROOT.rglob("walk"))

    if not walk_paths:
        logger.error(f"❌ walk 폴더를 찾을 수 없습니다!")
        return pd.DataFrame()

    logger.info(f"📂 {len(walk_paths)}개의 walk 폴더 발견")

    for walk_path in walk_paths:
        logger.info(f"   → {walk_path.relative_to(ASSET_ROOT)}")

        for png in walk_path.glob("*.png"):
            rel_path = png.relative_to(ASSET_ROOT)
            item_data = parse_clothes_path(rel_path)

            if item_data and item_data['asset_key'] not in seen_keys:
                items.append(item_data)
                seen_keys.add(item_data['asset_key'])

                # ✅ 상점 아이템 강조
                if item_data['obtain_method'] == 'store':
                    logger.info(f"🏪 STORE (3 CR): {item_data['asset_key']}")
                elif item_data['rarity'] == 'common':
                    logger.info(f"🟢 COMMON (gacha): {item_data['asset_key']}")
                else:
                    logger.debug(f"✅ {item_data['asset_key']}")

    df = pd.DataFrame(items)
    logger.info(f"✅ 총 {len(df)}개 아이템 스캔 완료")
    return df


def save_to_csv(df: pd.DataFrame):
    """CSV 저장"""
    output_columns = [
        'category', 'subcategory', 'style', 'color',
        'name', 'asset_key', 'base_path',
        'rarity', 'price_cr', 'obtain_method'
    ]

    output_path = Path(CSV_FILE)
    output_path.parent.mkdir(exist_ok=True)

    df[output_columns].to_csv(CSV_FILE, index=False, encoding="utf-8-sig")
    logger.info(f"💾 CSV 저장 완료: {CSV_FILE}")


if __name__ == "__main__":
    logger.info("=" * 60)
    logger.info("🚀 Clothes Asset Scanner 시작")
    logger.info("=" * 60)

    df = scan_clothes()

    if df.empty:
        logger.error("❌ 스캔 결과 없음 — 종료")
        exit(1)

    save_to_csv(df)

    # 통계 출력
    logger.info("\n📊 통계:")
    logger.info(f"   총 아이템: {len(df)}개")

    logger.info(f"\n   서브카테고리별:")
    for subcat, count in df['subcategory'].value_counts().items():
        logger.info(f"      - {subcat}: {count}개")

    logger.info(f"\n   희귀도별:")
    for rarity, count in df['rarity'].value_counts().items():
        logger.info(f"      - {rarity}: {count}개")

    logger.info(f"\n   획득 방법별:")
    for method, count in df['obtain_method'].value_counts().items():
        logger.info(f"      - {method}: {count}개")

    # ✅ 상점 아이템만 출력
    logger.info(f"\n🏪 상점 판매 아이템 (3 CR):")
    store_items = df[df['obtain_method'] == 'store'].sort_values(['subcategory', 'color'])
    for idx, row in store_items.iterrows():
        logger.info(f"   - {row['subcategory']:20s} | {row['color']:10s} | {row['price_cr']} CR")

    # Common 가챠 아이템
    logger.info(f"\n🎰 Common 가챠 아이템 (0 CR):")
    common_gacha = df[(df['rarity'] == 'common') & (df['obtain_method'] == 'gacha')].sort_values(
        ['subcategory', 'color'])
    for idx, row in common_gacha.iterrows():
        logger.info(f"   - {row['subcategory']:20s} | {row['color']:10s}")

    logger.info("=" * 60)
    logger.info("✅ 완료! boutique_items_clothes.csv 생성됨")
    logger.info("=" * 60)