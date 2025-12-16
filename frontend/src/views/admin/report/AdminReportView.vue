<template>
  <div class="admin-container">
    <!-- ✅ 내부 폭 제한 + 가운데정렬 래퍼 추가 -->
    <div class="admin-inner">
      <h2 class="title">🚨 신고 관리</h2>

      <!-- 상단 통계 -->
      <div class="summary-box">
        <div class="summary-card">
          <p>전체 신고 수</p>
          <span class="blue">{{ summary.totalCount }}</span>
        </div>

        <div class="summary-card">
          <p>처리 완료 신고 수</p>
          <span class="green">{{ summary.completedCount }}</span>
        </div>

        <div class="summary-card">
          <p>미처리 신고 수</p>
          <span class="red">{{ summary.pendingCount }}</span>
        </div>
      </div>

      <!-- 테이블 -->
      <div class="table-box">
        <div class="table-header">
          <span></span>

          <!-- 상태 필터 -->
          <select v-model="filterStatus">
            <option value="PENDING">대기중</option>
            <option value="REJECTED">반려</option>
            <option value="VALID">승인</option>
          </select>
        </div>

        <table class="report-table">
          <thead>
          <tr>
            <th>번호</th>
            <th>신고 글귀</th>
            <th>등록 일자</th>
            <th>처리 상태</th>
          </tr>
          </thead>

          <tbody>
          <tr
              v-for="(report, index) in paginatedList"
              :key="index"
              @click="goDetail(report.reportId)"
              class="click-row"
          >
            <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
            <td class="ellipsis">{{ report.quoteContent }}</td>
            <td>{{ formatDate(report.createdAt) }}</td>
            <td :class="statusClass(report.status)">
              {{ statusText(report.status) }}
            </td>
          </tr>
          </tbody>
        </table>

        <!-- 페이지네이션 -->
        <div class="pagination">
          <button
              v-for="page in totalPages"
              :key="page"
              @click="movePage(page)"
              :class="{ active: currentPage === page }"
          >
            {{ page }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { getReportSummary, getReportList } from "@/api/adminReportApi";
import { ref, computed, onMounted, watch } from "vue";

const router = useRouter();

// --------------------
// 상태
// --------------------
const summary = ref({
  totalCount: 0,
  completedCount: 0,
  pendingCount: 0,
});

const reportList = ref([]);
const filterStatus = ref("PENDING");

const currentPage = ref(1);
const pageSize = 7;

// --------------------
// 데이터 조회 함수
// --------------------
const fetchReports = async () => {
  try {
    const res = await getReportList(filterStatus.value);
    reportList.value = res.data;
  } catch (e) {
    console.error("❌ 신고 목록 조회 실패:", e);
  }
};

// --------------------
// 최초 로딩
// --------------------
onMounted(async () => {
  try {
    const summaryRes = await getReportSummary();
    summary.value = summaryRes.data;
    await fetchReports();
  } catch (e) {
    console.error("❌ 관리자 신고 조회 실패:", e);
  }
});

// --------------------
// 상태 변경 감지
// --------------------
watch(filterStatus, async () => {
  currentPage.value = 1;
  await fetchReports();
});

// --------------------
// 페이지네이션
// --------------------
const paginatedList = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return reportList.value.slice(start, start + pageSize);
});

const totalPages = computed(() => {
  return Math.ceil(reportList.value.length / pageSize);
});

// --------------------
// 액션
// --------------------
const movePage = (page) => {
  currentPage.value = page;
};

const goDetail = (reportId) => {
  if (!reportId) {
    alert("reportId 없음 — 백엔드 응답 확인 필요");
    return;
  }
  router.push(`/admin/reports/${reportId}`);
};

// --------------------
// 유틸
// --------------------
const statusText = (status) => {
  if (status === "PENDING") return "대기중";
  if (status === "VALID") return "처리됨(정지)";
  if (status === "REJECTED") return "처리됨(반려)";
};

const statusClass = (status) => {
  if (status === "VALID") return "green-text";
  if (status === "REJECTED") return "red-text";
  return "gray-text";
};

const formatDate = (date) => {
  return date?.replace("T", " ").substring(0, 10);
};
</script>

<style scoped>
.admin-container {
  background: #fff7e6;
  min-height: 100vh;
  padding: 48px 0;

  display: flex;
  justify-content: center;
}

.admin-inner {
  width: 100%;
  max-width: 980px;
  padding: 0 24px;
}

.title {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 30px;
}

.summary-box {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.summary-card {
  background: white;
  border-radius: 16px;
  padding: 20px 30px;
  width: 200px;
  text-align: center;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.summary-card span {
  font-size: 22px;
  font-weight: bold;
}

.blue {
  color: dodgerblue;
}
.green {
  color: green;
}
.red {
  color: red;
}

.table-box {
  background: white;
  border-radius: 24px;
  padding: 25px;
  border: 2px solid #f1b76a;
  width: 100%;
}

.table-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.report-table {
  width: 100%;
  border-collapse: collapse;
}

.report-table th {
  background: #fff6e5;
}

.report-table th,
.report-table td {
  border-bottom: 1px solid #eee;
  padding: 12px;
  text-align: center;
  font-size: 14px;
}

.click-row {
  cursor: pointer;
}
.click-row:hover {
  background: #fff1d6;
}

.ellipsis {
  max-width: 380px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination {
  margin-top: 15px;
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination button {
  border: none;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
  background: #f3f3f3;
}

.pagination .active {
  background: #ff7a7a;
  color: white;
}
</style>
