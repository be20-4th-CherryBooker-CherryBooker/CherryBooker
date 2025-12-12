<template>
  <article
    class="book-card"
    :class="[viewMode, statusClass, { 'is-completing': completing }]"
    @click="handleSelect"
  >
    <div v-if="book?.badgeIssued" class="badge">
      <span>🏅 완독</span>
    </div>

    <div v-if="viewMode === 'cover'" class="cover-wrapper">
      <img
        :src="coverImage"
        :alt="`${book?.title || '책'} 표지`"
        @error="handleImageError"
      />
    </div>

    <div v-else class="spine-wrapper">
      <div class="spine-title">{{ book?.title || "제목 미정" }}</div>
    </div>

    <div class="card-body">
      <span class="status-chip">{{ statusLabel }}</span>
      <p class="title" :title="book?.title">{{ book?.title || "제목 미정" }}</p>
      <p class="author">{{ book?.author || "저자 정보 없음" }}</p>

      <button
        v-if="canComplete"
        class="complete-btn"
        type="button"
        :disabled="completing"
        @click.stop="handleComplete"
      >
        {{ completing ? "완독 처리 중..." : "완독하기" }}
      </button>
    </div>
  </article>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  book: {
    type: Object,
    required: true,
  },
  completing: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(["select", "complete"]);

const fallbackCover = "/images/default-book.png";
const statusMap = {
  WISH: "읽고 싶은",
  READING: "읽는 중",
  READ: "완독",
};

const coverImage = computed(
  () => props.book?.coverImageUrl || fallbackCover
);

const viewMode = computed(() =>
  props.book?.displayType === "SPINE" ? "spine" : "cover"
);

const statusLabel = computed(
  () => statusMap[props.book?.status] || "상태 미정"
);

const statusClass = computed(
  () => `status-${(props.book?.status || "unknown").toLowerCase()}`
);

const canComplete = computed(() => props.book?.status === "READING");

const handleImageError = (event) => {
  event.target.src = fallbackCover;
};

const handleSelect = () => {
  emit("select", props.book);
};

const handleComplete = () => {
  if (!canComplete.value) return;
  emit("complete", props.book);
};
</script>

<style scoped>
.book-card {
  width: 100%;
  max-width: 180px;
  min-height: 260px;
  border-radius: 18px;
  background: white;
  border: 1px solid #f2e8dd;
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.08);
  padding: 16px 14px 18px;
  position: relative;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 26px rgba(39, 23, 2, 0.16);
}

.book-card.spine {
  background: #fff2e3;
  border-color: #f0c193;
}

.book-card.is-completing {
  opacity: 0.8;
}

.cover-wrapper,
.spine-wrapper {
  width: 120px;
  height: 170px;
}

.cover-wrapper img {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.2);
}

.spine-wrapper {
  border-radius: 12px;
  background: linear-gradient(130deg, #ffb996, #ffdfad);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  text-align: center;
}

.spine-title {
  writing-mode: vertical-rl;
  text-orientation: mixed;
  font-weight: 600;
  color: #6d4300;
}

.card-body {
  width: 100%;
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.status-chip {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 999px;
  display: inline-block;
  color: #fff;
  background: #999;
}

.status-wish .status-chip {
  background: #c9a7eb;
}

.status-reading .status-chip {
  background: #ffb347;
}

.status-read .status-chip {
  background: #65c27c;
}

.title {
  font-size: 15px;
  font-weight: 600;
  margin: 0;
  color: #33251d;
  line-height: 1.3;
}

.author {
  font-size: 13px;
  margin: 0;
  color: #8a7a6b;
}

.complete-btn {
  margin-top: 6px;
  padding: 6px 16px;
  border-radius: 20px;
  border: none;
  background: #ffcf7c;
  font-weight: 600;
  cursor: pointer;
}

.complete-btn:disabled {
  cursor: not-allowed;
  background: #e1d7ca;
  color: #8f8f8f;
}

.badge {
  position: absolute;
  top: 10px;
  left: 12px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 999px;
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 600;
  color: #c88000;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}
</style>
