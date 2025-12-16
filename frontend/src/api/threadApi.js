// src/api/threadApi.js
import apiClient from '@/api/httpClient'

/**
 * 스레드 목록 조회
 * GET /api/community/threads?page=&size=
 * 응답: ApiResponse<CommunityThreadListResponse>
 *  - data: { threads: [...], pagination: {...} }
 */
export async function fetchThreads({ page = 0, size = 4 } = {}) {
    const res = await apiClient.get('/community/threads', {
        params: { page, size },
    })
    return res.data.data // { threads, pagination }
}

/**
 * 스레드 상세 조회
 * GET /api/community/threads/{threadId}
 * 응답: ApiResponse<CommunityThreadDetailResponse>
 */
export async function fetchThreadDetail(threadId) {
    const res = await apiClient.get(`/community/threads/${threadId}`)
    return res.data.data
}

/**
 * 내 스레드 목록 조회 (루트만)
 * GET /api/community/threads/me?page=&size=
 * 응답: ApiResponse<CommunityThreadListResponse>
 */
export async function fetchMyThreads({ page = 0, size = 4 } = {}) {
    const res = await apiClient.get('/community/threads/me', {
        params: { page, size },
    })
    return res.data.data
}

/**
 * 스레드 생성
 * POST /api/community/threads
 * body 예시: { quoteId: number }
 */
export async function createThread(payload) {
    const res = await apiClient.post('/community/threads', payload)
    return res.data.data
}

/**
 * 스레드 수정
 * PUT 또는 PATCH /api/community/threads/{threadId}
 * (백엔드 설계에 맞춰 method/필드 조정)
 */
export async function updateThread(threadId, payload) {
    const res = await apiClient.put(`/community/threads/${threadId}`, payload)
    return res.data.data
}

/**
 * 스레드 삭제
 * DELETE /api/community/threads/{threadId}
 */
export async function deleteThread(threadId) {
    const res = await apiClient.delete(`/community/threads/${threadId}`)
    return res.data.data
}

/**
 * 스레드에 답변(릴레이) 등록
 * POST /api/community/threads/{threadId}/replies
 * body 예시: { quoteId: number }
 */
export async function createThreadReply(threadId, payload) {
    const res = await apiClient.post(
        `/community/threads/${threadId}/replies`,
        payload,
    )
    return res.data.data
}

/**
 * 스레드 답변 수정/삭제
 */


// 답변 수정
export async function updateReply(replyId, payload) {
    const res = await apiClient.put(`/community/threads/replies/${replyId}`, payload);
    return res.data.data;
}

// 답변 삭제
export async function deleteReply(replyId) {
    await apiClient.delete(`/community/threads/replies/${replyId}`);
}

