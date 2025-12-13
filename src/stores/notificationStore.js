// src/stores/notificationStore.js
import { ref } from 'vue'
import { defineStore } from 'pinia'
import { EventSourcePolyfill } from 'event-source-polyfill'
import { useAuthStore } from '@/stores/AuthStore'
import { fetchMyNotifications, fetchMyUnreadCount } from '@/api/notificationApi.js'

export const useNotificationStore = defineStore('notification', () => {
    const notifications = ref([]) // [{ id, title, body, read, createdAt }]
    const unreadCount = ref(0)

    const sse = ref(null)
    const connected = ref(false)

    // 재연결 제어
    const reconnectTimer = ref(null)
    const reconnectAttempt = ref(0)

    const authStore = useAuthStore()

    // =========================
    // 1) 알림 목록 조회
    // =========================
    async function loadNotifications(page = 0, size = 10) {
        // 새로고침 직후 대비
        if (!authStore.accessToken && typeof authStore.loadFromStorage === 'function') {
            authStore.loadFromStorage()
        }
        if (!authStore.accessToken) return

        const pageRes = await fetchMyNotifications({ page, size })

        notifications.value = (pageRes?.notifications || []).map((n) => ({
            id: n.notificationId,
            title: n.title,
            body: n.content,
            read: !!n.read,
            createdAt: n.createdAt,
        }))
    }

    // =========================
    // 2) 미읽음 개수 조회
    // =========================
    async function fetchUnreadCountAction() {
        if (!authStore.accessToken && typeof authStore.loadFromStorage === 'function') {
            authStore.loadFromStorage()
        }
        if (!authStore.accessToken) return

        const cnt = await fetchMyUnreadCount()
        unreadCount.value = Number(cnt) || 0
    }

    // =========================
    // 내부: SSE 연결 정리
    // =========================
    function cleanupSse() {
        if (reconnectTimer.value) {
            clearTimeout(reconnectTimer.value)
            reconnectTimer.value = null
        }
        if (sse.value) {
            try {
                sse.value.close()
            } catch (_) {}
            sse.value = null
        }
        connected.value = false
    }

    // =========================
    // 3) SSE 연결 (/api/notifications/me/stream)
    // - 기본 EventSource는 Authorization 헤더를 못 붙임
    // - EventSourcePolyfill로 Bearer 토큰 헤더를 붙여서 인증 통과
    // =========================
    async function connectSse() {
        if (connected.value) return

        // 새로고침 직후 대비
        if ((!authStore.user || !authStore.accessToken) && typeof authStore.loadFromStorage === 'function') {
            authStore.loadFromStorage()
        }
        if (!authStore.user) return

        // 토큰 만료면 갱신 시도(선택)
        if (typeof authStore.validateToken === 'function') {
            try {
                await authStore.validateToken()
            } catch (_) {
                // validateToken 내부에서 정리될 수 있음
            }
        }

        const token = authStore.accessToken
        if (!token) return

        // 혹시 남아있던 연결 정리
        cleanupSse()

        const es = new EventSourcePolyfill('/api/notifications/me/stream', {
            withCredentials: true,
            headers: {
                Authorization: `Bearer ${token}`,
            },
        })

        sse.value = es
        connected.value = true
        reconnectAttempt.value = 0

        es.addEventListener('INIT', (event) => {
            console.log('[SSE] INIT', event.data)
        })

        // 새로운 알림 생성
        es.addEventListener('NOTIFICATION', (event) => {
            try {
                const payload = JSON.parse(event.data)
                console.log('[SSE] NOTIFICATION', payload)

                // 중복 방지 (재연결/리플레이 대비)
                const exists = notifications.value.some((n) => n.id === payload.notificationId)
                if (!exists) {
                    notifications.value.unshift({
                        id: payload.notificationId,
                        title: payload.title,
                        body: payload.content,
                        read: false,
                        createdAt: payload.createdAt,
                    })
                }

                // 이벤트에 실린 unreadCount를 신뢰(있을 때)
                if (typeof payload.unreadCount !== 'undefined' && payload.unreadCount !== null) {
                    unreadCount.value = Number(payload.unreadCount) || 0
                }
            } catch (e) {
                console.error('[SSE] NOTIFICATION parse error', e, event.data)
            }
        })

        // 읽음 처리 후 미읽음 개수 갱신
        es.addEventListener('UNREAD_COUNT', (event) => {
            // 서버가 숫자만 보내면 event.data는 "3" 같은 문자열일 가능성이 큼
            const direct = Number(event.data)
            if (!Number.isNaN(direct)) {
                unreadCount.value = direct
                return
            }

            // 혹시 JSON으로 내려오는 경우 대비
            try {
                const parsed = JSON.parse(event.data)
                unreadCount.value = Number(parsed) || 0
            } catch (e) {
                console.error('[SSE] UNREAD_COUNT parse error', e, event.data)
            }
        })

        es.addEventListener('PING', () => {
            // keep-alive
        })

        es.onerror = (err) => {
            console.error('[SSE] error', err)

            // 연결 정리
            cleanupSse()

            // 로그인 상태면 재연결 시도 (지수 백오프 느낌으로 간단히)
            if (authStore.user && authStore.accessToken) {
                reconnectAttempt.value += 1
                const delay = Math.min(30000, 1000 * reconnectAttempt.value) // 1s,2s,3s.. 최대 30s
                reconnectTimer.value = setTimeout(() => {
                    connectSse()
                }, delay)
            }
        }
    }

    // =========================
    // 4) SSE 해제 (로그아웃 시)
    // =========================
    function disconnectSse() {
        cleanupSse()
        reconnectAttempt.value = 0
        notifications.value = []
        unreadCount.value = 0
    }

    return {
        notifications,
        unreadCount,
        connected,
        loadNotifications,
        fetchUnreadCount: fetchUnreadCountAction,
        connectSse,
        disconnectSse,
    }
})
