<template>
  <div class="library-container">
    <div class="page-title">
      <span>나의 서재</span>
    </div>

    <div class="search-row">
      <div class="center-box">
        <input
          v-model.trim="keyword"
          type="text"
          class="search-input"
          placeholder="책 제목 또는 저자를 입력하세요"
          @keyup.enter="handleSearch"
        />
        <button class="search-btn" @click="handleSearch">검색하기</button>
      </div>
    </div>

    <div class="filter-row">
      <button
        v-for="option in filterOptions"
        :key="option.value ?? 'ALL'"
        :class="['filter-btn', { active: option.value === selectedStatus }]"
        @click="changeFilter(option.value)"
      >
        {{ option.label }}
      </button>
    </div>

    <div class="grid-wrapper">
      <AddBookCard @click="openAddBookModal" />
      <BookCard
        v-for="book in books"
        :key="book.myLibId"
        :book="book"
        :completing="completingBookId === book.myLibId"
        @select="openBookDetail"
        @complete="markAsRead"
      />
    </div>

    <div
      v-if="!books.length && !isLoading && !errorMessage"
      class="empty-state"
    >
      {{ infoMessage || "아직 등록된 책이 없습니다. 첫 책을 추가해보세요!" }}
    </div>

    <div v-if="errorMessage" class="error-state">
      {{ errorMessage }}
    </div>

    <div v-if="infoMessage && !errorMessage" class="notice-row">
      {{ infoMessage }}
    </div>

    <div v-if="isLoading" class="loading-indicator">
      책을 불러오는 중...
    </div>

    <div ref="infiniteTarget" class="observer-target"></div>

    <ScrollArrow
      :direction="arrowDirection"
      @click="handleArrowClick"
    />

    <AddBookModal
      :show="showAddModal"
      @close="showAddModal = false"
      @added="handleBookAdded"
    />

    <BookDetailModal
      :show="showDetailModal"
      :book="selectedBook"
      @close="showDetailModal = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from "vue";
import api from "@/axios";
import BookCard from "@/components/mylib/BookCard.vue";
import AddBookCard from "@/components/mylib/AddBookCard.vue";
import AddBookModal from "@/components/mylib/AddBookModal.vue";
import BookDetailModal from "@/components/mylib/BookDetailModal.vue";
import ScrollArrow from "@/components/mylib/ScrollArrow.vue";
import { useAuthStore } from "@/stores/AuthStore";

const PAGE_SIZE = 8;
const MYLIB_BASE_URL = "/api/mylib";
const myLibApiUrl = (path = "") => `${MYLIB_BASE_URL}${path}`;
const authStore = useAuthStore();

const filterOptions = [
  { label: "전체", value: null },
  { label: "읽고 싶은", value: "WISH" },
  { label: "읽는 중", value: "READING" },
  { label: "완독", value: "READ" },
];

const keyword = ref("");
const selectedStatus = ref(null);
const books = ref([]);
const page = ref(0);
const hasMore = ref(true);
const isLoading = ref(false);
const errorMessage = ref("");
const infoMessage = ref("");
const completingBookId = ref(null);
const showAddModal = ref(false);
const showDetailModal = ref(false);
const selectedBook = ref(null);

const infiniteTarget = ref(null);
let observer;

const isAtBottom = ref(false);
const arrowDirection = computed(() => (isAtBottom.value ? "up" : "down"));

const parseApiResponse = (response) => {
  const body = response?.data ?? response;
  if (body?.success === false) {
    throw new Error(body?.message || "서버에서 오류가 발생했습니다.");
  }
  return body?.data ?? body;
};

const ensureAuthenticated = () => {
  if (authStore.isAuthenticated) {
    return true;
  }
  errorMessage.value = "로그인이 필요합니다.";
  return false;
};

const loadBooks = async ({ reset = false } = {}) => {
  if (!ensureAuthenticated() || isLoading.value || (!hasMore.value && !reset))
    return;

  if (reset) {
    books.value = [];
    page.value = 0;
    hasMore.value = true;
    errorMessage.value = "";
    infoMessage.value = "";
  }

  isLoading.value = true;

  try {

    const response = await api.get(myLibApiUrl("/books"), {
      params: {
        keyword: keyword.value || undefined,
        status: selectedStatus.value || undefined,
        page: page.value,
        size: PAGE_SIZE,
      },
      withCredentials: true,
    });

    const payload = parseApiResponse(response);
    if (!payload) throw new Error("응답 데이터가 비어 있습니다.");

    infoMessage.value = payload.notice || "";
    hasMore.value = payload.hasMore;

    if (reset) {
      books.value = payload.books || [];
    } else {
      books.value = [...books.value, ...(payload.books || [])];
    }

    page.value += 1;
  } catch (error) {
    console.error(error);
    errorMessage.value =
      error.response?.data?.message ||
      "나의 서재를 불러오는 중 오류가 발생했습니다.";
  } finally {
    isLoading.value = false;
  }
};

