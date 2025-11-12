import requests

url = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"
headers = {
    "Authorization": "Bearer S13P32A405-4cd203ce-2168-4382-af52-d65465913690",
    "Content-Type": "application/json"
}
payload = {
    "model": "gpt-4.1-mini",  # GMS에 표시된 모델 중 하나
    "messages": [
        {"role": "user", "content": "안녕! GMS API 테스트 중이야."}
    ]
}

resp = requests.post(url, headers=headers, json=payload)
print(resp.status_code)
print(resp.text)
