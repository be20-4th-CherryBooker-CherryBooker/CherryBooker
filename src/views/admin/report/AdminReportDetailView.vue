<template>
  <div class="detail-container">

    <h2>🚨 신고 내역 상세보기</h2>

    <!-- ✅ 신고자 정보 -->
    <div class="box">
      <h3>신고 상세</h3>
      <p><b>신고 대상 ID</b> {{ detail.reportedUserId }}</p>
      <p><b>닉네임</b> {{ detail.targetNickname }}</p>
      <p><b>신고 횟수</b> {{ detail.reportCount }}</p>
      <p><b>삭제 횟수</b> {{ detail.deleteCount }}</p>
    </div>

    <!-- ✅ 신고 글 -->
    <div class="box">
      <h3>신고 글귀</h3>
      <p>{{ detail.quoteContent }}</p>
    </div>

    <!-- ✅ 관리자 처리 -->
    <div class="box">
      <h3>관리자 처리</h3>

      <textarea v-model="adminComment" placeholder="처리 사유 입력"></textarea>

      <div class="btn-box">
        <button @click="process('VALID')" class="approve">신고 처리 완료</button>
        <button @click="process('REJECTED')" class="reject">신고 처리 반려</button>
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
  detail.value = res.data;
};

const process = async (status) => {
  await processReport({
    threadId: detail.value.threadId,
    threadReplyId: detail.value.threadReplyId,
    status: status,
    adminComment: adminComment.value
  });

  alert("처리 완료");
  router.push("/admin/reports");
};

onMounted(loadDetail);
</script>
<style scoped>
.detail-container {
  max-width: 900px;
  margin: auto;
}

.box {
  background: #fff;
  padding: 20px;
  border-radius: 16px;
  margin-bottom: 20px;
}

textarea {
  width: 100%;
  height: 100px;
  padding: 10px;
}

.btn-box {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.approve {
  background: red;
  color: white;
}

.reject {
  border: 1px solid red;
  color: red;
}
</style>
