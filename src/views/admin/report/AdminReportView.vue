<template>
  <div class="admin-container">
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

        <!-- 상태 필터 (PENDING만) -->
        <select v-model="filterStatus">
          <option value="PENDING">대기중</option>
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
</template>


<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getReportSummary, getReportList } from "@/api/adminReportApi";

const router = useRouter();

//  요약 데이터
const summary = ref({
  totalCount: 0,
  completedCount: 0,
  pendingCount: 0,
});

//  신고 목록
const reportList = ref([]);

// 필터 → 기본값 PENDING
const filterStatus = ref("PENDING");

// 페이지네이션
const currentPage = ref(1);
const pageSize = 7;

// 데이터 로딩
onMounted(async () => {
  try {
    const summaryRes = await getReportSummary();
    const listRes = await getReportList();
    summary.value = summaryRes.data;
    reportList.value = listRes.data;
  } catch (e) {
    console.error("❌ 관리자 신고 조회 실패:", e);
  }
});




// 필터 + 페이지네이션 적용 목록
const paginatedList = computed(() => {
  let list = reportList.value;

  if (filterStatus.value) {
    list = list.filter((r) => r.status === filterStatus.value);
  }

  const start = (currentPage.value - 1) * pageSize;
  return list.slice(start, start + pageSize);
});

// 총 페이지 수
const totalPages = computed(() => {
  let list = reportList.value;

  if (filterStatus.value) {
    list = list.filter((r) => r.status === filterStatus.value);
  }

  return Math.ceil(list.length / pageSize);
});

// 페이지 이동
const movePage = (page) => {
  currentPage.value = page;
};

// 상세 이동
const goDetail = (reportId) => {
  if (!reportId) {
    alert("reportId 없음 — 백엔드 응답 확인 필요");
    return;
  }
  router.push(`/admin/reports/${reportId}`);
};

// 상태 한글 변환
const statusText = (status) => {
  if (status === "PENDING") return "대기중";
  if (status === "VALID") return "처리됨(정지)";
  if (status === "REJECTED") return "처리됨(반려)";
};

// 상태 색상
const statusClass = (status) => {
  if (status === "VALID") return "green-text";
  if (status === "REJECTED") return "red-text";
  return "gray-text";
};

// 날짜 포맷
const formatDate = (date) => {
  return date?.replace("T", " ").substring(0, 10);
};
</script>


<style scoped>
.admin-container {
  padding: 40px;
  background: #fff7e6;
  min-height: 100vh;
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

.green-text {
  color: green;
}
.red-text {
  color: red;
}
.gray-text {
  color: gray;
}

.pagination {
  margin-top: 15px;
  display: flex;
  justify-content: center;
  gap: 8px;
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
