<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-container">

      <!-- 닫기 버튼 -->
      <button class="close-btn" @click="closeModal">✖</button>

      <h2 class="modal-title">도서 검색하기</h2>

      <!-- 검색 -->
      <div class="section">
        <label class="label">🔍 검색어(책 제목)</label>
        <div class="search-row">
          <input
              v-model="keyword"
              type="text"
              class="search-input"
              placeholder="도서 제목"
          />
          <button class="search-btn" @click="searchBooks" :disabled="loading">
            {{ loading ? "검색 중..." : "검색" }}
          </button>
        </div>
      </div>

      <!-- 검색 결과 리스트 -->
      <div class="results">
        <div
            class="book-item"
            v-for="book in results"
            :key="book.bookId"
            @click="selectBook(book)"
        >
          <img :src="book.coverImageUrl" class="book-cover" />
          <div class="book-info">
            <div class="book-title">{{ book.title }}</div>
            <div class="book-author">{{ book.author }}</div>
          </div>
        </div>

        <p v-if="!loading && results.length === 0" class="no-result">
          검색 결과가 없습니다.
        </p>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import axios from "axios";

const props = defineProps({
  show: Boolean,
});

const emit = defineEmits(["close", "selected"]);

const keyword = ref("");
const results = ref([]);
const loading = ref(false);

const closeModal = () => {
  keyword.value = "";
  results.value = [];
  loading.value = false;
  emit("close");
};

const searchBooks = async () => {
  if (!keyword.value.trim()) {
    alert("검색어를 입력해주세요.");
    return;
  }

  loading.value = true;

  try {
    const token = localStorage.getItem("accessToken");

    const res = await axios.get(`/api/mylib/library-books`, {
      params: { keyword: keyword.value, size: 10 },
      headers: { Authorization: `Bearer ${token}` },
    });

    // 백엔드 응답
    results.value = res.data.data || [];
  } catch (e) {
    console.error("도서 검색 실패", e);
    alert("도서 검색 중 오류가 발생했습니다.");
  } finally {
    loading.value = false;
  }
};

// 책 선택 후 부모 컴포넌트로 전달
const selectBook = (book) => {
  emit("selected", book);
  closeModal();
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-container {
  width: 500px;
  background: white;
  border-radius: 18px;
  padding: 26px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
  position: relative;
}

/* 닫기 버튼 */
.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  border: none;
  background: none;
  font-size: 20px;
  cursor: pointer;
}

.modal-title {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
}

.section {
  margin-top: 18px;
}

.label {
  font-size: 14px;
  margin-bottom: 6px;
  display: block;
}

.search-row {
  display: flex;
  gap: 8px;
}

.search-input {
  flex: 1;
  height: 34px;
  border: 1px solid #ddd;
  padding: 0 10px;
  border-radius: 12px;
  background: #fafafa;
}

.search-btn {
  width: 90px;
  background: #ffa83d;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 13px;
}

.results {
  margin-top: 20px;
  max-height: 300px;
  overflow-y: auto;
}

.book-item {
  display: flex;
  gap: 12px;
  padding: 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: 0.2s;
  border: 1px solid #eee;
  margin-bottom: 10px;
}

.book-item:hover {
  background: #fff5e6;
}

.book-cover {
  width: 55px;
  height: 75px;
  border-radius: 6px;
  object-fit: cover;
}

.book-info {
  text-align: left;
}

.book-title {
  font-size: 14px;
  font-weight: 600;
}

.book-author {
  font-size: 12px;
  color: #777;
}

.no-result {
  text-align: center;
  font-size: 13px;
  margin-top: 20px;
  color: #888;
}
</style>
