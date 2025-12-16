<template>
  <div class="loading">
    로그인 처리 중입니다...
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/AuthStore'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

onMounted(() => {
  const token = route.query.accessToken

  if (token) {
    console.log('✅ 토큰 받음:', token.substring(0, 20) + '...')

    // Pinia Store에 저장 (localStorage도 자동으로 저장됨)
    const success = authStore.handleOAuth2Success(token)

    if (success) {
      console.log('✅ 로그인 성공')
      console.log('   User:', authStore.user)
      console.log('   isAuthenticated:', authStore.isAuthenticated)

      // redirect 쿼리가 있으면 해당 페이지로
      const redirect = route.query.redirect || '/'
      router.replace(redirect)
    } else {
      console.error('❌ 토큰 처리 실패')
      router.replace('/login')
    }
  } else {
    console.error('❌ 토큰 없음!')
    router.replace('/login')
  }
})
</script>

<style scoped>
.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  font-size: 18px;
  color: #444;
  background: #fff7ea;
}
</style>