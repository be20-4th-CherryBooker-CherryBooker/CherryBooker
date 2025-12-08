<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-container">

      <!-- 닫기 버튼 -->
      <button class="close-btn" @click="closeModal">✖</button>

      <h2 class="modal-title">글귀 등록하기</h2>

      <!-- 도서 선택 -->
      <div class="section">
        <label class="label">도서 선택</label>

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
        <label class="label">글귀 이미지 업로드</label>

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
        <label class="label">글귀 내용</label>
        <textarea
            class="textarea"
            v-model="content"
            placeholder="OCR 결과 또는 직접 입력하세요."
        ></textarea>
      </div>

      <!-- 버튼 영역 -->
      <div class="btn-row">
        <button class="gray-btn" @click="requestOCR" :disabled="!imageFile">
          이미지 OCR
        </button>

        <button class="primary-btn" @click="submitQuote" :disabled="submitLoading">
          {{ submitLoading ? "등록 중..." : "등록하기" }}
        </button>
      </div>

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

// 모달 닫기
const closeModal = () => emit("close");

// 📌 1) 내 서재 목록 불러오기
const loadUserBooks = async () => {
  try {
    const res = await axios.get("/api/user-books");
    books.value = res.data;
  } catch (e) {
    console.error("도서 목록 불러오기 실패", e);
  }
};

// 📌 2) 이미지 업로드 + 미리보기 처리
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

// 📌 3) OCR 요청 (FastAPI 또는 PaddleOCR 서버)
const requestOCR = async () => {
  if (!imageFile.value) return alert("이미지를 먼저 업로드하세요.");

  const formData = new FormData();
  formData.append("image", imageFile.value);

  try {
    const res = await axios.post("http://localhost:8000/ocr", formData, {
      headers: { "Content-Type": "multipart/form-data" }
    });

    content.value = res.data.text; // OCR로 인식된 글귀
  } catch (e) {
    console.error(e);
    alert("OCR 처리 중 오류 발생");
  }
};

// 📌 4) 글귀 등록 API
const submitQuote = async () => {
  if (!selectedBookId.value) return alert("도서를 선택해주세요.");
  if (!content.value.trim()) return alert("글귀 내용을 입력해주세요.");

  submitLoading.value = true;

  try {
    const body = {
      userBookId: selectedBookId.value,
      content: content.value,
      imagePath: null // 이미지 저장 서버 구축 시 변경 가능
    };

    await axios.post("/api/quotes", body);

    alert("글귀가 등록되었습니다!");
    emit("created");
    emit("close");
  } catch (e) {
    console.error(e);
    alert("등록 중 오류가 발생했습니다.");
  } finally {
    submitLoading.value = false;
  }
};

onMounted(() => {
  loadUserBooks();
});
</script>

<style scoped>
/* 🔥 모달 전체 스타일 */
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
  max-height: 260px;
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

.gray-btn {
  padding: 8px 18px;
  border-radius: 12px;
  border: none;
  background: #ddd;
  cursor: pointer;
}
</style>
