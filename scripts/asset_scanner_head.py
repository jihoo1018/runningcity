# ============================================================
#  🧩 asset_scanner_head.py
#  ────────────────────────────────────────────────
#  - head 카테고리만 스캔
#  - head/[ears, eyebrows, eyes, faces, heads, nose]/[style]/color.png
#  - category=head, subcategory=ears/eyebrows/etc, style=thick/anger/etc
#  - common 중 light/dark만 store(3 CR), 나머지는 gacha(0 CR)
# ============================================================

import pandas as pd
from pathlib import Path
import logging

# =============== 설정 ===============
ASSET_ROOT = Path(r"C:\Users\SSAFY\Desktop\S13P31A405\AssetsStore\spritesheets\head")
CSV_FILE = "output/boutique_items_head.csv"
LOG_FILE = "asset_scan_head.log"

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


def parse_head_path(png_path: Path):
    """
    head 경로 파싱

    예시:
    - ears/medium/black.png → subcategory='ears', style='medium', color='black'
    - eyebrows/thick/brown.png → subcategory='eyebrows', style='thick', color='brown'
    - eyes/anger/blue.png → subcategory='eyes', style='anger', color='blue'
    - faces/blush/tan.png → subcategory='faces', style='blush', color='tan'
    - heads/black.png → subcategory='heads', style=None, color='black'
    - nose/amber.png → subcategory='nose', style=None, color='amber'
    """
    parts = png_path.parts

    if len(parts) < 2:
        return None

    subcategory = parts[0]  # ears, eyebrows, eyes, faces, heads, nose

    # ✅ heads, nose는 style 없음
    if subcategory in ['heads', 'nose']:
        if len(parts) == 2:
            # heads/black.png, nose/amber.png
            style = None
            color = png_path.stem
        else:
            # 예외 처리: heads나 nose에 하위 폴더가 있는 경우
            logger.warning(f"⚠️  예상치 못한 구조: {png_path}")
            return None

    # ✅ ears, eyebrows, eyes, faces는 style 있음
    else:
        if len(parts) >= 3:
            # ears/medium/black.png
            style = parts[1]  # medium, thick, thin, anger, etc
            color = png_path.stem
        else:
            # style 없는 경우
            style = None
            color = png_path.stem

    category = 'head'

    # base_path 생성
    if style:
        base_path = f"\\AssetsStore\\spritesheets\\{category}\\{subcategory}\\{style}\\{color}.png"
        asset_key = f"{subcategory}_{style}_{color}"
        name = f"{style.title()} {subcategory.title()} {color.title()}"
    else:
        base_path = f"\\AssetsStore\\spritesheets\\{category}\\{subcategory}\\{color}.png"
        asset_key = f"{subcategory}_{color}"
        name = f"{subcategory.title()} {color.title()}"

    rarity = assign_rarity(color)
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


def scan_head():
    """head 카테고리 스캔"""
    logger.info(f"🔍 Head 스캔 시작: {ASSET_ROOT}")

    if not ASSET_ROOT.exists():
        logger.error(f"❌ 폴더 없음: {ASSET_ROOT}")
        return pd.DataFrame()

    items = []
    seen_keys = set()

    # head 하위 모든 PNG 스캔
    for png in ASSET_ROOT.rglob("*.png"):
        rel_path = png.relative_to(ASSET_ROOT)
        item_data = parse_head_path(rel_path)

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
    logger.info("🚀 Head Asset Scanner 시작")
    logger.info("=" * 60)

    df = scan_head()

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

    # ✅ style이 있는 아이템 vs 없는 아이템
    logger.info(f"\n🎨 스타일 분포:")
    with_style = df[df['style'].notna()]
    without_style = df[df['style'].isna()]
    logger.info(f"   - 스타일 있음: {len(with_style)}개")
    logger.info(f"   - 스타일 없음: {len(without_style)}개")

    # ✅ 상점 아이템만 출력
    logger.info(f"\n🏪 상점 판매 아이템 (3 CR):")
    store_items = df[df['obtain_method'] == 'store'].sort_values(['subcategory', 'style', 'color'])
    for idx, row in store_items.iterrows():
        style_str = row['style'] if pd.notna(row['style']) else '-'
        logger.info(f"   - {row['subcategory']:15s} | {style_str:15s} | {row['color']:10s} | {row['price_cr']} CR")

    # Common 가챠 아이템 (일부만)
    logger.info(f"\n🎰 Common 가챠 아이템 샘플 (0 CR):")
    common_gacha = df[(df['rarity'] == 'common') & (df['obtain_method'] == 'gacha')].sort_values(
        ['subcategory', 'style', 'color']).head(10)
    for idx, row in common_gacha.iterrows():
        style_str = row['style'] if pd.notna(row['style']) else '-'
        logger.info(f"   - {row['subcategory']:15s} | {style_str:15s} | {row['color']:10s}")
    if len(df[(df['rarity'] == 'common') & (df['obtain_method'] == 'gacha')]) > 10:
        logger.info(f"   ... 외 {len(df[(df['rarity'] == 'common') & (df['obtain_method'] == 'gacha')]) - 10}개")

    logger.info("=" * 60)
    logger.info("✅ 완료! boutique_items_head.csv 생성됨")
    logger.info("=" * 60)