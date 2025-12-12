import api from '@/axios';

const API_BASE = '/admin/reports';

// 신고 요약 조회
export const getReportSummary = async () => {
    const res = await api.get(`${API_BASE}/summary`);
    return res.data;
};

// 신고 목록 조회 (status별)
export const getReportList = async (status) => {
    const res = await api.get(API_BASE, {
        params: { status }
    });
    return res.data;
};


// 신고 상세 조회
export const getReportDetail = async (reportId) => {
    const res = await api.get(`${API_BASE}/${reportId}`);
    return res.data;
};

// 신고 처리
export const processReport = async (payload) => {
    const res = await api.post(`${API_BASE}/process`, payload);
    return res.data;
};
