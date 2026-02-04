<template>
  <div class="history-container">
    <h2>📊 거래 내역</h2>
    <div class="card list-card">
      <div v-if="transactions.length === 0" class="empty-msg">
        거래 내역이 없습니다.
      </div>
      
      <ul v-else class="transaction-list">
        <li v-for="(tx, index) in transactions" :key="index" class="transaction-item">
          <div class="tx-info">
            <span class="tx-date">{{ tx.date }}</span>
            <span class="tx-target">{{ tx.type }} - {{ tx.counterpart }}</span>
          </div>
          <div :class="['tx-amount', tx.amount > 0 ? 'plus' : 'minus']">
            {{ tx.amount > 0 ? '+' : '' }}{{ Number(tx.amount).toLocaleString() }}원
          </div>
        </li>
      </ul>
    </div>
    
    <button class="btn-back" @click="$router.push('/')">메인으로 돌아가기</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const transactions = ref([]);

const fetchHistory = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/account/history');
    transactions.value = res.data;
  } catch (e) {
    console.error("내역 조회 실패", e);
  }
};

onMounted(() => {
  fetchHistory();
});
</script>

<style scoped>
.history-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  color: var(--secondary-color);
  margin-bottom: 20px;
  text-align: center;
}

.list-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
  min-height: 300px;
}

.transaction-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f3f4f6;
}

.transaction-item:last-child {
  border-bottom: none;
}

.tx-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tx-date {
  font-size: 0.85rem;
  color: var(--text-sub);
}

.tx-target {
  font-weight: 600;
  color: var(--text-main);
}

.tx-amount {
  font-weight: 700;
  font-size: 1.1rem;
}

.tx-amount.plus { color: #059669; } /* 입금: 초록색 */
.tx-amount.minus { color: #ef4444; } /* 출금: 빨간색 */

.empty-msg {
  text-align: center;
  color: var(--text-sub);
  margin-top: 100px;
}

.btn-back {
  display: block;
  width: 100%;
  margin-top: 20px;
  padding: 12px;
  background-color: white;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  color: var(--text-main);
  font-weight: 600;
}
.btn-back:hover { background-color: #f9fafb; }
</style>
