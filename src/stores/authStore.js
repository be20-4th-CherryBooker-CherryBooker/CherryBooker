// src/stores/authStore.js
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { jwtDecode } from 'jwt-decode'

import { loginApi, refreshApi, logoutApi } from '@/api/authApi'
import { fetchMyInfoApi, fetchNotificationAuthMe } from '@/api/userApi'
import router from '@/router'
import { useNotificationStore } from '@/stores/notificationStore.js'

/*
  useAuthStore: 인증 전역 상태
  - accessToken: 현재 로그인한 사용자의 Access Token (문자열)
  - user: { userId, username, role, ... } 형태의 사용자 정보
  - loading: 로그인/재발급 진행 여부
*/
export const useAuthStore = defineStore('auth', () => {
    // 1) state
    const accessToken = ref(null)
    const user = ref(null)
    const loading = ref(false)

    // 2) getters
    const isLoggedIn = computed(() => !!accessToken.value && !!user.value)

    const isAdmin = computed(() => {
        const role = user.value?.role
        return role === 'ADMIN' || role === 'ROLE_ADMIN'
    })

    const username = computed(() => user.value?.username || '')

    // 3) 유틸 함수들

    const setAccessToken = (token) => {
        accessToken.value = token
        if (token) {
            localStorage.setItem('accessToken', token)
        } else {
            localStorage.removeItem('accessToken')
        }
    }

    const setUser = (userData) => {
        user.value = userData || null
        if (userData) {
            localStorage.setItem('user', JSON.stringify(userData))
        } else {
            localStorage.removeItem('user')
        }
    }

    const setUserFromToken = (token) => {
        try {
            const payload = jwtDecode(token)
            console.log('[setUserFromToken] decoded payload =', payload)

            // sub가 userId이므로 Number로 변환
            const userIdFromToken = payload.sub ? Number(payload.sub) : null

            // 화면에서 표시용 이름: userName(=name) 우선, 없으면 이메일, 그것도 없으면 sub
            const usernameFromToken =
                payload.name ||    // userName
                payload.email ||   // 이메일
                payload.sub || ''  // 최후 fallback

            const roleFromToken = payload.role

            if (!userIdFromToken || !roleFromToken) {
                // 최소한 userId, role은 있어야 "로그인된 상태"로 인정
                setUser(null)
                return
            }

            setUser({
                userId: userIdFromToken,
                username: usernameFromToken,
                role: roleFromToken,
                email: payload.email ?? null,
                name: payload.name ?? null,
            })
        } catch (e) {
            console.error('Failed to decode JWT', e)
            setUser(null)
        }
    }

    /**
     * loadFromStorage:
     * - 새로고침 후 localStorage의 accessToken / user를 상태로 복원
     */
    const loadFromStorage = () => {
        const token = localStorage.getItem('accessToken')
        const userStr = localStorage.getItem('user')

        if (token) {
            accessToken.value = token
        }
        if (userStr) {
            try {
                user.value = JSON.parse(userStr)
            } catch {
                user.value = null
            }
        }
    }

    const clearAuthState = () => {
        setAccessToken(null)
        setUser(null)
        loading.value = false
        router.push('/login')
    }

    // 4) actions

    /**
     * 1) ID/PW 로그인용 (백엔드에 따로 /auth/login 같은 게 있다면 사용)
     *    - 현재 OAuth2(Google/Kakao)만 쓰는 경우, 이 함수는 안 써도 됨.
     *    - 백엔드 AuthResponse { accessToken, isNewUser } 구조와 맞춰 사용.
     */
    const login = async ({ userLoginId, password }) => {
        loading.value = true

        try {
            const res = await loginApi(userLoginId, password)
            const { success, data, message } = res.data

            if (!success) {
                return {
                    success: false,
                    message: message || '로그인에 실패했습니다.',
                }
            }

            // AuthResponse { accessToken, isNewUser }
            setAccessToken(data.accessToken)
            setUserFromToken(data.accessToken)

            const notificationStore = useNotificationStore()
            await notificationStore.fetchUnreadCount()
            notificationStore.connectSse()

            // 필요하다면 /users/me 로 full user 정보 덮어쓰기
            // await loadMyInfo()

            return { success: true }
        } catch (e) {
            return {
                success: false,
                message: e.response?.data?.message || '로그인 요청 중 오류가 발생했습니다.',
            }
        } finally {
            loading.value = false
        }
    }

    /**
     * 2) OAuth2 (Google/Kakao) 로그인 성공 콜백 처리용
     *
     * - 백엔드 OAuth2SuccessHandler 에서:
     *   redirectUrl = "http://localhost:5173/oauth2/success?accessToken=..."
     * - 프론트 /oauth2/success 페이지에서:
     *   const accessToken = route.query.accessToken
     *   authStore.handleOAuth2Login(accessToken)
     */
    const handleOAuth2Login = async (accessTokenFromQuery) => {
        if (!accessTokenFromQuery) {
            console.error('OAuth2 Login: accessToken이 없습니다.')
            return
        }

        setAccessToken(accessTokenFromQuery)
        setUserFromToken(accessTokenFromQuery)

        // 필요 시 서버에서 추가 정보 로드
        // await loadMyInfo()

        const notificationStore = useNotificationStore()
        await notificationStore.fetchUnreadCount()
        notificationStore.connectSse()
    }

    /**
     * 3) refreshTokens:
     * - POST /auth/refresh  (현재 AuthController.refresh)
     * - 응답: ApiResponse<AuthResponse>
     */
    const refreshTokens = async () => {
        try {
            const res = await refreshApi()
            const { success, data, message } = res.data

            if (!success) {
                throw new Error(message || '토큰 재발급 실패')
            }

            setAccessToken(data.accessToken)
            setUserFromToken(data.accessToken)
            // 필요하면 여기서도 loadMyInfo() 호출 가능
        } catch (e) {
            setAccessToken(null)
            setUser(null)
            throw e
        }
    }

    /**
     * 4) logout:
     * - POST /auth/logout 호출 → Redis refresh 삭제 + refreshToken 쿠키 삭제
     * - 호출 성공/실패와 상관 없이 프론트 상태 정리
     */
    const logout = async () => {
        const notificationStore = useNotificationStore()

        try {
            await logoutApi()
        } catch (e) {
            // 서버에서 401/500 등 나와도 클라이언트 상태는 지워야 하므로 경고만
            console.warn('logoutApi 호출 실패(무시하고 클라이언트 상태만 정리)', e)
        } finally {
            notificationStore.disconnectSse()
            clearAuthState()
            console.log('로그아웃 완료')
        }
    }

    /**
     * 5) /users/me 로 내 정보 불러오기
     *
     * - 현재 User 엔티티 필드:
     *   userId, userEmail, userName, userNickname, userRole, loginType ...
     * - DTO도 이와 유사하다고 가정하고 매핑.
     */
    const loadMyInfo = async () => {
        if (!accessToken.value) return

        try {
            const dto = await fetchMyInfoApi()
            console.log('[loadMyInfo] dto from /users/me =', dto)

            const resolvedUserId = dto.userId ?? null

            const resolvedUsername =
                dto.userNickname ??    // 닉네임 우선
                dto.userName ??        // 또는 이름
                dto.userEmail ?? ''    // 최후 fallback

            const resolvedRole =
                dto.userRole ??        // Enum 이름 (예: USER, ADMIN)
                dto.role ?? null

            setUser({
                ...dto,
                userId: resolvedUserId,
                username: resolvedUsername,
                role: resolvedRole,
            })
        } catch (e) {
            const status = e.response?.status

            if (status === 401) {
                console.warn('loadMyInfo: 401 → 토큰 무효, 상태 초기화')
                clearAuthState()
                return
            }

            console.error('loadMyInfo 에러', e)
            setUser(null)
        }
    }

    /**
     * 6) ensureUserId:
     *  - 이미 user.userId 있으면 그대로 반환
     *  - 없으면 /auth/me (혹은 알림 서비스용 /notifications/auth/me) 호출해서 채움
     *  - notificationApi.js 에서 userId 필요할 때 사용
     */
    const ensureUserId = async () => {
        if (user.value?.userId) {
            return user.value.userId
        }

        if (!accessToken.value) {
            return null
        }

        try {
            // 알림/공통 인증용 /auth/me 호출 (백엔드에서 UserPrincipal 기반 DTO 반환)
            const dto = await fetchNotificationAuthMe()
            console.log('[ensureUserId] dto from /auth/me =', dto)

            const resolvedUserId = dto.userId ?? Number(dto.id) ?? null

            const resolvedUsername =
                dto.username ??
                dto.userNickname ??
                dto.userName ??
                dto.loginId ??
                user.value?.username ??
                ''

            const resolvedRole =
                dto.role ??
                dto.userRole ??
                user.value?.role ??
                null

            setUser({
                ...(user.value || {}),
                userId: resolvedUserId,
                username: resolvedUsername,
                role: resolvedRole,
            })

            return resolvedUserId
        } catch (e) {
            console.error('[authStore.ensureUserId] /auth/me 로딩 실패', e)
            return null
        }
    }

    // 7) 외부로 노출
    return {
        accessToken,
        user,
        loading,
        isLoggedIn,
        isAdmin,
        username,
        setAccessToken,
        setUser,
        setUserFromToken,
        loadFromStorage,
        clearAuthState,
        login,
        handleOAuth2Login,
        refreshTokens,
        logout,
        loadMyInfo,
        ensureUserId,
    }
})
