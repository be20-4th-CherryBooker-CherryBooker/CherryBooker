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
            <div v-if="quotes.length" class="quote-list">
              <div
                  v-for="quote in quotes"
                  :key="quote.quoteId"
                  :class="['quote-item', { active: quote.quoteId === activeQuoteId }]"
                  @click="selectQuote(quote)"
              >
                <div class="quote-content">{{ quote.content }}</div>
                <div v-if="quote.comment" class="quote-comment-preview">
                  ✏️ {{ quote.comment }}
                </div>
                <div class="quote-meta">{{ formatQuoteDate(quote.createdAt) }}</div>
              </div>
            </div>
            <p v-else class="empty-quote">등록된 글귀가 없습니다.</p>
            <div class="file-row">
              <label class="file-label" :class="{ disabled: ocrLoading }">
                {{ ocrLoading ? "OCR 처리 중..." : "이미지 OCR" }}
                <input
                    type="file"
                    accept="image/*"
                    @change="handleOcrUpload"
                    :disabled="ocrLoading"
                />
              </label>
              <p class="ocr-hint">이미지 텍스트를 추출해 글귀로 저장합니다.</p>
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
              :disabled="!activeQuoteId"
              :placeholder="activeQuoteId ? '코멘트를 입력해주세요 (500자)' : '왼쪽에서 코멘트를 수정할 글귀를 선택하세요.'"
          ></textarea>
          <div class="comment-actions">
            <div v-if="activeQuote" class="comment-target">
              선택된 글귀: {{ truncate(activeQuote.content, 30) }}
            </div>
            <button @click="saveComment" :disabled="saving || !activeQuoteId">
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
import { storeToRefs } from "pinia";
import api from "@/axios";
import { useAuthStore } from "@/stores/AuthStore";

const props = defineProps({
  show: Boolean,
  book: Object,
});
const emit = defineEmits(["close", "open-quote-modal", "status-updated"]);

const MYLIB_BASE_URL = "/api/mylib";
const myLibApiUrl = (path = "") => `${MYLIB_BASE_URL}${path}`;
const fallbackCover = "/images/default-book.png";

const bookDetail = ref(null);
const comment = ref("");
const saving = ref(false);
const ocrLoading = ref(false);
const activeQuoteId = ref(null);

const close = () => {
  bookDetail.value = null;
  comment.value = "";
  activeQuoteId.value = null;
  emit("close");
};

const statusLabel = computed(() => {
  if (!bookDetail.value) return "-";
  const status = bookDetail.value.bookStatus ?? bookDetail.value.status;
  const map = { WISH: "읽고 싶은", READING: "읽는 중", READ: "완독" };
  return map[status] || "-";
});

const authStore = useAuthStore();
const { user } = storeToRefs(authStore);

const quotes = computed(() => bookDetail.value?.quotes ?? []);
const activeQuote = computed(() =>
    quotes.value.find((quote) => quote.quoteId === activeQuoteId.value) || null
);

watch(
    () => props.show,
    async (visible) => {
      if (!visible) {
        bookDetail.value = null;
        comment.value = "";
        activeQuoteId.value = null;
        return;
      }
      if (!props.book) return;
      await fetchBookDetail();
    }
);

const fetchBookDetail = async (focusQuoteId = null) => {
  if (!props.book?.myLibId) return;
  try {
    const { data } = await api.get(myLibApiUrl(`/books/${props.book.myLibId}/quotes`), {
      withCredentials: true,
    });
    bookDetail.value = data?.data ?? data;
    initializeQuoteSelection(focusQuoteId);
  } catch (error) {
    console.error("[BookDetailModal] 도서 정보를 불러오지 못했습니다.", error);
  }
};

const initializeQuoteSelection = (focusQuoteId = null) => {
  const quoteList = quotes.value;
  if (!quoteList.length) {
    activeQuoteId.value = null;
    comment.value = "";
    return;
  }
  const targetQuote =
      quoteList.find((quote) => quote.quoteId === focusQuoteId) ?? quoteList[0];
  activeQuoteId.value = targetQuote.quoteId;
  comment.value = targetQuote.comment || "";
};

const selectQuote = (quote) => {
  if (!quote) return;
  activeQuoteId.value = quote.quoteId;
  comment.value = quote.comment || "";
};

const formatQuoteDate = (date) => {
  if (!date) return "";
  const parsed = new Date(date);
  if (Number.isNaN(parsed.getTime())) return "";
  return parsed.toLocaleDateString();
};

const truncate = (text, length) => {
  if (!text) return "";
  return text.length > length ? `${text.slice(0, length)}…` : text;
};

