<template>
  <link href="https://fonts.googleapis.com/css2?family=Angkor&display=swap" rel="stylesheet">

  <div class="login-container">
    <!-- 배경 장식 -->
    <div class="bg-cherry"></div>

    <!-- 중앙 로그인 박스 -->
    <div class="login-box">
      <h1 class="title">CherryBooker</h1>
      <h2 class="subtitle">Login</h2>

      <!-- Google 로그인 -->
      <button class="google-btn" @click="handleSocialLogin('google')">
        <img
            class="google-icon"
            src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg"
            alt="Google logo"
        />
        Google로 로그인
      </button>

      <!-- Kakao 로그인 -->
      <button class="kakao-btn" @click="handleSocialLogin('kakao')">
        <img
            class="kakao-icon"
            src="https://developers.kakao.com/assets/img/about/logos/kakaolink/kakaolink_btn_small.png"
            alt="Kakao logo"
        />
        Kakao로 로그인
      </button>

      <p class="guide-text">CherryBooker에 처음이신가요?<br/>Google, 또는 Kakao로 시작하세요.</p>

      <router-link to="/admin/login" class="admin-login">
        관리자 로그인
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { redirectToSocialLogin } from '@/api/AuthApi'

const handleSocialLogin = (provider) => {
  redirectToSocialLogin(provider)
}
</script>

<style scoped>
.login-container {
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

.google-icon, .kakao-icon {
  width: 22px;
  height: 22px;
  margin-right: 10px;
}

/* 로그인 카드 */
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

.title {
  font-size: 36px;
  font-weight: 700;
  color: #e63e3e;
  margin-bottom: 10px;
  font-family: "Angkor", serif;
}

.subtitle {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 10px;
  color: #d85353;
  font-family: "Angkor", serif;
}

/* 버튼 공통 */
button {
  width: 100%;
  height: 48px;
  border-radius: 10px;
  border: none;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
  margin-bottom: 14px;
  transition: all 0.3s;
}

button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* Google 버튼 */
.google-btn {
  background: #4285F4;
  color: white;
}

.google-btn:hover {
  background: #357ae8;
}

/* Kakao 버튼 */
.kakao-btn {
  background: #FEE500;
  color: #3B1E1E;
}

.kakao-btn:hover {
  background: #fdd835;
}

/* 안내 문구 */
.guide-text {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 15px 0;
}

/* 관리자 로그인 */
.admin-login {
  background: none;
  color: #555;
  font-size: 14px;
  text-decoration: underline;
  cursor: pointer;
  display: inline-block;
  margin-top: 10px;
  transition: color 0.3s;
}

.admin-login:hover {
  color: #e63e3e;
}
</style>