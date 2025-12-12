<template>
  <div class="modal-overlay" @click="close">
    <div class="modal-box" @click.stop>

      <!-- 상단 영역 -->
      <div class="header">
        <button class="back-btn" @click="close">←</button>
        <div class="title">Quote</div>
        <div class="action-btns">
          <button class="delete-btn" @click="emitDelete">삭제</button>
          <button class="edit-btn" @click="emitEdit">수정</button>
        </div>
      </div>

      <!-- 책 정보 -->
      <div class="book-section">
        <img :src="quote.coverImageUrl || quote.imagePath || defaultImg" class="book-image"/>

        <div class="book-info">
          <div class="book-title">{{ quote.bookTitle }}</div>
          <div class="book-author">{{ quote.author }}</div>
        </div>
      </div>

      <!-- 글귀 내용 -->
      <div class="quote-box">
        {{ quote.content }}
      </div>

      <!-- 나의 코멘트 -->
      <div class="comment-section">
        <div class="comment-title"> 나의 코멘트 </div>

        <div class="comment-box">
          {{ quote.comment || "등록된 코멘트가 없습니다." }}
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from "vue";

const props = defineProps({
  show: Boolean,
  quote: Object
});

const emit = defineEmits(["close", "delete", "edit"]);

const close = () => emit("close");
const emitDelete = () => emit("delete", props.quote.quoteId);
const emitEdit = () => emit("edit", props.quote);

const defaultImg = "/images/default-book.png"; // 기본 책 이미지
</script>

<style scoped>
/* 전체 오버레이 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.35);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

/* 모달 박스 */
.modal-box {
  width: 400px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.2);
}

/* 헤더 */
.header {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  margin-bottom: 12px;
}

.back-btn {
  justify-self: start;
  font-size: 20px;
  background: none;
  border: none;
  cursor: pointer;
}

.title {
  justify-self: center;
  font-size: 20px;
  font-weight: 700;
}

.action-btns {
  justify-self: end;
  display: flex;
  gap: 6px;
}

.action-btns button {
  border: none;
  padding: 6px 12px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 13px;
  background: #eee;
}

.action-btns .delete-btn {
  background: #ff8d8d;
}

.action-btns .edit-btn {
  background: #ffe19a;
}

/* 책 정보 */
.book-section {
  display: flex;
  margin-top: 14px;
}

.book-image {
  width: 90px;
  height: 120px;
  border-radius: 6px;
  object-fit: cover;
}

.book-info {
  margin-left: 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.book-title {
  font-size: 16px;
  font-weight: 600;
}

.book-author {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

/* 글귀 박스 */
.quote-box {
  background: #f8f8f8;
  padding: 14px;
  border-radius: 12px;
  margin-top: 16px;
  font-size: 14px;
  line-height: 1.5;
}

/* 나의 코멘트 */
.comment-section {
  margin-top: 20px;
}

.comment-title {
  font-size: 15px;
  margin-bottom: 8px;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.comment-title::before {
  content: "↳";
  color: #ffb347;
  font-size: 18px;
  margin-right: 5px;
}

.comment-box {
  background: #fff1ce;
  padding: 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
}
</style>
