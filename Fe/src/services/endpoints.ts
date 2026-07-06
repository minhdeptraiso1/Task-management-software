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
  userProjectCandidates: '/users/project-candidates',
  auditLogs: '/audit-logs',
  projects: '/projects',
  notifications: '/notifications',
  unreadNotifications: '/notifications/unread-count',
  dashboard: '/dashboard',
} as const

export const projectPath = (id: string) => `${endpoints.projects}/${id}`
