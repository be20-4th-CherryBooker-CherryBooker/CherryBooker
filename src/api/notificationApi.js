// src/api/notificationApi.js
import apiClient from '@/api/httpClient'
import { useAuthStore } from '@/stores/authStore'

// 공통 헬퍼 함수
async function getCurrentUserId() {
    const authStore = useAuthStore()

    // 로그인 안 되어 있으면 바로 에러
    if (!authStore.isAuthenticated) {
        throw new Error('[notificationApi] 로그인 상태가 아닙니다.')
    }

    // authStore.ensureUserId()가 필요하면 /auth/me 부르면서 userId 채워줌
    const userId = await authStore.ensureUserId()

    if (!userId) {
        throw new Error('[notificationApi] 현재 로그인 유저의 userId가 없습니다.')
    }

    return userId
}

/**
 * 관리자 템플릿 목록/검색 조회
 */
export async function fetchAdminNotificationTemplates({
                                                          keyword = '',
                                                          page = 0,
                                                          size = 10,
                                                      } = {}) {
    const res = await apiClient.get('/admin/notifications/templates', {
        params: {
            keyword: keyword || undefined,
            page,
            size,
        },
    })

    return res.data?.data
}

/**
 * 관리자 템플릿 생성
 */
export async function createAdminNotificationTemplate({
                                                          templateKind = 'NORMAL',
                                                          templateTitle,
                                                          templateBody,
                                                      }) {
    const res = await apiClient.post('/admin/notifications/templates', {
        templateKind,
        templateTitle,
        templateBody,
    })

    return res.data?.data
}

/**
 * 관리자 템플릿 수정
 */
export async function updateAdminNotificationTemplate(
    templateId,
    { templateKind = 'NORMAL', templateTitle, templateBody },
) {
    const res = await apiClient.put(`/admin/notifications/templates/${templateId}`, {
        templateKind,
        templateTitle,
        templateBody,
    })

    return res.data?.data
}

/**
 * 관리자 템플릿 삭제
 */
export async function deleteAdminNotificationTemplate(templateId) {
    const res = await apiClient.delete(`/admin/notifications/templates/${templateId}`)
    return res.data
}

/**
 * 관리자용 알림 발송 내역 조회
 */
export async function fetchAdminNotificationHistory({
                                                        page = 0,
                                                        size = 10,
                                                    } = {}) {
    const res = await apiClient.get('/admin/notifications', {
        params: { page, size },
    })

    return res.data.data
}

/**
 * 관리자 알림 발송(즉시)
 */
export async function sendAdminNotificationNow(templateId, request) {
    const res = await apiClient.post(
        `/admin/notifications/${templateId}/send`,
        request,
    )

    return res.data.data
}

/**
 * 관리자 알림 발송(예약)
 */
export async function reserveAdminNotification(templateId, request) {
    const res = await apiClient.post(
        `/admin/notifications/${templateId}/reserve`,
        request,
    )

    return res.data.data
}

/* ===================== 유저용 알림 API ===================== */

// 내 알림 목록 조회
export async function fetchMyNotifications({ page = 0, size = 10 } = {}) {
    const userId = await getCurrentUserId()

    const res = await apiClient.get(`/users/${userId}/notifications`, {
        params: { page, size },
    })
    return res.data.data
}

// 내 미읽음 개수 조회
export async function fetchMyUnreadCount() {
    const userId = await getCurrentUserId()

    const res = await apiClient.get(`/users/${userId}/notifications/unread-count`)
    return res.data.data
}

// 내 알림 하나 읽음 처리
export async function markMyNotificationRead(notificationId) {
    const userId = await getCurrentUserId()

    const res = await apiClient.patch(
        `/users/${userId}/notifications/${notificationId}/read`,
    )
    return res.data
}

// 내 알림 모두 읽음 처리
export async function markMyNotificationsReadAll() {
    const userId = await getCurrentUserId()

    const res = await apiClient.patch(`/users/${userId}/notifications/read-all`)
    return res.data
}

// 내 알림 하나 삭제
export async function deleteMyNotification(notificationId) {
    const userId = await getCurrentUserId()

    const res = await apiClient.delete(
        `/users/${userId}/notifications/${notificationId}`,
    )
    return res.data
}

// 읽은 알림 모두 삭제
export async function deleteMyReadNotifications() {
    const userId = await getCurrentUserId()

    const res = await apiClient.delete(`/users/${userId}/notifications/read-all`)
    return res.data
}

/* ===================== 푸시 설정 on/off ===================== */

// 현재 내 push 설정 조회
export async function fetchMyPushSetting() {
    const res = await apiClient.get('/notifications/settings/push')
    return res.data.data
}

// 내 push 설정 변경
export async function updateMyPushSetting(notificationStatus) {
    const userId = await getCurrentUserId()
    const pushEnabled = notificationStatus === 'on'

    const res = await apiClient.patch('/notifications/settings/push', {
        memberId: userId,
        pushEnabled,
    })

    return res.data
}
