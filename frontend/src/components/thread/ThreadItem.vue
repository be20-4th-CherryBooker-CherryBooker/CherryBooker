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
        <div class="meta">
          <span class="user">{{ thread.userNickname }}</span>
          <span class="date">{{ formatDate(thread.createdAt) }}</span>
        </div>

        <!-- 🚨 신고 버튼 -->
        <button
            v-if="!isOwner"
            class="report-btn icon"
            :disabled="reported"
            @click.stop="onReport"
            :title="reported ? '이미 신고됨' : '부적절한 글 신고'"
        >
          {{ reported ? '✓' : '🚨' }}
        </button>
      </div>

      <div class="bubble-body">
        {{ thread.deleted ? "이 글귀는 삭제되었습니다." : thread.quoteContent }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/AuthStore";
import api from "@/axios";

const props = defineProps({
  thread: {
    type: Object,
    required: true,
  },
});

const router = useRouter();
const authStore = useAuthStore();

const reported = ref(false);

// 작성자 본인 여부
const isOwner = computed(() => {
  if (!authStore.user) return false;
  return Number(authStore.user.userId) === Number(props.thread.userId);
});

// 전체 행 클릭 → 상세 페이지
const goDetail = () => {
  router.push({
    name: "threadDetail",
    params: { threadId: props.thread.threadId },
  });
};

// 날짜 포맷
const formatDate = (dateTime) => {
  if (!dateTime) return "";
  return dateTime.replace("T", " ").slice(0, 16);
};

// 🚨 신고
const onReport = async () => {
  if (!authStore.isAuthenticated) {
    alert("로그인 후 신고할 수 있습니다.");
    router.push({ name: "login" });
    return;
  }

  if (reported.value) return;

  const ok = confirm("이 게시물을 신고하시겠습니까?");
  if (!ok) return;

  try {
    await api.post("/reports", {
      reporterId: authStore.user.userId,
      threadId: props.thread.threadId,
    });

    reported.value = true;
    alert("신고가 접수되었습니다.");
  } catch (e) {
    const status = e.response?.status;
    if (status === 400 || status === 409) {
      alert("이미 신고한 게시글입니다.");
      reported.value = true;
    } else {
      alert("신고 처리 중 오류가 발생했습니다.");
      console.error(e);
    }
  }
};
</script>

<style scoped>
/* ===== 전체 한 줄 ===== */
.thread-row {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 18px;
  margin-bottom: 26px;
  cursor: pointer;
}

/* ===== 프로필 ===== */
.avatar-wrap {
  width: 90px;
  display: flex;
  justify-content: center;
  margin-top: 6px;
}

.avatar-circle {
  width: 66px;
  height: 66px;
  border-radius: 50%;
  border: 4px solid #222;
  background: #fffaf0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.avatar-icon {
  width: 34px;
  height: 34px;
}

/* ===== 말풍선 ===== */
.bubble {
  position: relative;
  width: 720px;
  max-width: 70vw;
  background: #fffdf5;
  border-radius: 18px;
  overflow: hidden;
  border: 1.5px solid #f7d37a;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.bubble::before {
  content: "";
  position: absolute;
  left: -18px;
  top: 30px;
  border-width: 10px 18px 10px 0;
  border-style: solid;
  border-color: transparent #f7d37a transparent transparent;
}

/* 헤더 */
.bubble-header {
  background: #f7d37a;
  padding: 10px 20px 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.meta {
  display: flex;
  gap: 10px;
}

.user {
  font-weight: 600;
}

.date {
  font-size: 11px;
  color: #666;
}

/* 본문 */
.bubble-body {
  padding: 18px 24px 20px;
  font-weight: 600;
  font-size: 17px;
  line-height: 1.7;
  color: #333;
}

/* 🚨 신고 버튼 */
.report-btn {
  border: none;
  border-radius: 12px;
  cursor: pointer;
  background: #ffd6d6;
  color: #b30000;
}

.report-btn.icon {
  font-size: 14px;
  padding: 6px 8px;
}

.report-btn:hover {
  background: #ffb3b3;
}

.report-btn:disabled {
  background: #e0e0e0;
  color: #888;
  cursor: not-allowed;
}
</style>
