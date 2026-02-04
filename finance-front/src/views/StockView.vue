<template>
  <div class="stock-container">
    
    <div v-if="!hasAccount && !loadingAccount" class="welcome-card">
      <div class="icon-wrapper">📈</div>
      <h2>증권 계좌 개설</h2>
      <p>주식 거래를 시작하려면<br>전용 위탁 계좌가 필요합니다.</p>
      <ul class="benefit-list">
        <li>✔️ 실시간 시세 조회 무료</li>
        <li>✔️ 최저 수준의 거래 수수료</li>
        <li>✔️ 1초 만에 개설 완료</li>
      </ul>
      <button class="btn-create" @click="createAccount">계좌 개설하기</button>
    </div>

    <div v-else-if="hasAccount">
      <div class="header-row">
        <h2>📈 주식 거래소</h2>
        <span class="my-acc-badge">내 계좌: {{ myAccountNumber }}</span>
      </div>

      <!-- Trading Dashboard -->
      <div class="card trade-card">
        <div class="search-box">
          <select v-model="selectedExchange" class="exchange-select">
            <option value="">🇺🇸 미국 (US)</option>
            <option value=".KS">🇰🇷 코스피 (KOSPI)</option>
            <option value=".KQ">🇰🇷 코스닥 (KOSDAQ)</option>
          </select>
          
          <input 
            v-model="tickerInput" 
            @keyup.enter="searchStock" 
            placeholder="종목코드 (예: AAPL, 005930)" 
          />
          <button class="btn-primary" @click="searchStock" :disabled="loading">검색</button>
        </div>

        <div v-if="currentStock" class="stock-info">
          <div class="info-header">
            <h3>{{ currentStock.ticker }}</h3>
            <span class="price">${{ Number(currentStock.price).toLocaleString() }}</span>
          </div>
          
          <div class="order-form">
            <input v-model.number="quantity" type="number" min="1" placeholder="수량" />
            <div class="btn-group">
              <button class="btn-buy" @click="handleTrade('buy')">매수 (Buy)</button>
              <button class="btn-sell" @click="handleTrade('sell')">매도 (Sell)</button>
            </div>
          </div>
        </div>
        <p v-else-if="loading" class="loading-text">시세 조회 중...</p>
      </div>

      <!-- Portfolio Table -->
      <div class="card portfolio-card">
        <h3>내 보유 주식</h3>
        <table v-if="portfolio.length > 0" class="portfolio-table">
          <thead>
            <tr>
              <th>종목</th>
              <th>수량</th>
              <th>평단가</th>
              <th>현재가</th>
              <th>수익률</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in portfolio" :key="item.ticker">
              <td>{{ item.ticker }}</td>
              <td>{{ item.quantity }}주</td>
              <td>${{ item.avgPrice.toFixed(2) }}</td>
              <td>${{ item.currentPrice.toFixed(2) }}</td>
              <td :class="getProfitColor(item)">
                {{ calculateProfit(item) }}%
              </td>
            </tr>
          </tbody>
        </table>
        <p v-else class="empty-msg">보유 중인 주식이 없습니다.</p>
      </div>
    </div>

    <div v-else class="loading-screen">
      로딩 중...
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api';

// 상태 변수
const hasAccount = ref(false);
const myAccountNumber = ref('');
const loadingAccount = ref(true);

// Trading State
const tickerInput = ref('');
const currentStock = ref(null);
const quantity = ref(1);
const loading = ref(false);
const portfolio = ref([]);

// 1. Check Account
const checkStockAccount = async () => {
  try {
    const res = await api.get('/stocks/account');
    if (res.data.accountNumber) {
      hasAccount.value = true;
      myAccountNumber.value = res.data.accountNumber;
      loadPortfolio(); // Load portfolio if account exists
    } else {
      hasAccount.value = false;
    }
  } catch (e) {
    console.error("계좌 확인 실패", e);
  } finally {
    loadingAccount.value = false;
  }
};

// 2. Create Account
const createAccount = async () => {
  if (!confirm('증권 계좌를 개설하시겠습니까? (무료)')) return;
  try {
    const res = await api.post('/stocks/account');
    alert(`계좌 개설 완료!\n계좌번호: ${res.data}`);
    checkStockAccount(); 
  } catch (e) {
    alert("개설 실패: " + e.message);
  }
};

// 3. Search Stock
const searchStock = async () => {
  if (!tickerInput.value) return;
  loading.value = true;
  currentStock.value = null;
  try {
    const res = await api.get(`/stocks/price/${tickerInput.value.toUpperCase()}`);
    currentStock.value = res.data;
  } catch (e) {
    alert("주식 정보를 찾을 수 없습니다.");
  } finally {
    loading.value = false;
  }
};

