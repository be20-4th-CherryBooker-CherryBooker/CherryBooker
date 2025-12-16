<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-container">

      <!-- 닫기 버튼 -->
      <button class="close-btn" @click="closeModal">✖</button>

      <h2 class="modal-title">글귀 등록하기</h2>

      <!-- 도서 선택 -->
      <div class="section">
        <label class="label">📚 도서 선택</label>

        <div class="dropdown">
          <select v-model="selectedBookId" class="select-box">
            <option disabled value="">내 도서 목록 보기</option>
            <option v-for="book in books" :key="book.userBookId" :value="book.userBookId">
              {{ book.bookTitle }}
            </option>
          </select>
        </div>
      </div>

      <!-- 이미지 업로드 -->
      <div class="section">
        <label class="label">📸 글귀 이미지 업로드</label>

        <div class="upload-wrapper">
          <input type="file" @change="handleImageUpload" accept="image/*">
        </div>

        <!-- 이미지 미리보기 -->
        <div v-if="previewImage" class="preview-img-box">
          <img :src="previewImage" class="preview-img" />
        </div>
      </div>

      <!-- OCR 텍스트 결과 OR 직접 입력 -->
      <div class="section">
        <label class="label">📃 글귀 내용</label>
        <textarea
            class="textarea"
            v-model="content"
            placeholder="추출된 글귀가 입력됩니다."
        ></textarea>
      </div>

      <!-- 버튼 영역 -->
      <div class="btn-row">
        <button class="ocr-btn" @click="requestOCR" :disabled="!imageFile">
          이미지 OCR
        </button>

        <button class="primary-btn" @click="submitQuote" :disabled="submitLoading">
          {{ submitLoading ? "등록 중..." : "등록하기" }}
        </button>
      </div>

    </div>

    <div v-if="isOcrLoading" class="loading-overlay">
      <img src="/images/loading.gif" class="loading-img" />
      <p>OCR 추출 중...</p>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";

// props & emits
const props = defineProps({
  show: Boolean
});
const emit = defineEmits(["close", "created"]);

// 도서 목록
const books = ref([]);
const selectedBookId = ref("");

// 이미지 관련
const imageFile = ref(null);
const previewImage = ref("");

// 글귀 내용
const content = ref("");

// 로딩 상태
const submitLoading = ref(false);

// OCR 로딩 상태
const isOcrLoading = ref(false);

// 모달 닫기
const closeModal = () => {
  resetForm();
  emit("close");
};

// JWT에서 userId 추출
function parseJwt(token) {
  try {
    const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(decodeURIComponent(escape(window.atob(base64))));
  } catch (e) {
    console.error("JWT 파싱 실패", e);
    return null;
  }
}

function getUserIdFromToken() {
  const token = localStorage.getItem("accessToken");
  if (!token) return null;
  const payload = parseJwt(token);
  return payload?.sub ?? null; // sub 에 userId 들어있다고 가정
}

// 1) 내 서재 목록 불러오기
const loadUserBooks = async () => {
  try {
    const token = localStorage.getItem("accessToken");
    const userId = getUserIdFromToken();

    if (!token || !userId) {
      console.error("토큰 또는 userId 없음");
      return;
    }

    const res = await axios.get("/api/mylib/books", {
      params: {
        userId,
        page: 0,
        size: 50
      },
      headers: { Authorization: `Bearer ${token}` }
    });

    const rawList = res.data?.data?.books || [];

    books.value = rawList.map((item) => ({
      userBookId: item.myLibId,
      bookTitle: item.title,
      author: item.author,
      coverImageUrl: item.coverImageUrl,
      status: item.status
    }));
  } catch (e) {
    console.error("도서 목록 불러오기 실패", e);
    alert("도서 목록을 불러오는 중 오류가 발생했습니다.");
  }
};

// 2) 이미지 업로드 + 미리보기 처리
const handleImageUpload = (e) => {
  const file = e.target.files[0];
  if (!file) return;

  imageFile.value = file;

  const reader = new FileReader();
  reader.onload = () => {
    previewImage.value = reader.result;
  };
  reader.readAsDataURL(file);
};

const ensureBookReadingStatus = async (userBookId, token) => {
  if (!userBookId || !token) return;

  const targetBook = books.value.find((book) => book.userBookId === userBookId);
  const status = targetBook?.status;

  if (!targetBook || status === "READING" || status === "READ") {
    return;
  }
  if (status !== "WISH") {
    return;
  }

  try {
    await axios.patch(
        `/api/mylib/books/${userBookId}/status`,
        {
          targetStatus: "READING",
          trigger: "QUOTE_CAPTURE"
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        }
    );

    targetBook.status = "READING";
  } catch (error) {
    console.error("책 상태 변경 실패", error);
  }
};

