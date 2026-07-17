const API_URL = import.meta.env.VITE_API_URL ?? '/api'

export interface ApiError { code?: number; message: string }
export interface ApiResponse<T> { success: boolean; data: T; error: ApiError | null }

export class ApiRequestError extends Error {
  status: number
  code?: number
  body: unknown

  constructor(message: string, status: number, body: unknown, code?: number) {
    super(message)
    this.name = 'ApiRequestError'
    this.status = status
    this.code = code
    this.body = body
  }
}

function setCookie(name: string, value: string, days = 30) {
  const date = new Date()
  date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000)
  document.cookie = `${name}=${encodeURIComponent(value)};expires=${date.toUTCString()};path=/;SameSite=Lax`
}

function getCookie(name: string) {
  const match = document.cookie.match(new RegExp(`(^| )${name}=([^;]+)`))
  return match ? decodeURIComponent(match[2]) : null
}

function removeCookie(name: string) {
  document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;`
}

const tokenStore = {
  access: () => getCookie('taskflow_access_token'),
  refresh: () => getCookie('taskflow_refresh_token'),
  save: (accessToken: string, refreshToken: string) => {
    setCookie('taskflow_access_token', accessToken, 1) // Access token typically expires quickly
    setCookie('taskflow_refresh_token', refreshToken, 30) // Refresh token lasts longer
  },
  clear: () => {
    removeCookie('taskflow_access_token')
    removeCookie('taskflow_refresh_token')
  },
}

type AnyRecord = Record<string, unknown>

let refreshPromise: Promise<void> | null = null

function isRecord(value: unknown): value is AnyRecord {
  return Boolean(value) && typeof value === 'object' && !Array.isArray(value)
}

function getString(value: unknown) {
  return typeof value === 'string' && value.trim() ? value.trim() : ''
}

function defaultErrorMessage(status: number) {
  if (status === 400) return 'Dữ liệu gửi lên chưa hợp lệ'
  if (status === 401) return 'Phiên đăng nhập đã hết hạn'
  if (status === 403) return 'Bạn không có quyền thực hiện thao tác này'
  if (status === 404) return 'Không tìm thấy dữ liệu'
  if (status === 409) return 'Dữ liệu đang xung đột, vui lòng tải lại và thử lại'
  if (status >= 500) return 'Máy chủ đang gặp lỗi, vui lòng thử lại sau'
  return 'Không thể kết nối đến máy chủ'
}

function extractError(body: unknown, response: Response) {
  if (!isRecord(body)) return { message: defaultErrorMessage(response.status), code: undefined }

  const error = body.error
  if (isRecord(error)) {
    const message = getString(error.message) || getString(error.detail) || getString(error.error)
    const code = typeof error.code === 'number' ? error.code : undefined
    if (message) return { message, code }
  }

  if (typeof error === 'string' && error.trim()) return { message: error.trim(), code: undefined }

  const data = body.data
  if (isRecord(data)) {
    const message = getString(data.message) || getString(data.errorMessage)
    if (message) return { message, code: undefined }
  }

  const message =
    getString(body.message) ||
    getString(body.detail) ||
    getString(body.title) ||
    defaultErrorMessage(response.status)

  return { message, code: undefined }
}

function shouldAttachJsonContentType(body: BodyInit | null | undefined) {
  return Boolean(body) && !(body instanceof FormData) && !(body instanceof Blob) && !(body instanceof URLSearchParams)
}

async function parse<T>(response: Response): Promise<ApiResponse<T>> {
  if (response.status === 204) return { success: true, data: undefined as T, error: null }
  const body = await response.json().catch(() => null) as ApiResponse<T> | null
  if (!response.ok || !body?.success) {
    const { message, code } = extractError(body, response)
    throw new ApiRequestError(message, response.status, body, code)
  }
  return body
}

async function refreshAccessToken() {
  const refreshToken = tokenStore.refresh()
  if (!refreshToken) throw new Error('Phiên đăng nhập đã hết hạn')
  const response = await fetch(`${API_URL}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
  const result = await parse<{ accessToken: string; refreshToken: string }>(response)
  tokenStore.save(result.data.accessToken, result.data.refreshToken)
}

async function ensureFreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = refreshAccessToken().finally(() => {
      refreshPromise = null
    })
  }
  return refreshPromise
}

export async function apiFetch(path: string, options: RequestInit = {}, retry = true): Promise<Response> {
  const headers = new Headers(options.headers)
  if (shouldAttachJsonContentType(options.body) && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')

  const accessToken = tokenStore.access()
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`)

  const response = await fetch(`${API_URL}${path}`, { ...options, headers })
  if (response.status === 401 && retry && tokenStore.refresh() && path !== '/auth/refresh') {
    try {
      await ensureFreshAccessToken()
      return apiFetch(path, options, false)
    } catch {
      tokenStore.clear()
      window.dispatchEvent(new Event('auth:expired'))
      throw new ApiRequestError('Phiên đăng nhập đã hết hạn', 401, null)
    }
  }

  if (response.status === 401 && path !== '/auth/login' && path !== '/auth/refresh') {
    tokenStore.clear()
    window.dispatchEvent(new Event('auth:expired'))
  }

  return response
}

export async function apiRequest<T>(path: string, options: RequestInit = {}, retry = true): Promise<T> {
  const response = await apiFetch(path, options, retry)
  return (await parse<T>(response)).data
}

export async function downloadExcelFile(path: string, defaultFileName: string) {
  const response = await apiFetch(path, { method: 'GET' })
  if (!response.ok) {
    throw new Error('Không thể xuất file Excel')
  }
  const blob = await response.blob()
  let filename = defaultFileName
  const disposition = response.headers.get('Content-Disposition')
  if (disposition) {
    const filenameRegex = /filename\*?=(?:UTF-8'')?((['"]).*?\2|[^;\n]*)/i
    const matches = filenameRegex.exec(disposition)
    if (matches != null && matches[1]) { 
      filename = decodeURIComponent(matches[1].replace(/['"]/g, ''))
    }
  }

  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  window.URL.revokeObjectURL(url)
}

export { tokenStore }
