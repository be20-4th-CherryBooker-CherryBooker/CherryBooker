<script setup>
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'

import { computed } from "vue";
import { useRoute } from "vue-router";

import NavigationBar from "@/components/NavigationBar.vue";
import NotificationBell from "@/components/notification/NotificationBell.vue";
import AlertIconSrc from '@/assets/icon/icon-headbar-alert.svg'

import { useAuthStore } from '@/stores/AuthStore'
import {useNotificationStore} from "@/stores/notificationStore.js";

const route = useRoute()
const authStore = useAuthStore()
const { isAuthenticated } = storeToRefs(authStore)

const showGlobalBell = computed(() => {
  const hideNav = route.meta?.hideNav === true
  return isAuthenticated.value && !hideNav
})

const notificationStore = useNotificationStore()

watch(
    () => authStore.isAuthenticated,
    async (v) => {
      if (v) {
        notificationStore.connectSse()
        await Promise.all([
          notificationStore.loadNotifications(0),
          notificationStore.fetchUnreadCount(),
        ])
      } else {
        notificationStore.disconnectSse()
      }
    },
    { immediate: true }
)

import AdminNavBar from "@/components/AdminNavBar.vue";

const route = useRoute();

/** 현재 경로가 /admin 으로 시작하면 true */
const isAdminPage = computed(() => route.path.startsWith("/admin"));
</script>

<template>
  <div>

    <!-- 사용자 네비바 (admin 페이지가 아닐 때만 표시) -->
    <NavigationBar
        v-if="!isAdminPage && !$route.meta?.hideNav"
    />

    <!-- 관리자 네비바 (admin 페이지에서만 표시) -->
    <AdminNavBar
        v-if="isAdminPage"
    />

    <!-- 실제 화면 -->
    <router-view />

    <!--  알림 종: 로그인 + 네비가 보이는 페이지에서만 -->
    <div
        class="global-notification-bell"
        v-if="showGlobalBell"
    >
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

* { margin: 0; padding: 0; box-sizing: border-box; }
body { background-color: #FFFAEE; }
</style>
