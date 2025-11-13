# ============================================================
#  🧩 asset_scanner_v6.py
#  ────────────────────────────────────────────────
#  - 새로운 DB 구조에 맞춰 수정
#  - category, subcategory, style, color 분리
#  - base_path에 {animation} 패턴 사용
#  - walk 폴더만 스캔 (run/jump은 패턴 처리)
# ============================================================

import pandas as pd
from pathlib import Path
import logging

# =============== 설정 ===============
ASSET_ROOT = Path(r"/AssetsStore/spritesheets")
CSV_FILE = "../output/boutique_items.csv"
LOG_FILE = "asset_scan.log"

# 희귀도 키워드 (우선순위 순서대로)
RARITY_KEYWORDS = {
    "legendary": ["zombie", "platinum", "fur_gold"],
    "epic": ["bronze", "bright", "lavender", "fur_copper"],
    "rare": ["amber", "blue", "green", "pink", "purple", "olive", "taupe"],
    "common": ["black", "brown", "grey", "gray", "white", "tan", "light", "dark"],
}

PRICE_MAP = {
    "common": 3,
    "rare": 0,
    "epic": 0,
    "legendary": 0,
}

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


def parse_path(png_path: Path, category_path: Path):
    """
    PNG 파일 경로를 파싱해서 DB 필드 추출

    예시:
    - bodies/male/walk/black.png → subcategory='male', style=None, color='black'
    - clothes/male/longsleeve/walk/red.png → subcategory='longsleeve', style=None, color='red'
    - hair/long/blonde.png → subcategory='long', style=None, color='blonde'
    - head/eyes/anger/brown.png → subcategory='eyes', style='anger', color='brown'
    - head/nose/walk/amber.png → subcategory='nose', style=None, color='amber'
    """
    category = category_path.name
    rel_path = png_path.relative_to(category_path)
    parts = rel_path.parts
    color = png_path.stem

    # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    # bodies: male/walk/color.png
    # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    if category == 'bodies':
        subcategory = parts[0]  # male
        style = None
        base_path = f"\\AssetsStore\\spritesheets\\{category}\\{subcategory}\\{{animation}}\\{color}.png"
        asset_key = f"{subcategory}_{color}"
        name = f"{subcategory.title()} {color.title()} Body"

    # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    # clothes: male/longsleeve/walk/color.png
    # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    elif category == 'clothes':
        gender = parts[0]  # male
        subcategory = parts[1]  # longsleeve, shorts, etc
        style = None
        base_path = f"\\AssetsStore\\spritesheets\\{category}\\{gender}\\{subcategory}\\{{animation}}\\{color}.png"
        asset_key = f"{gender}_{subcategory}_{color}"
        name = f"{subcategory.replace('_', ' ').title()} {color.title()}"

    # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    # hair: style/color.png
    # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    elif category == 'hair':
        subcategory = parts[0]  # long, afro, pixie, etc
        style = None
        base_path = f"\\AssetsStore\\spritesheets\\{category}\\{subcategory}\\{color}.png"
        asset_key = f"{subcategory}_{color}"
        name = f"{subcategory.title()} {color.title()} Hair"

    # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    # head: subcategory/[style]/color.png
    # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    elif category == 'head':
        subcategory = parts[0]  # eyes, faces, eyebrows, nose, heads, ears

        # ✅ nose/heads는 애니메이션 폴더 무시
        if subcategory in ['nose', 'heads']:
            style = None

            # walk/run/jump 폴더가 있는지 확인
            has_animation_folder = len(parts) > 2 and parts[1] in ['walk', 'run', 'jump']

            if has_animation_folder:
                # {animation} 패턴 사용
                base_path = f"\\AssetsStore\\spritesheets\\{category}\\{subcategory}\\{{animation}}\\{color}.png"
            else:
                base_path = f"\\AssetsStore\\spritesheets\\{category}\\{subcategory}\\{color}.png"

            asset_key = f"{subcategory}_{color}"
            name = f"{subcategory.title()} {color.title()}"

        # ✅ ears도 애니메이션 폴더 처리
        elif subcategory in ['ears']:
            # ears/medium/walk/color.png 같은 구조
            if len(parts) > 3 and parts[2] in ['walk', 'run', 'jump']:
                style = parts[1]  # medium
                base_path = f"\\AssetsStore\\spritesheets\\{category}\\{subcategory}\\{style}\\{{animation}}\\{color}.png"
                asset_key = f"{subcategory}_{style}_{color}"
                name = f"{style.title()} {subcategory.title()} {color.title()}"
            elif len(parts) > 2:
                style = parts[1]  # medium
                base_path = f"\\AssetsStore\\spritesheets\\{category}\\{subcategory}\\{style}\\{color}.png"
                asset_key = f"{subcategory}_{style}_{color}"
                name = f"{style.title()} {subcategory.title()} {color.title()}"
            else:
                style = None
                base_path = f"\\AssetsStore\\spritesheets\\{category}\\{subcategory}\\{color}.png"
                asset_key = f"{subcategory}_{color}"
                name = f"{subcategory.title()} {color.title()}"

        # ✅ eyes/faces/eyebrows (style 있음)
        elif len(parts) > 2:
            style = parts[1]  # anger, thick, etc
            base_path = f"\\AssetsStore\\spritesheets\\{category}\\{subcategory}\\{style}\\{color}.png"
            asset_key = f"{subcategory}_{style}_{color}"
            name = f"{style.title()} {subcategory.title()} {color.title()}"

        # ✅ 기타 (style 없음)
        else:
            style = None
            base_path = f"\\AssetsStore\\spritesheets\\{category}\\{subcategory}\\{color}.png"
            asset_key = f"{subcategory}_{color}"
            name = f"{subcategory.title()} {color.title()}"

    else:
        return None

    rarity = assign_rarity(color)

    return {
        'category': category,
        'subcategory': subcategory,
        'style': style,
        'color': color,
        'name': name,
        'asset_key': asset_key,
        'base_path': base_path,
        'rarity': rarity,
        'price_cr': PRICE_MAP[rarity],
        'obtain_method': 'store' if rarity == 'common' else 'gacha',
    }


