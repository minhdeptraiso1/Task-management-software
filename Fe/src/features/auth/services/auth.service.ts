import { apiRequest, tokenStore } from '../../../services/apiClient'
import { endpoints } from '../../../services/endpoints'
import type { AuthTokens, LoginCredentials } from '../models/auth.model'

export async function login(credentials: LoginCredentials) {
  const tokens = await apiRequest<AuthTokens>(endpoints.login, { method: 'POST', body: JSON.stringify(credentials) })
  tokenStore.save(tokens.accessToken, tokens.refreshToken)
}

export async function logout() {
  const refreshToken = tokenStore.refresh()
  try { if (refreshToken) await apiRequest<void>(endpoints.logout, { method: 'POST', body: JSON.stringify({ refreshToken }) }, false) }
  finally { tokenStore.clear() }
}
