<!-- src/views/thread/ThreadDetailView.vue -->
<template>
  <div class="detail-container" v-if="detail">
    <!-- 전체 스레드 흐름 (루트 + 릴레이) -->
    <div class="thread-flow">
      <div
          v-for="(message, idx) in messages"
          :key="message.isRoot ? message.id : message.replyId"
          :class="['thread-row', idx % 2 === 0 ? 'left' : 'right']"
      >
        <!-- 프로필 영역 -->
        <div class="avatar-wrap">
          <div class="avatar-circle">
            <img src="/images/user.png" alt="user" class="avatar-icon" />
          </div>
        </div>

        <!-- 말풍선 카드 -->
        <div class="bubble" :class="{ root: message.isRoot }">
          <div class="bubble-header">
            <div class="meta">
              <!-- 날짜 먼저, 그 다음 사용자 -->
              <span class="date">{{ formatDate(message.createdAt) }}</span>
              <span class="user">{{ message.userNickname }}</span>
              <span v-if="message.updated" class="badge">수정됨</span>
              <span v-if="message.deleted" class="badge deleted">삭제됨</span>
            </div>

            <!-- 내 글이고, 아직 삭제되지 않은 경우에만 수정/삭제 노출 -->
            <div v-if="isOwner(message) && !message.deleted" class="actions">
              <button @click.stop="onDelete(message)">삭제</button>
              <button @click.stop="openEditModal(message)">수정</button>
            </div>
          </div>

          <div class="bubble-body">
            {{ message.deleted ? "이 글귀는 삭제되었습니다." : message.quoteContent }}
          </div>
        </div>
      </div>
    </div>

    <!-- 아래 오른쪽에 + 버튼 (답변 등록) -->
    <button class="reply-create-btn" @click="openReplyModal">
      답변 남기기
    </button>

    <!-- 답변 등록 모달 (CMT-007) -->
    <ThreadCreateModal
        v-if="replyModalOpen"
        mode="reply"
        :thread-id="detail.threadId"
        @close="replyModalOpen = false"
        @created="() => { replyModalOpen = false; loadDetail(); }"
    />

    <!-- 스레드/답변 수정 모달 (CMT-002, CMT-008) -->
    <!-- editContext: 'root' | 'reply' -->
    <ThreadCreateModal
        v-if="editModalOpen"
        :mode="editContext"
        :thread-id="detail.threadId"
        :is-edit="true"
        :target-id="editTargetId"
        @close="editModalOpen = false"
        @created="() => { editModalOpen = false; loadDetail(); }"
    />

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { fetchThreadDetail, deleteThread, deleteReply } from '@/api/threadApi'
import ThreadCreateModal from '@/components/thread/ThreadCreateModal.vue'

const route = useRoute()
const authStore = useAuthStore()

// 새로고침 이후 복구
if (!authStore.accessToken) {
  authStore.loadFromStorage()
}

const detail = ref(null)

const replyModalOpen = ref(false)
const editModalOpen = ref(false)
const editContext = ref('root')     // 'root' | 'reply'
const editTargetId = ref(null)      // 수정 대상 ID

// 내 userId
const myUserId = computed(() => {
  const raw = authStore.user?.userId
  return raw != null ? Number(raw) : null
})

// 상세 불러오기
const loadDetail = async () => {
  try {
    const id = route.params.threadId
    const body = await fetchThreadDetail(id)
    detail.value = body

    console.log(
        '[ThreadDetail] myUserId =',
        myUserId.value,
        'thread.userId =',
        body.userId
    )
  } catch (e) {
    console.error('스레드 상세 조회 실패', e)
  }
}

const formatDate = (dateTime) => {
  if (!dateTime) return ''
  return dateTime.replace('T', ' ').slice(0, 16)
}

// 루트 + 릴레이 합치기
const messages = computed(() => {
  if (!detail.value) return []

  const root = {
    id: detail.value.threadId,
    userId: detail.value.userId,
    userNickname: detail.value.userNickname,
    createdAt: detail.value.createdAt,
    updated: detail.value.updated,
    deleted: detail.value.deleted,
    quoteContent: detail.value.quoteContent,
    isRoot: true,
  }

  const replies = (detail.value.replies || []).map((r) => ({
    ...r,
    isRoot: false,
  }))

  return [root, ...replies]
})

