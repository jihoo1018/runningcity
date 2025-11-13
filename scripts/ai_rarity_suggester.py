# ============================================================
# 🧠 ai_rarity_suggester_gms_v3.py
# - 희귀도 + 획득방식 (store / gacha) 자동 분류
# - 기본템 규칙 (light / amber / brown + 지정 파츠) 강제 반영
# - GMS Gateway API (GPT-4.1-mini)
# ============================================================

import requests
import json
import time
import pandas as pd
from datetime import datetime
from config import CSV_FILE

# ==========================================
# 🔑 GMS API 설정
# ==========================================
GMS_API_URL = "{gms 서버 url}"
API_KEY = "{api 키}"
MODEL = "gpt-4.1-mini"
LOG_FILE = "output/gms_rarity.log"

# ==========================================
# 🧩 프롬프트 생성
# ==========================================
def build_prompt(items_batch):
    items_text = "\n".join([
        f"- {item['asset_key']} ({item['category']}): {item['name']}"
        for item in items_batch
    ])

    return f"""당신은 피트니스 게임 "러닝시티(RunningCity)"의 밸런스 디자이너입니다.
사이버펑크 스타일의 러너 커스터마이징 아이템을 밸런스 있게 분류해야 합니다.

각 아이템의 **희귀도(rarity)** 와 **획득 방식(obtain_method)** 을 지정해주세요.

{items_text}

---

### 🧩 희귀도 분류 기준
1. **common (기본템, 흔함)**
2. **rare (눈에 띄는 아이템)**
3. **epic (특수 효과, 독특한 스타일)**
4. **legendary (극도로 희귀, 상징적인 아이템)**

---

### 🛒 획득 방식 분류 기준
- **store**: 상점에서 코인(CR)으로 구매 가능한 기본템
  - 예: 평범한 티셔츠, 기본 헤어, 베이직 바디 컬러 등
- **gacha**: 뽑기를 통해서만 획득 가능
  - 예: 화려한, 사이버틱한, 특이한 색상·형태 등

---

**반드시 JSON 형식으로 응답하세요.**

예시:
```json
{{
  "male_blue": {{"rarity": "rare", "obtain_method": "gacha"}},
  "male_black": {{"rarity": "common", "obtain_method": "store"}}
}}
"""

# ==========================================
# 🤖 GMS API 요청
# ==========================================
def get_ai_classification(items_batch):
    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json"
    }

    payload = {
        "model": MODEL,
        "messages": [{"role": "user", "content": build_prompt(items_batch)}],
        "temperature": 0.4,
        "max_tokens": 2500
    }

    try:
        response = requests.post(GMS_API_URL, headers=headers, json=payload, timeout=80)
        log_entry = f"\n[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] " \
                    f"Batch size: {len(items_batch)} | Status: {response.status_code}\n"
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
        return {}

# ==========================================
# 🎯 기본템 규칙 (store 전용) 강제 적용
# ==========================================
basic_colors = ["light", "amber", "brown"]
basic_parts = [
    "bodies", "longsleeve", "shorts", "afro", "ears", "eyebrows_thin"
]

def force_basic_rule(row):
    color_ok = any(c in row["asset_key"] for c in basic_colors)
    part_ok = any(p in row["asset_key"] for p in basic_parts)
    if color_ok and part_ok:
        row["obtain_method"] = "store"
        row["rarity"] = "common"
        row["price_cr"] = 500
        row["is_gacha_only"] = False
    else:
        row["obtain_method"] = "gacha"
        row["is_gacha_only"] = True
        if row["rarity"] == "common":
            # gacha common 은 구매불가
            row["price_cr"] = 0
    return row

# ==========================================
# 🪄 희귀도 + 획득방식 자동 적용
# ==========================================
def apply_ai_classification():
    print("=" * 60)
    print(f"🤖 RunningCity GMS 희귀도/획득방식 분류기 (모델: {MODEL})")
    print("=" * 60)

    df = pd.read_csv(CSV_FILE, encoding="utf-8-sig")
    print(f"\n📄 {len(df)}개 아이템 로드 완료")

    batch_size = 20
    all_classifications = {}

    for i in range(0, len(df), batch_size):
        batch = df.iloc[i:i + batch_size]
        items_batch = batch[["asset_key", "name", "category"]].to_dict("records")

        print(f"\n🔍 배치 {i // batch_size + 1}/{(len(df) - 1)//batch_size + 1} 처리 중...")
        result = get_ai_classification(items_batch)
        all_classifications.update(result)
        print(f"   ✅ {len(result)}개 항목 분류 완료")

        if i + batch_size < len(df):
            time.sleep(1)

    # 🔄 DataFrame 업데이트
    df["rarity"] = df["asset_key"].map(
        lambda k: all_classifications.get(k, {}).get("rarity", "common")
    )
    df["obtain_method"] = df["asset_key"].map(
        lambda k: all_classifications.get(k, {}).get("obtain_method", "store")
    )

    # 가격 매핑 (store만 가격 부여)
    price_map = {
        "common": 500,
        "rare": 0,
        "epic": 0,
        "legendary": 0
    }
    df["price_cr"] = df.apply(
        lambda x: price_map[x["rarity"]] if x["obtain_method"] == "store" else 0,
        axis=1
    )
    df["is_gacha_only"] = df["obtain_method"] == "gacha"

    # ✅ 기본템 규칙 강제 적용
    df = df.apply(force_basic_rule, axis=1)

    # CSV 저장
    df.to_csv(CSV_FILE, index=False, encoding="utf-8-sig")

    print("\n" + "=" * 60)
    print("✅ AI 희귀도 + 획득방식 + 기본템 규칙 분류 완료!")
    print("=" * 60)

    # 통계 출력
    rarity_dist = df["rarity"].value_counts().sort_index()
    print("\n📊 희귀도 분포:")
    for rarity, count in rarity_dist.items():
        percentage = (count / len(df)) * 100
        print(f"   {rarity:10s}: {count:3d}개 ({percentage:5.1f}%)")

    method_dist = df["obtain_method"].value_counts()
    print("\n🛒 획득방식 분포:")
    for method, count in method_dist.items():
        percentage = (count / len(df)) * 100
        print(f"   {method:10s}: {count:3d}개 ({percentage:5.1f}%)")

    print(f"\n💰 평균 가격: {df['price_cr'].mean():.0f} CR")
    print(f"🎰 가챠 전용: {df['is_gacha_only'].sum()}개 "
          f"({df['is_gacha_only'].sum() / len(df) * 100:.1f}%)")

# ==========================================
# 🚀 실행
# ==========================================
if __name__ == "__main__":
    if not API_KEY or not GMS_API_URL:
        print("❌ GMS API 키 또는 URL이 설정되지 않았습니다.")
        exit(1)

    apply_ai_classification()

    print("\n" + "=" * 60)
    print("💡 다음 단계:")
    print("   1. output/boutique_items.csv 확인")
    print("   2. 결과 검토 후 db_manager.py 실행")
    print("   3. 필요 시 Excel에서 희귀도/획득방식 수정 가능")
    print("=" * 60)
