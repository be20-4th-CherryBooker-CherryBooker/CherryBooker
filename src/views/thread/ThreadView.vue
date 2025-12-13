<template>
  <div class="threads-container">

    <h2 class="page-title">글귀 스레드</h2>

    <div class="thread-list">
      <ThreadItem
          v-for="thread in threads"
          :key="thread.threadId"
          :thread="thread"
      />
    </div>

    <button class="create-btn" @click="openCreateModal">
      등록하기
    </button>

    <ThreadItem
        v-for="thread in threads"
        :key="thread.threadId"
        :thread="thread"
        @openCommentModal="openCommentModal"
    />

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import ThreadItem from "@/components/thread/ThreadItem.vue";

const threads = ref([]);
const showCommentModal = ref(false);
const targetThreadId = ref(null);

// 댓글 모달 열기
const openCommentModal = (threadId) => {
  targetThreadId.value = threadId;
  showCommentModal.value = true;
  console.log("댓글 모달 오픈!", threadId);
};

const loadThreads = async () => {
  const res = await fetch("/threads.json");
  const data = await res.json();
  threads.value = data.content;
};

const openCreateModal = () => {
  // 글 등록 모달 열기
};

onMounted(() => {
  loadThreads();
});
</script>

<style scoped>
.threads-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 30px 20px;
  font-family: "Pretendard", sans-serif;
  text-align: center;
}

.page-title {
  display: inline-block;
  margin: 20px auto 10px auto;
  padding: 14px 70px;
  border: 2px solid #df3e3e;
  border-radius: 40px;

  font-size: 20px;
  font-weight: 600;
  color: #df3e3e;
  text-align: center;

  background: #ffffff;
  box-shadow: 0 4px 10px rgba(223, 62, 62, 0.15);

  font-family: "Pretendard", sans-serif;
}

.thread-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 30px;
}

.create-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background: #ffa83d;
  border: none;
  padding: 12px 24px;
  border-radius: 24px;
  color: #fff;
  cursor: pointer;
}
</style>
