const TOKEN_KEY = 'lt_token';

export const UNAUTHORIZED_EVENT = 'lt:unauthorized';

export interface Link {
  id: string;
  url: string;
  tags: string[];
}

export interface Notification {
  id: string;
  linkId: string | null;
  message: string;
  createdAt: string;
  readAt: string | null;
}

export interface TokenResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface MeResponse {
  id: string;
  email: string;
  telegramLinked: boolean;
}

export interface LinksResponse {
  links: Link[];
  size: number;
}

export interface NotificationsResponse {
  items: Notification[];
  unreadCount: number;
}

export interface MarkReadResponse {
  updated: number;
}

interface ErrorBody {
  description?: string;
  code?: string;
  exceptionName?: string;
  exceptionMessage?: string;
  stacktrace?: unknown;
}

export class ApiError extends Error {
  readonly status: number;
  readonly description?: string;
  readonly exceptionMessage?: string;
  readonly code?: string;
  readonly exceptionName?: string;

  constructor(status: number, body?: ErrorBody | null) {
    super(body?.description ?? body?.exceptionMessage ?? `Ошибка запроса (${status})`);
    this.name = 'ApiError';
    this.status = status;
    this.description = body?.description;
    this.exceptionMessage = body?.exceptionMessage;
    this.code = body?.code;
    this.exceptionName = body?.exceptionName;
  }
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export function apiErrorMessage(err: unknown): string {
  if (err instanceof ApiError) {
    return err.description ?? err.exceptionMessage ?? `Ошибка запроса (${err.status})`;
  }
  if (err instanceof Error && err.message !== '') {
    return err.message;
  }
  return 'Произошла неизвестная ошибка';
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const headers: Record<string, string> = {};
  const token = getToken();
  if (token !== null) {
    headers.Authorization = `Bearer ${token}`;
  }
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }

  let res: Response;
  try {
    res = await fetch(path, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, { description: 'Нет соединения с сервером' });
  }

  let payload: unknown = null;
  try {
    const text = await res.text();
    if (text.length > 0) {
      payload = JSON.parse(text) as unknown;
    }
  } catch {
    payload = null;
  }

  if (!res.ok) {
    if (res.status === 401) {
      clearToken();
      window.dispatchEvent(new CustomEvent(UNAUTHORIZED_EVENT));
    }
    throw new ApiError(res.status, (payload as ErrorBody | null) ?? null);
  }

  return payload as T;
}

export function register(email: string, password: string): Promise<MeResponse> {
  return request<MeResponse>('POST', '/auth/register', { email, password });
}

export function login(email: string, password: string): Promise<TokenResponse> {
  return request<TokenResponse>('POST', '/auth/login', { email, password });
}

export function me(): Promise<MeResponse> {
  return request<MeResponse>('GET', '/auth/me');
}

export function getLinks(): Promise<LinksResponse> {
  return request<LinksResponse>('GET', '/api/links');
}

export function addLink(url: string, tags: string[]): Promise<Link> {
  return request<Link>('POST', '/api/links', { link: url, tags });
}

export function removeLink(url: string): Promise<Link> {
  return request<Link>('DELETE', '/api/links', { link: url });
}

export function getNotifications(unread: boolean, limit: number): Promise<NotificationsResponse> {
  const query = new URLSearchParams({
    unread: String(unread),
    limit: String(limit),
  });
  return request<NotificationsResponse>('GET', `/api/notifications?${query.toString()}`);
}

export function markRead(ids: string[] | null, all: boolean): Promise<MarkReadResponse> {
  return request<MarkReadResponse>('POST', '/api/notifications/read', { ids, all });
}
