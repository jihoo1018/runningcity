# db_manager.py
import pandas as pd
import psycopg2
from psycopg2.extras import execute_values
import uuid
import logging

# ✅ config.py에서 설정 가져오기
from config import DB_CONFIG, CSV_FILE, LOG_FILE

# ============================================
# 로깅 설정
# ============================================
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
        print("🧩 DB_CONFIG:", DB_CONFIG)
        conn = psycopg2.connect(**DB_CONFIG)
        conn.set_client_encoding('UTF8')
        logger.info("✅ DB 연결 성공")
        return conn
    except Exception as e:
        logger.error(f"❌ DB 연결 실패: {e}")
        logger.error(f"   에러 타입: {type(e).__name__}")
        import traceback
        traceback.print_exc()
        return None


def create_table(conn):
    """테이블 생성 (핵심 필드만)"""
    create_sql = """
    CREATE TABLE IF NOT EXISTS boutique_items (
        -- 기본 식별자
        item_id BIGSERIAL PRIMARY KEY,

        -- 카테고리 및 식별 정보
        category VARCHAR(20) NOT NULL CHECK (category IN ('bodies', 'clothes', 'hair', 'head')),
        name VARCHAR(100) NOT NULL,
        asset_key VARCHAR(100) NOT NULL,
        path VARCHAR(200) NOT NULL,

        -- 등급 및 가격
        rarity VARCHAR(20) CHECK (rarity IN ('common', 'rare', 'epic', 'legendary')),
        price_cr INT NOT NULL DEFAULT 0 CHECK (price_cr >= 0),

        -- 획득 방법
        is_gacha_only BOOLEAN DEFAULT false,
        obtain_method VARCHAR(20) CHECK (obtain_method IN ('gacha', 'store')),

        -- 타임스탬프
        created_at timestamptz DEFAULT now(),
        updated_at timestamptz DEFAULT now(),

        -- 제약조건
        UNIQUE (category, asset_key)
    );

    -- 인덱스 생성
    CREATE INDEX IF NOT EXISTS idx_boutique_category ON boutique_items(category);
    CREATE INDEX IF NOT EXISTS idx_boutique_rarity ON boutique_items(rarity);
    CREATE INDEX IF NOT EXISTS idx_boutique_obtain ON boutique_items(obtain_method);
    CREATE INDEX IF NOT EXISTS idx_boutique_price ON boutique_items(price_cr);
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
    """DataFrame을 DB에 삽입 (핵심 필드만)"""

    logger.info(f"💾 {len(df)}개 아이템 삽입 시작...")

    # 필요한 컬럼만 추출
    columns = ["category", "name", "asset_key", "path", "rarity", "price_cr", "is_gacha_only", "obtain_method"]
    df = df[columns].copy()

    cur = conn.cursor()
    success_count = 0
    skip_count = 0
    error_count = 0

    for idx, row in df.iterrows():
        try:
            cur.execute("""
                INSERT INTO boutique_items
                (category, name, asset_key, path, rarity, price_cr, is_gacha_only, obtain_method)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (category, asset_key) DO NOTHING
            """, (
                row["category"],
                row["name"],
                row["asset_key"],
                row["path"],
                row["rarity"] if pd.notna(row["rarity"]) else None,
                int(row["price_cr"]) if pd.notna(row["price_cr"]) else 0,
                bool(row["is_gacha_only"]),
                row["obtain_method"] if pd.notna(row["obtain_method"]) else None,
            ))

            if cur.rowcount > 0:
                success_count += 1
            else:
                skip_count += 1

        except Exception as e:
            error_count += 1
            logger.error(f"❌ [{row['category']}] {row['asset_key']}: {e}")

    conn.commit()
    cur.close()

    logger.info("=" * 60)
    logger.info(f"✅ 삽입 완료: {success_count}개")
    logger.info(f"⏭️  스킵: {skip_count}개 (중복)")
    logger.info(f"❌ 실패: {error_count}개")
    logger.info("=" * 60)
    logger.info("=" * 60)
    logger.info(f"✅ 삽입 완료: {success_count}개")
    logger.info(f"⏭️  스킵: {skip_count}개 (중복)")
    logger.info(f"❌ 실패: {error_count}개")
    logger.info("=" * 60)


def verify_data(conn):
    """데이터 검증"""

    logger.info("\n🔍 데이터 검증 중...")

    cur = conn.cursor()

    # 전체 개수
    cur.execute("SELECT COUNT(*) FROM boutique_items")
    total = cur.fetchone()[0]
    logger.info(f"\n📊 전체 아이템 수: {total}개\n")

    # 카테고리별 개수
    cur.execute("""
                SELECT category, COUNT(*) as count
                FROM boutique_items
                GROUP BY category
                ORDER BY category
                """)

    logger.info("📦 카테고리별 분포:")
    logger.info("-" * 40)
    for row in cur.fetchall():
        logger.info(f"   {row[0]:12s}: {row[1]:4d}개")

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

    logger.info("\n✨ Rarity별 분포:")
    logger.info("-" * 40)
    for row in cur.fetchall():
        logger.info(f"   {row[0]:12s}: {row[1]:4d}개")

    # 획득 방법별 개수
    cur.execute("""
                SELECT obtain_method, COUNT(*) as count
                FROM boutique_items
                GROUP BY obtain_method
                ORDER BY obtain_method
                """)

    logger.info("\n🎁 획득 방법별 분포:")
    logger.info("-" * 40)
    for row in cur.fetchall():
        logger.info(f"   {row[0]:12s}: {row[1]:4d}개")

    cur.close()


def get_store_items_summary(conn):
    """상점 판매 아이템 상세 확인"""

    logger.info("\n" + "=" * 60)
    logger.info("🏪 상점 판매 아이템 상세")
    logger.info("=" * 60)

    cur = conn.cursor()

    # 전체 상점 아이템
    cur.execute("""
                SELECT category, name, rarity, price_cr
                FROM boutique_items
                WHERE obtain_method = 'store'
                ORDER BY category, price_cr, name
                """)

    store_items = cur.fetchall()

    if store_items:
        logger.info(f"\n총 {len(store_items)}개의 상점 판매 아이템:")
        logger.info("-" * 60)
        for item in store_items:
            logger.info(f"   [{item[0]:8s}] {item[1]:20s} | {item[2]:9s} | {item[3]:5d} CR")
    else:
        logger.info("\n⚠️  상점 판매 아이템이 없습니다!")

    # 카테고리별 확인
    logger.info("\n🔍 카테고리별 상점 아이템:")
    logger.info("-" * 40)

    categories = ['bodies', 'clothes', 'hair', 'head']
    missing_categories = []

    for category in categories:
        cur.execute("""
                    SELECT COUNT(*)
                    FROM boutique_items
                    WHERE obtain_method = 'store'
                      AND category = %s
                    """, (category,))

        count = cur.fetchone()[0]
        if count > 0:
            logger.info(f"   {category:12s}: {count:4d}개 ✅")
        else:
            logger.info(f"   {category:12s}:    0개 ⚠️")
            missing_categories.append(category)

    if missing_categories:
        logger.info(f"\n⚠️  경고: {', '.join(missing_categories)} 카테고리에 상점 아이템 없음")
    else:
        logger.info("\n✅ 모든 카테고리에 상점 아이템 존재")

    cur.close()


if __name__ == "__main__":
    logger.info("=" * 60)
    logger.info("🚀 Boutique Items DB Manager 시작")
    logger.info("=" * 60)

    # 1. CSV 파일 확인
    if not CSV_FILE.exists():
        logger.error(f"\n❌ CSV 파일 없음: {CSV_FILE}")
        logger.info("💡 먼저 'python asset_scanner.py'를 실행하세요")
        exit(1)

    # 2. CSV 읽기
    try:
        df = pd.read_csv(CSV_FILE, encoding="utf-8-sig")
        logger.info(f"\n📄 CSV 로드 완료: {len(df)}개 아이템")
    except Exception as e:
        logger.error(f"❌ CSV 읽기 실패: {e}")
        exit(1)

    # 3. 데이터 전처리
    df['description'] = df['description'].fillna('')
    df['walk_count'] = df['walk_count'].fillna(0).astype(int)
    df['run_count'] = df['run_count'].fillna(0).astype(int)
    df['jump_count'] = df['jump_count'].fillna(0).astype(int)
    df['total_files'] = df['total_files'].fillna(0).astype(int)
    df['has_all_animations'] = df['has_all_animations'].fillna(False).astype(bool)

    logger.info(f"   └─ 완전한 애니메이션: {df['has_all_animations'].sum()}개")
    logger.info(f"   └─ 불완전한 애니메이션: {(~df['has_all_animations']).sum()}개")

    # 4. DB 연결
    conn = connect_db()
    if not conn:
        exit(1)

    # 5. 테이블 생성
    create_table(conn)

    # 6. 데이터 삽입
    insert_items(conn, df)

    # 7. 검증
    verify_data(conn)

    # 8. 상점 아이템 상세 확인
    get_store_items_summary(conn)

    # 9. 연결 종료
    conn.close()
    logger.info("\n✅ DB 연결 종료")

    logger.info("\n" + "=" * 60)
    logger.info("🎉 모든 작업 완료!")
    logger.info("=" * 60)
    logger.info("\n💡 다음 단계:")
    logger.info("   1. DBeaver에서 데이터 확인")
    logger.info("   2. SELECT * FROM boutique_items LIMIT 10;")
    logger.info("   3. 백엔드 API 개발 시작")
    logger.info("=" * 60)
