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

    <!-- 페이징 영역 -->
    <div class="pagination" v-if="pagination.totalPages > 1">
      <!-- 맨 처음 (<<) -->
      <button
          class="page-nav-btn"
          :disabled="pagination.currentPage === 0"
          @click="changePage(0)"
      >
        &laquo;
      </button>

      <!-- 이전 (<) -->
      <button
          class="page-nav-btn"
          :disabled="pagination.currentPage === 0"
          @click="changePage(pagination.currentPage - 1)"
      >
        &lt;
      </button>

      <!-- 페이지 숫자들 -->
      <button
          v-for="page in pageNumbers"
          :key="page"
          class="page-number"
          :class="{ active: page === pagination.currentPage }"
          @click="changePage(page)"
      >
        {{ page + 1 }}
      </button>

      <!-- 다음 (>) -->
      <button
          class="page-nav-btn"
          :disabled="pagination.currentPage >= pagination.totalPages - 1"
          @click="changePage(pagination.currentPage + 1)"
      >
        &gt;
      </button>

      <!-- 맨 끝 (>>) -->
      <button
          class="page-nav-btn"
          :disabled="pagination.currentPage >= pagination.totalPages - 1"
          @click="changePage(pagination.totalPages - 1)"
      >
        &raquo;
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
import { ref, onMounted, computed } from 'vue'
import ThreadItem from '@/components/thread/ThreadItem.vue'
import ThreadCreateModal from '@/components/thread/ThreadCreateModal.vue'
import { fetchThreads } from '@/api/threadApi'
import { useAuthStore } from '@/stores/AuthStore'
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

/**
 * 가운데 숫자 버튼들 (1 2 3 4 5 ...)
 * - 최대 5개까지만 보여주고, 현재 페이지 기준으로 앞뒤를 잘라서 노출
 */
const pageNumbers = computed(() => {
  const total = pagination.value.totalPages || 0
  const current = pagination.value.currentPage || 0
  const maxToShow = 5

  if (total <= maxToShow) {
    return Array.from({ length: total }, (_, i) => i)
  }

  let start = current - Math.floor(maxToShow / 2)
  let end = current + Math.floor(maxToShow / 2)

  if (start < 0) {
    start = 0
    end = maxToShow - 1
  }
  if (end > total - 1) {
    end = total - 1
    start = total - maxToShow
  }

  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

const openCreateModal = () => {
  // 로그인 안 한 경우 로그인 페이지로 유도
  if (!authStore.isAuthenticated) {
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

/* ===== 페이징 영역 ===== */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

/* <<, <, >, >> 버튼 */
.page-nav-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 10px;
  border-radius: 16px;
  border: none;
  background: #ffe3b5;
  color: #444;
  font-size: 14px;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.06);
}

/* 번호 버튼 */
.page-number {
  width: 40px;
  height: 40px;
  border-radius: 14px;
  border: none;
  background: #f6f6f6;
  color: #333;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.08);
}

/* 현재 페이지 (강조 색) */
.page-number.active {
  background: #ff8f8f;   /* 필요하면 프로젝트 색상에 맞게 조정 */
  color: #fff;
}

/* disabled 상태 */
.page-nav-btn:disabled,
.page-number:disabled {
  opacity: 0.4;
  cursor: default;
  box-shadow: none;
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
