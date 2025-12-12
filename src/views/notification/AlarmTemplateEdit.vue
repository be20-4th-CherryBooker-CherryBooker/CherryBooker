<template>
  <div class="template-edit-page">
    <div class="edit-container">
      <header class="page-header">
        <h1>알림 수정</h1>
      </header>

      <section class="form-card">
        <form class="template-form" @submit.prevent="handleSubmit">
          <!-- 템플릿 ID (읽기 전용) -->
          <div class="form-row">
            <label class="field-label">템플릿 ID</label>
            <div class="field-main readonly-field">
              {{ templateId }}
            </div>
          </div>

          <!-- 유형 -->
          <div class="form-row">
            <label class="field-label">유형</label>
            <div class="field-main">
              <el-select
                  v-model="form.templateType"
                  class="type-select"
                  placeholder="알림 유형 선택"
              >
                <el-option label="시스템 공지" value="SYSTEM" />
                <el-option label="답변 알림" value="EVENT_THREAD_REPLY" />
                <el-option label="신고 알림" value="EVENT_THREAD_REPORTED" />
              </el-select>
            </div>
          </div>

          <!-- 제목 -->
          <div class="form-row">
            <label class="field-label">제목</label>
            <div class="field-main">
              <el-input
                  v-model="form.title"
                  placeholder="제목을 입력하세요."
                  class="title-input"
              />
            </div>
          </div>

          <!-- 내용 -->
          <div class="form-row form-row--textarea">
            <label class="field-label">내용</label>
            <div class="field-main">
              <el-input
                  v-model="form.body"
                  type="textarea"
                  :rows="14"
                  placeholder="내용을 입력하세요."
                  class="body-input"
              />
            </div>
          </div>

          <div v-if="errorMessage" class="error-text">
            {{ errorMessage }}
          </div>

          <div class="button-row">
            <el-button class="btn-outline" @click="goDetail">
              취소
            </el-button>
            <el-button
                type="danger"
                class="btn-submit"
                native-type="submit"
                :loading="saving"
            >
              수정 완료
            </el-button>
          </div>
        </form>
      </section>

      <CommonModal
          v-model="confirmVisible"
          mode="confirm"
          :message="confirmMessage"
          confirm-text="수정"
          cancel-text="취소"
          @confirm="handleConfirmUpdate"
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
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CommonModal from '@/components/common/CommonModal.vue'
import {
  fetchAdminNotificationTemplates,
  updateAdminNotificationTemplate,
} from '@/api/notificationApi.js'

const route = useRoute()
const router = useRouter()

const templateId = Number(route.params.templateId || 0)

const form = reactive({
  templateType: route.query.templateType || 'SYSTEM',
  title: route.query.title || '',
  body: route.query.body || '',
})

const saving = ref(false)
const errorMessage = ref('')

// confirm(수정 확인)
const confirmVisible = ref(false)
const confirmMessage = ref('수정 하시겠습니까?')

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

// 템플릿 데이터 재조회
const loadTemplateIfNeeded = async () => {
  try {
    const pageData = await fetchAdminNotificationTemplates({
      page: 0,
      size: 100,
    })

    const found = pageData?.templates?.find((t) => t.templateId === templateId)

    if (found) {
      form.title = found.title
      form.body = found.body
      form.templateType = found.templateType || 'SYSTEM'
    }
  } catch (err) {
    console.error('[AlarmTemplateEdit] loadTemplateIfNeeded error', err)
    errorMessage.value = '템플릿 정보를 불러오지 못했습니다.'
  }
}

onMounted(() => {
  loadTemplateIfNeeded()
})

const goDetail = () => {
  router.push({
    name: 'AdminAlarmTemplateDetail',
    params: { templateId },
  })
}

const doUpdate = async () => {
  if (saving.value) return

  try {
    saving.value = true
    errorMessage.value = ''

    await updateAdminNotificationTemplate(templateId, {
      templateType: form.templateType || 'SYSTEM',
      title: form.title.trim(),
      body: form.body.trim(),
    })

    openAlert('수정되었습니다.', () => {
      router.push({
        name: 'AdminAlarmTemplateDetail',
        params: { templateId },
      })
    })
  } catch (err) {
    console.error('[AlarmTemplateEdit] update error', err)
    errorMessage.value = '템플릿 수정에 실패했습니다.'
    openAlert('템플릿 수정 중 오류가 발생했습니다.')
  } finally {
    saving.value = false
  }
}

const handleSubmit = () => {
  if (!form.title.trim() || !form.body.trim()) {
    openAlert('제목과 내용을 모두 입력해주세요.')
    return
  }
  confirmMessage.value = '수정 하시겠습니까?'
  confirmVisible.value = true
}

const handleConfirmUpdate = () => {
  doUpdate()
}
</script>

<style scoped lang="scss">
.template-edit-page {
  padding: 24px 32px 40px;
  background: #fff7e5;
  min-height: 100%;
}

.edit-container {
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

.form-card {
  background: #ffffff;
  border-radius: 18px;
  border: 1px solid #f1e0c6;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.03);
  padding: 20px 22px 18px;
}

.template-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 90px 1fr;
  column-gap: 16px;
  padding: 6px 0;

  &--textarea {
    align-items: flex-start;
  }
}

.field-label {
  font-size: 14px;
  font-weight: 700;
  color: #333333;
  padding-top: 6px;
}

.field-main {
  width: 100%;
}

.readonly-field {
  padding: 6px 14px;
  border-radius: 999px;
  background: #f9fafb;
  font-size: 14px;
  color: #4b5563;
}

.type-select :deep(.el-input__wrapper),
.title-input :deep(.el-input__wrapper) {
  border-radius: 999px;
  height: 40px;
}

.body-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  resize: none;
}

.button-row {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.btn-outline {
  min-width: 80px;
  border-radius: 999px;
  border-color: #ff9f1c;
  color: #ff9f1c;
  font-weight: 700;
}

.btn-submit {
  min-width: 100px;
  border-radius: 999px;
  font-weight: 700;
  background-color: #ff9f1c;
  border-color: #ff9f1c;
}

.error-text {
  margin-top: 6px;
  color: #dc2626;
  font-weight: 600;
}
</style>
