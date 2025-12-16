import axios from 'axios';
import { useAuthStore } from '@/stores/AuthStore';
import router from '@/router';

// ========== axios instance ==========
const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
    withCredentials: true,
});

// ========== Request Interceptor ==========
api.interceptors.request.use(
    (config) => {
        const authStore = useAuthStore();

        // 인증이 필요 없는 요청 (로그인, OAuth 등)
        const publicEndpoints = [
            '/oauth2/authorization',
            '/admin/login',
            '/auth/refresh',
        ];

        const isPublicEndpoint = publicEndpoints.some((endpoint) =>
            config.url.includes(endpoint)
        );

        if (isPublicEndpoint) {
            delete config.headers.Authorization;
            return config;
        }

        // 일반 요청은 access token 자동 포함
        if (authStore.accessToken) {
            config.headers.Authorization = `Bearer ${authStore.accessToken}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

// ========== Response Interceptor (401 처리 및 토큰 재발급) ==========
let isRefreshing = false;
let logoutShown = false;

api.interceptors.response.use(
    (response) => response,

    async (error) => {
        const authStore = useAuthStore();
        const originalRequest = error.config;

        // 네트워크 에러 등
        if (!error.response) return Promise.reject(error);

        const status = error.response.status;

        // 401 이외의 에러는 그대로 전달
        if (status !== 401) return Promise.reject(error);

        // refresh 요청 자체가 실패한 경우
        if (originalRequest.url.includes('/auth/refresh')) {
            if (!logoutShown) {
                logoutShown = true;
                alert('세션이 만료되었습니다. 다시 로그인해주세요.');
            }
            authStore.clearAuthState();
            router.push('/login');
            return Promise.reject(error);
        }

        // 인증 관련 요청은 refresh 하지 않음
        if (originalRequest.url.includes('/auth/')) {
            return Promise.reject(error);
        }

        // 토큰이 없으면 로그인 페이지로
        if (!authStore.accessToken) {
            authStore.clearAuthState();
            router.push('/login');
            return Promise.reject(error);
        }

        // 이미 재시도했거나 refresh 중이면 중복 방지
        if (originalRequest._retry || isRefreshing) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;
        isRefreshing = true;

        try {
            // refreshToken으로 accessToken 재발급
            await authStore.refreshTokens();
            isRefreshing = false;

            // 재발급한 토큰으로 원래 요청 재시도
            originalRequest.headers.Authorization = `Bearer ${authStore.accessToken}`;
            return api(originalRequest);
        } catch (refreshError) {
            isRefreshing = false;

            if (!logoutShown) {
                logoutShown = true;
                alert('세션이 만료되었습니다. 다시 로그인해주세요.');
            }

            authStore.clearAuthState();
            router.push('/login');
            return Promise.reject(refreshError);
        }
    }
);

export default api;