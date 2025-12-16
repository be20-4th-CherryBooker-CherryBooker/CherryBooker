// src/api/reportApi.js
import api from '@/axios';

// 원클릭 신고 (사용자)
export const reportThread = async ({ reporterId, threadId }) => {
    const res = await api.post('/reports', {
        reporterId,
        threadId,
    });
    return res.data;
};
