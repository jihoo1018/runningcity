# db_manager.py
import pandas as pd
import psycopg2
from psycopg2.extras import execute_values
import logging
from pathlib import Path

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
        logger.info("🔌 DB 연결 시도 중...")
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


def insert_items(conn, df):
    """
    DataFrame을 DB에 삽입
    새로운 구조: category, subcategory, style, color, name, asset_key, base_path
    """
    logger.info(f"💾 {len(df)}개 아이템 삽입 시작...")

    # ✅ 새 구조에 맞는 컬럼만 추출
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

    # NULL 처리
    df['style'] = df['style'].replace({pd.NA: None, '': None})
    df['color'] = df['color'].replace({pd.NA: None, '': None})

    cur = conn.cursor()
    success_count = 0
    skip_count = 0
    error_count = 0

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
                if row["rarity"] == "common":
                    logger.debug(f"🟢 COMMON 삽입: {row['asset_key']}")
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

    # Subcategory별 개수
    cur.execute("""
                SELECT category, subcategory, COUNT(*) as count
                FROM boutique_items
                GROUP BY category, subcategory
                ORDER BY category, subcategory
                """)

    logger.info("\n🔹 서브카테고리별 분포:")
    logger.info("-" * 50)
    current_category = None
    for row in cur.fetchall():
        if current_category != row[0]:
            current_category = row[0]
            logger.info(f"\n   [{current_category}]")
        logger.info(f"      └─ {row[1]:20s}: {row[2]:4d}개")

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
        method = row[0] if row[0] else 'NULL'
        logger.info(f"   {method:12s}: {row[1]:4d}개")

    cur.close()


def get_store_items_summary(conn):
    """상점 판매 아이템 상세 확인"""
    logger.info("\n" + "=" * 60)
    logger.info("🏪 상점 판매 아이템 상세 (Common 아이템)")
    logger.info("=" * 60)

    cur = conn.cursor()

    # Common 아이템 = 상점 판매 아이템
    cur.execute("""
                SELECT category, subcategory, style, color, name, rarity, price_cr
                FROM boutique_items
                WHERE obtain_method = 'store'
                ORDER BY category, subcategory, style, color
                """)

    store_items = cur.fetchall()

    if store_items:
        logger.info(f"\n총 {len(store_items)}개의 상점 판매 아이템 (가격: 3 CR):")
        logger.info("-" * 80)

        current_category = None
        for item in store_items:
            if current_category != item[0]:
                current_category = item[0]
                logger.info(f"\n📂 [{current_category}]")

            subcategory = item[1]
            style = item[2] if item[2] else '-'
            color = item[3] if item[3] else '-'
            name = item[4]
            price = item[6]

            logger.info(f"   └─ {subcategory:15s} | {style:10s} | {color:10s} | {price:3d} CR")
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
        logger.warning(f"\n⚠️  경고: {', '.join(missing_categories)} 카테고리에 상점 아이템 없음")
        logger.info("💡 각 카테고리에 최소 1개씩 common 아이템 필요")
    else:
        logger.info("\n✅ 모든 카테고리에 상점 아이템 존재")

    cur.close()


def check_animation_patterns(conn):
    """애니메이션 패턴 확인"""
    logger.info("\n" + "=" * 60)
    logger.info("🎬 애니메이션 패턴 확인")
    logger.info("=" * 60)

    cur = conn.cursor()

    # {animation} 패턴이 있는 아이템
    cur.execute("""
                SELECT category, COUNT(*) as count
                FROM boutique_items
                WHERE base_path LIKE '%{animation}%'
                GROUP BY category
                ORDER BY category
                """)

    logger.info("\n📹 애니메이션 패턴 포함 아이템:")
    logger.info("-" * 40)
    for row in cur.fetchall():
        logger.info(f"   {row[0]:12s}: {row[1]:4d}개")

    # 샘플 출력
    cur.execute("""
                SELECT category, subcategory, base_path
                FROM boutique_items
                WHERE base_path LIKE '%{animation}%' LIMIT 3
                """)

    logger.info("\n📄 샘플 경로:")
    logger.info("-" * 80)
    for row in cur.fetchall():
        logger.info(f"   [{row[0]}] {row[1]}")
        logger.info(f"      → {row[2]}\n")

    cur.close()


if __name__ == "__main__":
    logger.info("=" * 60)
    logger.info("🚀 Boutique Items DB Manager v2 시작")
    logger.info("   (새 DB 구조 대응: category/subcategory/style/color)")
    logger.info("=" * 60)

    # 1. CSV 파일 확인
    csv_path = Path(CSV_FILE)
    if not csv_path.exists():
        logger.error(f"\n❌ CSV 파일 없음: {CSV_FILE}")
        logger.info("💡 먼저 'python asset_scanner.py'를 실행하세요")
        exit(1)

    # 2. CSV 읽기
    try:
        df = pd.read_csv(CSV_FILE, encoding="utf-8-sig")
        logger.info(f"\n📄 CSV 로드 완료: {len(df)}개 아이템")
        logger.info(f"   컬럼: {list(df.columns)}")
    except Exception as e:
        logger.error(f"❌ CSV 읽기 실패: {e}")
        exit(1)

    # 3. DB 연결
    conn = connect_db()
    if not conn:
        exit(1)

    # 4. 데이터 삽입
    insert_items(conn, df)

    # 5. 검증
    verify_data(conn)

    # 6. 상점 아이템 상세 확인
    get_store_items_summary(conn)

    # 7. 애니메이션 패턴 확인
    check_animation_patterns(conn)

    # 8. 연결 종료
    conn.close()
    logger.info("\n✅ DB 연결 종료")

    logger.info("\n" + "=" * 60)
    logger.info("🎉 모든 작업 완료!")
    logger.info("=" * 60)
    logger.info("\n💡 다음 단계:")
    logger.info("   1. DBeaver에서 데이터 확인")
    logger.info("   2. SELECT * FROM boutique_items WHERE rarity='common' LIMIT 10;")
    logger.info("   3. 백엔드 API 개발 시작")
    logger.info("=" * 60)