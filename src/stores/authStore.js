import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import { jwtDecode } from 'jwt-decode';
import router from '@/router';
import {
    adminLoginApi,
    refreshTokenApi,
    logoutApi,
    getUserMeApi
} from '@/api/AuthApi';

export const useAuthStore = defineStore('auth', () => {
    // ==================
    // State
    // ==================
    const accessToken = ref(null);
    const user = ref(null);
    const loading = ref(false);

    // ==================
    // Getters
    // ==================
    const isAuthenticated = computed(() => !!accessToken.value && !!user.value);

    const isAdmin = computed(() => {
        const role = user.value?.role;
        return role === 'ADMIN' || role === 'ROLE_ADMIN';
    });

    const username = computed(() => user.value?.name || user.value?.nickname || '');

    const userInfo = computed(() => user.value);

    const isTokenExpired = computed(() => {
        if (!accessToken.value) {
            return true;
        }
        try {
            const payload = jwtDecode(accessToken.value);
            const exp = payload.exp * 1000;
            return Date.now() >= exp;
        } catch (e) {
            console.error('JWT 디코딩 실패 (isTokenExpired):', e);
            return true;
        }
    });

    // ==================
    // Private Helpers
    // ==================

    const setAccessToken = (token) => {
        accessToken.value = token;
        if (token) {
            localStorage.setItem('accessToken', token);
        } else {
            localStorage.removeItem('accessToken');
        }
    };

    const setUser = (userData) => {
        user.value = userData || null;
        if (userData) {
            localStorage.setItem('user', JSON.stringify(userData));
        } else {
            localStorage.removeItem('user');
        }
    };

    const setUserFromToken = (token) => {
        try {
            const payload = jwtDecode(token);

            // sub를 userId로 사용 (Number 변환)
            const userId = payload.sub ? Number(payload.sub) : null;

            setUser({
                userId,              // thread / 권한 체크용
                id: userId,          // 혹시 id로 쓰는 곳도 대비
                email: payload.email ?? null,
                name: payload.name ?? null,
                nickname: payload.nickname ?? null, // 있으면 사용
                role: payload.role ?? null,
            });
        } catch (e) {
            console.error('JWT 디코딩 실패:', e);
            setUser(null);
        }
    };

    const fetchUserMe = async () => {
        try {
            const response = await getUserMeApi();
            const data = response.data;

            user.value = {
                ...user.value,
                email: data.email,
                name: data.name,
                nickname: data.nickname,
                createdAt: data.createdAt
            };

            localStorage.setItem('user', JSON.stringify(user.value));
        } catch (e) {
            console.error('유저 정보 불러오기 실패:', e);
            throw e;
        }
    };

    const loadFromStorage = () => {
        const token = localStorage.getItem('accessToken');
        const userStr = localStorage.getItem('user');

        if (token) {
            accessToken.value = token;
        }

        if (userStr) {
            try {
                user.value = JSON.parse(userStr);
            } catch {
                user.value = null;
            }
        }
    };

    // ==================
    // Actions
    // ==================

    const clearAuthState = () => {
        setAccessToken(null);
        setUser(null);
        loading.value = false;

        localStorage.removeItem('accessToken');
        localStorage.removeItem('user');
    };

    const handleOAuth2Success = (token) => {
        if (!token) {
            console.error('토큰이 없습니다.');
            return false;
        }

        try {
            setAccessToken(token);
            setUserFromToken(token);
            return true;
        } catch (error) {
            console.error('OAuth2 토큰 처리 실패:', error);
            return false;
        }
    };

    const loginAdmin = async (email, password) => {
        loading.value = true;

        try {
            const response = await adminLoginApi(email, password);
            const { success, data, message } = response.data;

            if (!success) {
                return {
                    success: false,
                    message: message || '로그인 실패'
                };
            }

            setAccessToken(data.accessToken);
            setUserFromToken(data.accessToken);

            if (!isAdmin.value) {
                clearAuthState();
                return {
                    success: false,
                    message: '관리자 권한이 없습니다.'
                };
            }

            return { success: true, user: user.value };

        } catch (error) {
            console.error('관리자 로그인 실패:', error);
            return {
                success: false,
                message: error.response?.data?.message || '로그인 요청 중 오류 발생'
            };
        } finally {
            loading.value = false;
        }
    };

    const refreshTokens = async () => {
        try {
            const response = await refreshTokenApi();
            const { success, data, message } = response.data;

            if (!success) {
                throw new Error(message || '토큰 재발급 실패');
            }

            setAccessToken(data.accessToken);
            setUserFromToken(data.accessToken);

            return true;

        } catch (error) {
            console.error('토큰 갱신 실패:', error);
            clearAuthState();
            throw error;
        }
    };

    const logout = async () => {
        try {
            await logoutApi();
        } catch (error) {
            console.error('로그아웃 요청 실패:', error);
        } finally {
            clearAuthState();
            router.push('/login');
        }
    };

    const validateToken = async () => {
        if (!accessToken.value) {
            return;
        }

        if (isTokenExpired.value) {
            try {
                await refreshTokens();
            } catch (error) {
                // refreshTokens handles its own error logging and state clearing
            }
        }
    };

    return {
        // State
        accessToken,
        user,
        loading,

        // Getters
        isAuthenticated,
        isAdmin,
        username,
        userInfo,
        isTokenExpired,

        // Actions
        handleOAuth2Success,
        loginAdmin,
        refreshTokens,
        logout,
        clearAuthState,
        loadFromStorage,
        fetchUserMe,
        validateToken,

        // Helpers
        setAccessToken,
        setUser,
        setUserFromToken,
    };
});