<template>
  <div class="mypage-container">
    <!-- 로딩 중 -->
    <div v-if="loading" class="loading">
      <div class="spinner"></div>
      <p>정보를 불러오는 중...</p>
    </div>

    <!-- 에러 -->
    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="loadMyPageInfo" class="retry-btn">다시 시도</button>
    </div>

    <!-- 마이페이지 콘텐츠 -->
    <div v-else class="mypage-content">
      <!-- 상단 메시지 -->
      <div class="tree-message">{{ treeMessage }}</div>

      <!-- 메인 영역 -->
      <div class="main-section">
        <!-- 왼쪽 카드 -->
        <div class="left-card">
          <div class="user-avatar">
            <img src="/images/character1.png" alt="프로필" />
          </div>
          <h2 class="user-name">{{ userName }}님,<br/>반가워요!</h2>
          <div class="quick-actions">
            <button @click="goToProfile" class="action-btn">
              🔔 내 프로필 보기
            </button>
            <button @click="goToMyLibrary" class="action-btn">
              📚 내 스레드 보기
            </button>
          </div>
        </div>

        <!-- 중앙 나무 -->
        <div class="center-tree">
          <div class="tree-image">
            <img :src="getTreeImage()" :alt="treeInfo.treeStage" />
          </div>
        </div>

        <!-- 오른쪽 카드 -->
        <div class="right-card">
          <!-- 통계 -->
          <div class="stat-item">
            <span class="stat-emoji">📚</span>
            <span class="stat-text">총 누적 {{ statistics.totalReadBooks }}권</span>
          </div>
          <div class="stat-item">
            <span class="stat-emoji">🖊️</span>
            <span class="stat-text">총 스레드 {{ statistics.totalThreads }}개</span>
          </div>
          <div class="stat-item">
            <span class="stat-emoji">🍒</span>
            <span class="stat-text">저장한 글귀 {{ statistics.totalQuotes }}개</span>
          </div>

          <!-- 파이 차트 -->
          <div class="chart-section">
            <div class="chart-wrapper">
              <div class="chart-container">
                <canvas ref="chartCanvas" width="200" height="200"></canvas>
                <!-- 차트 중앙 텍스트 -->
                <div class="chart-center-text">
                  <div class="center-label">{{ topStatus.label }}</div>
                  <div class="center-percent">{{ topStatus.percent }}%</div>
                </div>
              </div>
            </div>
            <!-- 범례 - 나머지 항목들만 표시 -->
            <div class="chart-legend">
              <div
                  v-for="item in otherStatuses"
                  :key="item.key"
                  class="legend-item"
              >
                <span class="legend-line" :class="item.key"></span>
                <span class="legend-text">{{ item.label }} {{ item.percent }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyPageInfoApi } from '@/api/UserApi'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

const router = useRouter()

// State
const loading = ref(false)
const error = ref(null)
const myPageData = ref(null)
const chartCanvas = ref(null)
let chartInstance = null

// Computed
const userName = computed(() => myPageData.value?.nickname || '사용자')

const treeInfo = computed(() => myPageData.value?.treeInfo || {
  readBooksThisMonth: 0,
  treeStage: 'STAGE1'
})

const treeMessage = computed(() => {
  const count = treeInfo.value.readBooksThisMonth

  if (count === 0) {
    return '아직 읽은 책이 없어요!'
  }

  return `이번달 ${count}권 ~~`
})

const statistics = computed(() => myPageData.value?.statistics || {
  totalReadBooks: 0,
  totalThreads: 0,
  totalQuotes: 0
})

const bookStatusRatio = computed(() => myPageData.value?.bookStatusRatio || {
  reading: 0,
  read: 0,
  wish: 0,
  readingPercent: 0,
  readPercent: 0,
  wishPercent: 0
})

