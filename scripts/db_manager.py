# db_manager.py
import pandas as pd
import psycopg2
from psycopg2.extras import execute_values
import uuid
import logging
from config import *

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
        conn = psycopg2.connect(**DB_CONFIG)
        logger.info("✅ DB 연결 성공")
        return conn
    except Exception as e:
        logger.error(f"❌ DB 연결 실패: {e}")
        return None


def create_table(conn):
    """테이블 생성 (존재하지 않으면)"""

    create_sql = """
                 CREATE TABLE IF NOT EXISTS boutique_items \
                 ( \
                     item_id \
                     UUID \
                     PRIMARY \
                     KEY \
                     DEFAULT \
                     gen_random_uuid \
                 ( \
                 ),
                     category VARCHAR \
                 ( \
                     20 \
                 ) NOT NULL CHECK \
                 ( \
                     category \
                     IN \
                 ( \
                     'bodies', \
                     'clothes', \
                     'hair', \
                     'head' \
                 )),
                     name VARCHAR \
                 ( \
                     100 \
                 ) NOT NULL,
                     asset_key VARCHAR \
                 ( \
                     100 \
                 ) NOT NULL,
                     rarity VARCHAR \
                 ( \
                     20 \
                 ) CHECK \
                 ( \
                     rarity \
                     IN \
                 ( \
                     'common', \
                     'rare', \
                     'epic', \
                     'legendary' \
                 )),
                     price_cr INT NOT NULL DEFAULT 0,
                     is_gacha_only BOOLEAN DEFAULT false,
                     description TEXT,
                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, \
                     UNIQUE \
                 ( \
                     category, \
                     asset_key \
                 )
                     );

                 CREATE INDEX IF NOT EXISTS idx_boutique_category ON boutique_items(category);
                 CREATE INDEX IF NOT EXISTS idx_boutique_rarity ON boutique_items(rarity);
                 CREATE INDEX IF NOT EXISTS idx_boutique_gacha ON boutique_items(is_gacha_only); \
                 """

    try:
        cur = conn.cursor()
        cur.execute(create_sql)
        conn.commit()
        cur.close()
        logger.info("✅ 테이블 생성/확인 완료")
    except Exception as e:
        logger.error(f"❌ 테이블 생성 실패: {e}")
        conn.rollback()


def insert_items(conn, df):
    """DataFrame을 DB에 삽입"""

    logger.info(f"💾 {len(df)}개 아이템 삽입 시작...")

    # 완전한 애니메이션을 가진 아이템만 삽입
    df_complete = df[df["has_all_animations"] == True].copy()

    logger.info(f"   └─ 삽입 대상: {len(df_complete)}개")

    cur = conn.cursor()

    success_count = 0
    skip_count = 0
    error_count = 0

    for _, row in df_complete.iterrows():
        try:
            cur.execute("""
                        INSERT INTO boutique_items
                        (item_id, category, name, asset_key, rarity, price_cr, is_gacha_only, description)
                        VALUES (%s, %s, %s, %s, %s, %s, %s, %s) ON CONFLICT (category, asset_key) DO NOTHING
                        """, (
                            str(uuid.uuid4()),
                            row["category"],
                            row["name"],
                            row["asset_key"],
                            row["rarity"],
                            int(row["price_cr"]),
                            bool(row["is_gacha_only"]),
                            row["description"] if row["description"] else None
                        ))

            if cur.rowcount > 0:
                success_count += 1
                logger.debug(f"✅ {row['asset_key']}")
            else:
                skip_count += 1
                logger.debug(f"⏭️  {row['asset_key']} (이미 존재)")

        except Exception as e:
            error_count += 1
            logger.error(f"❌ {row['asset_key']}: {e}")

    conn.commit()
    cur.close()

    logger.info("=" * 50)
    logger.info(f"✅ 삽입 완료: {success_count}개")
    logger.info(f"⏭️  스킵: {skip_count}개 (중복)")
    logger.info(f"❌ 실패: {error_count}개")
    logger.info("=" * 50)


def verify_data(conn):
    """데이터 검증"""

    logger.info("🔍 데이터 검증 중...")

    cur = conn.cursor()

    # 카테고리별 개수
    cur.execute("""
                SELECT category, COUNT(*) as count
                FROM boutique_items
                GROUP BY category
                ORDER BY category
                """)

    logger.info("📊 카테고리별 분포:")
    for row in cur.fetchall():
        logger.info(f"   └─ {row[0]}: {row[1]}개")

    # Rarity별 개수
    cur.execute("""
                SELECT rarity, COUNT(*) as count
                FROM boutique_items
                GROUP BY rarity
                ORDER BY
                    CASE rarity
                    WHEN 'common' THEN 1
                    WHEN 'rare' THEN 2
                    WHEN 'epic' THEN 3
                    WHEN 'legendary' THEN 4
                END
                """)

    logger.info("📊 Rarity별 분포:")
    for row in cur.fetchall():
        logger.info(f"   └─ {row[0]}: {row[1]}개")

    cur.close()


if __name__ == "__main__":
    logger.info("=" * 50)
    logger.info("🚀 DB Manager 시작")
    logger.info("=" * 50)

    # 1. CSV 읽기
    if not CSV_FILE.exists():
        logger.error(f"❌ CSV 파일 없음: {CSV_FILE}")
        logger.info("💡 먼저 'python asset_scanner.py'를 실행하세요")
        exit(1)

    df = pd.read_csv(CSV_FILE, encoding="utf-8-sig")
    logger.info(f"📄 CSV 로드: {len(df)}개 아이템")

    # 2. DB 연결
    conn = connect_db()
    if not conn:
        exit(1)

    # 3. 테이블 생성
    create_table(conn)

    # 4. 데이터 삽입
    insert_items(conn, df)

    # 5. 검증
    verify_data(conn)

    # 6. 연결 종료
    conn.close()
    logger.info("✅ DB 연결 종료")

    logger.info("=" * 50)
    logger.info("🎉 모든 작업 완료!")
    logger.info("=" * 50)