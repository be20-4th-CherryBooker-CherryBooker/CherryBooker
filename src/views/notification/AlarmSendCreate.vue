<template>
  <div class="send-create-page">
    <div class="send-create-container">
      <header class="page-header">
        <h1>알림 발송</h1>
      </header>

      <section class="form-card">
        <form class="send-form" @submit.prevent="handleSubmit">
          <!-- 템플릿 선택 -->
          <div class="form-row">
            <label class="field-label">템플릿</label>
            <div class="field-main">
              <el-select
                  v-model="selectedTemplateId"
                  placeholder="발송할 템플릿을 선택하세요."
                  class="template-select"
                  :loading="loadingTemplates"
                  clearable
              >
                <el-option
                    v-for="t in templateOptions"
                    :key="t.id"
                    :label="t.title"
                    :value="t.id"
                />
              </el-select>
            </div>
          </div>

          <!-- (선택) 템플릿 미리보기 -->
          <div v-if="selectedTemplateBody" class="form-row form-row--textarea">
            <label class="field-label">미리보기</label>
            <div class="field-main">
              <el-input
                  :model-value="selectedTemplateBody"
                  type="textarea"
                  :rows="8"
                  readonly
                  class="preview-input"
              />
            </div>
          </div>

          <div class="button-row">
            <el-button
                type="danger"
                class="submit-btn"
                native-type="submit"
                :loading="sending"
            >
              전체 발송
            </el-button>
            <el-button class="cancel-btn" @click="handleCancel">
              취소
            </el-button>
          </div>
        </form>
      </section>

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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import CommonModal from '@/components/common/CommonModal.vue'
import {
  fetchAdminNotificationTemplates,
  sendNotificationByTemplateToAll,
} from '@/api/notificationApi.js'

const router = useRouter()

const templateOptions = ref([]) // { id, title, body }[]
const selectedTemplateId = ref(null)

const loadingTemplates = ref(false)
const sending = ref(false)

const alertVisible = ref(false)
const alertMessage = ref('')
const alertConfirmAction = ref(null)

const openAlert = (msg, onConfirm = null) => {
  alertMessage.value = msg
  alertConfirmAction.value = onConfirm
  alertVisible.value = true
}
const handleAlertConfirm = () => {
  if (typeof alertConfirmAction.value === 'function') alertConfirmAction.value()
}

// 선택된 템플릿 본문
const selectedTemplateBody = computed(() => {
  const t = templateOptions.value.find((x) => x.id === selectedTemplateId.value)
  return t?.body || ''
})

const loadTemplates = async () => {
  loadingTemplates.value = true
  try {
    const pageData = await fetchAdminNotificationTemplates({ page: 0, size: 100 })

    // 전체 발송은 SYSTEM 템플릿만 허용
    templateOptions.value = (pageData?.templates || [])
        .filter((t) => t.templateType === 'SYSTEM')
        .map((t) => ({
          id: t.templateId,
          title: t.title,
          body: t.body,
        }))
  } catch (err) {
    console.error(err)
    openAlert('템플릿 목록을 불러오지 못했습니다.')
  } finally {
    loadingTemplates.value = false
  }
}

onMounted(loadTemplates)

const handleSubmit = async () => {
  if (!selectedTemplateId.value) {
    openAlert('발송할 템플릿을 선택해주세요.')
    return
  }

  try {
    sending.value = true

    await sendNotificationByTemplateToAll(selectedTemplateId.value, {
      variables: {}, // 필요하면 추후 추가
    })

    openAlert('전체 회원에게 알림이 발송되었습니다.', () => {
      router.push({ name: 'AdminAlarmSendList' })
    })
  } catch (err) {
    console.error(err)
    openAlert('알림 발송 중 오류가 발생했습니다.')
  } finally {
    sending.value = false
  }
}

const handleCancel = () => {
  router.push({ name: 'AdminAlarmSendList' })
}
</script>

<style scoped lang="scss">
.send-create-page {
  padding: 24px 32px 40px;
  background: #fff7e5;
  min-height: 100%;
}

.send-create-container {
  max-width: 720px;
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
  padding: 18px 22px 18px;
}

.send-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 80px 1fr;
  column-gap: 16px;

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

.template-select :deep(.el-input__wrapper) {
  border-radius: 999px;
  height: 40px;
}

.preview-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  resize: none;
}

.button-row {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}

.submit-btn,
.cancel-btn {
  min-width: 90px;
  border-radius: 999px;
  font-weight: 700;
}

.submit-btn {
  background-color: #ff9f1c;
  border-color: #ff9f1c;
}

.cancel-btn {
  border-color: #ff9f1c;
  color: #ff9f1c;
}
</style>
