<template>
  <div v-if="show" class="modal-overlay" @click.self="close">
    <div class="modal-container">

      <button class="close-btn" @click="close">✖</button>

      <div class="modal-title">{{ bookDetail?.title || "책 제목" }}</div>

      <div class="content">
        <div class="left-pane">
          <div class="cover-info">
            <img :src="bookDetail?.coverImageUrl || fallbackCover" alt="cover" class="detail-cover" />
            <div class="info-text">
              <p><strong>제목:</strong> {{ bookDetail?.title }}</p>
              <p><strong>저자:</strong> {{ bookDetail?.author }}</p>
              <p><strong>상태:</strong> {{ statusLabel }}</p>
            </div>
          </div>

          <div class="quotes-section">
            <div class="quote-header">글귀</div>
            <div v-if="bookDetail?.quotes?.length" class="quote-list">
              <div
                  v-for="quote in bookDetail.quotes"
                  :key="quote.quoteId"
                  class="quote-item"
              >
                {{ quote.content }}
              </div>
            </div>
            <p v-else class="empty-quote">등록된 글귀가 없습니다.</p>
            <div class="file-row">
              <label class="file-label">
                이미지 OCR
                <input type="file" accept="image/*" @change="handleOcrUpload" />
              </label>
            </div>
          </div>

          <div class="footer">
            <button class="primary-btn" @click="$emit('open-quote-modal', bookDetail)">
              글귀 등록
            </button>
          </div>
        </div>

        <div class="comment-box">
          <textarea
              v-model="comment"
              maxlength="500"
              placeholder="500자"
          ></textarea>
          <div class="comment-actions">
            <button @click="saveComment" :disabled="saving">
              {{ saving ? "저장 중..." : "저장" }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import api from "@/axios";

const props = defineProps({
  show: Boolean,
  book: Object,
});
const emit = defineEmits(["close", "open-quote-modal"]);

const MYLIB_BASE_URL = "/api/mylib";
const myLibApiUrl = (path = "") => `${MYLIB_BASE_URL}${path}`;
const fallbackCover = "/images/default-book.png";

const bookDetail = ref(null);
const comment = ref("");
const saving = ref(false);

const close = () => {
  bookDetail.value = null;
  comment.value = "";
  emit("close");
};

const statusLabel = computed(() => {
  if (!bookDetail.value) return "-";
  const map = { WISH: "읽고 싶은", READING: "읽는 중", READ: "완독" };
  return map[bookDetail.value.bookStatus] || "-";
});

watch(
    () => props.show,
    async (visible) => {
      if (!visible || !props.book) return;
      try {
        const { data } = await api.get(myLibApiUrl(`/books/${props.book.myLibId}/quotes`), {
          withCredentials: true,
        });
        bookDetail.value = data?.data ?? data;
        comment.value = "";
      } catch (error) {
        console.error("[BookDetailModal] 도서 정보를 불러오지 못했습니다.", error);
      }
    }
);

const handleOcrUpload = async (event) => {
  const file = event.target.files?.[0];
  if (!file) return;
  alert("OCR 기능은 추후 연결됩니다.");
};

const saveComment = async () => {
  if (!comment.value.trim()) {
    alert("코멘트를 입력해주세요.");
    return;
  }
  saving.value = true;
  try {
    alert("코멘트 저장 기능은 추후 API 연결 예정입니다.");
    comment.value = "";
  } catch (error) {
    console.error("[BookDetailModal] 코멘트 저장 실패", error);
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-container {
  width: min(900px, 90vw);
  background: #fffaf0;
  border-radius: 28px;
  padding: 28px 32px;
  position: relative;
  box-shadow: 0 20px 35px rgba(0, 0, 0, 0.2);
}

.close-btn {
  position: absolute;
  top: 14px;
  right: 14px;
  border: none;
  background: none;
  font-size: 24px;
  cursor: pointer;
}

.modal-title {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  color: #df3e3e;
  border: 2px solid #df3e3e;
  border-radius: 30px;
  display: inline-block;
  padding: 10px 40px;
  margin: 0 auto 20px auto;
}

.content {
  display: flex;
  gap: 24px;
}

.left-pane {
  width: 45%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cover-info {
  display: flex;
  gap: 18px;
  align-items: center;
}

.detail-cover {
  width: 150px;
  border-radius: 12px;
}

.info-text p {
  margin: 4px 0;
  font-size: 14px;
}

.quotes-section {
  background: #fff;
  border-radius: 20px;
  padding: 14px;
  border: 1px solid #f2ddba;
}

.quote-header {
  font-weight: 600;
  margin-bottom: 8px;
}

.quote-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quote-item {
  background: #fff7ea;
  border-radius: 12px;
  padding: 10px;
  border: 1px solid #f2ddba;
}

.empty-quote {
  color: #9c8b74;
  font-size: 14px;
}

.file-row {
  margin-top: 12px;
}

.file-label {
  display: inline-flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #7a6a54;
}

.file-label input {
  font-size: 13px;
}

.comment-box {
  flex: 1;
  background: #fff6e3;
  border-radius: 20px;
  padding: 16px;
  border: 1px solid #f0c98b;
}

.comment-box textarea {
  width: 100%;
  height: 240px;
  border: none;
  background: transparent;
  resize: none;
  font-family: inherit;
  outline: none;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
}

.comment-actions button {
  margin-top: 8px;
  padding: 6px 18px;
  border-radius: 18px;
  border: none;
  background: #ffcf7c;
  cursor: pointer;
}

.footer {
  text-align: right;
}

.primary-btn {
  border: none;
  background: #ffcf7c;
  border-radius: 20px;
  padding: 10px 24px;
  cursor: pointer;
}
</style>
