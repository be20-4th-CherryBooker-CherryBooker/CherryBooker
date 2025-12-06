<template>
  <link href="https://fonts.googleapis.com/css2?family=Angkor&display=swap" rel="stylesheet">

  <nav class="nav-container">
    <!-- 왼쪽 영역 -->
    <div class="nav-item"
         :class="{ active: isActive('/mypage') }"
         @click="goTo('/mypage')">
      <img src="/images/mypage.png" alt="user" />
    </div>

    <div class="nav-item"
         :class="{ active: isActive('/books') }"
         @click="goTo('/books')">
      <img src="/images/book.png" alt="book" />
    </div>

    <!-- 중앙 로고 -->
    <div class="nav-logo" @click="goTo('/')">
      CherryBooker
    </div>

    <!-- 오른쪽 영역 -->
    <div class="nav-item"
         :class="{ active: isActive('/quotes') }"
         @click="goTo('/quotes')">
      <img src="/images/quote.png" alt="quote" />
    </div>

    <div class="nav-item"
         :class="{ active: isActive('/menu') }"
         @click="goTo('/menu')">
      <img src="/images/thread.png" alt="menu" />
    </div>
  </nav>
</template>

<script setup>
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();

const goTo = (path) => {
  router.push(path);
};

const isActive = (path) => route.path.startsWith(path);
</script>

<style scoped>
.nav-container {
  width: 100%;
  height: 60px;
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
  background: #ffffff;
  position: relative;
}

.nav-item {
  position: relative;
}

.nav-item.active::after {
  content: "";
  position: absolute;
  bottom: -13px;
  left: 50%;
  transform: translateX(-50%);
  width: 700%;
  height: 4px;
  background-color: #EF3E42;
  border-radius: 2px;
}

.nav-item img {
  width: 28px;
  height: 28px;
  cursor: pointer;
  opacity: 0.9;
  transition: 0.2s;
}

.nav-item img:hover {
  opacity: 1;
  transform: scale(1.05);
}

.nav-logo {
  font-size: 20px;
  font-weight: 700;
  color: #EF3E42; /* 체리색 */
  cursor: pointer;
  font-family: "Angkor", serif;
}
</style>
