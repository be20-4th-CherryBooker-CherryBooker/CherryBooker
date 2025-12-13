<template>
  <div class="thread-wrapper">

    <!-- 상단 (작성자 + 날짜) -->
    <div class="thread-header" @click="toggle">

      <!-- 왼쪽: 프로필 + 아이디 + 날짜 -->
      <div class="left-info">
        <div class="profile">
          <img src="/images/user.png" class="avatar" />
          <span class="user">{{ thread.userName }}</span>
          <span class="date">{{ thread.createdAt }}</span>
        </div>
      </div>

      <!-- 오른쪽: + 버튼 -->
      <button class="comment-add-btn" @click.stop="openCommentModal">
        <img src="/images/add.png" class="addComment" />
      </button>

    </div>

    <!-- 본문 -->
    <div class="thread-content" @click="toggle">
      {{ thread.content }}
    </div>

    <!-- 펼침 상태일 때만 코멘트 목록 표시 -->
    <div v-if="isOpen" class="comment-area">

      <div class="comment-list">
        <CommentItem
            v-for="comment in comments"
            :key="comment.commentId"
            :comment="comment"
        />
      </div>

      <p class="comment-empty" v-if="comments.length === 0">
        코멘트가 등록되지 않았습니다.
      </p>

    </div>

  </div>
</template>

<script setup>
import { ref } from "vue";
import CommentItem from "./CommentItem.vue";

const props = defineProps({
  thread: Object
});

// 부모에게 댓글 모달 열기 요청
const emit = defineEmits(["openCommentModal"]);

const isOpen = ref(false);
const comments = ref([]);

// + 버튼 클릭 시 실행
const openCommentModal = () => {
  emit("openCommentModal", props.thread.threadId);
};

// 스레드 펼치기/접기 + 댓글 로딩
const toggle = async () => {
  isOpen.value = !isOpen.value;

  if (isOpen.value && comments.value.length === 0) {
    const res = await fetch(`/comments-${props.thread.threadId}.json`);
    const data = await res.json();
    comments.value = data.content; // 댓글 배열
  }
};
</script>

<style scoped>
/* 전체 카드 */
.thread-wrapper {
  border-radius: 14px;
  overflow: hidden; /* 헤더 + 본문 연결 */
  border: 1.5px solid #f3d48b;
  background: #fff7e0; /* 연한 배경 */
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}

/* 상단 헤더 */
.thread-header {
  background: #f7d37a; /* 진한 노랑 */
  padding: 10px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

/* 프로필 영역 */
.profile {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 1px solid #555;
}

.date {
  font-size: 12px;
  color: #555;
}

/* + 버튼 */
.comment-add-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.addComment {
  width: 20px;
  height: 20px;
}

/* 글 내용 */
.thread-content {
  padding: 16px 18px;
  font-size: 15px;
  line-height: 1.5;
  color: #333;
  background: #fffdf5;
  cursor: pointer;
}

/* 댓글 박스 */
.comment-area {
  background: #ffffff;
  border-top: 1px solid #f3d48b;
  padding: 12px 15px;
}

/* 댓글 리스트 */
.comment-list {
  margin-bottom: 10px;
}

/* 댓글 없음 */
.comment-empty {
  font-size: 13px;
  color: #777;
  margin-bottom: 10px;
}

/* 댓글 입력창 */
.comment-write {
  display: flex;
  gap: 8px;
}

.comment-write input {
  flex: 1;
  border-radius: 8px;
  border: 1px solid #ccc;
  padding: 6px;
}

.comment-write button {
  background: #ffa83d;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
}
</style>