const changeBookStatusToReading = async () => {
  if (!bookDetail.value?.myLibId) return;
  const status = bookDetail.value.bookStatus ?? bookDetail.value.status;
  if (status !== "WISH") return;

  try {
    await api.patch(
        myLibApiUrl(`/books/${bookDetail.value.myLibId}/status`),
        {
          targetStatus: "READING",
          trigger: "QUOTE_CAPTURE"
        },
        { withCredentials: true }
    );

    bookDetail.value = {
      ...bookDetail.value,
      bookStatus: "READING",
      status: "READING"
    };

    emit("status-updated", {
      myLibId: bookDetail.value.myLibId,
      status: "READING"
    });
  } catch (error) {
    console.error("[BookDetailModal] 책 상태 변경 실패", error);
  }
};

const handleOcrUpload = async (event) => {
  const file = event.target.files?.[0];
  if (!file) return;

  if (!bookDetail.value?.myLibId) {
    alert("도서 정보를 불러오지 못했습니다. 다시 시도해주세요.");
    return;
  }

  const userId = user.value?.userId || user.value?.id;
  if (!userId) {
    alert("로그인 정보가 없습니다. 다시 로그인해주세요.");
    return;
  }

  const formData = new FormData();
  formData.append("file", file);

  try {
    ocrLoading.value = true;

    const uploadRes = await api.post("/api/files/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" }
    });

    const uploadedImagePath =
        uploadRes.data?.data?.imagePath ??
        uploadRes.data?.imagePath ??
        null;

    if (!uploadedImagePath) {
      throw new Error("업로드된 이미지 경로를 확인할 수 없습니다.");
    }

    const ocrRes = await api.post("/api/ocr/extract", {
      imagePath: uploadedImagePath
    });

    const extractedText =
        ocrRes.data?.data?.full_text ??
        ocrRes.data?.full_text ??
        ocrRes.data?.data?.fullText ??
        ocrRes.data?.fullText ??
        "";

    if (!extractedText.trim()) {
      alert("추출된 텍스트가 없습니다. 다른 이미지를 시도해주세요.");
      return;
    }

    const createPayload = {
      userId,
      userBookId: bookDetail.value.myLibId,
      content: extractedText,
      imagePath: uploadedImagePath,
      bookTitle: bookDetail.value.title,
      author: bookDetail.value.author
    };

    const createRes = await api.post("/api/quotes", createPayload);
    const createdQuoteId =
        createRes.data?.data?.quoteId ??
        createRes.data?.quoteId ??
        null;

    await changeBookStatusToReading();
    await fetchBookDetail(createdQuoteId);
    alert("OCR 결과로 글귀가 등록되었습니다.");
  } catch (error) {
    console.error("[BookDetailModal] OCR 업로드 실패", error);
    const message =
        error.response?.data?.message ??
        error.message ??
        "OCR 처리 중 오류가 발생했습니다.";
    alert(message);
  } finally {
    ocrLoading.value = false;
    if (event?.target) {
      event.target.value = "";
    }
  }
};

const saveComment = async () => {
  if (!activeQuoteId.value) {
    alert("코멘트를 수정할 글귀를 선택해주세요.");
    return;
  }
  if (!comment.value.trim()) {
    alert("코멘트를 입력해주세요.");
    return;
  }
  saving.value = true;
  try {
    await api.patch(`/api/quotes/${activeQuoteId.value}/comment`, {
      comment: comment.value
    });

    if (bookDetail.value) {
      bookDetail.value = {
        ...bookDetail.value,
        quotes: quotes.value.map((quote) =>
            quote.quoteId === activeQuoteId.value
                ? { ...quote, comment: comment.value }
                : quote
        )
      };
    }

    alert("코멘트가 저장되었습니다.");
  } catch (error) {
    console.error("[BookDetailModal] 코멘트 저장 실패", error);
    alert(error.response?.data?.message || "코멘트를 저장하는 중 오류가 발생했습니다.");
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
  padding: 12px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}

.quote-item.active {
  border-color: #ff9f4c;
  background: #fff0d9;
}

.quote-content {
  font-weight: 600;
  font-size: 14px;
}

.quote-comment-preview {
  margin-top: 6px;
  font-size: 13px;
  color: #675a46;
}

.quote-meta {
  margin-top: 6px;
  font-size: 12px;
  color: #a28f76;
}

.empty-quote {
  color: #9c8b74;
  font-size: 14px;
  margin: 16px 0;
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
  background: #ffe0ba;
  padding: 10px 12px;
  border-radius: 16px;
  cursor: pointer;
}

.file-label.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.file-label input {
  font-size: 13px;
}

.file-label input[disabled] {
  cursor: not-allowed;
}

.ocr-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #a68c65;
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
  padding: 6px;
}

.comment-box textarea:disabled {
  color: #b5a690;
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.comment-target {
  font-size: 13px;
  color: #7a6a54;
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