// 가장 높은 비율의 항목
const topStatus = computed(() => {
  const ratio = bookStatusRatio.value
  const statuses = [
    { key: 'reading', label: '읽는중', percent: Math.round(ratio.readingPercent) },
    { key: 'read', label: '읽은책', percent: Math.round(ratio.readPercent) },
    { key: 'wish', label: '읽을책', percent: Math.round(ratio.wishPercent) }
  ]

  // 가장 높은 비율 찾기
  const top = statuses.reduce((max, current) =>
          current.percent > max.percent ? current : max
      , statuses[0])

  return top
})

// 나머지 항목들 (가장 높은 것 제외)
const otherStatuses = computed(() => {
  const ratio = bookStatusRatio.value
  const statuses = [
    { key: 'reading', label: '읽는중', percent: Math.round(ratio.readingPercent) },
    { key: 'read', label: '읽은책', percent: Math.round(ratio.readPercent) },
    { key: 'wish', label: '읽을책', percent: Math.round(ratio.wishPercent) }
  ]

  // 가장 높은 것 제외하고 반환
  return statuses
      .filter(item => item.key !== topStatus.value.key)
      .sort((a, b) => b.percent - a.percent) // 높은 순으로 정렬
})

// Methods
const loadMyPageInfo = async () => {
  loading.value = true
  error.value = null

  try {
    console.log('📊 마이페이지 정보 조회 시작')

    const response = await getMyPageInfoApi()
    myPageData.value = response.data

    console.log('✅ 마이페이지 정보 로드 성공:', myPageData.value)

    await nextTick()
    await nextTick()

    setTimeout(() => {
      renderChart()
    }, 100)

  } catch (e) {
    console.error('❌ 마이페이지 정보 로드 실패:', e)
    const errorMessage = e.response?.data?.message || '마이페이지 정보를 불러오는데 실패했습니다.'
    error.value = errorMessage
  } finally {
    loading.value = false
  }
}

const getTreeImage = () => {
  const stage = treeInfo.value.treeStage
  const imageMap = {
    STAGE1: '/src/assets/tree-stage1.svg',
    STAGE2: '/src/assets/tree-stage2.svg',
    STAGE3: '/src/assets/tree-stage3.svg',
    STAGE4: '/src/assets/tree-stage4.svg'
  }
  return imageMap[stage] || imageMap.STAGE1
}

const renderChart = () => {
  console.log('🎨 차트 렌더링 시작')

  if (!chartCanvas.value) {
    console.error('❌ Canvas 요소가 없습니다!')
    return
  }

  if (chartInstance) {
    try {
      chartInstance.destroy()
    } catch (e) {
      console.warn('차트 파괴 중 에러 (무시 가능):', e)
    }
    chartInstance = null
  }

  const ratio = bookStatusRatio.value

  console.log('📊 차트 데이터:', {
    reading: ratio.reading,
    read: ratio.read,
    wish: ratio.wish,
    total: ratio.reading + ratio.read + ratio.wish
  })

  const totalBooks = ratio.reading + ratio.read + ratio.wish

  if (totalBooks === 0) {
    console.log('⚠️ 책이 없어서 차트를 그리지 않습니다')
    return
  }

  try {
    const ctx = chartCanvas.value.getContext('2d')

    if (!ctx) {
      console.error('❌ Canvas context를 가져올 수 없습니다!')
      return
    }

    chartInstance = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: ['읽는중', '읽은책', '읽을책'],
        datasets: [{
          data: [ratio.reading, ratio.read, ratio.wish],
          backgroundColor: [
            '#5fa8d3',  // 읽는중 - 파란색
            '#7cd992',  // 읽은책 - 초록색
            '#ff9aa3'   // 읽을책 - 분홍색
          ],
          borderWidth: 2,
          borderColor: '#fff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: true,
        cutout: '70%',
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            enabled: true,
            callbacks: {
              label: (context) => {
                const label = context.label || ''
                const value = context.parsed || 0
                const total = context.dataset.data.reduce((a, b) => a + b, 0)
                const percent = total > 0 ? ((value / total) * 100).toFixed(1) : 0
                return `${label}: ${value}권 (${percent}%)`
              }
            }
          }
        }
      }
    })

    console.log('✅ 차트 생성 완료')
  } catch (e) {
    console.error('❌ 차트 생성 실패:', e)
  }
}

