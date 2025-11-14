# ============================================================
# 🧠 ai_translator_common.py
# - 모든 카테고리 아이템 이름을 한국어로 번역 + Rarity 자동 판독
# - GMS Gateway API (GPT-4.1-mini) 사용
# - price_cr > 0인 Store 아이템은 rarity 변경 안함
# - 사용법: python ai_translator_common.py bodies
# ============================================================

import requests
import json
import time
import pandas as pd
from datetime import datetime
import sys

# ==========================================
# 🔑 GMS API 설정
# ==========================================
GMS_API_URL = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"
API_KEY = "S13P32A405-4cd203ce-2168-4382-af52-d65465913690"
MODEL = "gpt-4.1-mini"
LOG_FILE = "output/gms_translation.log"


# ==========================================
# 🧩 프롬프트 생성 (번역 + Rarity)
# ==========================================
def build_prompt(items_batch, category):
    items_text = "\n".join([
        f"- {item['asset_key']}: {item['name']}"
        for item in items_batch
    ])

    translation_guide = {
        "bodies": "예: Male Black Body → 남성 블랙 바디",
        "hair": "예: Long Blonde Hair → 롱 블론드 헤어",
        "clothes": "예: Longsleeve Red → 롱슬리브 레드",
        "head": "예: Anger Eyes Brown → 화난 눈 브라운"
    }

    return f"""당신은 게임 "러닝시티(RunningCity)"의 번역가이자 밸런스 디자이너입니다.
사이버펑크 스타일의 러너 커스터마이징 아이템({category})을 분류해주세요.

**아이템 목록:**
{items_text}

---

**작업 1: 한국어 번역**
1. 자연스러운 한국어로 번역
2. 게임 분위기에 맞게 멋지고 감각적으로
3. 색상은 한국어 음차 (Black → 블랙, Light → 라이트, Amber → 앰버)
4. 스타일/형태는 의미 번역 또는 음차 적절히 사용
5. 간결하고 직관적으로

**카테고리별 참고:**
{translation_guide.get(category, "")}

---

**작업 2: 희귀도(rarity) 분류**
- **common**: 기본적이고 평범한 색상/스타일
  예: black, brown, grey, white, tan, light, dark 등
- **rare**: 눈에 띄는 색상/스타일
  예: blue, green, pink, purple, amber, olive, taupe 등
- **epic**: 특수하고 독특한 색상/효과
  예: bronze, bright, lavender, copper 등
- **legendary**: 극도로 희귀하고 상징적
  예: zombie, platinum, fur_gold 등

---

**반드시 JSON 형식으로 응답하세요.**

예시:
```json
{{
  "male_black": {{
    "name_ko": "남성 블랙 바디",
    "rarity": "common"
  }},
  "male_amber": {{
    "name_ko": "남성 앰버 바디",
    "rarity": "rare"
  }},
  "male_platinum": {{
    "name_ko": "남성 플래티넘 바디",
    "rarity": "legendary"
  }}
}}
```
"""


# ==========================================
# 🤖 GMS API 요청
# ==========================================
def get_ai_classification(items_batch, category):
    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": MODEL,
        "messages": [{"role": "user", "content": build_prompt(items_batch, category)}],
        "temperature": 0.4,
        "max_tokens": 3000
    }

    try:
        response = requests.post(GMS_API_URL, headers=headers, json=payload, timeout=80)

        log_entry = f"\n[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] " \
                    f"Category: {category} | Batch size: {len(items_batch)} | Status: {response.status_code}\n"

        if response.status_code != 200:
            log_entry += f"❌ ERROR: {response.text}\n"
            with open(LOG_FILE, "a", encoding="utf-8") as f:
                f.write(log_entry)
            print(log_entry)
            return {}

        data = response.json()
        content = data["choices"][0]["message"]["content"]
        log_entry += f"✅ RESPONSE:\n{content}\n"

        with open(LOG_FILE, "a", encoding="utf-8") as f:
            f.write(log_entry)

        return extract_json(content)

    except Exception as e:
        print(f"❌ GMS API 호출 실패: {e}")
        return {}


# ==========================================
# 🧠 JSON 파싱
# ==========================================
def extract_json(response_text: str):
    try:
        if "```json" in response_text:
            json_str = response_text.split("```json")[1].split("```")[0].strip()
        elif "```" in response_text:
            json_str = response_text.split("```")[1].split("```")[0].strip()
        else:
            json_str = response_text.strip()
        return json.loads(json_str)
    except Exception as e:
        print(f"⚠️ JSON 파싱 실패: {e}")
        print(f"원본 응답: {response_text[:200]}...")
        return {}


