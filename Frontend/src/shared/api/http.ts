// src/shared/api/http.ts

const API_ORIGIN = import.meta.env.VITE_API_ORIGIN ?? "";

function toURL(path: string): string {
  if (/^https?:\/\//i.test(path)) return path;
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
