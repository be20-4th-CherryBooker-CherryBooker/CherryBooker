<!-- src/components/thread/ThreadCreateModal.vue -->
<template>
  <div class="modal-backdrop" @click.self="close">
    <div class="modal-panel">
      <!-- 헤더 -->
      <div class="modal-header">
        <span class="modal-title">{{ modalTitle }}</span>
        <button class="close-btn" @click="close">×</button>
      </div>


      <!-- 도서 선택 -->
      <section class="section">
        <div class="section-label">📚 도서 선택</div>

        <div class="select-box" @click="toggleBookList">
          <span class="select-label">
            <template v-if="loadingBooks">도서 목록 불러오는 중...</template>
            <template v-else>{{ selectedBookLabel }}</template>
          </span>
          <span class="select-arrow">▾</span>
        </div>

        <div v-if="showBookList" class="option-list">
          <div
              v-for="book in books"
              :key="book.myLibId"
              class="option-item"
              @click="selectBook(book)"
          >
            {{ book.title }} <span class="option-sub">({{ book.author }})</span>
          </div>

          <div v-if="!books.length && !loadingBooks" class="option-empty">
            내 서재에 등록된 도서가 없습니다.
          </div>
        </div>
      </section>

      <!-- 글귀 선택 -->
      <section class="section">
        <div class="section-label">📖 글귀 선택</div>

        <div
            class="select-box"
            :class="{ disabled: !selectedBookId }"
            @click="toggleQuoteList"
        >
          <span class="select-label">
            <template v-if="!selectedBookId">
              도서를 먼저 선택해주세요
            </template>
            <template v-else-if="loadingQuotes">
              글귀 목록 불러오는 중...
            </template>
            <template v-else>
              {{ selectedQuoteLabel }}
            </template>
          </span>
          <span class="select-arrow">▾</span>
        </div>

        <div v-if="showQuoteList" class="option-list quote-list">
          <div
              v-for="quote in quotes"
              :key="quote.quoteId"
              class="option-item"
              @click="selectQuote(quote)"
          >
            “{{ quote.content }}”
          </div>

          <div v-if="!quotes.length && !loadingQuotes" class="option-empty">
            선택한 도서에 등록된 글귀가 없습니다.
          </div>
        </div>
      </section>

      <!-- 푸터 -->
      <div class="modal-footer">
        <button
            class="submit-btn"
            :disabled="!selectedQuoteId || creating"
            @click="submit"
        >
          {{ creating ? '등록 중...' : '등록하기' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { fetchMyBooks, fetchMyBookQuotes } from '@/api/mylibApi'
import {
  createThread,
  createThreadReply,
  updateThread,
  updateReply,
} from '@/api/threadApi'

// 모드 + 수정 여부
const props = defineProps({
  // 'root' = 스레드, 'reply' = 답변
  mode: {
    type: String,
    default: 'root',
  },
  // reply 생성 시 부모 스레드 ID, root 수정 시에도 재사용 가능
  threadId: {
    type: Number,
    default: null,
  },
  // 수정 모드 여부
  isEdit: {
    type: Boolean,
    default: false,
  },
  // 실제 수정 대상 ID (없으면 threadId 사용)
  targetId: {
    type: Number,
    default: null,
  },
})

const emit = defineEmits(['close', 'created'])

const books = ref([])
const quotes = ref([])
const selectedBookId = ref(null)
const selectedQuoteId = ref(null)

const showBookList = ref(false)
const showQuoteList = ref(false)

const loadingBooks = ref(false)
const loadingQuotes = ref(false)
const creating = ref(false)

const selectedBookLabel = computed(() => {
  if (!selectedBookId.value) return '내 도서 목록 보기'
  const book = books.value.find(b => b.myLibId === selectedBookId.value)
  return book ? book.title : '선택된 도서'
})

const selectedQuoteLabel = computed(() => {
  if (!selectedBookId.value) return '도서를 먼저 선택해주세요'
  if (!selectedQuoteId.value) return '내 글귀 목록 보기'
  const quote = quotes.value.find(q => q.quoteId === selectedQuoteId.value)
  return quote ? quote.content : '선택된 글귀'
})

// 모달 제목
const modalTitle = computed(() => {
  if (props.isEdit) {
    return props.mode === 'root' ? '스레드 수정하기' : '스레드 답변 수정하기'
  }
  return props.mode === 'root' ? '스레드 등록하기' : '스레드 답변 등록하기'
})


// 도서 리스트 로딩
const loadBooks = async () => {
  loadingBooks.value = true
  try {
    // BookStatus.READ 만 노출하고 싶다면 status: 'READ'
    const data = await fetchMyBooks({ status: 'READ', page: 0, size: 100 })
    books.value = data?.books ?? []
  } catch (e) {
    console.error('내 서재 도서 조회 실패', e)
    alert('내 서재 도서를 불러오지 못했습니다.')
  } finally {
    loadingBooks.value = false
  }
}

// 도서 선택 시 해당 책의 글귀 로딩
const selectBook = async (book) => {
  selectedBookId.value = book.myLibId
  selectedQuoteId.value = null
  quotes.value = []
  showBookList.value = false

  loadingQuotes.value = true
  try {
    const data = await fetchMyBookQuotes(book.myLibId)
    // MyBookDetailResponse: { quotes: QuoteSnippetResponse[] }
    quotes.value = data?.quotes ?? []
  } catch (e) {
    console.error('도서 글귀 조회 실패', e)
    alert('선택한 도서의 글귀를 불러오지 못했습니다.')
  } finally {
    loadingQuotes.value = false
  }
}

const selectQuote = (quote) => {
  selectedQuoteId.value = quote.quoteId
  showQuoteList.value = false
}

const submit = async () => {
  if (!selectedQuoteId.value) return

  creating.value = true
  try {
    const quotePayload = { quoteId: selectedQuoteId.value }

    if (props.isEdit) {
      // 수정 모드
      const targetId = props.targetId ?? props.threadId
      if (!targetId) {
        throw new Error('edit mode 에서는 targetId 또는 threadId 가 필요합니다.')
      }

      if (props.mode === 'root') {
        // CMT-002: 스레드 수정
        await updateThread(targetId, quotePayload)
      } else {
        // CMT-008: 답변 수정
        await updateReply(targetId, quotePayload)
      }
    } else {
      // 생성 모드
      if (props.mode === 'root') {
        // CMT-001: 스레드 생성
        await createThread(quotePayload)
      } else {
        // CMT-007: 답변 생성 (부모 스레드 ID 필요)
        if (!props.threadId) {
          throw new Error('reply 생성에는 threadId 가 필요합니다.')
        }
        await createThreadReply(props.threadId, quotePayload)
      }
    }

    // 부모에 "성공" 알림 → 목록/상세 리로드
    emit('created')
  } catch (e) {
    console.error('스레드 생성/수정 실패', e)
    alert('요청 처리에 실패했습니다.')
  } finally {
    creating.value = false
    emit('close')
  }
}


const close = () => {
  emit('close')
}

// 토글 함수
const toggleBookList = () => {
  if (loadingBooks.value) return
  showBookList.value = !showBookList.value
}

const toggleQuoteList = () => {
  if (!selectedBookId.value || loadingQuotes.value) return
  showQuoteList.value = !showQuoteList.value
}

onMounted(loadBooks)
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.modal-panel {
  width: 520px;
  max-width: 90vw;
  background: #fffdf5;
  border-radius: 24px;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.18);
  padding: 24px 28px 20px;
  font-family: 'Pretendard', sans-serif;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
}

.close-btn {
  border: none;
  background: transparent;
  font-size: 20px;
  cursor: pointer;
}

.section {
  margin-top: 16px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.select-box {
  margin-top: 4px;
  padding: 10px 12px;
  background: #f7d37a;
  border-radius: 4px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
}

.select-box.disabled {
  opacity: 0.5;
  cursor: default;
}

.select-label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.select-arrow {
  font-size: 12px;
}

.option-list {
  margin-top: 6px;
  max-height: 180px;
  overflow-y: auto;
  background: #ffffff;
  border-radius: 4px;
  border: 1px solid #f2d28a;
}

.option-item {
  padding: 8px 10px;
  font-size: 14px;
  cursor: pointer;
}

.option-item:hover {
  background: #fff7dd;
}

.option-sub {
  font-size: 12px;
  color: #777;
  margin-left: 4px;
}

.quote-list .option-item {
  line-height: 1.5;
}

.option-empty {
  padding: 10px;
  font-size: 13px;
  color: #777;
}

.modal-footer {
  margin-top: 22px;
  display: flex;
  justify-content: center;
}

.submit-btn {
  min-width: 120px;
  padding: 10px 24px;
  border-radius: 20px;
  border: none;
  background: #ffa83d;
  color: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
}

.submit-btn:disabled {
  opacity: 0.4;
  cursor: default;
}
</style>
