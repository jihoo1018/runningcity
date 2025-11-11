# ============================================================
#  🧩 asset_scanner_v5.py
#  ────────────────────────────────────────────────
#  - spritesheets/ 아래 모든 카테고리 자동 스캔
#  - walk 폴더 기준 (run/jump은 존재한다고 가정)
#  - rarity: common / rare / unique / legendary
#  - price_cr: common만 500, 나머지는 0
# ============================================================

import pandas as pd
from pathlib import Path
import logging

# =============== 설정 ===============
# ✅ 네 실제 경로에 맞게 수정
ASSET_ROOT = Path(r"C:\Users\SSAFY\Desktop\S13P31A405\AssetsStore\spritesheets")
CSV_FILE = "output/boutique_items.csv"
LOG_FILE = "asset_scan.log"

# 희귀도 키워드
RARITY_KEYWORDS = {
    "legendary": ["zombie", "platinum", "fur_gold"],
    "unique": ["bronze", "bright", "lavender", "fur_copper"],
    "rare": ["amber", "blue", "green", "pink", "purple", "olive", "taupe"],
    "common": ["black", "brown", "grey", "gray", "white", "tan", "light", "dark"],
}

PRICE_MAP = {
    "common": 500,
    "rare": 0,
    "unique": 0,
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


def assign_rarity(asset_key: str) -> str:
    """색상명 기반 rarity 자동 지정"""
    lower = asset_key.lower()
    for rarity, keywords in RARITY_KEYWORDS.items():
        for kw in keywords:
            if kw in lower:
                return rarity
    return "common"


def scan_assets():
    """spritesheets 전체를 탐색해서 walk 폴더 기준으로 에셋 등록"""
    logger.info(f"🔍 전체 스캔 시작: {ASSET_ROOT}")

    if not ASSET_ROOT.exists():
        logger.error(f"❌ 폴더 없음: {ASSET_ROOT}")
        return pd.DataFrame()

    items = []

    # 1️⃣ spritesheets/ 하위 모든 카테고리(bodies, clothes, hair, head 등)
    for category_path in ASSET_ROOT.iterdir():
        if not category_path.is_dir():
            continue
        category = category_path.name
        logger.info(f"📂 {category} 카테고리 탐색 중...")

        # 2️⃣ walk 폴더를 포함한 모든 하위 폴더 검색
        walk_paths = list(category_path.rglob("walk"))
        if not walk_paths:
            logger.warning(f"⚠️  {category} 내 walk 폴더 없음")
            continue

        for walk_path in walk_paths:
            for png in walk_path.glob("*.png"):
                color = png.stem.lower()
                rarity = assign_rarity(color)

                # path 예시: male/amber
                rel_path = walk_path.relative_to(category_path).parent / color

                items.append({
                    "category": category,
                    "asset_key": f"{rel_path}".replace("\\", "_").replace("/", "_"),
                    "path": str(rel_path).replace("\\", "/"),
                    "name": color.replace("_", " ").title(),
                    "rarity": rarity,
                    "price_cr": PRICE_MAP[rarity],
                    "is_gacha_only": rarity != "common",
                    "description": "",
                    "walk_count": 1,
                    "run_count": 1,
                    "jump_count": 1,
                    "total_files": 3,
                    "has_all_animations": True,
                })

    df = pd.DataFrame(items)
    logger.info(f"✅ 총 {len(df)}개 아이템 스캔 완료")
    return df


def save_to_csv(df: pd.DataFrame):
    """CSV 저장"""
    output_columns = [
        "category", "asset_key", "path", "name",
        "rarity", "price_cr", "is_gacha_only", "description",
        "walk_count", "run_count", "jump_count", "total_files",
        "has_all_animations"
    ]

    df[output_columns].to_csv(CSV_FILE, index=False, encoding="utf-8-sig")
    logger.info(f"💾 CSV 저장 완료: {CSV_FILE}")


if __name__ == "__main__":
    logger.info("=" * 60)
    logger.info("🚀 Asset Scanner v5 시작 (다중 카테고리 대응)")
    logger.info("=" * 60)

    df = scan_assets()

    if df.empty:
        logger.error("❌ 스캔 결과 없음 — 종료")
        exit(1)

    save_to_csv(df)

    logger.info("\n📊 희귀도 분포:")
    print(df["rarity"].value_counts())

    logger.info("=" * 60)
    logger.info("✅ 완료! boutique_items.csv 생성됨")
    logger.info("=" * 60)
