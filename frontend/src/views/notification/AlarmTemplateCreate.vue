<template>
  <div class="template-create-page">
    <div class="create-container">
      <header class="page-header">
        <h1>알림 등록</h1>
      </header>

      <section class="form-card">
        <form class="template-form" @submit.prevent="handleSubmit">

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

          <div class="button-row">
            <el-button
                type="danger"
                class="submit-btn"
                native-type="submit"
                :loading="loading"
            >
              등록
            </el-button>
            <el-button class="cancel-btn" @click="handleCancel">
              취소
            </el-button>
          </div>
        </form>
      </section>

      <CommonModal
          v-model="confirmVisible"
          mode="confirm"
          :message="confirmMessage"
          confirm-text="등록"
          cancel-text="취소"
          @confirm="handleConfirm"
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
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import CommonModal from '@/components/common/CommonModal.vue'
import { createAdminNotificationTemplate } from '@/api/notificationApi.js'

const router = useRouter()

const form = reactive({
  title: '',
  body: '',
})

const loading = ref(false)

// confirm
const confirmVisible = ref(false)
const confirmMessage = ref('등록 하시겠습니까?')
const confirmAction = ref(null)

const openConfirm = (msg, onConfirm) => {
  confirmMessage.value = msg
  confirmAction.value = onConfirm
  confirmVisible.value = true
}

const handleConfirm = () => {
  if (typeof confirmAction.value === 'function') confirmAction.value()
}

// alert
const alertVisible = ref(false)
const alertMessage = ref('')
const alertAction = ref(null)

const openAlert = (msg, onConfirm = null) => {
  alertMessage.value = msg
  alertAction.value = onConfirm
  alertVisible.value = true
}

const handleAlertConfirm = () => {
  if (typeof alertAction.value === 'function') alertAction.value()
}

const doCreate = async () => {
  if (loading.value) return

  try {
    loading.value = true

    await createAdminNotificationTemplate({
      templateType: 'SYSTEM',
      title: form.title.trim(),
      body: form.body.trim(),
    })

    openAlert('등록되었습니다.', () => {
      router.push({ name: 'AdminAlarmTemplates' })
    })
  } catch (err) {
    console.error(err)
    openAlert('템플릿 등록에 실패했습니다. 다시 시도해주세요.')
  } finally {
    loading.value = false
  }
}

const handleSubmit = () => {
  if (!form.title.trim() || !form.body.trim()) {
    openAlert('제목과 내용을 모두 입력해주세요.')
    return
  }
  openConfirm('등록 하시겠습니까?', doCreate)
}

const handleCancel = () => {
  router.push({ name: 'AdminAlarmTemplates' })
}
</script>

<style scoped lang="scss">
.template-create-page {
  padding: 24px 32px 40px;
  background: #fff7e5;
  min-height: 100%;
}

.create-container {
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
  grid-template-columns: 80px 1fr;
  column-gap: 16px;
  align-items: center;

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

.title-input :deep(.el-input__wrapper) {
  border-radius: 999px;
  height: 40px;
}

.body-input :deep(.el-textarea__inner) {
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
  min-width: 80px;
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