def scan_assets():
    """
    spritesheets 전체를 탐색
    - bodies/clothes: walk 폴더만 스캔 (run/jump은 패턴 처리)
    - hair/head: walk 폴더 우선, run/jump 제외
    """
    logger.info(f"🔍 전체 스캔 시작: {ASSET_ROOT}")

    if not ASSET_ROOT.exists():
        logger.error(f"❌ 폴더 없음: {ASSET_ROOT}")
        return pd.DataFrame()

    items = []
    seen_keys = set()

    for category_path in ASSET_ROOT.iterdir():
        if not category_path.is_dir():
            continue

        category = category_path.name
        logger.info(f"📂 {category} 카테고리 탐색 중...")

        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        # bodies, clothes: walk 폴더만 스캔
        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        if category in ['bodies', 'clothes']:
            walk_paths = list(category_path.rglob("walk"))

            if not walk_paths:
                logger.warning(f"⚠️  {category} 내 walk 폴더 없음")
                continue

            for walk_path in walk_paths:
                for png in walk_path.glob("*.png"):
                    item_data = parse_path(png, category_path)

                    if item_data and item_data['asset_key'] not in seen_keys:
                        items.append(item_data)
                        seen_keys.add(item_data['asset_key'])

                        if item_data['rarity'] == 'common':
                            logger.info(f"🟢 COMMON: {item_data['asset_key']}")
                        else:
                            logger.debug(f"✅ {item_data['asset_key']}")

        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        # hair, head: walk 우선, run/jump 제외
        # ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        else:
            for png in category_path.rglob("*.png"):
                # ✅ run, jump 폴더는 건너뛰기
                if any(part in ['run', 'jump'] for part in png.parts):
                    logger.debug(f"⏭️  스킵 (run/jump): {png.name}")
                    continue

                item_data = parse_path(png, category_path)

                if item_data and item_data['asset_key'] not in seen_keys:
                    items.append(item_data)
                    seen_keys.add(item_data['asset_key'])

                    if item_data['rarity'] == 'common':
                        logger.info(f"🟢 COMMON: {item_data['asset_key']}")
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
    logger.info("🚀 Asset Scanner v6 시작 (새 DB 구조 대응)")
    logger.info("=" * 60)

    df = scan_assets()

    if df.empty:
        logger.error("❌ 스캔 결과 없음 — 종료")
        exit(1)

    save_to_csv(df)

    # 통계 출력
    logger.info("\n📊 통계:")
    logger.info(f"   총 아이템: {len(df)}개")

    logger.info(f"\n   카테고리별:")
    for cat, count in df['category'].value_counts().items():
        logger.info(f"      - {cat}: {count}개")

    logger.info(f"\n   희귀도별:")
    for rarity, count in df['rarity'].value_counts().items():
        logger.info(f"      - {rarity}: {count}개")

    logger.info(f"\n   획득 방법별:")
    for method, count in df['obtain_method'].value_counts().items():
        logger.info(f"      - {method}: {count}개")

    # ✅ Common 아이템 상세 출력
    logger.info(f"\n🟢 Common 아이템 목록 (가격: 3 CR):")
    common_items = df[df['rarity'] == 'common'].sort_values(['category', 'subcategory', 'color'])

    current_category = None
    for idx, row in common_items.iterrows():
        if current_category != row['category']:
            current_category = row['category']
            logger.info(f"\n   [{current_category}]")

        logger.info(f"      └─ {row['subcategory']:15s} | {row['color']:10s}")

    # ✅ 애니메이션 패턴 확인
    logger.info(f"\n🎬 애니메이션 패턴 아이템:")
    animation_items = df[df['base_path'].str.contains('{animation}', regex=False)]
    logger.info(f"   총 {len(animation_items)}개 아이템이 애니메이션 패턴 사용")

    for cat, count in animation_items['category'].value_counts().items():
        logger.info(f"      - {cat}: {count}개")

    logger.info("=" * 60)
    logger.info("✅ 완료! boutique_items.csv 생성됨")
    logger.info("=" * 60)
