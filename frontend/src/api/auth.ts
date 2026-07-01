import request from './request'
import type { ApiResponse, LoginParams, LoginResult, UserInfo } from '@/types'

export function login(data: LoginParams) {
  return request.post<ApiResponse<LoginResult>>('/auth/login', data)
}

export function register(data: LoginParams) {
  return request.post<ApiResponse>('/auth/register', data)
}

export function refreshToken(refreshToken: string) {
  return request.post<ApiResponse<LoginResult>>('/auth/refresh', { refreshToken })
}

export function logout() {
  return request.post<ApiResponse>('/auth/logout')
}

export function getCurrentUser() {
  return request.get<ApiResponse<UserInfo>>('/auth/me')
}
