# config.py
import os
from pathlib import Path
import sys

# ===== 프로젝트 루트 경로 =====
PROJECT_ROOT = Path(__file__).parent.parent  # scripts의 부모 디렉토리
ASSET_ROOT = PROJECT_ROOT / "AssetsStore" / "spritesheets"

# ===== Asset 설정 =====
CATEGORIES = ["bodies", "clothes", "hair", "head"]
ANIMATIONS = ["walk", "run", "jump"]

# ============================================
# 환경변수 강제 설정 (Windows 인코딩 문제 해결)
# ============================================
os.environ['PGCLIENTENCODING'] = 'UTF8'
os.environ['LANG'] = 'en_US.UTF-8'
os.environ['LC_ALL'] = 'en_US.UTF-8'

# Windows 콘솔 인코딩 UTF-8로 변경
if sys.platform == 'win32':
    try:
        import locale
        locale.setlocale(locale.LC_ALL, 'en_US.UTF-8')
    except:
        pass

# ============================================
# 설정
# ============================================
# DB_CONFIG = {
#     "host": "localhost",
#     "port": 5432,
#     "database": "runningcity",
#     "user": "postgres",
#     "password": "1234",  # 실제 비밀번호로 변경
#     "client_encoding": "UTF8",
#     "options": "-c client_encoding=UTF8"
# }

# ============================================
# 설정
# ============================================
DB_CONFIG = {
    "host": "k13a405.p.ssafy.io",
    "port": 5432,
    "database": "RUNNINGCITY_DEV",
    "user": "ging",
    "password": "runrunging@~@",
    "client_encoding": "UTF8",
    "options": "-c client_encoding=UTF8"
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
# S3_BUCKET = "runningcity-assets"  # AWS S3 사용 시



LOG_LEVEL = "INFO"