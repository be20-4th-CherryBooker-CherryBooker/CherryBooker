<!-- src/components/thread/ThreadItem.vue -->
<template>
  <div class="thread-row" @click="goDetail">
    <!-- 왼쪽 : 프로필 아이콘 -->
    <div class="avatar-wrap">
      <div class="avatar-circle">
        <img src="/images/user.png" alt="user" class="avatar-icon" />
      </div>
    </div>

    <!-- 오른쪽 : 말풍선 카드 -->
    <div class="bubble">
      <div class="bubble-header">
        <span class="user">{{ thread.userNickname }}</span>
        <span class="date">{{ formatDate(thread.createdAt) }}</span>
      </div>
      <div class="bubble-body">
        {{ thread.deleted ? "이 글귀는 삭제되었습니다." : thread.quoteContent }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";

const props = defineProps({
  thread: {
    type: Object,
    required: true,
  },
});

const router = useRouter();

// 전체 행 클릭 → 상세 페이지로 이동
const goDetail = () => {
  router.push({
    name: "threadDetail",
    params: { threadId: props.thread.threadId },
  });
};

const formatDate = (dateTime) => {
  if (!dateTime) return "";
  return dateTime.replace("T", " ").slice(0, 16);
};
</script>

<style scoped>
/* ===== 전체 한 줄(프로필 + 말풍선) ===== */
.thread-row {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 18px;
  margin-bottom: 26px;
  cursor: pointer;
}

/* ===== 프로필 쪽 ===== */
.avatar-wrap {
  width: 90px;                   /* 고정 폭 -> 버블들이 정렬됨 */
  display: flex;
  justify-content: center;
  margin-top: 6px;               /* 말풍선과 수직 정렬용 */
}

.avatar-circle {
  width: 66px;
  height: 66px;
  border-radius: 50%;
  border: 4px solid #222;        /* 두꺼운 라인 */
  background: #fffaf0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);  /* 둥근 그림자 */
}

.avatar-icon {
  width: 34px;
  height: 34px;
}

/* ===== 말풍선 카드 ===== */
.bubble {
  position: relative;
  width: 720px;          /* 카드 폭 고정 */
  max-width: 70vw;       /* 화면이 좁으면 줄어들기 */
  background: #fffdf5;
  border-radius: 18px;
  overflow: hidden;
  border: 1.5px solid #f7d37a;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

/* 말풍선 꼬리 (프로필 방향으로 뾰족하게) */
.bubble::before {
  content: "";
  position: absolute;
  left: -18px;
  top: 30px;
  border-width: 10px 18px 10px 0;
  border-style: solid;
  border-color: transparent #f7d37a transparent transparent; /* 헤더 색과 맞추기 */
}

/* 상단 노란 바 */
.bubble-header {
  background: #f7d37a;
  padding: 10px 20px 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}

.user {
  font-weight: 600;
}

.date {
  font-size: 11px;
  color: #666;
}

/* 본문 영역 */
.bubble-body {
  padding: 18px 24px 20px;
  font-weight: 600;
  font-size: 17px;
  line-height: 1.7;
  color: #333;
}
</style>