# ==========================================
# 🪄 번역 + Rarity 적용
# ==========================================
def apply_ai_classification(category):
    csv_file = f"output/boutique_items_{category}.csv"

    print("=" * 60)
    print(f"🤖 {category.upper()} 번역 + Rarity 분류기 (모델: {MODEL})")
    print("=" * 60)

    try:
        df = pd.read_csv(csv_file, encoding="utf-8-sig")
    except FileNotFoundError:
        print(f"❌ CSV 파일을 찾을 수 없습니다: {csv_file}")
        print(f"💡 먼저 'python asset_scanner_{category}.py'를 실행하세요")
        return

    print(f"\n📄 {len(df)}개 아이템 로드 완료")

    # ✅ price_cr > 0인 아이템 제외 (store 아이템은 건드리지 않음)
    store_items = df[df['price_cr'] > 0].copy()
    gacha_items = df[df['price_cr'] == 0].copy()

    print(f"   └─ 🏪 Store 아이템 (Rarity 변경 안함): {len(store_items)}개")
    print(f"   └─ 🎰 Gacha 아이템 (Rarity 자동 분류): {len(gacha_items)}개")

    batch_size = 20
    all_classifications = {}

    # ✅ 1. Gacha 아이템 처리 (번역 + Rarity 분류)
    if len(gacha_items) > 0:
        print(f"\n🎰 Gacha 아이템 처리 중...")
        for i in range(0, len(gacha_items), batch_size):
            batch = gacha_items.iloc[i:i + batch_size]
            items_batch = batch[["asset_key", "name"]].to_dict("records")

            print(f"   배치 {i // batch_size + 1}/{(len(gacha_items) - 1) // batch_size + 1} 처리 중...")
            result = get_ai_classification(items_batch, category)
            all_classifications.update(result)
            print(f"   ✅ {len(result)}개 항목 분류 완료")

            if i + batch_size < len(gacha_items):
                time.sleep(1)

        # Gacha 아이템 업데이트
        gacha_items["name"] = gacha_items["asset_key"].apply(
            lambda k: all_classifications.get(k, {}).get("name_ko",
                 gacha_items[gacha_items["asset_key"] == k]["name"].iloc[
                     0] if len(
                     gacha_items[gacha_items["asset_key"] == k]) > 0 else k)
        )

        gacha_items["rarity"] = gacha_items["asset_key"].apply(
            lambda k: all_classifications.get(k, {}).get("rarity", "rare")
        )

    # ✅ 2. Store 아이템 처리 (번역만, Rarity 유지)
    if len(store_items) > 0:
        print(f"\n🏪 Store 아이템 번역 중 (Rarity 유지)...")
        store_classifications = {}

        for i in range(0, len(store_items), batch_size):
            batch = store_items.iloc[i:i + batch_size]
            items_batch = batch[["asset_key", "name"]].to_dict("records")

            print(f"   배치 {i // batch_size + 1}/{(len(store_items) - 1) // batch_size + 1} 처리 중...")
            result = get_ai_classification(items_batch, category)
            store_classifications.update(result)
            print(f"   ✅ {len(result)}개 항목 번역 완료")

            if i + batch_size < len(store_items):
                time.sleep(1)

        # Store 아이템은 번역만 적용
        store_items["name"] = store_items["asset_key"].apply(
            lambda k: store_classifications.get(k, {}).get("name_ko",
           store_items[store_items["asset_key"] == k]["name"].iloc[
               0] if len(
               store_items[store_items["asset_key"] == k]) > 0 else k)
        )

        # rarity는 건드리지 않음!

    # 🔄 DataFrame 합치기
    df = pd.concat([store_items, gacha_items], ignore_index=True)

    # CSV 저장
    df.to_csv(csv_file, index=False, encoding="utf-8-sig")

    print("\n" + "=" * 60)
    print("✅ 번역 + Rarity 분류 완료!")
    print("=" * 60)

    # 샘플 출력
    if len(store_items) > 0:
        print("\n📋 Store 아이템 샘플 (Rarity 변경 안됨):")
        for idx, row in store_items.head(5).iterrows():
            print(f"   {row['name']:40s} | {row['rarity']:10s} | {row['price_cr']} CR")

    if len(gacha_items) > 0:
        print("\n📋 Gacha 아이템 샘플 (Rarity 자동 분류됨):")
        for idx, row in gacha_items.head(10).iterrows():
            print(f"{row['name']:40s} | {row['rarity']}")


    # 통계
    print(f"\n📊 통계:")
    print(f"   - 전체 아이템: {len(df)}개")
    print(f"   - Store 아이템: {len(store_items)}개")
    print(f"   - Gacha 아이템: {len(gacha_items)}개")

    if len(gacha_items) > 0:
        print(f"\n✨ Rarity 분포 (Gacha 아이템만):")
        for rarity, count in gacha_items['rarity'].value_counts().items():
            print(f"   - {rarity}: {count}개")


# ==========================================
# 🚀 실행
# ==========================================
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("사용법: python ai_translator_common.py [category]")
        print("예시:")
        print("  python ai_translator_common.py bodies")
        print("  python ai_translator_common.py hair")
        print("  python ai_translator_common.py clothes")
        print("  python ai_translator_common.py head")
        exit(1)

    category = sys.argv[1].lower()

    if category not in ["bodies", "hair", "clothes", "head"]:
        print(f"❌ 잘못된 카테고리: {category}")
        print("사용 가능한 카테고리: bodies, hair, clothes, head")
        exit(1)

    if not API_KEY or not GMS_API_URL:
        print("❌ GMS API 키 또는 URL이 설정되지 않았습니다.")
        exit(1)

    apply_ai_classification(category)

    print("\n" + "=" * 60)
    print("💡 다음 단계:")
    print(f"   1. output/boutique_items_{category}.csv 확인")
    print("   2. 필요 시 수동 수정")
    print(f"   3. python db_manager_{category}.py 실행")
    print("=" * 60)