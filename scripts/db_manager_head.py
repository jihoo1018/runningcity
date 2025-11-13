# db_manager_head.py
import pandas as pd
import psycopg2
import logging
from pathlib import Path

# ✅ config.py에서 설정 가져오기
from config import DB_CONFIG

# ============================================
# 설정
# ============================================
CSV_FILE = "output/boutique_items_head.csv"
LOG_FILE = "db_insert_head.log"

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(LOG_FILE, encoding='utf-8'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)


def connect_db():
    """DB 연결"""
    try:
        logger.info("🔌 DB 연결 시도 중...")
        conn = psycopg2.connect(**DB_CONFIG)
        conn.set_client_encoding('UTF8')
        logger.info("✅ DB 연결 성공")
        return conn
    except Exception as e:
        logger.error(f"❌ DB 연결 실패: {e}")
        return None


def insert_head(conn, df):
    """Head 데이터 삽입"""
    logger.info(f"💾 {len(df)}개 Head 아이템 삽입 시작...")

    required_columns = [
        "category", "subcategory", "style", "color",
        "name", "asset_key", "base_path",
        "rarity", "price_cr", "obtain_method"
    ]

    # 컬럼 확인
    for col in required_columns:
        if col not in df.columns:
            logger.error(f"❌ 필수 컬럼 누락: {col}")
            return

    df = df[required_columns].copy()
    df['style'] = df['style'].replace({pd.NA: None, '': None})
    df['color'] = df['color'].replace({pd.NA: None, '': None})

    success_count = 0
    skip_count = 0
    error_count = 0

    conn.autocommit = True
    cur = conn.cursor()

    for idx, row in df.iterrows():
        try:
            cur.execute("""
                        INSERT INTO boutique_items
                        (category, subcategory, style, color, name, asset_key, base_path,
                         rarity, price_cr, obtain_method)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s,
                                %s) ON CONFLICT (category, subcategory, style, color, asset_key) DO NOTHING
                        """, (
                            row["category"],
                            row["subcategory"],
                            row["style"] if pd.notna(row["style"]) else None,
                            row["color"] if pd.notna(row["color"]) else None,
                            row["name"],
                            row["asset_key"],
                            row["base_path"],
                            row["rarity"] if pd.notna(row["rarity"]) else None,
                            int(row["price_cr"]) if pd.notna(row["price_cr"]) else 0,
                            row["obtain_method"] if pd.notna(row["obtain_method"]) else None,
                        ))

            if cur.rowcount > 0:
                success_count += 1
                if idx % 50 == 0:  # 50개마다 로그
                    logger.info(f"✅ 진행 중... {success_count}개 삽입")
            else:
                skip_count += 1

        except Exception as e:
            error_count += 1
            logger.error(f"❌ {row['asset_key']}: {e}")
            if error_count == 1:  # 첫 에러만 상세히
                logger.error(f"   ▶ subcategory: {row['subcategory']}")
                logger.error(f"   ▶ style: {row['style']}")
                logger.error(f"   ▶ color: {row['color']}")

    conn.autocommit = False
    cur.close()

    logger.info("=" * 60)
    logger.info(f"✅ 삽입 완료: {success_count}개")
    logger.info(f"⏭️  스킵: {skip_count}개 (중복)")
    logger.info(f"❌ 실패: {error_count}개")
    logger.info("=" * 60)


if __name__ == "__main__":
    logger.info("=" * 60)
    logger.info("🚀 Head DB Manager 시작")
    logger.info("=" * 60)

    # 1. CSV 확인
    csv_path = Path(CSV_FILE)
    if not csv_path.exists():
        logger.error(f"\n❌ CSV 파일 없음: {CSV_FILE}")
        logger.info("💡 먼저 'python asset_scanner_head.py'를 실행하세요")
        exit(1)

    # 2. CSV 읽기
    try:
        df = pd.read_csv(CSV_FILE, encoding="utf-8-sig")
        logger.info(f"\n📄 CSV 로드 완료: {len(df)}개 아이템")
    except Exception as e:
        logger.error(f"❌ CSV 읽기 실패: {e}")
        exit(1)

    # 3. DB 연결
    conn = connect_db()
    if not conn:
        exit(1)

    # 4. 데이터 삽입
    insert_head(conn, df)

    # 5. 검증
    cur = conn.cursor()
    cur.execute("SELECT COUNT(*) FROM boutique_items WHERE category='head'")
    total = cur.fetchone()[0]
    logger.info(f"\n📊 DB에 저장된 Head 아이템: {total}개")

    cur.execute("""
                SELECT subcategory, COUNT(*)
                FROM boutique_items
                WHERE category = 'head'
                GROUP BY subcategory
                ORDER BY subcategory
                """)
    logger.info("\n   서브카테고리별:")
    for row in cur.fetchall():
        logger.info(f"      - {row[0]}: {row[1]}개")

    cur.close()
    conn.close()

    logger.info("\n✅ 완료!")
    logger.info("=" * 60)