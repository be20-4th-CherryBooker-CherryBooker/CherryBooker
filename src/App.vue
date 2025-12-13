<script setup>
import { computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'

import NavigationBar from "@/components/NavigationBar.vue"
import AdminNavBar from "@/components/AdminNavBar.vue"
import NotificationBell from "@/components/notification/NotificationBell.vue"
import AlertIconSrc from '@/assets/icon/icon-headbar-alert.svg'

import { useAuthStore } from '@/stores/AuthStore'
import { useNotificationStore } from "@/stores/notificationStore.js"

const route = useRoute()

const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const { isAuthenticated } = storeToRefs(authStore)

/** 현재 경로가 /admin 으로 시작하면 true */
const isAdminPage = computed(() => route.path.startsWith("/admin"))

/** 알림 종 노출 조건: 로그인 + 네비가 숨겨진 페이지가 아님 */
const showGlobalBell = computed(() => {
  const hideNav = route.meta?.hideNav === true
  return isAuthenticated.value && !hideNav
})

/**
 * 앱 부팅 시점: 새로고침 대비
 * - localStorage에서 인증 복원
 * - 토큰 만료면 refresh 시도
 */
onMounted(async () => {
  if (typeof authStore.loadFromStorage === 'function') {
    authStore.loadFromStorage()
  }
  if (typeof authStore.validateToken === 'function') {
    await authStore.validateToken()
  }
})

/**
 * 로그인 상태 변화를 단일 관문으로 처리
 * - 로그인 되면: SSE 연결 + 알림/카운트 초기 로딩
 * - 로그아웃 되면: SSE 해제 + store 정리
 */
watch(
    () => authStore.isAuthenticated,
    async (v) => {
      if (v) {
        // connectSse 내부에서도 loadFromStorage/validateToken 방어가 있지만,
        // 여기서 먼저 초기 데이터 로딩을 확실히 해둠
        await Promise.all([
          notificationStore.fetchUnreadCount(),
          notificationStore.loadNotifications(0),
        ])

        notificationStore.connectSse()
      } else {
        notificationStore.disconnectSse()
      }
    },
    { immediate: true }
)
</script>

<template>
  <div>
    <!-- 사용자 네비바 (admin 페이지가 아닐 때만 표시) -->
    <NavigationBar v-if="!isAdminPage && !$route.meta?.hideNav" />

    <!-- 관리자 네비바 (admin 페이지에서만 표시) -->
    <AdminNavBar v-if="isAdminPage" />

    <!-- 실제 화면 -->
    <router-view />

    <!-- 알림 종: 로그인 + 네비가 보이는 페이지에서만 -->
    <div class="global-notification-bell" v-if="showGlobalBell">
      <NotificationBell :icon-src="AlertIconSrc" />
    </div>
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  margin: 0;
  background-color: #FFFAEE;
}

.app-container {
  font-family: "Pretendard", sans-serif;
}

.global-notification-bell {
  position: fixed;
  top: 100px;
  right: 40px;
  z-index: 999;
}
</style>
