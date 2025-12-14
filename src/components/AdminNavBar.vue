<template>
  <nav class="admin-nav">

    <div class="nav-item">
      <img src="/images/user.svg" alt="user" />
    </div>

    <div class="nav-item"
         :class="{ active: isActive('/admin/reports') }"
         @click="goTo('/admin/reports')">
      <img src="/images/eye-off.svg" alt="reports" />
    </div>

    <div class="nav-logo">
      CherryBooker Admin
    </div>

    <div class="nav-item"
         :class="{ active: isActive('/admin/alarms') }"
         @click="goTo('/admin/alarms')">
      <img src="/images/bell.svg" alt="logs"/>
    </div>

    <div class="nav-item"
         :class="{ active: isActive('/admin/alarms/send') }"
         @click="goTo('/admin/alarms/send')">
      <img src="/images/megaphone.svg" alt="templates"  />
    </div>

  </nav>
</template>


<script setup>
import { useRoute, useRouter } from "vue-router";

const router = useRouter();
const route = useRoute();

const goTo = (p) => router.push(p);
const isActive = (p) => route.path.startsWith(p);
</script>

<style scoped>
.admin-nav {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  align-items: center;
  text-align: center;
  height: 65px;
  background: white;
  border-bottom: 1px solid #eee;
}

/* nav-item 기본 */
.nav-item {
  position: relative;
}

/* 아이콘 */
.nav-item img {
  width: 28px;
  height: 28px;
  filter: brightness(0);
  cursor: pointer;
}

/* hover */
.nav-item img:hover {
  filter: brightness(0.3);
}

/* active 상태 시 빨간색 강조 */
.nav-item.active img {
  filter: invert(43%) sepia(89%) saturate(2466%)
  hue-rotate(346deg) brightness(95%) contrast(90%);
}

/* active underline */
.nav-item.active::after {
  content: "";
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 40%;
  height: 3px;
  background: #EF3E42;
  border-radius: 2px;
}

/* 중앙 로고 */
.nav-logo {
  font-size: 20px;
  font-weight: 700;
  color: #EF3E42; /* 체리색 */
  cursor: pointer;
  font-family: "Angkor", serif;
}

</style>