// 3) OCR 요청 (FastAPI 또는 PaddleOCR 서버)
const requestOCR = async () => {
  if (!imageFile.value) return alert("이미지를 먼저 업로드하세요.");

  const formData = new FormData();
  formData.append("file", imageFile.value);

  try {
    isOcrLoading.value = true;

    const res = await axios.post("http://localhost:8000/ocr", formData, {
      headers: { "Content-Type": "multipart/form-data" }
    });

    content.value = res.data.full_text;
  } catch (e) {
    console.error(e);
    alert("OCR 처리 중 오류 발생");
  } finally {
    isOcrLoading.value = false;
  }
};

// 4) 글귀 등록 API + 이미지 업로드 연동
const submitQuote = async () => {
  if (!selectedBookId.value) {
    alert("도서를 선택해주세요.");
    return;
  }
  if (!content.value.trim()) {
    alert("글귀 내용을 입력해주세요.");
    return;
  }

  submitLoading.value = true;

  try {
    const token = localStorage.getItem("accessToken");
    const userId = getUserIdFromToken();

    if (!token || !userId) {
      alert("로그인 정보가 없습니다. 다시 로그인해주세요.");
      return;
    }

    let uploadedPath = null;

    // ✅ 4-1) 이미지가 있다면 업로드 먼저
    if (imageFile.value) {
      const formData = new FormData();
      formData.append("file", imageFile.value);

      const uploadRes = await axios.post("/api/files/upload", formData, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "multipart/form-data"
        }
      });

      uploadedPath =
          uploadRes.data?.data?.filePath ??
          uploadRes.data?.filePath ??
          null;
    }

    const selectedBook = books.value.find(
        (b) => b.userBookId === selectedBookId.value
    );

    if (!selectedBook) {
      alert("선택한 도서를 찾을 수 없습니다.");
      return;
    }

    // ✅ 4-2) 백엔드 기준: userId를 body에 포함
    const body = {
      userId,
      userBookId: selectedBookId.value,
      content: content.value,
      imagePath: uploadedPath,
      bookTitle: selectedBook.bookTitle,
      author: selectedBook.author
    };

    await axios.post("/api/quotes", body, {
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json"
      }
    });

    await ensureBookReadingStatus(selectedBookId.value, token);

    alert("글귀가 등록되었습니다!");
    emit("created");
    closeModal();
  } catch (e) {
    console.error("❌ 글귀 등록 실패", e);
    alert("등록 중 오류가 발생했습니다.");
  } finally {
    submitLoading.value = false;
  }
};

// 창 닫으면 리셋
const resetForm = () => {
  selectedBookId.value = "";
  imageFile.value = null;
  previewImage.value = "";
  content.value = "";
};

onMounted(() => {
  loadUserBooks();
});
</script>

<style scoped>
/* 모달 전체 스타일 */
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
  width: 480px;
  background: white;
  border-radius: 18px;
  padding: 28px;
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

/* 제목 */
.modal-title {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 18px;
}

/* 섹션 */
.section {
  margin-top: 18px;
  text-align: left;
}

.label {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 6px;
  display: block;
}

.select-box {
  width: 100%;
  padding: 10px;
  border-radius: 12px;
  border: 1px solid #ddd;
  background: #fafafa;
}

/* 이미지 업로드 */
.upload-wrapper input {
  padding: 6px 0;
}

.preview-img-box {
  margin-top: 10px;
  text-align: center;
}

.preview-img {
  width: 100%;
  max-height: 200px;
  border-radius: 10px;
  object-fit: cover;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

/* 텍스트 입력 */
.textarea {
  width: 100%;
  height: 120px;
  border-radius: 12px;
  border: 1px solid #ccc;
  padding: 10px;
  background: #fafafa;
  resize: none;
}

/* 버튼 */
.btn-row {
  margin-top: 22px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.primary-btn {
  padding: 8px 18px;
  border-radius: 12px;
  border: none;
  background: #ff9f3e;
  color: white;
  font-weight: 600;
  cursor: pointer;
}

.ocr-btn {
  padding: 8px 18px;
  border-radius: 12px;
  border: none;
  background: #ff8d8d;
  color: white;
  font-weight: 600;
  cursor: pointer;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255,255,255,0.85);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-radius: 18px;
  z-index: 3000;
}

.loading-img {
  width: 80px;
  height: 80px;
}

.loading-overlay p {
  margin-top: 10px;
  color: #333;
  font-size: 14px;
  font-weight: 600;
}

</style>
