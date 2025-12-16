<template>
  <div class="home-container">

    <!-- 검색창 -->
    <div class="search-box" @click="openSearchModal">
      <input
          type="text"
          placeholder="읽고 싶은 책을 검색해보세요 !"
          class="search-input"
          readonly
      />
      <span class="search-emoji">🔍</span>
    </div>

    <!-- 검색 모달 -->
    <SearchBookModal
        :show="showSearchModal"
        @close="showSearchModal = false"
        @selected="goToRegisterPage"
    />

    <!-- 선반 + 체리 영역 -->
    <div class="illustration-wrapper">

      <div class="shelf-area">
        <img src="/images/shelf.png" class="shelf-img" />
        <img src="/images/default-book.png" class="book-img" />

        <!-- 체리 캐릭터 -->
        <img src="/images/character1.png" class="cherry-img" />

        <div class="bubble">
          책 완주를 위하여~
        </div>
      </div>

    </div>

  </div>
</template>

<script setup>
import { ref } from "vue";
import SearchBookModal from "@/components/book/SearchBookModal.vue";

// 모달 상태
const showSearchModal = ref(false);

// 검색창 클릭 → 모달 열기
const openSearchModal = () => {
  showSearchModal.value = true;
};

// 모달에서 책 선택됨 → 책 등록 페이지로 이동시키거나 원하는 처리 가능
const goToRegisterPage = (book) => {
  console.log("선택된 책:", book);

  showSearchModal.value = false;
};
</script>

<style scoped>
.home-container {
  width: 100%;
  padding-top: 80px;
  text-align: center;
  font-family: "Pretendard", sans-serif;
}

/* 검색창 */
.search-box {
  width: 600px;
  height: 48px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  background: white;
  border-radius: 30px;
  padding: 0 20px;
  border: 1px solid #f1e4d0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: #666;
}

.search-emoji {
  font-size: 20px;
  cursor: pointer;
}

/* 메인 일러스트 영역 */
.illustration-wrapper {
  margin-top: 60px;
  display: flex;
  justify-content: center;
}

/* 선반 컨테이너 */
.shelf-area {
  position: relative;
  width: 500px;
}

/* 선반 */
.shelf-img {
  width: 100%;
}

.book-img {
  position: absolute;
  bottom: 165px;
  left: 32%;
  width: 120px;
  transform: translateX(-50%);
}

/* 체리를 선반 위로 위치시키기 */
.cherry-img {
  position: absolute;
  bottom: 120px; /* 선반 위로 올리는 핵심 */
  left: 70%;
  transform: translateX(-50%);
  width: 260px;
}

/* 말풍선 */
.bubble {
  position: absolute;
  top: -10px;
  right: -120px;

  background: #ffffff;
  border: 2px solid #ff6a6a;
  padding: 12px 20px;
  border-radius: 20px;
  font-size: 14px;
  color: #ff4b4b;
  white-space: nowrap;
  box-shadow: 0 4px 10px rgba(255, 88, 88, 0.15);
}
</style>