// 내가 쓴 글인지
const isOwner = (message) => {
  console.log(
      '[isOwner]',
      'myUserId =', myUserId.value,
      'message.userId =', message.userId
  )
  if (myUserId.value == null) return false
  return Number(message.userId) === myUserId.value
}

// 답변 등록 모달 열기
const openReplyModal = () => {
  replyModalOpen.value = true
}

// 수정 모달 열기
const openEditModal = (message) => {
  editContext.value = message.isRoot ? 'root' : 'reply'
  editTargetId.value = message.isRoot ? message.id : message.replyId
  editModalOpen.value = true
}

// 삭제
const onDelete = async (message) => {
  if (!confirm('정말 삭제하시겠습니까?')) return

  try {
    if (message.isRoot) {
      await deleteThread(message.id)
    } else {
      await deleteReply(message.replyId)
    }
    await loadDetail()
  } catch (e) {
    console.error('삭제 실패', e)
    alert('삭제에 실패했습니다.')
  }
}

onMounted(loadDetail)
</script>


<style scoped>
.detail-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 30px 20px;
  font-family: "Pretendard", sans-serif;
}

/* ===== 전체 스레드 흐름 ===== */
.thread-flow {
  display: flex;
  flex-direction: column;
  gap: 26px;
}

/* ===== 한 줄(프로필 + 말풍선) ===== */
.thread-row {
  display: flex;
  align-items: flex-start;
  gap: 18px;
}

/* 왼쪽 말풍선 */
.thread-row.left {
  justify-content: flex-start;
}

/* 오른쪽 말풍선 */
.thread-row.right {
  justify-content: flex-end;
  flex-direction: row-reverse;
}

/* ===== 프로필 쪽 ===== */
.avatar-wrap {
  width: 90px; /* 고정 폭 -> 버블 정렬 */
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

/* ===== 말풍선 카드 ===== */
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

/* 말풍선 꼬리 - 왼쪽 */
.thread-row.left .bubble::before {
  content: "";
  position: absolute;
  left: -18px;
  top: 30px;
  border-width: 10px 18px 10px 0;
  border-style: solid;
  border-color: transparent #f7d37a transparent transparent;
}

/* 말풍선 꼬리 - 오른쪽 */
.thread-row.right .bubble::before {
  content: "";
  position: absolute;
  right: -18px;
  top: 30px;
  border-width: 10px 0 10px 18px;
  border-style: solid;
  border-color: transparent transparent transparent #f7d37a;
}

/* 상단 노란 바 */
.bubble-header {
  background: #f7d37a;
  padding: 10px 20px 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 13px;
}

.bubble-header .meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 오른쪽 버블일 때 헤더를 오른쪽에 붙이기 */
.thread-row.right .bubble-header {
  justify-content: flex-end;
}

/* 본문 영역 */
.bubble-body {
  padding: 18px 24px 20px;
  font-weight: 600;
  font-size: 17px;
  line-height: 1.7;
  color: #333;
}

/* 오른쪽 버블 본문 텍스트 오른쪽 정렬 */
.thread-row.right .bubble-body {
  text-align: right;
}

/* 유저 / 날짜 텍스트 */
.user {
  font-weight: 600;
}

.date {
  font-size: 11px;
  color: #666;
}

/* 루트/릴레이 수정/삭제 버튼 */
.actions button {
  border: none;
  padding: 6px 10px;
  border-radius: 12px;
  font-size: 12px;
  margin-left: 6px;
  cursor: pointer;
  background: #ffe19a;
}

.actions button:first-child {
  background: #ff9a9a;
}

/* 뱃지 */
.badge {
  display: inline-block;
  margin-left: 4px;
  padding: 2px 6px;
  border-radius: 8px;
  background: #ffebae;
  font-size: 11px;
}

.badge.deleted {
  background: #ffd0d0;
}

/* 답변 등록 버튼 */
.reply-create-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background: #ffa83d;
  color: #fff;
  border: none;
  padding: 12px 22px;
  border-radius: 24px;
  cursor: pointer;
}
</style>