const handleSearch = () => {
  loadBooks({ reset: true });
};

const changeFilter = (status) => {
  if (selectedStatus.value === status) return;
  selectedStatus.value = status;
  handleSearch();
};

const openAddBookModal = () => {
  showAddModal.value = true;
};

const handleBookAdded = () => {
  showAddModal.value = false;
  loadBooks({ reset: true });
};

const openBookDetail = (book) => {
  selectedBook.value = book;
  showDetailModal.value = true;
};

const markAsRead = async (book) => {
  if (book.status !== "READING" || completingBookId.value) {
    return;
  }

  completingBookId.value = book.myLibId;

  try {
    await api.patch(
      myLibApiUrl(`/books/${book.myLibId}/status`),
      { targetStatus: "READ" },
      { withCredentials: true }
    );

    books.value = books.value.map((item) =>
        item.myLibId === book.myLibId
            ? {
              ...item,
              status: "READ",
              badgeIssued: true,
              displayType: "SPINE",
            }
            : item
    );
  } catch (error) {
    console.error(error);
    alert(
        error.response?.data?.message ||
        "책 상태를 변경하는 중 오류가 발생했습니다."
    );
  } finally {
    completingBookId.value = null;
  }
};

const handleArrowClick = () => {
  if (isAtBottom.value) {
    window.scrollTo({ top: 0, behavior: "smooth" });
  } else {
    window.scrollBy({ top: window.innerHeight, behavior: "smooth" });
  }
};

const handleScrollState = () => {
  const nearBottom =
    window.innerHeight + window.scrollY >= document.body.offsetHeight - 160;
  isAtBottom.value = nearBottom;
};

const setupObserver = () => {
  if (observer) observer.disconnect();
  observer = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) {
        loadBooks();
      }
    },
    { rootMargin: "0px 0px 120px 0px" }
  );

  if (infiniteTarget.value) {
    observer.observe(infiniteTarget.value);
  }
};

onMounted(() => {
  setupObserver();
  loadBooks({ reset: true });
  window.addEventListener("scroll", handleScrollState);
  handleScrollState();
});

onBeforeUnmount(() => {
  if (observer) observer.disconnect();
  window.removeEventListener("scroll", handleScrollState);
});
</script>

<style scoped>
.library-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 40px 20px 120px;
  font-family: "Pretendard", sans-serif;
  text-align: center;
}

.page-title {
  display: inline-block;
  margin: 10px auto 18px auto;
  padding: 16px 100px;
  border: 2px solid #df3e3e;
  border-radius: 40px;
  font-size: 22px;
  font-weight: 600;
  color: #df3e3e;
  background: #ffffff;
  box-shadow: 0 4px 10px rgba(223, 62, 62, 0.15);
}

.search-row {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.center-box {
  display: flex;
  gap: 8px;
}

.search-input {
  width: 280px;
  height: 38px;
  border: 1px solid #e2ddd6;
  border-radius: 20px;
  padding: 0 16px;
  background: #fff;
  font-size: 14px;
}

.search-btn {
  min-width: 90px;
  height: 38px;
  border: none;
  border-radius: 20px;
  background: #ffd992;
  color: #6d4300;
  font-weight: 600;
  cursor: pointer;
}

.filter-row {
  margin-top: 18px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

.filter-btn {
  padding: 6px 18px;
  border-radius: 20px;
  border: 1px solid #ddd;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
  color: #6e6e6e;
  transition: background 0.2s ease, color 0.2s ease;
}

.filter-btn.active {
  background: #ffb8b8;
  color: white;
  border-color: #ffb8b8;
}

.grid-wrapper {
  margin-top: 40px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 28px;
  justify-items: center;
}

.empty-state,
.error-state,
.notice-row {
  margin-top: 24px;
  font-size: 14px;
}

.empty-state {
  color: #7f7f7f;
}

.error-state {
  color: #d64545;
  font-weight: 600;
}

.notice-row {
  color: #a08a6c;
}

.loading-indicator {
  margin-top: 20px;
  color: #666;
}

.observer-target {
  width: 100%;
  height: 1px;
}

@media (max-width: 768px) {
  .page-title {
    padding: 12px 40px;
    font-size: 18px;
  }

  .grid-wrapper {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }
}
</style>
