<template>
  <div class="loading">
    로그인 처리 중입니다...
  </div>
</template>

<script setup>
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();

// URL 예: /oauth2/success?accessToken=abcd1234
const token = route.query.accessToken;

if (token) {
  localStorage.setItem("accessToken", token);
  console.log("토큰 저장 완료:", token);

  // 원하는 페이지로 이동
  router.push("/");
} else {
  console.error("토큰 없음!");
  router.push("/login");
}
</script>

<style scoped>
.loading {
  text-align: center;
  padding-top: 100px;
  font-size: 18px;
  color: #444;
}
</style>
