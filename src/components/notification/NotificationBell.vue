<!-- src/components/notification/NotificationBell.vue -->
<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/AuthStore'
import {
  markMyNotificationRead,
  markMyNotificationsReadAll,
  deleteMyNotification,
  deleteMyReadNotifications,
} from '@/api/notificationApi.js'
import { useNotificationStore } from '@/stores/notificationStore.js'
import { icons } from '@/assets/icon/icons.js' // 알림 아이콘 모음

const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const isNotificationOpen = ref(false)
const hasLoaded = ref(false)
const activeTab = ref('unread')

// 아이콘 hover 상태
const isHover = ref(false)
const currentIcon = computed(() =>
    isHover.value
        ? icons.notification.hover
        : icons.notification.default,
)

// store.notifications -> UI용
const uiNotifications = computed(() =>
    notificationStore.notifications.map((n) => ({
      id: n.id,
      title: n.title,
      body: n.body,
      read: n.read,
      createdAt: n.createdAt,
    })),
)

// 탭별 필터
const filteredNotifications = computed(() => {
  const list = uiNotifications.value
  if (activeTab.value === 'unread') {
    return list.filter((n) => !n.read)
  }
  return list.filter((n) => n.read)
})

// 뱃지 숫자
const unreadCount = computed(() => notificationStore.unreadCount)

// 최초 한 번 미리 로딩
onMounted(async () => {
  if (authStore.isAuthenticated && !notificationStore.notifications.length) {
    await Promise.all([
      notificationStore.loadNotifications(0),
      notificationStore.fetchUnreadCount(),
    ])
  }
})

const setActiveTab = (tab) => {
  activeTab.value = tab
}

const toggleNotification = async () => {
  if (!authStore.isAuthenticated) {
    // 필요하면 alert / 로그인 페이지 이동 추가 가능
    return
  }

  const next = !isNotificationOpen.value
  isNotificationOpen.value = next

  // 처음 열릴 때만 서버에서 한 번 더 로드
  if (next && !hasLoaded.value) {
    await Promise.all([
      notificationStore.loadNotifications(0),
      notificationStore.fetchUnreadCount(),
    ])
    hasLoaded.value = true
  }
}

const closeNotification = () => {
  isNotificationOpen.value = false
}

const reloadAfterChange = async () => {
  await Promise.all([
    notificationStore.loadNotifications(0),
    notificationStore.fetchUnreadCount(),
  ])
}

// 단건 읽음
const markAsRead = async (id) => {
  try {
    await markMyNotificationRead(id)
    await reloadAfterChange()
  } catch (err) {
    console.error('[NotificationBell] 알림 읽음 처리 실패:', err)
  }
}

// 모두 읽음
const markAllRead = async () => {
  try {
    await markMyNotificationsReadAll()
    await reloadAfterChange()
  } catch (err) {
    console.error('[NotificationBell] 전체 읽음 처리 실패:', err)
  }
}

// 단건 삭제
const removeNotification = async (id) => {
  try {
    await deleteMyNotification(id)
    await reloadAfterChange()
  } catch (err) {
    console.error('[NotificationBell] 알림 삭제 실패:', err)
  }
}

// 읽은 알림 전체 삭제
const removeAllRead = async () => {
  try {
    await deleteMyReadNotifications()
    await reloadAfterChange()
  } catch (err) {
    console.error('[NotificationBell] 읽은 알림 전체 삭제 실패:', err)
  }
}
</script>

