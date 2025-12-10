import axios from "axios";

const API_BASE = "http://localhost:8080/admin/reports";

// ✅ 신고 요약 조회
export const getReportSummary = async () => {
    const res = await axios.get(`${API_BASE}/summary`);
    return res.data;
};

// ✅ 신고 목록 조회
export const getReportList = async () => {
    const res = await axios.get(`${API_BASE}`);
    return res.data;
};

// ✅ 신고 상세 조회
export const getReportDetail = async (reportId) => {
    const res = await axios.get(`${API_BASE}/${reportId}`);
    return res.data;
};

// ✅ 신고 처리
export const processReport = async (payload) => {
    const res = await axios.post(`${API_BASE}/process`, payload);
    return res.data;
};
