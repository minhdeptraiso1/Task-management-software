export interface LoginCredentials { email: string; password: string }
export interface AuthTokens { accessToken: string; refreshToken: string }
export interface ChangePasswordData { currentPassword: string; newPassword: string; confirmPassword: string }
