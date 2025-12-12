<template>
  <div class="library-container">
    <!-- 상단 제목 -->
    <div class="page-title">
      <span>내 프로필</span>
    </div>

    <!-- 로딩 중 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>정보를 불러오는 중...</p>
    </div>

    <!-- 에러 -->
    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="loadUserInfo" class="retry-btn">다시 시도</button>
    </div>

    <!-- 프로필 카드 -->
    <div v-else class="profile-card">
      <!-- 프로필 이미지 -->
      <div class="profile-img-box">
        <img src="/images/character1.png" class="profile-img" />
      </div>

      <!-- 정보 폼 -->
      <div class="info-row">
        <span class="label">닉네임</span>
        <div class="input-box">
          <input
              type="text"
              v-model="nickname"
              class="input"
              :disabled="!isEditingNickname"
              @keyup.enter="isEditingNickname && saveNickname()"
          />
          <button
              v-if="!isEditingNickname"
              @click="startEditNickname"
              class="edit-btn"
          >
            수정
          </button>
          <button
              v-else
              @click="saveNickname"
              class="save-btn"
              :disabled="savingNickname"
          >
            {{ savingNickname ? '저장 중...' : '저장' }}
          </button>
          <button
              v-if="isEditingNickname"
              @click="cancelEditNickname"
              class="cancel-btn"
          >
            취소
          </button>
        </div>
      </div>

      <div class="info-row">
        <span class="label">이메일</span>
        <div class="input-box">
          <input type="text" :value="email" class="input" disabled />
        </div>
      </div>

      <div class="info-row">
        <span class="label">가입일</span>
        <div class="input-box">
          <input type="text" :value="joinDate" class="input" disabled />
        </div>
      </div>

      <!-- 버튼 2개 -->
      <div class="btn-row">
        <button @click="handleWithdraw" class="delete-btn">회원탈퇴</button>
        <button @click="handleLogout" class="logout-btn">로그아웃</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/AuthStore'
import { getMyProfileApi, updateNicknameApi } from '@/api/UserApi'
import { logoutApi } from '@/api/AuthApi'

const authStore = useAuthStore()
const router = useRouter()

// State
const loading = ref(false)
const error = ref(null)
const isEditingNickname = ref(false)
const savingNickname = ref(false)
const originalNickname = ref('')
const nickname = ref('')
const email = ref('-')
const joinDate = ref('-')


const loadUserInfo = async () => {
  loading.value = true
  error.value = null

  try {
    console.log('📋 프로필 정보 조회 시작')

    const response = await getMyProfileApi()
    const userData = response.data

    console.log('✅ 프로필 정보 로드 성공:', userData)

    // 데이터 설정
    nickname.value = userData.nickname || ''
    originalNickname.value = nickname.value
    email.value = userData.email || '-'

    // 가입일
    if (userData.createdAt) {
      const date = new Date(userData.createdAt)
      joinDate.value = date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      }).replace(/\. /g, '.').replace(/\.$/, '')
    }

    // AuthStore 업데이트
    if (authStore.user) {
      authStore.user.nickname = userData.nickname
      authStore.user.email = userData.email
      authStore.user.createdAt = userData.createdAt
    }

  } catch (e) {
    console.error('❌ 프로필 정보 로드 실패:', e)
    const errorMessage = e.response?.data?.message || '사용자 정보를 불러오는데 실패했습니다.'
    error.value = errorMessage
  } finally {
    loading.value = false
  }
}

// 닉네임 수정 시작
const startEditNickname = () => {
  isEditingNickname.value = true
  originalNickname.value = nickname.value
}

// 닉네임 수정 취소
const cancelEditNickname = () => {
  nickname.value = originalNickname.value
  isEditingNickname.value = false
}

// 닉네임 저장
const saveNickname = async () => {
  const trimmedNickname = nickname.value.trim()

  if (!trimmedNickname) {
    alert('닉네임을 입력해주세요.')
    return
  }

  if (trimmedNickname === originalNickname.value) {
    isEditingNickname.value = false
    return
  }

  savingNickname.value = true

  try {
    console.log('📝 닉네임 수정 요청:', trimmedNickname)

    await updateNicknameApi(trimmedNickname)

    console.log('✅ 닉네임 수정 성공')

    await loadUserInfo()

    isEditingNickname.value = false

    alert('닉네임이 수정되었습니다.')
  } catch (e) {
    console.error('❌ 닉네임 수정 실패:', e)

    const errorMessage = e.response?.data?.message || '닉네임 수정에 실패했습니다.'
    alert(errorMessage)

    // 원래 닉네임으로 복구
    nickname.value = originalNickname.value
  } finally {
    savingNickname.value = false
  }
}

