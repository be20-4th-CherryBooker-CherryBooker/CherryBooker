<template>
  <div class="template-detail-page">
    <div class="detail-container">
      <header class="page-header">
        <h1>알림 상세</h1>
      </header>

      <section class="detail-card">
        <div class="detail-row">
          <div class="field-label">템플릿 ID</div>
          <div class="field-value">{{ template.id }}</div>
        </div>

        <div class="detail-row">
          <div class="field-label">유형</div>
          <div class="field-value">
            {{ displayTemplateType }}
          </div>
        </div>

        <div class="detail-row">
          <div class="field-label">제목</div>
          <div class="field-value">
            {{ template.title }}
          </div>
        </div>

        <div class="detail-row detail-row--textarea">
          <div class="field-label">내용</div>
          <div class="field-value multiline">
            <pre>{{ template.body }}</pre>
          </div>
        </div>

        <div class="detail-row">
          <div class="field-label">등록일</div>
          <div class="field-value">
            {{ formattedCreatedAt }}
          </div>
        </div>

        <div v-if="errorMessage" class="error-text">
          {{ errorMessage }}
        </div>

        <div class="button-row">
          <el-button class="btn-outline" @click="goList">
            목록으로
          </el-button>

          <div class="button-right">
            <el-button class="btn-outline" @click="goEdit">
              수정하기
            </el-button>
            <el-button type="danger" class="btn-danger" @click="onClickDelete">
              삭제하기
            </el-button>
          </div>
        </div>
      </section>

      <CommonModal
          v-model="confirmVisible"
          mode="confirm"
          :message="confirmMessage"
          confirm-text="삭제"
          cancel-text="취소"
          @confirm="handleConfirmDelete"
      />

      <CommonModal
          v-model="alertVisible"
          mode="alert"
          :message="alertMessage"
          confirm-text="확인"
          @confirm="handleAlertConfirm"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CommonModal from '@/components/common/CommonModal.vue'
import {
  fetchAdminNotificationTemplates,
  deleteAdminNotificationTemplate,
} from '@/api/notificationApi.js'

const route = useRoute()
const router = useRouter()

const templateId = Number(route.params.templateId || 0)

const template = reactive({
  id: templateId,
  title: route.query.title || '',
  body: route.query.body || '',
  createdAt: route.query.createdAt || '',
  templateType: route.query.templateType || 'SYSTEM',
})

const errorMessage = ref('')

// 삭제 confirm
const confirmVisible = ref(false)
const confirmMessage = ref('정말로 이 알림 템플릿을 삭제하시겠습니까?')
const deleteInProgress = ref(false)

// alert
const alertVisible = ref(false)
const alertMessage = ref('')
const alertAfterAction = ref(null)

const openAlert = (msg, afterAction = null) => {
  alertMessage.value = msg
  alertAfterAction.value = afterAction
  alertVisible.value = true
}

const handleAlertConfirm = () => {
  if (typeof alertAfterAction.value === 'function') {
    alertAfterAction.value()
  }
}

const loadTemplateIfNeeded = async () => {
  try {
    const pageData = await fetchAdminNotificationTemplates({
      page: 0,
      size: 100,
    })

    const found = pageData?.templates?.find((t) => t.templateId === template.id)

    if (found) {
      template.title = found.title
      template.body = found.body
      template.createdAt = found.createdAt || ''
      template.templateType = found.templateType || 'SYSTEM'
    }
  } catch (err) {
    console.error('[AlarmTemplateDetail] loadTemplateIfNeeded error', err)
    errorMessage.value = '템플릿 정보를 불러오지 못했습니다.'
  }
}

onMounted(() => {
  loadTemplateIfNeeded()
})

const displayTemplateType = computed(() => {
  switch (template.templateType) {
    case 'EVENT_THREAD_REPLY':
      return '답변 알림'
    case 'EVENT_THREAD_REPORTED':
      return '신고 알림'
    case 'SYSTEM':
    default:
      return '시스템 공지'
  }
})

const formattedCreatedAt = computed(() => {
  if (!template.createdAt) return ''
  const [date, time] = String(template.createdAt).split('T')
  const hm = (time || '').slice(0, 5)
  return `${date} ${hm}`
})

const goList = () => {
  router.push({ name: 'AdminAlarmTemplates' })
}

const goEdit = () => {
  router.push({
    name: 'AdminAlarmTemplateEdit',
    params: { templateId: template.id },
    query: {
      title: template.title,
      body: template.body,
      createdAt: template.createdAt,
      templateType: template.templateType,
    },
  })
}

const onClickDelete = () => {
  confirmMessage.value = '정말로 이 알림 템플릿을 삭제하시겠습니까?'
  confirmVisible.value = true
}

const handleConfirmDelete = async () => {
  if (deleteInProgress.value) return

  try {
    deleteInProgress.value = true
    await deleteAdminNotificationTemplate(template.id)

    openAlert('삭제되었습니다.', () => {
      router.push({ name: 'AdminAlarmTemplates' })
    })
  } catch (err) {
    console.error('[AlarmTemplateDetail] delete error', err)
    openAlert('템플릿 삭제에 실패했습니다.')
  } finally {
    deleteInProgress.value = false
  }
}
</script>

<style scoped lang="scss">
.template-detail-page {
  padding: 24px 32px 40px;
  background: #fff7e5;
  min-height: 100%;
}

.detail-container {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 16px;

  h1 {
    font-size: 22px;
    font-weight: 700;
    color: #222222;
  }
}

.detail-card {
  background: #ffffff;
  border-radius: 18px;
  border: 1px solid #f1e0c6;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.03);
  padding: 20px 22px 16px;
}

.detail-row {
  display: grid;
  grid-template-columns: 90px 1fr;
  column-gap: 16px;
  padding: 8px 0;
  border-bottom: 1px solid #f7efe1;

  &--textarea {
    align-items: flex-start;
  }

  &:last-of-type {
    border-bottom: none;
  }
}

.field-label {
  font-size: 14px;
  font-weight: 700;
  color: #333333;
  padding-top: 6px;
}

.field-value {
  font-size: 14px;
  color: #444444;
}

.field-value.multiline {
  white-space: pre-wrap;

  pre {
    margin: 0;
    font: inherit;
    white-space: pre-wrap;
  }
}

.button-row {
  margin-top: 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.button-right {
  display: flex;
  gap: 8px;
}

.btn-outline {
  min-width: 80px;
  border-radius: 999px;
  border-color: #ff9f1c;
  color: #ff9f1c;
  font-weight: 700;
}

.btn-danger {
  min-width: 80px;
  border-radius: 999px;
  font-weight: 700;
  background-color: #ff7b8a;
  border-color: #ff7b8a;
}

.error-text {
  margin-top: 12px;
  color: #dc2626;
  font-weight: 600;
}
</style>
