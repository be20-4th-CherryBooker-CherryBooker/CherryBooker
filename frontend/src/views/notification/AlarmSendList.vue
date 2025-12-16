<!-- src/views/notification/AlarmSendList.vue -->
<template>
  <div class="send-list-page">
    <div class="send-list-container">
      <header class="page-header">
        <h1>발송 목록</h1>
      </header>

      <!-- 검색 영역 (제목 + 발송일 범위 – 프론트 필터용) -->
      <section class="search-card">
        <div class="search-row search-row--keyword">
          <div class="field-label">알림 제목</div>
          <div class="field-main">
            <el-input
                v-model="keyword"
                placeholder="발송한 알림 제목 검색"
                class="keyword-input"
                clearable
            />
          </div>
          <el-button
              type="danger"
              class="search-btn"
              @click="handleSearch"
          >
            검색하기
          </el-button>
        </div>

        <div class="search-row search-row--date">
          <div class="field-label">발송일</div>
          <div class="field-main date-range">
            <el-date-picker
                v-model="startDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="시작일"
                class="date-input"
            />
            <span class="tilde">~</span>
            <el-date-picker
                v-model="endDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="종료일"
                class="date-input"
            />
          </div>
        </div>
      </section>

      <!-- 테이블 -->
      <section class="table-card">
        <div v-if="errorMessage" class="error-text">
          {{ errorMessage }}
        </div>

        <div v-if="!loading" class="table-wrapper">
          <div class="table-head">
            <div class="col no">번호</div>
            <div class="col title">제목</div>
            <div class="col body">내용</div>
            <div class="col status">상태</div>
            <div class="col date">발송일</div>
          </div>

          <div v-if="displayLogs.length" class="table-body">
            <div
                v-for="(item, idx) in displayLogs"
                :key="item.id"
                class="table-row"
            >
              <div class="col no">
                {{ idx + 1 + pagination.currentPage * pagination.size }}
              </div>
              <div class="col title">{{ item.title }}</div>
              <div class="col body">{{ item.body }}</div>
              <div class="col status">
                <span
                    class="status-pill"
                    :class="statusClass(item.status)"
                >
                  <span class="dot" />
                  <span>{{ statusLabel(item.status) }}</span>
                </span>
              </div>
              <div class="col date">
                {{ formatDateTime(item.sentAt) }}
              </div>
            </div>
          </div>

          <div v-else class="empty-state">
            조회된 발송 내역이 없습니다.
          </div>
        </div>

        <div v-else class="empty-state">
          발송 내역을 불러오는 중입니다...
        </div>

        <div class="table-footer">
          <div class="pagination" v-if="pagination.totalPages > 0">
            <button
                class="page-btn"
                :disabled="pagination.currentPage === 0"
                @click="goPage(pagination.currentPage - 1)"
            >
              이전
            </button>

            <button
                v-for="page in pagination.totalPages"
                :key="page"
                class="page-btn"
                :class="{ active: pagination.currentPage === page - 1 }"
                @click="goPage(page - 1)"
            >
              {{ page }}
            </button>

            <button
                class="page-btn"
                :disabled="pagination.currentPage >= pagination.totalPages - 1"
                @click="goPage(pagination.currentPage + 1)"
            >
              다음
            </button>
          </div>

          <el-button
              type="danger"
              class="send-btn"
              @click="goSendCreate"
          >
            발송하기
          </el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchAdminNotificationSendLogs } from '@/api/notificationApi.js'

const router = useRouter()

const keyword = ref('')
const startDate = ref(null)
const endDate = ref(null)

const logs = ref([])

const pagination = ref({
  currentPage: 0,
  totalPages: 0,
  totalItems: 0,
  size: 10,
})

const loading = ref(false)
const errorMessage = ref('')

const loadLogs = async (page = 0) => {
  loading.value = true
  errorMessage.value = ''

  try {
    const pageData = await fetchAdminNotificationSendLogs({
      page,
      size: pagination.value.size,
    })

    const p = pageData?.pagination || {
      currentPage: 0,
      totalPages: 0,
      totalItems: 0,
    }

    pagination.value.currentPage = p.currentPage ?? 0
    pagination.value.totalPages = p.totalPages ?? 0
    pagination.value.totalItems = p.totalItems ?? 0

    logs.value = (pageData?.logs || []).map((log) => ({
      id: log.sendLogId,
      templateId: log.templateId,
      title: log.templateTitle,
      body: log.bodySnapshot,
      status: log.status, // PENDING, SUCCESS, FAILED
      sentAt: log.sentAt,
    }))
  } catch (err) {
    console.error(err)
    errorMessage.value = '발송 내역을 불러오지 못했습니다.'
    logs.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 서버 검색은 없고, 프론트 필터만 사용
  pagination.value.currentPage = 0
}

const displayLogs = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  const from = startDate.value || null
  const to = endDate.value || null

  return logs.value.filter((item) => {
    const titleMatch = !kw || item.title.toLowerCase().includes(kw)
    const dateStr = (item.sentAt || '').toString().split('T')[0] || ''
    const fromOk = !from || dateStr >= from
    const toOk = !to || dateStr <= to

    return titleMatch && fromOk && toOk
  })
})

