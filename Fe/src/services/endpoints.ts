export const endpoints = {
  login: '/auth/login',
  refresh: '/auth/refresh',
  logout: '/auth/logout',
  logoutAll: '/auth/logout-all',
  changePassword: '/auth/change-password',
  me: '/users/me',
  users: '/users',
  userSearch: '/users/search',
  userRoles: '/users/roles',
  auditLogs: '/audit-logs',
} as const
