import api from '@/axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * 소셜 로그인 리다이렉트
 * @param {string} provider - 'google' | 'kakao'
 */
export const redirectToSocialLogin = (provider) => {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/${provider}`;
};

/**
 * 관리자 로그인
 * @param {string} email
 * @param {string} password
 * @returns {Promise}
 */
export const adminLoginApi = async (email, password) => {
    const response = await api.post('/auth/admin/login', { email, password });
    return response;
};

/**
 * 토큰 재발급
 * @returns {Promise}
 */
export const refreshTokenApi = async () => {
    const response = await api.post('/auth/refresh', {});
    return response;
};

/**
 * 로그아웃
 * @returns {Promise}
 */
export const logoutApi = async () => {
    const response = await api.post('/auth/logout', {});
    return response;
};

