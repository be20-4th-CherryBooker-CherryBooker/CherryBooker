// src/api/mylibApi.js
import apiClient from '@/api/httpClient.js'

/**
 * 내 서재 책 목록 조회
 * 백엔드: GET /mylib/books
 * 응답: MyLibrarySliceResponse
 */
export async function fetchMyBooks({
                                       keyword = '',
                                       status,           // 'WISH' | 'READING' | 'READ' 중 하나 (옵션)
                                       page = 0,
                                       size = 50,        // 모달이라 한 번에 많이 가져와도 무방
                                   } = {}) {
    const res = await apiClient.get('/mylib/books', {
        params: {
            keyword: keyword || undefined,
            status: status || undefined,
            page,
            size,
        },
    })

    // { success, data: { ...MyLibrarySliceResponse }, ... } 형태라고 가정
    return res.data?.data
}

/**
 * 특정 내 서재 도서의 글귀 목록 조회
 * 백엔드: GET /mylib/books/{myLibId}/quotes
 * 응답: MyBookDetailResponse
 */
export async function fetchMyBookQuotes(myLibId) {
    const res = await apiClient.get(`/mylib/books/${myLibId}/quotes`)
    return res.data?.data
}
