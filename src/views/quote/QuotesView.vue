<template>
  <div class="quotes-container">

    <!-- 상단 제목 -->
    <div class="page-title">
      <span>내 글귀</span>
    </div>

    <p class="subtitle">내가 남긴 모든 글귀들을 확인해보세요!</p>

    <!-- 검색창 + 버튼 -->
    <div class="search-row">
      <div class="left-space"></div>

      <div class="center-box">
        <input
            v-model="keyword"
            type="text"
            class="search-input"
            placeholder="책 제목"
        />
        <button class="search-btn" @click="searchQuotes">검색하기</button>
      </div>

      <div class="add-btn-box">
        <button class="add-btn" @click.stop="openCreateModal">추가하기</button>
      </div>
    </div>

    <!-- 글귀 카드 리스트 -->
    <div class="quotes-grid">
      <div
          class="quote-card"
          v-for="quote in quotes"
          :key="quote.quoteId"
      >
        <div class="quote-title">📚 {{ quote.bookTitle }} – {{ quote.author }}</div>

        <div class="quote-content">
          "{{ quote.content }}"
        </div>

        <div class="detail-btn" @click="openDetail(quote)">
          +
        </div>
      </div>
    </div>

    <!-- 무한 스크롤의 관찰 대상 -->
    <div id="infinite-observer"></div>

    <!-- 글귀 등록 모달 -->
    <QuoteCreateModal
        :show="showCreateModal"
        @close="showCreateModal = false"
        @created="refreshQuotes"
    />

    <!-- 글귀 자세히보기 모달 -->
    <QuoteDetailModal
        v-if="showDetailModal"
        :quote="selectedQuote"
        @close="showDetailModal = false"
        @delete="deleteQuote"
        @edit="editQuote"
    />

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import QuoteCreateModal from "@/components/quote/QuoteCreateModal.vue";
import QuoteDetailModal from "@/components/quote/QuoteDetailModal.vue";

const router = useRouter();
const showDetailModal = ref(false);
const selectedQuote = ref(null);

// 모달 상태
const showCreateModal = ref(false);

const openCreateModal = () => {
  showCreateModal.value = true;
};

// 무한 스크롤 상태
const quotes = ref([]);
const page = ref(0);
const size = 9;
const isLast = ref(false);
const isLoading = ref(false);

const keyword = ref("");

// API 요청 (백엔드 주소만 바꿔주면 됨)
const loadQuotes = async () => {
  if (isLast.value || isLoading.value) return;

  isLoading.value = true;

  const res = await fetch(`/api/quotes/my?page=${page.value}&size=${size}&keyword=${keyword.value || ""}`);
  const data = await res.json();

  quotes.value.push(...data.content);
  isLast.value = data.last;

  page.value++;
  isLoading.value = false;
};

// 검색
const searchQuotes = () => {
  quotes.value = [];
  page.value = 0;
  isLast.value = false;
  loadQuotes();
};

// 글귀 상세보기 모달
const openDetail = (quote) => {
  selectedQuote.value = quote;
  showDetailModal.value = true;
};

// 글귀 등록 후 목록 새로고침
const refreshQuotes = () => {
  quotes.value = [];
  page.value = 0;
  isLast.value = false;
  loadQuotes();
};

// 삭제
const deleteQuote = async (quoteId) => {
  if (!confirm("정말 삭제하시겠습니까?")) return;

  await fetch(`/api/quotes/${quoteId}`, { method: "DELETE" });

  showDetailModal.value = false;
  refreshQuotes();
};

// 수정
const editQuote = async (quote) => {
  const newComment = prompt("새로운 코멘트를 입력하세요", quote.comment || "");

  if (newComment === null) return;

  await fetch(`/api/quotes/${quote.quoteId}/comment`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ comment: newComment }),
  });

  alert("코멘트가 수정되었습니다.");

  showDetailModal.value = false;
  refreshQuotes();
};

// IntersectionObserver 등록
onMounted(() => {
  const target = document.querySelector("#infinite-observer");

  const observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting) {
      loadQuotes();
    }
  });

  observer.observe(target);
  loadQuotes();
});
</script>

<style scoped>
/* 전체 레이아웃 */
.quotes-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 30px 20px;
  font-family: "Pretendard", sans-serif;
  text-align: center;
}

#infinite-observer {
  height: 1px;
}

/* 제목 */
.page-title {
  display: inline-block;
  margin: 20px auto 10px auto;
  padding: 14px 70px;
  border: 2px solid #df3e3e;
  border-radius: 40px;

  font-size: 20px;
  font-weight: 600;
  color: #df3e3e;
  text-align: center;

  background: #ffffff;
  box-shadow: 0 4px 10px rgba(223, 62, 62, 0.15);

  font-family: "Pretendard", sans-serif;
}

/* 부제 */
.subtitle {
  text-align: center;
  font-size: 14px;
  color: #777;
  margin-top: 10px;
}

/* 검색창 + 버튼 */
.search-row {
  display: flex;
  align-items: center;
  margin-top: 25px;
  width: 100%;
}

.left-space {
  flex: 1;
}

/* 가운데 검색 영역 */
.center-box {
  flex: 2;
  display: flex;
  justify-content: center;
  gap: 6px;
}

.search-input {
  width: 240px;
  height: 32px;
  border: 1px solid #ddd;
  border-radius: 16px;
  padding: 0 12px;
  background: #fafafa;
}

.search-btn {
  height: 32px;
  padding: 0 14px;
  border: none;
  border-radius: 16px;
  background: #ffd892;
  cursor: pointer;
  font-size: 13px;
}

/* 추가하기 버튼 */
.add-btn-box {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.add-btn {
  height: 32px;
  padding: 0 14px;
  border: none;
  border-radius: 16px;
  background: #ffa83d;
  color: white;
  cursor: pointer;
  font-size: 13px;
  white-space: nowrap;
}

/* 그리드 */
.quotes-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
  margin-top: 30px;
}

/* 카드 */
.quote-card {
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.07);
  transition: 0.2s;
  cursor: pointer;
}

.quote-card:hover {
  transform: translateY(-4px);
}

.quote-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #333;
}

.quote-content {
  font-size: 13px;
  color: #555;
  line-height: 1.5;
  height: 70px;
  overflow: hidden;
}

.detail-btn {
  font-size: 22px;
  font-weight: bold;
  color: #ffa83d;
  text-align: center;
  margin-top: 10px;
  cursor: pointer;
}
</style>
