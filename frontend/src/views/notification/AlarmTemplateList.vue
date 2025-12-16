<!-- src/views/notification/AlarmTemplateList.vue -->
<template>
  <div class="template-page">
    <div class="template-container">
      <header class="page-header">
        <h1>알림 관리</h1>
      </header>

      <!-- 검색 영역 -->
      <section class="search-card">
        <div class="search-row">
          <label class="search-label" for="templateTitle">알림 제목</label>
          <el-input
              id="templateTitle"
              v-model="searchQuery"
              class="search-input"
              placeholder="예: 정기 점검"
              clearable
          />
          <el-button
              type="danger"
              class="search-button"
              @click="handleSearch"
          >
            검색하기
          </el-button>
        </div>
      </section>

      <!-- 목록 -->
      <section class="table-card">
        <div v-if="errorMessage" class="error-text">
          {{ errorMessage }}
        </div>

        <div v-if="!loading" class="table-wrapper">
          <div class="table-head">
            <div class="col no">번호</div>
            <div class="col title">제목</div>
            <div class="col content">내용</div>
          </div>

          <div v-if="filteredTemplates.length" class="table-body">
            <div
                v-for="(item, idx) in filteredTemplates"
                :key="item.id"
                class="table-row"
                @click="goDetail(item)"
            >
              <div class="col no">
                {{ idx + 1 + pagination.currentPage * pagination.size }}
              </div>
              <div class="col title">{{ item.title }}</div>
              <div class="col content">{{ item.body }}</div>
            </div>
          </div>

          <div v-else class="empty-state">
            검색 조건에 맞는 템플릿이 없습니다.
          </div>
        </div>

        <div v-else class="empty-state">
          템플릿 목록을 불러오는 중입니다...
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
              class="create-button"
              @click="goCreate"
          >
            등록하기
          </el-button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchAdminNotificationTemplates } from '@/api/notificationApi.js'

const router = useRouter()

const searchQuery = ref('')
const templates = ref([])

const pagination = ref({
  currentPage: 0,
  totalPages: 0,
  totalItems: 0,
  size: 10,
})

const loading = ref(false)
const errorMessage = ref('')

const loadTemplates = async (page = 0) => {
  loading.value = true
  errorMessage.value = ''

  try {
    const pageData = await fetchAdminNotificationTemplates({
      keyword: searchQuery.value.trim(),
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

    templates.value = (pageData?.templates || []).map((t) => ({
      id: t.templateId,
      type: t.templateType,
      title: t.title,
      body: t.body,
      createdAt: t.createdAt ?? null,
    }))
  } catch (err) {
    console.error(err)
    errorMessage.value = '템플릿 목록을 불러오지 못했습니다.'
    templates.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadTemplates(0)
}

const filteredTemplates = computed(() => templates.value)

const goPage = (page) => {
  if (page < 0 || page >= pagination.value.totalPages) return
  loadTemplates(page)
}

const goCreate = () => {
  router.push({ name: 'AdminAlarmTemplateCreate' })
}

const goDetail = (item) => {
  router.push({
    name: 'AdminAlarmTemplateDetail',
    params: { templateId: item.id },
    query: {
      title: item.title,
      body: item.body,
      createdAt: item.createdAt || '',
      templateType: item.type || '',
    },
  })
}

onMounted(() => {
  loadTemplates(0)
})
</script>

<style scoped lang="scss">
.template-page {
  padding: 24px 32px 40px;
  background: #fff7e5;
  min-height: 100%;
}

.template-container {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  padding-bottom: 8px;

  h1 {
    font-size: 22px;
    font-weight: 700;
    color: #222222;
  }
}

.search-card,
.table-card {
  background: #ffffff;
  border-radius: 18px;
  border: 1px solid #f1e0c6;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.03);
}

.search-card {
  padding: 16px 20px;
}

.search-row {
  display: grid;
  grid-template-columns: 120px 1fr 110px;
  gap: 12px;
  align-items: center;
}

.search-label {
  font-weight: 700;
  font-size: 14px;
  color: #333333;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 999px;
  height: 40px;
}

.search-button.el-button {
  height: 40px;
  border-radius: 999px;
  font-weight: 700;
  background-color: #ff9f1c;
  border-color: #ff9f1c;
}

.search-button.el-button:hover:not(.is-disabled) {
  background-color: #ff8a00;
  border-color: #ff8a00;
}

.table-card {
  margin-top: 18px;
  padding: 18px 20px 14px;
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
  grid-template-columns: 80px 220px 1fr;
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
  min-height: 50px;
  font-size: 13px;
  color: #444444;
  border-bottom: 1px solid #f7efe1;
  cursor: pointer;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row:hover {
  background: #fff9f0;
}

.col {
  padding: 0 16px;
}

.col.content {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-state {
  padding: 28px 16px;
  text-align: center;
  color: #aaaaaa;
  font-weight: 600;
}

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
  color: #cccccc;
  cursor: default;
}

.create-button.el-button {
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
