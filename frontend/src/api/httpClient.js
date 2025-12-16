// src/api/httpClient.js
import axios from 'axios'
import { useAuthStore } from '@/stores/AuthStore' // 대소문자 통일 (중요)

const apiClient = axios.create({
    baseURL: '/api',
    withCredentials: true,
})

apiClient.interceptors.request.use(
    (config) => {
        config.headers = config.headers ?? {}

        // Pinia가 준비되기 전 타이밍에서도 최소한 localStorage 토큰은 붙도록 방어
        let token = localStorage.getItem('accessToken')

        try {
            const authStore = useAuthStore()

            // 새로고침 직후 대비
            if (!authStore.accessToken && typeof authStore.loadFromStorage === 'function') {
                authStore.loadFromStorage()
            }

            token = authStore.accessToken || token
        } catch (e) {
            // useAuthStore 호출이 실패하는 환경(핀야 미설치 등) 대비: localStorage 토큰만 사용
        }

        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }

        return config
    },
    (error) => Promise.reject(error),
)

export default apiClient
