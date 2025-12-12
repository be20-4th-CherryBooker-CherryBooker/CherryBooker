<template>
  <link href="https://fonts.googleapis.com/css2?family=Angkor&display=swap" rel="stylesheet">

  <div class="admin-login-container">

    <!-- 배경 장식 -->
    <div class="bg-cherry"></div>

    <!-- 로그인 박스 -->
    <div class="login-box">

      <!-- 배경 + 제목 -->
      <h1 class="logo-title">CherryBooker</h1>

      <h2 class="login-title">Admin Login</h2>

      <input
          v-model="username"
          type="text"
          placeholder="관리자 이름"
          class="input-field"
      />

      <input
          v-model="password"
          type="password"
          placeholder="비밀번호"
          class="input-field"
      />

      <button class="login-btn" type="button" @click="login">로그인</button>

      <router-link to="/login" class="back-user">
        ← 사용자 로그인 페이지로 돌아가기
      </router-link>
    </div>

  </div>
</template>

<script setup>
import { ref } from "vue";
import router from "@/router/index.js";

const username = ref("");
const password = ref("");

const login = () => {
  if (!username.value || !password.value) {
    alert("아이디와 비밀번호를 입력하세요.");
    return;
  }

  alert("관리자 로그인 진행! (백엔드 연결 예정)");

  console.log("login start");
  router.push("/admin/reports");
  console.log("after push");

};
</script>

<style scoped>
.admin-login-container {
  width: 100%;
  height: 100vh;
  background: #fff7ea;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  font-family: "Pretendard", sans-serif;
}

/* 배경 이미지 */
.bg-cherry {
  background-image: url("/images/cherry_bg.png");
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;

  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 0;
}

/* CherryBooker 제목 */
.logo-title {
  font-family: "Angkor", serif;
  font-size: 36px;
  font-weight: 700;
  color: #df3e3e;
  margin-bottom: 10px;
}

/* 로그인 박스 */
.login-box {
  width: 420px;
  background: white;
  padding: 30px 30px;
  border-radius: 22px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.1);
  text-align: center;
  position: relative;
  z-index: 2;
}

/* Admin Login 텍스트 */
.login-title {
  font-size: 26px;
  color: #df3e3e;
  font-weight: 700;
  margin-bottom: 25px;
  font-family: "Angkor", serif;
}

/* 입력창 */
.input-field {
  width: 100%;
  height: 42px;
  border: 1px solid #f0d9b5;
  border-radius: 10px;
  padding: 0 12px;
  margin-bottom: 15px;
  font-size: 14px;
  outline: none;
}

/* 로그인 버튼 */
.login-btn {
  width: 100%;
  height: 45px;
  border-radius: 24px;
  border: 2px solid #df3e3e;
  background: white;
  color: #df3e3e;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 5px;
  transition: 0.2s;
}

.login-btn:hover {
  background: #ffe0e0;
}

/* 사용자 로그인 페이지 링크 */
.back-user {
  margin-top: 15px;
  display: block;
  font-size: 13px;
  color: #555;
  text-decoration: none;
}

.back-user:hover {
  color: #df3e3e;
}
</style>
