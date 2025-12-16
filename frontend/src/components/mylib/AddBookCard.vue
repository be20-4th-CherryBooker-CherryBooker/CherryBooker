<template>
  <div
    class="add-book-card"
    role="button"
    tabindex="0"
    @click="emitClick"
    @keyup.enter.prevent="emitClick"
    @keyup.space.prevent="emitClick"
  >
    <div class="circle">
      <span>+</span>
    </div>
    <p class="title">새 책 추가</p>
    <p class="description">내 서재에 책을 채워보세요.</p>
  </div>
</template>

<script setup>
const emit = defineEmits(["click"]);

const emitClick = () => {
  emit("click");
};
</script>

<style scoped>
.add-book-card {
  width: 160px;
  min-height: 240px;
  border: 2px dashed #ffc8c8;
  border-radius: 18px;
  background: #fff6f3;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #d16464;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  outline: none;
  padding: 16px;
  text-align: center;
}

.add-book-card:hover,
.add-book-card:focus-visible {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(209, 100, 100, 0.18);
}

.circle {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #ffe4e4;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 600;
}

.title {
  font-weight: 600;
  font-size: 16px;
  margin: 0;
}

.description {
  font-size: 13px;
  color: #a36a6a;
  margin: 0;
}
</style>
