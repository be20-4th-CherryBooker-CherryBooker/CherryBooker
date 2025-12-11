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

      <textarea
          v-model="adminComment"
          placeholder="관리자가 처리한 신고 내용 관련 코멘트"
          class="admin-textarea"
      ></textarea>

      <div class="btn-box">
        <button @click="process('VALID')" class="btn done">신고 처리 완료</button>
        <button @click="process('REJECTED')" class="btn reject">신고 처리 반려</button>
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

const loadDetail = async () => {
  const res = await getReportDetail(route.params.reportId);
  console.log("🔥 res = ", res);
  console.log("🔥 res.data = ", res.data);
  console.log("🔥 res.data.data = ", res.data?.data);
  detail.value = res.data;
};

const goBack = () => {
  router.push("/admin/reports");
};

const process = async (status) => {
  try {
    await processReport({
      reportId: detail.value.reportId,  // 🔥 핵심
      status: status,                  // VALID or REJECTED
      adminComment: adminComment.value,
    });

    alert("처리 완료되었습니다.");
    router.push("/admin/reports");
  } catch (e) {
    console.error("🚨 신고 처리 오류:", e);
    alert("신고 처리 실패!");
  }
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

/* 상단 제목 + 뒤로가기 버튼 */
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  background: none;
  border: none;
  color: #d94848;
  font-size: 15px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: 0.2s;
}

.back-btn:hover {
  background: #ffeaea;
}

.title {
  font-size: 22px;
  margin-bottom: 25px;
  font-weight: 600;
  color: #444;
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

.info-box {
  width: 45%;
}

.content-box {
  width: 55%;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin: 12px 0;
}

.label {
  font-weight: 500;
  color: #555;
}

.value {
  font-weight: 600;
}

.highlight {
  color: #d35457;
}

.quote {
  margin-top: 15px;
  white-space: pre-line;
  line-height: 1.6;
  font-size: 15px;
  color: #333;
}

.admin-box {
  margin-top: 20px;
}

.admin-textarea {
  width: 100%;
  height: 120px;
  margin-top: 15px;
  padding: 14px;
  border-radius: 12px;
  border: 1px solid #ddd;
  font-size: 14px;
  outline: none;
  resize: none;
}

.admin-textarea:focus {
  border-color: #e88e8e;
  box-shadow: 0 0 4px rgba(255, 150, 150, 0.4);
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
  font-size: 15px;
  cursor: pointer;
  transition: 0.2s;
}

.done {
  background: #ffdddd;
  color: #d94848;
  border: 1px solid #d94848;
}

.done:hover {
  background: #d94848;
  color: #fff;
}

.reject {
  background: white;
  color: #d94848;
  border: 1px solid #d94848;
}

.reject:hover {
  background: #d94848;
  color: #fff;
}
</style>
