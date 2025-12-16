<template>
  <!-- detail 이 로딩되기 전엔 화면을 렌더링하지 않음 -->
  <div v-if="detail && detail.reportId" class="detail-container">

    <!-- 🔙 제목 + 뒤로가기 버튼 -->
    <div class="header-row">
      <h2 class="title">🚫 신고 내역 상세보기</h2>

      <button class="back-btn" @click="goBack">
        ← 뒤로가기
      </button>
    </div>

    <div class="layout-row">
      <!-- 신고 상세 -->
      <div class="box info-box">
        <h3>신고 상세</h3>

        <div class="info-item">
          <span class="label">신고 당한 ID</span>
          <span class="value highlight">
            {{ detail.reportedUserId }} ({{ detail.targetNickname }})
          </span>
        </div>

        <div class="info-item">
          <span class="label">신고 횟수</span>
          <span class="value">{{ detail.reportCount }}</span>
        </div>

        <div class="info-item">
          <span class="label">삭제 횟수</span>
          <span class="value">{{ detail.deleteCount }}</span>
        </div>

        <!-- ⭐ 중요 안내 -->
        <div class="info-item notice">
          <span class="label">처리 범위</span>
          <span class="value highlight">
            동일 게시물의 모든 신고에 동일하게 적용됨
          </span>
        </div>
      </div>

      <!-- 신고 글귀 -->
      <div class="box content-box">
        <h3>신고 글귀</h3>
        <p class="quote">{{ detail.quoteContent }}</p>
      </div>
    </div>

    <!-- 관리자 처리 -->
    <div class="box admin-box">
      <h3>관리자 처리</h3>

      <!-- 이미 처리된 신고 -->
      <div v-if="detail.status !== 'PENDING'" class="already-processed">
        <p>이미 처리된 신고입니다.</p>
        <p class="processed-status">
          처리 상태: {{ statusText(detail.status) }}
        </p>
        <p v-if="detail.adminComment" class="admin-comment">
          관리자 코멘트: {{ detail.adminComment }}
        </p>
      </div>

      <!-- 처리 가능 상태 -->
      <div v-else>
        <textarea
            v-model="adminComment"
            placeholder="관리자가 처리한 신고 내용 관련 코멘트"
            class="admin-textarea"
        ></textarea>

        <p class="warning-text">
          게시물에 대한 다수 신고에 동일 적용.
        </p>

        <div class="btn-box">
          <button
              class="btn done"
              :disabled="processing"
              @click="process('VALID')"
          >
            신고 처리 완료
          </button>
          <button
              class="btn reject"
              :disabled="processing"
              @click="process('REJECTED')"
          >
            신고 처리 반려
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getReportDetail, processReport } from "@/api/adminReportApi";

const route = useRoute();
const router = useRouter();

const detail = ref({});
const adminComment = ref("");
const processing = ref(false);

const loadDetail = async () => {
  const res = await getReportDetail(route.params.reportId);
  detail.value = res.data;
};

const goBack = () => {
  router.push("/admin/reports");
};

const process = async (status) => {
  if (processing.value) return;

  if (!adminComment.value.trim()) {
    alert("관리자 코멘트를 입력해주세요.");
    return;
  }

  processing.value = true;

  try {
    await processReport({
      reportId: detail.value.reportId,
      status,
      adminComment: adminComment.value,
    });

    alert("처리 완료되었습니다.");
    router.push("/admin/reports");
  } catch (e) {
    console.error("🚨 신고 처리 오류:", e);
    alert("신고 처리 실패!");
  } finally {
    processing.value = false;
  }
};

const statusText = (status) => {
  if (status === "PENDING") return "대기중";
  if (status === "VALID") return "처리됨(승인)";
  if (status === "REJECTED") return "처리됨(반려)";
};

onMounted(loadDetail);
</script>

<style scoped>
.detail-container {
  max-width: 1000px;
  margin: 40px auto;
  padding: 10px;
  font-family: "Pretendard", sans-serif;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  background: none;
  border: none;
  color: #d94848;
  cursor: pointer;
}

.title {
  font-size: 22px;
  margin-bottom: 25px;
}

.layout-row {
  display: flex;
  gap: 25px;
  margin-bottom: 25px;
}

.box {
  background: #fff;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0px 3px 10px rgba(0, 0, 0, 0.05);
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin: 12px 0;
}

.notice {
  margin-top: 20px;
}

.highlight {
  color: #d35457;
  font-weight: 600;
}

.quote {
  white-space: pre-line;
  line-height: 1.6;
}

.admin-textarea {
  width: 100%;
  height: 120px;
  margin-top: 15px;
  padding: 14px;
  border-radius: 12px;
  border: 1px solid #ddd;
}

.warning-text {
  margin-top: 10px;
  font-size: 13px;
  color: #888;
}

.btn-box {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.btn {
  padding: 10px 22px;
  border-radius: 30px;
  cursor: pointer;
}

.done {
  background: #ffdddd;
  color: #d94848;
  border: 1px solid #d94848;
}

.reject {
  background: white;
  color: #d94848;
  border: 1px solid #d94848;
}

.already-processed {
  color: #666;
  font-size: 14px;
}

.processed-status {
  margin-top: 8px;
  font-weight: 600;
}

.admin-comment {
  margin-top: 10px;
  color: #444;
}
</style>