// 로그아웃
const handleLogout = async () => {
  if (!confirm('로그아웃 하시겠습니까?')) return

  try {
    console.log('🚪 로그아웃 요청')
    await logoutApi()
    console.log('✅ 로그아웃 성공')
  } catch (e) {
    console.error('❌ 로그아웃 실패:', e)
    // 실패해도 클라이언트에서는 로그아웃 처리
  } finally {
    authStore.clearAuthState()

    // 로그인 페이지로 강제 이동
    router.push({ name: 'login' }) // 또는 path: '/login'
  }
}

// 회원탈퇴
const handleWithdraw = () => {
  if (confirm('정말 회원탈퇴 하시겠습니까?\n탈퇴 후에는 계정을 복구할 수 없습니다.')) {
    // TODO: 회원탈퇴 API 연동
    alert('회원탈퇴 기능은 준비 중입니다.')
  }
}

// 마운트 시 사용자 정보 로드
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
/* 전체 레이아웃 */
.library-container {
  max-width: 1100px;
  margin: 0 auto;
  padding: 30px 20px;
  font-family: "Pretendard", sans-serif;
  text-align: center;
}

/* 제목 */
.page-title {
  display: inline-block;
  margin: 20px auto 10px auto;
  padding: 14px 70px;
  border: 2px solid #df3e3e;
  border-radius: 40px;
  font-size: 20px;
  font-weight: 600;
  color: #df3e3e;
  background: #ffffff;
  box-shadow: 0 4px 10px rgba(223, 62, 62, 0.15);
}

/* 로딩 */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 5px solid #ffe5b4;
  border-top: 5px solid #df3e3e;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading p {
  color: #666;
  font-size: 16px;
}

/* 에러 */
.error {
  padding: 60px 20px;
}

.error p {
  color: #df3e3e;
  font-size: 16px;
  margin-bottom: 20px;
}

.retry-btn {
  background: #df3e3e;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.retry-btn:hover {
  background: #c53535;
}

/* 프로필 카드 */
.profile-card {
  margin: 30px auto;
  width: 500px;
  padding: 30px;
  background: #fff7e8;
  border-radius: 24px;
  border: 3px solid #c38a52;
  box-shadow: 0 8px 15px rgba(0,0,0,0.08);
}

.profile-img-box {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.profile-img {
  width: 140px;
  height: 140px;
  background: #f7d37a;
  border-radius: 50%;
  padding: 20px;
}

/* 폼 라인 */
.info-row {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 15px;
  gap: 10px;
}

.label {
  width: 70px;
  text-align: right;
  font-size: 15px;
  font-weight: 500;
}

.input-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.input {
  width: 260px;
  height: 34px;
  border: 1px solid #f2c46c;
  border-radius: 18px;
  padding: 0 12px;
  background: #fffdf7;
  font-size: 14px;
  transition: all 0.3s;
}

.input:disabled {
  background: #f5f5f5;
  color: #999;
}

.input:focus {
  outline: none;
  border-color: #ffa500;
  box-shadow: 0 0 0 3px rgba(255, 165, 0, 0.1);
}

/* 버튼들 */
.edit-btn,
.save-btn,
.cancel-btn {
  border: none;
  padding: 6px 14px;
  border-radius: 15px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.edit-btn {
  background: #ffd37c;
}

.edit-btn:hover {
  background: #ffc55c;
  transform: translateY(-1px);
}

.save-btn {
  background: #7cd992;
  color: white;
}

.save-btn:hover:not(:disabled) {
  background: #6bc982;
  transform: translateY(-1px);
}

.save-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.cancel-btn {
  background: #ff9aa3;
  color: white;
}

.cancel-btn:hover {
  background: #ff8a93;
  transform: translateY(-1px);
}

/* 하단 버튼 */
.btn-row {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-top: 20px;
}

.delete-btn,
.logout-btn {
  border: none;
  padding: 10px 24px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.delete-btn {
  background: #ff9aa3;
  color: white;
}

.delete-btn:hover {
  background: #ff8a93;
  transform: translateY(-2px);
}

.logout-btn {
  background: #fcd487;
  color: #444;
}

.logout-btn:hover {
  background: #fcc477;
  transform: translateY(-2px);
}
</style>