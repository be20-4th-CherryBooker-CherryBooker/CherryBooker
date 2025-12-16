// src/api/notificationApi.js
import apiClient from '@/api/httpClient'

/* ===================== 관리자 템플릿 관리 ===================== */

export async function fetchAdminNotificationTemplates({ keyword = '', page = 0, size = 10 } = {}) {
    const res = await apiClient.get('/admin/notifications/templates', {
        params: { keyword: keyword || undefined, page, size },
    })
    return res.data?.data
}

export async function createAdminNotificationTemplate({ templateType = 'SYSTEM', title, body }) {
    const res = await apiClient.post('/admin/notifications/templates', { templateType, title, body })
    return res.data?.data
}

export async function updateAdminNotificationTemplate(templateId, { templateType = 'SYSTEM', title, body }) {
    const res = await apiClient.put(`/admin/notifications/templates/${templateId}`, {
        templateType,
        title,
        body,
    })
    return res.data?.data
}

export async function deleteAdminNotificationTemplate(templateId) {
    const res = await apiClient.delete(`/admin/notifications/templates/${templateId}`)
    return res.data?.data
}

/* ===================== 관리자 발송/발송 로그 ===================== */

export async function sendNotificationByTemplate(templateId, { targetUserId, variables = {} }) {
    const res = await apiClient.post(`/admin/notifications/templates/${templateId}/send`, {
        targetUserId,
        variables,
    })
    return res.data?.data
}

export async function sendNotificationByTemplateToAll(templateId, payload) {
    const res = await apiClient.post(`/admin/notifications/templates/${templateId}/send-all`, payload)
    return res.data?.data
}

export async function fetchAdminNotificationSendLogs({ page = 0, size = 10 } = {}) {
    const res = await apiClient.get('/admin/notifications/send-logs', {
        params: { page, size },
    })
    return res.data?.data
}

/* ===================== 사용자 알림함 ===================== */

export async function fetchMyNotifications({ page = 0, size = 10 } = {}) {
    const res = await apiClient.get('/notifications/me', { params: { page, size } })
    return res.data?.data
}

export async function fetchMyUnreadCount() {
    const res = await apiClient.get('/notifications/me/unread-count')
    return res.data?.data
}

export async function markMyNotificationRead(notificationId) {
    const res = await apiClient.patch(`/notifications/me/${notificationId}/read`)
    return res.data?.data // 보통 null
}

export async function markMyNotificationsReadAll() {
    const res = await apiClient.patch('/notifications/me/read-all')
    return res.data?.data
}

export async function deleteMyNotification(notificationId) {
    const res = await apiClient.delete(`/notifications/me/${notificationId}`)
    return res.data?.data
}

export async function deleteMyReadNotifications() {
    const res = await apiClient.delete('/notifications/me/read')
    return res.data?.data
}
