# config.py
import os
from pathlib import Path

# ===== 프로젝트 루트 경로 =====
PROJECT_ROOT = Path(__file__).parent.parent  # scripts의 부모 디렉토리
ASSET_ROOT = PROJECT_ROOT / "AssetsStore" / "spritesheets"

# ===== Asset 설정 =====
CATEGORIES = ["bodies", "clothes", "hair", "head"]
ANIMATIONS = ["walk", "run", "jump"]

# ===== DB 설정 =====
DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "database": "runningcity",
    "user": "postgres",
    "password": "1234"  # 실제 비밀번호로 변경
}

# ===== 출력 설정 =====
OUTPUT_DIR = PROJECT_ROOT / "scripts" / "output"
CSV_FILE = OUTPUT_DIR / "boutique_items.csv"
SQL_FILE = OUTPUT_DIR / "insert_boutique_items.sql"
LOG_FILE = OUTPUT_DIR / "sync.log"

# 출력 디렉토리 생성
OUTPUT_DIR.mkdir(exist_ok=True)

# ===== CDN 설정 (나중에 사용) =====
CDN_BASE_URL = "https://cdn.runningcity.com/store-assets"
S3_BUCKET = "runningcity-assets"  # AWS S3 사용 시

# ===== 가격 정책 =====
PRICE_MAP = {
    "common": 500,
    "rare": 1500,
    "epic": 3000,
    "legendary": 5000
}

# ===== Rarity 자동 판별 키워드 =====
RARITY_KEYWORDS = {
    "legendary": ["legendary", "royal", "divine", "ultimate"],
    "epic": ["epic", "elite", "master", "grand"],
    "rare": ["rare", "armor", "helmet", "special"],
    "common": ["basic", "light", "simple", "starter"]
}

LOG_LEVEL = "INFO"