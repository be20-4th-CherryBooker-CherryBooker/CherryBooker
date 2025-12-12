<template>
  <button
    type="button"
    class="scroll-arrow"
    :class="`arrow-${direction}`"
    :aria-label="ariaLabel"
    @click="$emit('click')"
  >
    <span class="icon" aria-hidden="true">
      {{ direction === "up" ? "↑" : "↓" }}
    </span>
  </button>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  direction: {
    type: String,
    default: "down",
    validator: (value) => ["up", "down"].includes(value),
  },
});

const ariaLabel = computed(() =>
  props.direction === "up" ? "맨 위로 이동" : "아래로 이동"
);
</script>

<style scoped>
.scroll-arrow {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: #ffb8b8;
  color: #fff;
  font-size: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 26px rgba(223, 62, 62, 0.25);
  cursor: pointer;
  transition: transform 0.2s ease, opacity 0.2s ease;
  z-index: 1200;
}

.scroll-arrow:hover {
  transform: translateY(-2px);
}

.arrow-up .icon {
  transform: rotate(0deg);
}

.arrow-down .icon {
  transform: rotate(0deg);
}

@media (max-width: 768px) {
  .scroll-arrow {
    right: 12px;
    bottom: 12px;
    width: 42px;
    height: 42px;
    font-size: 22px;
  }
}
</style>