// 4. Trade (Buy/Sell)
const handleTrade = async (type) => {
  if (!currentStock.value || quantity.value <= 0) return;
  if (!confirm(`${currentStock.value.ticker} ${quantity.value}주를 ${type === 'buy' ? '매수' : '매도'}하시겠습니까?`)) return;

  try {
    await api.post(`/stocks/${type}`, {
      ticker: currentStock.value.ticker,
      quantity: quantity.value
    });
    alert("주문 체결 완료!");
    loadPortfolio(); 
  } catch (e) {
    alert("거래 실패: " + (e.message || "오류"));
  }
};

// 5. Load Portfolio
const loadPortfolio = async () => {
  try {
    const res = await api.get('/stocks/portfolio');
    portfolio.value = res.data;
  } catch (e) {
    console.error("포트폴리오 로드 실패", e);
  }
};

// Utilities
const calculateProfit = (item) => {
  if (item.avgPrice === 0) return 0;
  const profit = ((item.currentPrice - item.avgPrice) / item.avgPrice) * 100;
  return profit.toFixed(2);
};

const getProfitColor = (item) => {
  const profit = calculateProfit(item);
  return profit > 0 ? 'text-red' : (profit < 0 ? 'text-blue' : '');
};

onMounted(() => {
  checkStockAccount(); // 시작하자마자 체크
});
</script>

<style scoped>
.stock-container { padding: 20px; max-width: 1200px; margin: 0 auto; }

/* 새로 추가된 스타일 (가입 화면) */
.welcome-card {
  text-align: center;
  background: white;
  padding: 60px 20px;
  border-radius: 20px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.05);
  max-width: 500px;
  margin: 40px auto;
}

.icon-wrapper { font-size: 4rem; margin-bottom: 20px; }
.welcome-card h2 { color: var(--secondary-color); font-size: 1.8rem; margin-bottom: 10px; }
.welcome-card p { color: var(--text-sub); line-height: 1.6; margin-bottom: 30px; }

.benefit-list {
  list-style: none;
  padding: 0;
  margin-bottom: 40px;
  text-align: left;
  display: inline-block;
}
.benefit-list li { margin-bottom: 10px; font-size: 1.1rem; color: var(--text-main); }

.btn-create {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: white;
  padding: 16px 40px;
  border-radius: 12px;
  font-size: 1.1rem;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  transition: transform 0.2s;
}
.btn-create:hover { transform: translateY(-2px); }

/* 계좌 뱃지 스타일 */
.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.my-acc-badge { 
  background: #eff6ff; color: #2563eb; 
  padding: 8px 16px; border-radius: 20px; font-weight: 600; 
  border: 1px solid #bfdbfe;
}

.card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
.trade-card { margin-bottom: 24px; }

/* 검색창 스타일 */
.search-box { 
  display: flex; 
  gap: 10px; 
  margin-bottom: 20px; 
}

/* 새로 추가된 드롭다운 스타일 */
.exchange-select {
  padding: 14px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 1rem;
  background-color: white;
  cursor: pointer;
  outline: none;
  min-width: 130px; /* 글자 잘리지 않게 너비 확보 */
  transition: border-color 0.2s;
}

.exchange-select:focus {
  border-color: var(--primary-color);
}

.search-box input { 
  flex: 1; 
  padding: 14px; 
  border: 2px solid #e5e7eb; 
  border-radius: 12px; 
  font-size: 1rem; 
  transition: border-color 0.2s; 
}
.search-box input:focus { 
  outline: none; 
  border-color: var(--primary-color); 
}

.btn-primary { background: var(--secondary-color); color: white; padding: 0 24px; border-radius: 8px; font-weight: 600; }

/* 주식 정보 스타일 */
.stock-info { border-top: 1px solid #f3f4f6; padding-top: 20px; }
.info-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.info-header h3 { font-size: 2rem; margin: 0; }
.price { font-size: 1.5rem; font-weight: bold; color: var(--primary-color); }

.order-form { display: flex; gap: 10px; align-items: center; }
.order-form input { width: 100px; padding: 10px; border: 1px solid #d1d5db; border-radius: 8px; }
.btn-group { display: flex; gap: 10px; flex: 1; }
.btn-buy { flex: 1; background: #ef4444; color: white; padding: 12px; border-radius: 8px; font-weight: 600; } /* 매수: 빨강 */
.btn-sell { flex: 1; background: #3b82f6; color: white; padding: 12px; border-radius: 8px; font-weight: 600; } /* 매도: 파랑 */

/* 테이블 스타일 */
.portfolio-table { width: 100%; border-collapse: collapse; }
.portfolio-table th { text-align: left; padding: 10px; border-bottom: 2px solid #f3f4f6; color: var(--text-sub); }
.portfolio-table td { padding: 15px 10px; border-bottom: 1px solid #f3f4f6; font-weight: 500; }
.text-red { color: #ef4444; }
.text-blue { color: #3b82f6; }
.loading-text, .empty-msg { text-align: center; color: var(--text-sub); margin-top: 20px; }
</style>
