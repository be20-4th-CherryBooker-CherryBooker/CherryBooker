<template>
  <div v-if="show" class="modal-overlay" @click.self="close">
    <div class="modal-container">
      <button class="close-btn" @click="close">✖</button>

      <h2 class="modal-title">도서 검색하기</h2>

      <label class="search-label">📚 도서 검색</label>
      <div class="search-row">
        <input
            v-model.trim="keyword"
            class="search-input"
            placeholder="책 제목 또는 ISBN"
            @keyup.enter="search"
        />
        <button class="search-btn" @click="search">
          검색하기
        </button>
      </div>

      <div class="result-wrapper">
        <p v-if="!results.length && !isLoading" class="placeholder">
          원하는 책 제목을 입력해서 검색해보세요.
        </p>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <div v-for="book in results" :key="book.isbn" class="book-row">
          <div class="cover-box">
            <img
                :src="book.coverImageUrl || fallbackCover"
                :alt="book.title"
                @error="handleImageError"
            />
            <button
                class="select-btn"
                :disabled="isRegistering"
                @click="registerBook(book)"
            >
              {{ isRegistering ? "추가 중..." : "책 추가" }}
            </button>
          </div>

          <div class="book-info">
            <p><strong>제목:</strong> {{ book.title }}</p>
            <p><strong>저자:</strong> {{ book.author || "정보 없음" }}</p>
            <p><strong>ISBN:</strong> {{ book.isbn || "정보 없음" }}</p>
          </div>
        </div>
      </div>

      <div v-if="isLoading" class="loading-row">검색 중...</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import api from "@/axios";
import { useAuthStore } from "@/stores/AuthStore";

const props = defineProps({
  show: Boolean,
});

const emit = defineEmits(["close", "added"]);

const keyword = ref("");
const results = ref([]);
const isLoading = ref(false);
const isRegistering = ref(false);
const errorMessage = ref("");
const fallbackCover = "/images/default-book.png";
const MYLIB_BASE_URL = "/api/mylib";
const myLibApiUrl = (path = "") => `${MYLIB_BASE_URL}${path}`;
const authStore = useAuthStore();

const close = () => {
  keyword.value = "";
  results.value = [];
  errorMessage.value = "";
  emit("close");
};

const assertAuthenticated = () => {
  if (authStore.isAuthenticated) {
    return true;
  }
  errorMessage.value = "로그인이 필요합니다.";
  return false;
};

const search = async () => {
  if (!keyword.value || !assertAuthenticated()) return;
  isLoading.value = true;
  errorMessage.value = "";
  try {
    console.info("[MyLib] Searching external books:", keyword.value);
    const { data } = await api.get(myLibApiUrl("/library-books"), {
      params: {
        keyword: keyword.value,
        size: 10,
      },
      withCredentials: true,
    });
    const payload = data?.data ?? data;
    results.value = Array.isArray(payload) ? payload : [];
    console.info("[MyLib] Book search results:", results.value.length);
  } catch (error) {
    console.error(error);
    errorMessage.value =
        error.response?.data?.message || "도서 검색 중 오류가 발생했습니다.";
  } finally {
    isLoading.value = false;
  }
};

const registerBook = async (book) => {
  if (isRegistering.value || !assertAuthenticated()) return;
  isRegistering.value = true;
  try {
    await api.post(
      myLibApiUrl("/register-books"),
      {
        keyword: book.title,
        isbnHint: book.isbn,
      },
      { withCredentials: true }
    );
    emit("added");
    close();
  } catch (error) {
    console.error(error);
    errorMessage.value =
        error.response?.data?.message || "책 추가 중 오류가 발생했습니다.";
  } finally {
    isRegistering.value = false;
  }
};

const handleImageError = (event) => {
  event.target.src = fallbackCover;
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-container {
  width: min(480px, 92%);
  background: white;
  border-radius: 24px;
  padding: 32px 28px 40px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);
  position: relative;
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  border: none;
  background: none;
  font-size: 20px;
  cursor: pointer;
}

.modal-title {
  text-align: center;
  font-size: 20px;
  margin-bottom: 18px;
}

.search-label {
  font-size: 14px;
  font-weight: 600;
}

.search-row {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.search-input {
  flex: 1;
  height: 40px;
  border-radius: 20px;
  border: 1px solid #e1ded9;
  padding: 0 16px;
}

.search-btn {
  width: 100px;
  border: none;
  border-radius: 20px;
  background: #ffde99;
  font-weight: 600;
  cursor: pointer;
}

.result-wrapper {
  margin-top: 24px;
  max-height: 420px;
  overflow-y: auto;
}

.book-row {
  display: flex;
  gap: 18px;
  margin-bottom: 24px;
}

.cover-box {
  width: 120px;
  position: relative;
}

.cover-box img {
  width: 100%;
  height: 170px;
  object-fit: cover;
  border-radius: 14px;
  box-shadow: 0 8px 14px rgba(0, 0, 0, 0.15);
}

.select-btn {
  position: absolute;
  inset: auto 10px 10px;
  border: none;
  border-radius: 20px;
  background: rgba(0, 0, 0, 0.85);
  color: white;
  padding: 8px 12px;
  width: calc(100% - 20px);
  opacity: 0;
  cursor: pointer;
  transition: opacity 0.2s ease;
}

.cover-box:hover .select-btn {
  opacity: 1;
}

.book-info {
  flex: 1;
  font-size: 14px;
  color: #3a2f27;
}

.placeholder {
  text-align: center;
  color: #8f8f8f;
}

.error-message {
  color: #d64545;
  text-align: center;
  margin-bottom: 12px;
}

.loading-row {
  text-align: center;
  margin-top: 8px;
}
</style>
