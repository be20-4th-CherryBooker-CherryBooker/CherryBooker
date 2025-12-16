<template>
  <teleport to="body">
    <transition name="cm-fade">
      <div v-if="modelValue" class="cm-overlay" @click.self="onOverlay">
        <div class="cm-modal" role="dialog" aria-modal="true">
          <div class="cm-body">
            <div class="cm-message">{{ message }}</div>
          </div>

          <!-- confirm: 2버튼 / alert: 1버튼 -->
          <div class="cm-actions">
            <button
                class="cm-btn cm-btn--confirm"
                type="button"
                @click="onConfirm"
            >
              {{ confirmText }}
            </button>

            <button
                v-if="mode === 'confirm'"
                class="cm-btn cm-btn--cancel"
                type="button"
                @click="onCancel"
            >
              {{ cancelText }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { onBeforeUnmount, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  mode: { type: String, default: 'alert' }, // 'alert' | 'confirm'
  message: { type: String, default: '' },
  confirmText: { type: String, default: '확인' },
  cancelText: { type: String, default: '취소' },
  closeOnOverlay: { type: Boolean, default: false },
  closeOnEsc: { type: Boolean, default: true },
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const close = () => emit('update:modelValue', false)

const onConfirm = () => {
  emit('confirm')
  close()
}

const onCancel = () => {
  emit('cancel')
  close()
}

const onOverlay = () => {
  if (props.closeOnOverlay) onCancel()
}

const onKeyDown = (e) => {
  if (!props.modelValue) return
  if (props.closeOnEsc && e.key === 'Escape') onCancel()
}

watch(
    () => props.modelValue,
    (open) => {
      if (open) document.addEventListener('keydown', onKeyDown)
      else document.removeEventListener('keydown', onKeyDown)
    },
)

onBeforeUnmount(() => document.removeEventListener('keydown', onKeyDown))
</script>

<style scoped lang="scss">
.cm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 24px;
}

.cm-modal {
  width: min(560px, 100%);
  background: #fff;
  border: 1px solid #f1e0c6;
  border-radius: 18px;
  padding: 18px 18px 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
}

.cm-body {
  background: #f2f2f2;
  border-radius: 10px;
  padding: 52px 18px;
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.cm-message {
  font-size: 14px;
  font-weight: 600;
  color: #222;
  line-height: 1.6;
  white-space: pre-wrap;
}

.cm-actions {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.cm-btn {
  min-width: 72px;
  padding: 8px 18px;
  border: 0;
  border-radius: 999px;
  font-weight: 700;
  cursor: pointer;
}

.cm-btn--confirm {
  background: #ff7b8a;
  color: #fff;
}

.cm-btn--cancel {
  background: #ffd36b;
  color: #6b4b00;
}

.cm-fade-enter-active,
.cm-fade-leave-active {
  transition: opacity 0.15s ease;
}
.cm-fade-enter-from,
.cm-fade-leave-to {
  opacity: 0;
}
</style>
