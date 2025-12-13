<script setup>
import { computed } from "vue";
import { useRoute } from "vue-router";

import NavigationBar from "@/components/NavigationBar.vue";
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
  </div>
</template>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { background-color: #FFFAEE; }
</style>
