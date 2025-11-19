// src/shared/api/http.ts

const API_ORIGIN = import.meta.env.VITE_API_ORIGIN ?? "";

function toURL(path: string): string {
  if (/^https?:\/\//i.test(path)) return path;

  
  /* ✅ ADDED: 개발(프록시) 모드일 때는 프론트 경로 앞에 자동으로 "/api"를 붙여서
     Vite proxy(/api -> /api/v1)를 타게 함. 운영에서는 API_ORIGIN이 /api/v1까지 포함되어
     있으므로 기존 로직을 그대로 사용. */
  const isApiOriginEmpty = !API_ORIGIN;
  if (isApiOriginEmpty) {
    // 항상 슬래시로 시작하도록 보정
    let p = path.startsWith("/") ? path : `/${path}`;
    // 이미 /api/로 시작하면 그대로 사용, 아니면 /api 접두어 부여
    if (!p.startsWith("/api/")) p = `/api${p}`;
    return p; // 같은 오리진 경로로 반환 → Vite가 프록시 처리
  }

  if (!path.startsWith("/")) return `${API_ORIGIN}/${path}`;
  return `${API_ORIGIN}${path}`;
}

function isJson(res: Response) {
  const ct = res.headers.get("content-type") || "";
  return ct.includes("application/json");
}

function hasBody(res: Response) {
  // 일부 서버는 200이지만 본문이 없음 → content-length 0 이거나 미지정
  const len = res.headers.get("content-length");
  return res.status !== 204 && res.status !== 205 && len !== "0";
}

// GET: 항상 JSON 기대
export async function apiGet<T>(path: string): Promise<T> {
  const url = toURL(path);
  // const res = await fetch(url, { credentials: "omit" });
  const res = await fetch(url, { credentials: "include" }); // fetch 기본 옵션 중 credentials="include"로 바꾸면, 쿠키 기반 인증도 대응 가능

  if (!res.ok) {
    // 에러 응답 본문 파싱
    let errorData;
    if (hasBody(res) && isJson(res)) {
      errorData = await res.json();
    }

    const error: any = new Error(`GET ${path} -> ${res.status}`);
    error.response = {
      status: res.status,
      data: errorData,
    };
    throw error;
  }

  if (!isJson(res)) {
    const text = await res.text();
    throw new Error(`200 OK 🔶 non-JSON response:\n${text.slice(0, 120)}...`);
  }
  return (await res.json()) as T;
}

export type WriteMethod = "POST" | "PATCH" | "DELETE";

// POST/PATCH/DELETE: JSON이면 파싱, 아니면 void 반환
export async function apiPost<T = void, B = unknown>(
  path: string,
  body?: B,
  method: WriteMethod = "POST"
): Promise<T> {
  const url = toURL(path);

  const headers: HeadersInit = {};
  const init: RequestInit = {
    method,
    credentials: "omit",
    headers,
  };

  if (body !== undefined) {
    (headers as Record<string, string>)["Content-Type"] = "application/json";
    (init as RequestInit & { body: BodyInit }).body = JSON.stringify(body);
  }

  const res = await fetch(url, init);

  if (!res.ok) {
    // 에러 응답 본문 파싱
    let errorData;
    if (hasBody(res) && isJson(res)) {
      errorData = await res.json();
    }

    const error: any = new Error(`${method} ${path} -> ${res.status}`);
    error.response = {
      status: res.status,
      data: errorData,
    };
    throw error;
  }

  if (hasBody(res) && isJson(res)) {
    return (await res.json()) as T;
  }
  // 본문이 없거나 JSON이 아니면 성공으로 간주하고 void 반환
  return undefined as T;
}