<template>
  <el-popover
      placement="bottom-end"
      v-model:visible="isNotificationOpen"
      popper-class="notification-popper"
      width="520"
      @hide="closeNotification"
  >
    <template #reference>
      <el-badge
          :value="unreadCount"
          :hidden="unreadCount === 0"
          class="notification-badge"
          type="danger"
      >
        <!-- 박스 래퍼 + SVG -->
        <div
            class="alert-wrapper"
            @click.stop="toggleNotification"
            @mouseenter="isHover = true"
            @mouseleave="isHover = false"
        >
          <img
              :src="currentIcon"
              alt="알림"
              class="alert-icon"
          />
        </div>
      </el-badge>
    </template>

    <!-- 알림 패널 -->
    <div class="notification-panel">
      <!-- 상단 탭 + 전체 버튼 -->
      <div class="panel-header">
        <div class="tab-group">
          <button
              type="button"
              class="tab-button"
              :class="{ active: activeTab === 'unread' }"
              @click="setActiveTab('unread')"
          >
            읽지 않음
          </button>
          <button
              type="button"
              class="tab-button"
              :class="{ active: activeTab === 'read' }"
              @click="setActiveTab('read')"
          >
            읽음
          </button>
        </div>

        <button
            v-if="activeTab === 'unread'"
            type="button"
            class="header-action header-action--orange"
            @click="markAllRead"
        >
          모두 읽음
        </button>
        <button
            v-else
            type="button"
            class="header-action header-action--pink"
            @click="removeAllRead"
        >
          모두 삭제
        </button>
      </div>

      <!-- 리스트 영역 -->
      <div class="list-wrapper">
        <div class="list-head">
          <div class="col col-no">번호</div>
          <div class="col col-title">제목</div>
          <div class="col col-body">내용</div>
          <div class="col col-action">관리</div>
        </div>

        <!-- 알림 목록 -->
        <div v-if="filteredNotifications.length" class="list-body">
          <div
              v-for="(item, idx) in filteredNotifications"
              :key="item.id"
              class="list-row"
          >
            <div class="col col-no">{{ idx + 1 }}</div>
            <div class="col col-title">{{ item.title }}</div>
            <div class="col col-body">
              {{ item.body }}
            </div>
            <div class="col col-action">
              <button
                  v-if="activeTab === 'unread'"
                  type="button"
                  class="pill-btn pill-btn--orange"
                  @click="markAsRead(item.id)"
              >
                읽음
              </button>
              <button
                  v-else
                  type="button"
                  class="pill-btn pill-btn--pink"
                  @click="removeNotification(item.id)"
              >
                삭제
              </button>
            </div>
          </div>
        </div>

        <!-- 알림 없을 때 넉넉한 영역 + 중앙 정렬 -->
        <div v-else class="list-empty">
          표시할 알림이 없습니다.
        </div>
      </div>
    </div>
  </el-popover>
</template>

<style scoped>

/* 뱃지 위치 */
.notification-badge :deep(.el-badge__content) {
  top: 8px;
  right: auto;
  left: 4px;
  transform: translate(-50%, -50%);
}

.alert-wrapper {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

/* 팝오버 껍데기 */
.notification-popper {
  padding: 0;
  border-radius: 18px;
  border: 1px solid #f3e2c6;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.08);
}

/* 컨테이너: 최소 높이 + flex 레이아웃 */
.notification-panel {
  background: #ffffff;
  border-radius: 18px;
  overflow: hidden;
  min-height: 260px; /* 알림이 없어도 박스가 어느 정도는 커 보이게 */
  display: flex;
  flex-direction: column;
}

/* 상단 탭 영역 */
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px 10px;
  background: #fff7e5;
  border-bottom: 1px solid #f1e0c6;
}

.tab-group {
  display: inline-flex;
  padding: 3px;
  border-radius: 999px;
  background: #fff1d6;
}

.tab-button {
  min-width: 88px;
  padding: 6px 14px;
  border-radius: 999px;
  border: none;
  background: transparent;
  font-size: 13px;
  font-weight: 600;
  color: #9b9b9b;
  cursor: pointer;
}

.tab-button.active {
  background: #ff9f1c;
  color: #ffffff;
  box-shadow: 0 3px 6px rgba(255, 159, 28, 0.4);
}

/* 상단 우측 버튼 */
.header-action {
  min-width: 92px;
  height: 32px;
  border-radius: 999px;
  border: none;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.header-action--orange {
  background: #ff9f1c;
  color: #ffffff;
}

.header-action--pink {
  background: #ff7b8a;
  color: #ffffff;
}

/* 리스트 전체 래퍼: 남은 공간 채우기 */
.list-wrapper {
  padding: 10px 16px 14px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 헤더 + 행 공통 그리드 */
.list-head,
.list-row {
  display: grid;
  grid-template-columns: 52px 110px 1fr 80px;
  align-items: center;
}

.list-head {
  height: 34px;
  border-bottom: 1px solid #f2f2f2;
  font-size: 12px;
  font-weight: 700;
  color: #9b9b9b;
  flex-shrink: 0;
}

/* 실제 리스트 영역 */
.list-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.list-row {
  min-height: 44px;
  border-bottom: 1px solid #f8f3ea;
  font-size: 13px;
  color: #555555;
}

.list-row:last-child {
  border-bottom: none;
}

.col {
  padding: 0 6px;
}

.col-body {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 비어 있을 때: 가운데 배치 + 여유 공간 */
.list-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 6px 28px;
  text-align: center;
  font-size: 13px;
  color: #b0b0b0;
}

/* 행 오른쪽 pill 버튼 */
.pill-btn {
  padding: 4px 12px;
  border-radius: 999px;
  border: none;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}

.pill-btn--orange {
  background: #ffb347;
  color: #ffffff;
}

.pill-btn--pink {
  background: #ff909f;
  color: #ffffff;
}
</style>
