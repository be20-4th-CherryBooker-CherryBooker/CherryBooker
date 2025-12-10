<template>
  <div class="book-card" @click="$emit('select', book)">
    <div class="cover-wrapper" :class="{ spine: book.displayType === 'SPINE' }">
      <img
          :src="resolvedCover"
          :alt="book.title"
          class="book-cover"
          @error="handleImageError"
      />

      <div v-if="book.badgeIssued" class="badge-check">
        ✓
      </div>

      <div v-if="book.status === 'READING'" class="complete-overlay">
        <button
            class="complete-btn"
            :disabled="completing"
            @click.stop="$emit('complete', book)"
        >
          {{ completing ? "변경 중..." : "완독" }}
        </button>
      </div>
    </div>

    <div class="book-title">{{ book.title }}</div>
    <div class="book-author">{{ book.author }}</div>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";

const props = defineProps({
  book: {
    type: Object,
    required: true,
  },
  completing: {
    type: Boolean,
    default: false
  }
});

defineEmits(["select", "complete"]);

const fallbackCover = "/images/default-book.png";
const currentCover = ref(props.book.coverImageUrl || fallbackCover);

const resolvedCover = computed(() => currentCover.value || fallbackCover);

console.log("[BookCard] status:", props.book.status, "title:", props.book.title);

const handleImageError = () => {
  currentCover.value = fallbackCover;
};
</script>

<style scoped>
.book-card {
  width: 180px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.book-card:hover {
  transform: translateY(-4px);
}

.cover-wrapper {
  position: relative;
  width: 150px;
  height: 210px;
  border-radius: 16px;
  background: #f7f2e9;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.cover-wrapper.spine img {
  object-fit: contain;
}

.book-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.badge-check {
  position: absolute;
  top: 8px;
  right: 10px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #41c891;
  color: white;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.complete-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0);
  opacity: 0;
  pointer-events: none;
  transition: background 0.2s ease, opacity 0.2s ease;
}

.complete-btn {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s ease;
  padding: 10px 22px;
  border-radius: 20px;
  border: none;
  background: rgba(28, 28, 28, 0.92);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.cover-wrapper:hover .complete-overlay {
  opacity: 1;
  pointer-events: auto;
  background: rgba(0, 0, 0, 0.25);
}

.cover-wrapper:hover .complete-btn {
  opacity: 1;
  pointer-events: auto;
}

.cover-wrapper:hover .status-pill.reading {
  opacity: 0;
}

.book-title {
  font-weight: 600;
  font-size: 14px;
  color: #3d2c28;
  text-align: center;
  line-height: 1.3;
}

.book-author {
  font-size: 12px;
  color: #7b6a65;
  text-align: center;
}

@media (max-width: 768px) {
  .book-card {
    width: 140px;
  }

  .cover-wrapper {
    width: 120px;
    height: 170px;
  }
}
</style>
