<template>
  <div class="dashboard">
    <div class="header-section">
      <h2>👋 안녕하세요, 사용자님</h2>
      <p class="subtitle">오늘의 금융 자산 현황입니다.</p>
    </div>

    <div class="grid-container">
      <div class="card status-card">
        <div class="card-header">
          <h3>🖥️ 시스템 상태</h3>
          <span :class="['status-badge', isConnected ? 'online' : 'offline']">
            {{ isConnected ? '정상 가동' : '연결 끊김' }}
          </span>
        </div>
        <p class="status-text">{{ serverStatus }}</p>
        <button class="btn-primary" @click="checkServer" :disabled="isLoading">
          {{ isLoading ? '연결 중...' : '서버 연결 테스트' }}
        </button>
      </div>

      <div class="card balance-card">
        <h3>💰 총 보유 자산</h3>
        <div class="balance-amount">₩ 0</div>
        <div class="card-actions">
          <button class="btn-secondary">입금</button>
          <button class="btn-secondary">송금</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const serverStatus = ref('서버 연결을 확인해주세요.');
const isConnected = ref(false);
const isLoading = ref(false);

const checkServer = async () => {
  isLoading.value = true;
  try {
    const response = await axios.get('http://localhost:8080/api/health');
    serverStatus.value = response.data;
    isConnected.value = true;
  } catch (error) {
    serverStatus.value = '서버 응답 없음: ' + error.message;
    isConnected.value = false;
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* 헤더 섹션 */
.header-section {
  margin-bottom: 30px;
}

h2 {
  font-size: 2rem;
  margin-bottom: 8px;
  color: var(--secondary-color);
}

.subtitle {
  color: var(--text-sub);
  font-size: 1.1rem;
}

/* 그리드 레이아웃 */
.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); /* 반응형 그리드 */
  gap: 24px;
}

/* 카드 공통 스타일 */
.card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #e5e7eb;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

h3 {
  font-size: 1.25rem;
  margin: 0;
  color: var(--secondary-color);
  font-weight: 600;
}

/* 상태 뱃지 */
.status-badge {
  padding: 4px 12px;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 600;
}

.status-badge.offline {
  background-color: #fee2e2;
  color: #ef4444;
}

.status-badge.online {
  background-color: #d1fae5;
  color: #059669;
}

.status-text {
  color: var(--text-sub);
  margin-bottom: 24px;
  min-height: 24px;
}

/* 버튼 스타일 */
.btn-primary {
  width: 100%;
  padding: 12px;
  background-color: var(--primary-color);
  color: white;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
}

.btn-primary:hover {
  background-color: #1d4ed8;
}

.btn-primary:disabled {
  background-color: #93c5fd;
  cursor: not-allowed;
}

/* 자산 카드 전용 스타일 */
.balance-card {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%); /* 그라데이션 */
  color: white;
}

.balance-card h3 {
  color: rgba(255, 255, 255, 0.9);
}

.balance-amount {
  font-size: 2.5rem;
  font-weight: 800;
  margin: 20px 0;
}

.card-actions {
  display: flex;
  gap: 10px;
}

.btn-secondary {
  flex: 1;
  padding: 10px;
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 8px;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

.btn-secondary:hover {
  background-color: rgba(255, 255, 255, 0.3);
}
</style>