const goToProfile = () => {
  router.push('/mypage/profile')
}

const goToMyLibrary = () => {
  router.push('/library')
}

onMounted(() => {
  loadMyPageInfo()
})

onUnmounted(() => {
  if (chartInstance) {
    try {
      chartInstance.destroy()
    } catch (e) {
      console.warn('차트 정리 중 에러 (무시 가능):', e)
    }
  }
})
</script>

<style scoped>
/* 전체 레이아웃 */
.mypage-container {
  width: 100%;
  min-height: 100vh;
  background: #fffbf0;
  font-family: "Pretendard", sans-serif;
  padding: 20px;
}

/* 로딩 */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  min-height: 100vh;
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
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  min-height: 100vh;
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

/* 메인 콘텐츠 */
.mypage-content {
  max-width: 1400px;
  margin: 0 auto;
}

/* 상단 메시지 */
.tree-message {
  background: #fff;
  border: 2px solid #df3e3e;
  border-radius: 50px;
  padding: 12px 50px;
  font-size: 18px;
  font-weight: 500;
  color: #df3e3e;
  text-align: center;
  width: fit-content;
  margin: 0 auto 30px auto;
  box-shadow: 0 4px 10px rgba(223, 62, 62, 0.15);
  position: relative;
}

/* 말풍선 꼬리 */
.tree-message::after {
  content: '';
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 12px solid transparent;
  border-right: 12px solid transparent;
  border-top: 20px solid #df3e3e;
}

.tree-message::before {
  content: '';
  position: absolute;
  bottom: -16px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 10px solid transparent;
  border-right: 10px solid transparent;
  border-top: 17px solid #fff;
  z-index: 1;
}

/* 메인 섹션 */
.main-section {
  display: grid;
  grid-template-columns: 280px 1fr 280px;
  gap: 30px;
  align-items: start;
}

/* 왼쪽 카드 */
.left-card {
  background: #fff7e8;
  border-radius: 24px;
  border: 3px solid #c38a52;
  padding: 30px 25px;
  text-align: center;
  box-shadow: 0 8px 15px rgba(0,0,0,0.08);
}

.user-avatar {
  width: 90px;
  height: 90px;
  background: #f7d37a;
  border-radius: 50%;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px auto;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.user-name {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 20px;
  line-height: 1.5;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  border: none;
  padding: 14px 20px;
  border-radius: 25px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
  background: #ffd37c;
  color: #333;
}

.action-btn:hover {
  background: #ffc55c;
  transform: translateY(-2px);
}

/* 중앙 나무 */
.center-tree {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
}

.tree-image {
  width: 100%;
  max-width: 700px;
  height: 600px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.tree-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 오른쪽 카드 */
.right-card {
  background: #fff7e8;
  border-radius: 24px;
  border: 3px solid #c38a52;
  padding: 25px;
  box-shadow: 0 8px 15px rgba(0,0,0,0.08);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px;
  background: #fffdf7;
  border-radius: 15px;
  margin-bottom: 12px;
}

.stat-emoji {
  font-size: 24px;
}

.stat-text {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

/* 차트 섹션 */
.chart-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 2px solid #f2c46c;
}

.chart-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}

.chart-container {
  width: 200px;
  height: 200px;
  position: relative;
}

/* 차트 중앙 텍스트 */
.chart-center-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  pointer-events: none;
}

.center-label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.center-percent {
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.chart-legend {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 차트 범례 - 선 모양 */
.legend-line {
  width: 40px;
  height: 3px;
  border-radius: 2px;
  flex-shrink: 0;
}

.legend-line.reading {
  background: #5fa8d3;
}

.legend-line.read {
  background: #7cd992;
}

.legend-line.wish {
  background: #ff9aa3;
}

.legend-text {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

/* 반응형 */
@media (max-width: 1200px) {
  .main-section {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .tree-image {
    height: 400px;
  }
}

@media (max-width: 768px) {
  .tree-image {
    height: 300px;
  }

  .left-card,
  .right-card {
    padding: 20px;
  }
}
</style>