const goPage = (page) => {
  if (page < 0 || page >= pagination.value.totalPages) return
  loadLogs(page)
}

const formatDateTime = (str) => {
  if (!str) return ''
  const [date, time] = String(str).split('T')
  const hm = (time || '').slice(0, 5)
  return `${date} ${hm}`
}

const statusLabel = (status) => {
  switch (status) {
    case 'SUCCESS':
      return '발송완료'
    case 'FAILED':
      return '발송실패'
    default:
      return '발송대기'
  }
}

const statusClass = (status) => {
  switch (status) {
    case 'SUCCESS':
      return 'status-success'
    case 'FAILED':
      return 'status-failed'
    default:
      return 'status-pending'
  }
}

const goSendCreate = () => {
  router.push({ name: 'AdminAlarmSendCreate' })
}

onMounted(() => {
  loadLogs(0)
})
</script>

<style scoped lang="scss">
.send-list-page {
  padding: 24px 32px 40px;
  background: #fff7e5;
  min-height: 100%;
}

.send-list-container {
  max-width: 980px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 12px;

  h1 {
    font-size: 22px;
    font-weight: 700;
    color: #222222;
  }
}

.search-card {
  background: #ffffff;
  border-radius: 18px;
  border: 1px solid #f1e0c6;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.03);
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.search-row {
  display: grid;
  grid-template-columns: 90px 1fr 110px;
  align-items: center;
  column-gap: 12px;
}

.search-row--date .field-main {
  grid-column: 2 / 4;
}

.field-label {
  font-size: 14px;
  font-weight: 700;
  color: #333333;
}

.field-main {
  width: 100%;
}

.keyword-input :deep(.el-input__wrapper),
.date-input :deep(.el-input__wrapper) {
  border-radius: 999px;
  height: 38px;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tilde {
  font-size: 16px;
  color: #6b7280;
}

.search-btn.el-button {
  height: 38px;
  border-radius: 999px;
  font-weight: 700;
  background-color: #ff9f1c;
  border-color: #ff9f1c;
}

/* 테이블 카드 */
.table-card {
  margin-top: 18px;
  background: #ffffff;
  border-radius: 18px;
  border: 1px solid #f1e0c6;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.03);
  padding: 18px 20px 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.table-wrapper {
  border-radius: 14px;
  border: 1px solid #f1e0c6;
  overflow: hidden;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 70px 180px 1fr 120px 150px;
  align-items: center;
}

.table-head {
  background: #fff3dd;
  height: 46px;
  font-size: 13px;
  font-weight: 700;
  color: #666666;
}

.table-row {
  min-height: 52px;
  border-bottom: 1px solid #f7efe1;
  font-size: 13px;
  color: #444444;
}

.table-row:last-child {
  border-bottom: none;
}

.col {
  padding: 0 12px;
}

.col.body {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-state {
  padding: 26px 16px;
  text-align: center;
  color: #aaaaaa;
  font-weight: 600;
}

/* 상태 pill */
.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}

.status-pill .dot {
  width: 6px;
  height: 6px;
  border-radius: 999px;
}

.status-success {
  background: #e6f6ed;
  color: #15803d;
}
.status-success .dot {
  background: #22c55e;
}

.status-pending {
  background: #fef3c7;
  color: #d97706;
}
.status-pending .dot {
  background: #f59e0b;
}

.status-failed {
  background: #fee2e2;
  color: #b91c1c;
}
.status-failed .dot {
  background: #ef4444;
}

/* 하단 */
.table-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.pagination {
  display: flex;
  gap: 6px;
}

.page-btn {
  min-width: 40px;
  height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid #f1e0c6;
  background: #ffffff;
  font-size: 12px;
  font-weight: 600;
  color: #555555;
  cursor: pointer;
}

.page-btn.active {
  background: #ff9f1c;
  border-color: #ff9f1c;
  color: #ffffff;
}

.page-btn:disabled {
  cursor: default;
  color: #cccccc;
}

.send-btn.el-button {
  height: 40px;
  border-radius: 999px;
  font-weight: 700;
  background-color: #ff9f1c;
  border-color: #ff9f1c;
}

.error-text {
  color: #dc2626;
  font-weight: 600;
}
</style>
