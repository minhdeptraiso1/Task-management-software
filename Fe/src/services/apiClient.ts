const API_URL = import.meta.env.VITE_API_URL ?? '/api'

export interface ApiError { code?: number; message: string }
export interface ApiResponse<T> { success: boolean; data: T; error: ApiError | null }

const tokenStore = {
  access: () => localStorage.getItem('taskflow_access_token'),
  refresh: () => localStorage.getItem('taskflow_refresh_token'),
  save: (accessToken: string, refreshToken: string) => {
    localStorage.setItem('taskflow_access_token', accessToken)
    localStorage.setItem('taskflow_refresh_token', refreshToken)
  },
  clear: () => {
    localStorage.removeItem('taskflow_access_token')
    localStorage.removeItem('taskflow_refresh_token')
  },
}

async function parse<T>(response: Response): Promise<ApiResponse<T>> {
  const body = await response.json().catch(() => null) as ApiResponse<T> | null
  if (!response.ok || !body?.success) throw new Error(body?.error?.message || 'Không thể kết nối đến máy chủ')
  return body
}

async function refreshAccessToken() {
  const refreshToken = tokenStore.refresh()
  if (!refreshToken) throw new Error('Phiên đăng nhập đã hết hạn')
  const response = await fetch(`${API_URL}/auth/refresh`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ refreshToken }) })
  const result = await parse<{ accessToken: string; refreshToken: string }>(response)
  tokenStore.save(result.data.accessToken, result.data.refreshToken)
}

export async function apiRequest<T>(path: string, options: RequestInit = {}, retry = true): Promise<T> {
  const headers = new Headers(options.headers)
  if (options.body) headers.set('Content-Type', 'application/json')
  const accessToken = tokenStore.access()
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`)
  const response = await fetch(`${API_URL}${path}`, { ...options, headers })
  if (response.status === 401 && retry && tokenStore.refresh() && path !== '/auth/refresh') {
    try { await refreshAccessToken(); return apiRequest<T>(path, options, false) }
    catch { tokenStore.clear(); window.dispatchEvent(new Event('auth:expired')); throw new Error('Phiên đăng nhập đã hết hạn') }
  }
  return (await parse<T>(response)).data
}

export { tokenStore }
