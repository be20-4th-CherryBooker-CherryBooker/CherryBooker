<!-- src/views/thread/ThreadListView.vue -->
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

    <!-- 간단 페이징 영역 (페이지당 4개) -->
    <div class="pagination" v-if="pagination.totalPages > 1">
      <button
          class="page-btn"
          :disabled="pagination.currentPage === 0"
          @click="changePage(pagination.currentPage - 1)"
      >
        이전
      </button>

      <span class="page-info">
        {{ pagination.currentPage + 1 }} / {{ pagination.totalPages }}
      </span>

      <button
          class="page-btn"
          :disabled="pagination.currentPage >= pagination.totalPages - 1"
          @click="changePage(pagination.currentPage + 1)"
      >
        다음
      </button>
    </div>

    <button class="create-btn" @click="openCreateModal">
      등록하기
    </button>

    <!-- 스레드 등록 모달 -->
    <ThreadCreateModal
        v-if="createModalOpen"
        @close="closeCreateModal"
        @created="onThreadCreated"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import ThreadItem from '@/components/thread/ThreadItem.vue'
import ThreadCreateModal from '@/components/thread/ThreadCreateModal.vue'
import { fetchThreads } from '@/api/threadApi'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'

const threads = ref([])
const pagination = ref({
  currentPage: 0,
  totalPages: 0,
  totalItems: 0,
})

const pageSize = 4
const createModalOpen = ref(false)

const router = useRouter()
const authStore = useAuthStore()

const loadThreads = async (page = 0) => {
  try {
    const body = await fetchThreads({ page, size: pageSize })
    threads.value = body?.threads ?? []
    pagination.value =
        body?.pagination ?? { currentPage: 0, totalPages: 0, totalItems: 0 }
  } catch (e) {
    console.error('스레드 목록 조회 실패', e)
  }
}

const changePage = (page) => {
  if (page < 0 || page >= pagination.value.totalPages) return
  loadThreads(page)
}

const openCreateModal = () => {
  // 로그인 안 한 경우 로그인 페이지로 유도
  if (!authStore.isLoggedIn) {
    alert('로그인 후 이용 가능합니다.')
    router.push({ name: 'login' })
    return
  }
  createModalOpen.value = true
}

const closeCreateModal = () => {
  createModalOpen.value = false
}

// 모달에서 스레드가 성공적으로 생성된 경우
const onThreadCreated = () => {
  // 첫 페이지를 다시 불러오거나, 현재 페이지를 새로고침
  loadThreads(0)
}

onMounted(() => loadThreads(0))
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

  background: #ffffff;
  box-shadow: 0 4px 10px rgba(223, 62, 62, 0.15);
}

.thread-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-top: 30px;
  align-items: center; /* 카드들을 가운데 정렬 */
}

/* 페이징 영역 최소 스타일만 추가 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

.page-btn {
  background: #ffa83d;
  border: none;
  padding: 6px 12px;
  border-radius: 18px;
  color: #fff;
  cursor: pointer;
  font-size: 13px;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: default;
}

.page-info {
  font-size: 13px;
  color: #555;
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
