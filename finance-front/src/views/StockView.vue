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
      <!-- NEW: Account Summary (Assets) -->
      <div class="card account-summary">
        <div class="summary-header">
          <h3>💰 증권 계좌 자산</h3>
          <span class="badge" @click="copyAccountNumber" title="클릭하여 복사">계좌번호 : {{ myAccountNumber }} 📋</span>
        </div>
        <div class="balance-row">
          <div class="balance-item">
            <span class="label">🇰🇷 원화 예수금</span>
            <span class="value">₩ {{ Number(balanceKRW).toLocaleString() }}</span>
          </div>
          <div class="balance-item">
            <span class="label">🇺🇸 달러 예수금</span>
            <span class="value">$ {{ Number(balanceUSD).toLocaleString() }}</span>
          </div>
        </div>
        <div class="action-buttons">
          <button class="btn-secondary" @click="showTransferModal = true">🏦 은행에서 가져오기</button>
          <button class="btn-secondary" @click="showExchangeModal = true">💱 환전하기</button>
          <button class="btn-secondary" @click="showHistoryModal = true"> 거래 내역</button>
        </div>
      </div>

      <div class="header-row" style="margin-top: 20px;">
        <h2>📈 주식 거래소</h2>
      </div>

      <!-- Trading Dashboard -->
      <div class="card trade-card">
        <div class="search-box">
          <select v-model="selectedExchange" class="exchange-select">
            <option value="">🇺🇸 미국 (US)</option>
            <option value=".KS">🇰🇷 코스피 (KOSPI)</option>
            <option value=".KQ">🇰🇷 코스닥 (KOSDAQ)</option>
          </select>

          <input v-model="tickerInput" @keyup.enter="searchStock" placeholder="종목코드 (예: AAPL, 005930)" />
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

      <!-- Transaction History -->
      <!-- Transaction History Modal (Full Screen / Overlay) -->
      <div v-if="showHistoryModal" class="history-modal-overlay">
        <div class="history-modal-content">
          <div class="history-header">
            <button class="btn-back" @click="showHistoryModal = false">←</button>
            <h2>거래 내역</h2>
            <div style="width: 24px;"></div> <!-- Spacer -->
          </div>

          <!-- Currency Tabs -->
          <div class="history-tabs">
            <div class="tab" :class="{ active: historyTab === 'KRW' }" @click="historyTab = 'KRW'">원화</div>
            <div class="tab" :class="{ active: historyTab === 'USD' }" @click="historyTab = 'USD'">달러</div>
          </div>

          <!-- Balance Info in History -->
          <div class="history-balance">
            <span class="label">주문 가능 금액</span>
            <span class="value">
              {{ historyTab === 'KRW' ? '₩' : '$' }}
              {{ historyTab === 'KRW' ? Number(balanceKRW).toLocaleString() : Number(balanceUSD).toLocaleString() }}
            </span>
          </div>

          <!-- Filter/Month Selection (Placeholder) -->
          <div class="history-filter">
            <div class="filter-chips">
              <span class="chip active">거래</span>
              <span class="chip">환전</span>
              <span class="chip">입출금</span>
            </div>
          </div>

          <!-- Transaction List -->
          <div class="history-list">
            <div v-if="filteredTransactions.length === 0" class="empty-history">
              거래 내역이 없습니다.
            </div>
            <div v-else v-for="t in filteredTransactions" :key="t.id" class="history-item">
              <div class="item-left">
                <div class="item-title">{{ t.type === 'DEPOSIT' ? '입금' : (t.type === 'EXCHANGE' ? '환전' : '거래') }}</div>
                <div class="item-date">{{ new Date(t.date).toLocaleString([], {
                  month: 'short', day: 'numeric', hour:
                    '2-digit', minute: '2-digit'
                }) }}</div>
              </div>
              <div class="item-right">
                <div class="item-amount" :class="t.type === 'DEPOSIT' || t.amount > 0 ? 'plus' : 'minus'">
                  {{ t.type === 'DEPOSIT' ? '+' : '' }}{{ historyTab === 'KRW' ? '₩' : '$' }}{{
                    Number(t.amount).toLocaleString() }}
                </div>
                <div class="item-balance-after">
                  {{ historyTab === 'KRW' ? '₩' : '$' }}{{ Number(t.amount).toLocaleString() }}
                  <!-- Placeholder for balance after -->
                </div>
              </div>
            </div>
          </div>

          <!-- Bottom Action Buttons -->
          <div class="history-actions">
            <button class="btn-action dark" @click="showExchangeModal = true">원화로 환전</button>
            <button class="btn-action blue" @click="showExchangeModal = true">달러로 환전</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Loading Screen -->
    <div v-else class="loading-screen">
      로딩 중...
    </div>

    <!-- Modals -->
    <div v-if="showTransferModal" class="modal-overlay" @click.self="showTransferModal = false">
      <div class="modal-content">
        <h3>예수금 충전</h3>
        <p>은행 계좌 잔액을 증권 계좌로 옮깁니다.</p>

        <div class="balance-info">
          <p>현재 은행 잔액: <strong>₩ {{ Number(balanceBank).toLocaleString() }}</strong></p>
          <p v-if="transferAmount">이체 후 잔액: <strong>₩ {{ Number(remainingBankBalance).toLocaleString() }}</strong></p>
        </div>

        <input v-model="transferAmount" type="number" placeholder="이체할 금액 (원)" class="modal-input" />
        <button class="btn-confirm" @click="handleTransfer">이체하기</button>
      </div>
    </div>

    <div v-if="showExchangeModal" class="modal-overlay" @click.self="showExchangeModal = false">
      <div class="modal-content">
        <h3>💱 환전 ({{ exchangeDirection === 'KRW_TO_USD' ? 'KRW → USD' : 'USD → KRW' }})</h3>

        <!-- Direction Toggle -->
        <!-- Direction Toggle -->
        <div class="toggle-group">
          <button :class="{ active: exchangeDirection === 'KRW_TO_USD' }" @click="exchangeDirection = 'KRW_TO_USD'">원화 →
            달러</button>
          <button :class="{ active: exchangeDirection === 'USD_TO_KRW' }" @click="exchangeDirection = 'USD_TO_KRW'">달러 →
            원화</button>
        </div>

        <p v-if="exchangeDirection === 'KRW_TO_USD'" class="info-text">현재 환율을 적용하여 달러로 바꿉니다.</p>
        <p v-else class="info-text">보유한 달러를 원화로 바꿉니다.</p>

        <!-- Source Selection (Only for KRW -> USD) -->
        <!-- Source Selection (Only for KRW -> USD) -->
        <div v-if="exchangeDirection === 'KRW_TO_USD'" class="source-select">
          <p class="section-label">출금 계좌 선택</p>

          <label class="radio-label" :class="{ selected: exchangeSource === 'BANK' }">
            <div class="radio-content">
              <input type="radio" v-model="exchangeSource" value="BANK">
              <span class="account-name">은행 계좌</span>
            </div>
            <span class="account-bal">₩ {{ Number(balanceBank).toLocaleString() }}</span>
          </label>

          <label class="radio-label" :class="{ selected: exchangeSource === 'STOCK' }">
            <div class="radio-content">
              <input type="radio" v-model="exchangeSource" value="STOCK">
              <span class="account-name">증권 계좌</span>
            </div>
            <span class="account-bal">₩ {{ Number(balanceKRW).toLocaleString() }}</span>
          </label>
        </div>

        <div v-else class="source-select">
          <p class="section-label">출금 (보유 달러)</p>
          <div class="radio-label selected" style="cursor: default;">
            <div class="radio-content">
              <span class="account-name">증권 계좌</span>
            </div>
            <span class="account-bal" style="color: #2563eb; font-weight: bold;">$ {{
              Number(balanceUSD).toLocaleString() }}</span>
          </div>
        </div>

        <input v-model="exchangeAmount" type="number"
          :placeholder="exchangeDirection === 'KRW_TO_USD' ? '환전할 원화 금액' : '환전할 달러 금액'" class="modal-input" />

        <!-- Rate & Estimate -->
        <!-- Rate & Estimate -->
        <div class="exchange-info">
          <p><strong>현재 환율:</strong> 약 {{ Number(currentExchangeRate).toLocaleString() }}원/$ <span class="sub-text">(실시간
              10초마다 업데이트)</span></p>
          <p v-if="exchangeAmount">
            <strong>예상 결과:</strong>
            <span class="result-highlight">
              {{ exchangeDirection === 'KRW_TO_USD' ? '$' : '₩' }} {{ calculateExchangeEstimate(exchangeAmount) }}
            </span>
          </p>
        </div>

        <button class="btn-confirm" @click="handleExchange">환전하기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import api from '@/api';

// 상태 변수
const hasAccount = ref(false);
const myAccountNumber = ref('');
const loadingAccount = ref(true);
const balanceKRW = ref(0);
const balanceUSD = ref(0);
const balanceBank = ref(0); // Bank Balance

// Trading State
const tickerInput = ref('');
const selectedExchange = ref('');
const currentStock = ref(null);
const quantity = ref(1);
const loading = ref(false);
const portfolio = ref([]);
const transactions = ref([]); // Transaction History

// Modals
const showTransferModal = ref(false);
const showExchangeModal = ref(false);
const showHistoryModal = ref(false);
const historyTab = ref('KRW'); // 'KRW' or 'USD'
const transferAmount = ref('');
const exchangeAmount = ref('');

// Exchange State
const exchangeDirection = ref('KRW_TO_USD'); // 'KRW_TO_USD' or 'USD_TO_KRW'
const exchangeSource = ref('BANK'); // 'BANK' or 'STOCK'
const currentExchangeRate = ref(1300); // Default fallback
let exchangeRateInterval = null;

// Watch Modal to start/stop polling
watch(showExchangeModal, (newVal) => {
  if (newVal) {
    fetchExchangeRate();
    exchangeRateInterval = setInterval(fetchExchangeRate, 10000); // Poll every 10s
  } else {
    if (exchangeRateInterval) clearInterval(exchangeRateInterval);
  }
});

const fetchExchangeRate = async () => {
  try {
    const res = await api.get('/stocks/price/KRW=X');
    if (res.data && res.data.price) {
      currentExchangeRate.value = res.data.price;
    }
  } catch (e) {
    console.error("환율 조회 실패", e);
  }
};

// 1. Check Account
const checkStockAccount = async () => {
  try {
    const res = await api.get('/stocks/account');
    if (res.data.accountNumber) {
      hasAccount.value = true;
      myAccountNumber.value = res.data.accountNumber;
      balanceKRW.value = res.data.balanceKRW;
      balanceUSD.value = res.data.balanceUSD;
      balanceBank.value = res.data.bankBalance !== undefined ? res.data.bankBalance : 0; // Prevent NaN
      loadPortfolio();
      loadTransactions(); // Load History 
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
  if (!tickerInput.value || !tickerInput.value.trim()) return;
  loading.value = true;
  currentStock.value = null;

  try {
    const ticker = tickerInput.value.trim().toUpperCase();
    const fullTicker = ticker + selectedExchange.value;

    // 1. Get Current Price
    const res = await api.get(`/stocks/price/${fullTicker}`);
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
    // Update Balances after trade
    checkStockAccount();
  } catch (e) {
    alert("거래 실패: " + (e.response?.data || e.message));
  }
};

// 5. Transfer (Bank -> Stock)
const handleTransfer = async () => {
  try {
    await api.post('/stocks/transfer/deposit', { amount: transferAmount.value });
    alert("충전 완료!");
    showTransferModal.value = false;
    transferAmount.value = '';
    checkStockAccount(); // Refresh
  } catch (e) { alert("이체 실패: " + (e.response?.data || e.message)); }
};

// 6. Exchange
const handleExchange = async () => {
  try {
    await api.post('/stocks/exchange', {
      amount: exchangeAmount.value,
      sourceType: exchangeSource.value,
      direction: exchangeDirection.value
    });
    alert("환전 완료!");
    showExchangeModal.value = false;
    exchangeAmount.value = '';
    checkStockAccount(); // Refresh
  } catch (e) { alert("환전 실패: " + (e.response?.data || e.message)); }
};

const calculateExchangeEstimate = (amount) => {
  const rate = currentExchangeRate.value;
  if (exchangeDirection.value === 'KRW_TO_USD') {
    const est = amount / rate;
    return est < 0.01 ? est.toPrecision(3) : est.toFixed(2);
  } else {
    return Math.floor(amount * rate).toLocaleString();
  }
};

// Load Portfolio
const loadPortfolio = async () => {
  try {
    const res = await api.get('/stocks/portfolio');
    portfolio.value = res.data;
  } catch (e) {
    console.error("포트폴리오 로드 실패", e);
  }
};

// Load Transactions
const loadTransactions = async () => {
  try {
    const res = await api.get('/stocks/transactions');
    transactions.value = res.data;
  } catch (e) {
    console.error("거래 내역 로드 실패", e);
  }
};

const filteredTransactions = computed(() => {
  return transactions.value.filter(t => {
    if (historyTab.value === 'KRW') {
      return t.currency === 'KRW' || t.currency === 'KRW_TO_USD'; // Include Exchanges in KRW view? Maybe separate or both
    } else {
      return t.currency === 'USD' || t.currency === 'USD_TO_KRW';
    }
  });
});

// Utilities
const remainingBankBalance = computed(() => {
  const amount = Number(transferAmount.value) || 0;
  return balanceBank.value - amount;
});

const calculateProfit = (item) => {
  if (item.avgPrice <= 0) return 0;
  const profit = ((item.currentPrice - item.avgPrice) / item.avgPrice) * 100;
  return profit.toFixed(2);
};

const getProfitColor = (item) => {
  const profit = calculateProfit(item);
  return profit > 0 ? 'text-red' : (profit < 0 ? 'text-blue' : '');
};

// Copy Account Number
const copyAccountNumber = async () => {
  if (!myAccountNumber.value) return;
  try {
    await navigator.clipboard.writeText(myAccountNumber.value);
    alert('계좌번호가 복사되었습니다!');
  } catch (err) {
    console.error('복사 실패:', err);
  }
};

onMounted(() => {
  checkStockAccount();
});
</script>

<style scoped>
.stock-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* Welcome Card */
.welcome-card {
  text-align: center;
  background: white;
  padding: 60px 20px;
  border-radius: 20px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  max-width: 500px;
  margin: 40px auto;
}

.icon-wrapper {
  font-size: 4rem;
  margin-bottom: 20px;
}

.welcome-card h2 {
  color: var(--secondary-color);
  font-size: 1.8rem;
  margin-bottom: 10px;
}

.welcome-card p {
  color: var(--text-sub);
  line-height: 1.6;
  margin-bottom: 30px;
}

.benefit-list {
  list-style: none;
  padding: 0;
  margin-bottom: 40px;
  text-align: left;
  display: inline-block;
}

.benefit-list li {
  margin-bottom: 10px;
  font-size: 1.1rem;
  color: var(--text-main);
}

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

.btn-create:hover {
  transform: translateY(-2px);
}

/* Account Summary (Assets) */
.card.account-summary {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
  color: white;
  margin-bottom: 20px;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.badge {
  background: rgba(255, 255, 255, 0.15);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 1rem;
  font-weight: 500;
  letter-spacing: 0.5px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(4px);
  cursor: pointer;
  transition: background 0.2s;
}

.badge:hover {
  background: rgba(255, 255, 255, 0.25);
}

.balance-row {
  display: flex;
  gap: 40px;
  margin-bottom: 20px;
}

.balance-item {
  display: flex;
  flex-direction: column;
}

.balance-item .label {
  font-size: 0.9rem;
  color: #94a3b8;
  margin-bottom: 5px;
}

.balance-item .value {
  font-size: 1.8rem;
  font-weight: bold;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 10px 15px;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.2s;
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.2);
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.my-acc-badge {
  background: #eff6ff;
  color: #2563eb;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: 600;
  border: 1px solid #bfdbfe;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.trade-card {
  margin-bottom: 24px;
}

/* Search Box */
.search-box {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.exchange-select {
  padding: 14px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 1rem;
  background-color: white;
  cursor: pointer;
  outline: none;
  min-width: 130px;
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
}

.search-box input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.start-box .btn-primary {
  background: var(--secondary-color);
  color: white;
  padding: 0 24px;
  border-radius: 8px;
  font-weight: 600;
}

/* Stock Info */
.stock-info {
  border-top: 1px solid #f3f4f6;
  padding-top: 20px;
}

.info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.info-header h3 {
  font-size: 2rem;
  margin: 0;
}

.price {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--primary-color);
}

.order-form {
  display: flex;
  gap: 10px;
  align-items: center;
}

.order-form input {
  width: 100px;
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
}

.btn-group {
  display: flex;
  gap: 10px;
  flex: 1;
}

.btn-buy {
  flex: 1;
  background: #ef4444;
  color: white;
  padding: 12px;
  border-radius: 8px;
  font-weight: 600;
}

.btn-sell {
  flex: 1;
  background: #3b82f6;
  color: white;
  padding: 12px;
  border-radius: 8px;
  font-weight: 600;
}

/* Portfolio Table */
.portfolio-table {
  width: 100%;
  border-collapse: collapse;
}

.portfolio-table th {
  text-align: left;
  padding: 10px;
  border-bottom: 2px solid #f3f4f6;
  color: var(--text-sub);
}

.portfolio-table td {
  padding: 15px 10px;
  border-bottom: 1px solid #f3f4f6;
  font-weight: 500;
}

.text-red {
  color: #ef4444;
}

.text-blue {
  color: #3b82f6;
}

.loading-text,
.empty-msg {
  text-align: center;
  color: var(--text-sub);
  margin-top: 20px;
}

/* Modals */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 16px;
  width: 400px;
  text-align: center;
}

.modal-content h3 {
  margin-top: 0;
  margin-bottom: 10px;
}

.modal-content p {
  color: #64748b;
  margin-bottom: 20px;
}

.modal-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 1.1rem;
  box-sizing: border-box;
  /* Fix width overflow */
}

.hint {
  font-size: 0.9rem;
  color: #94a3b8;
  margin-top: -15px;
  margin-bottom: 20px;
}

.btn-confirm {
  width: 100%;
  background: #2563eb;
  color: white;
  padding: 12px;
  border-radius: 8px;
  font-weight: 600;
}

.balance-info {
  background: #f8fafc;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: left;
}

.balance-info p {
  margin: 5px 0;
  font-size: 0.95rem;
  color: #334155;
}

.balance-info strong {
  color: #0f172a;
}

.badge-blue {
  background: #dbeafe;
  color: #1e40af;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.85rem;
}

.badge-green {
  background: #dcfce7;
  color: #166534;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.85rem;
}

/* History Modal (Dark Theme) */
.history-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: white;
  z-index: 2000;
  display: flex;
  flex-direction: column;
}

.history-modal-content {
  background: #121212;
  color: white;
  height: 100%;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.btn-back {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
}

.history-header h2 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 500;
}

.history-tabs {
  display: flex;
  border-bottom: 1px solid #333;
  margin-bottom: 20px;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 10px;
  color: #888;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: 0.2s;
}

.tab.active {
  color: white;
  border-bottom-color: white;
  font-weight: bold;
}

.history-balance {
  margin-bottom: 25px;
}

.history-balance .label {
  display: block;
  color: #aaa;
  font-size: 0.9rem;
  margin-bottom: 5px;
}

.history-balance .value {
  font-size: 2rem;
  font-weight: bold;
}

.history-filter {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  color: #888;
}

.filter-chips {
  display: flex;
  gap: 8px;
}

.chip {
  padding: 6px 14px;
  border-radius: 20px;
  background: #222;
  font-size: 0.9rem;
  cursor: pointer;
}

.chip.active {
  background: #333;
  color: white;
  border: 1px solid #555;
}

.history-list {
  flex: 1;
  overflow-y: auto;
}

.history-item {
  display: flex;
  justify-content: space-between;
  padding: 15px 0;
  border-bottom: 1px solid #222;
}

.item-title {
  font-size: 1rem;
  margin-bottom: 4px;
}

.item-date {
  font-size: 0.8rem;
  color: #666;
}

.item-amount {
  font-size: 1.1rem;
  font-weight: bold;
  text-align: right;
  margin-bottom: 4px;
}

.item-amount.plus {
  color: #f43f5e;
}

/* Red for positive/deposit */
.item-amount.minus {
  color: white;
}

.item-balance-after {
  font-size: 0.8rem;
  color: #666;
  text-align: right;
}

.empty-history {
  text-align: center;
  color: #555;
  margin-top: 50px;
}

.history-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #222;
}

.btn-action {
  flex: 1;
  padding: 14px;
  border-radius: 12px;
  font-weight: bold;
  border: none;
  cursor: pointer;
}

.btn-action.dark {
  background: #333;
  color: white;
}

.btn-action.blue {
  background: #3b82f6;
  color: white;
}
</style>

<style scoped>
/* Exchange Modal Styling */
.info-text {
  color: #64748b;
  margin-bottom: 20px;
  font-size: 0.95rem;
}

.section-label {
  text-align: left;
  font-weight: 600;
  margin-bottom: 10px;
  color: #1e293b;
  font-size: 1rem;
}

.toggle-group {
  display: flex;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 12px;
  margin-bottom: 24px;
}

.toggle-group button {
  flex: 1;
  padding: 10px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: 0.2s;
}

.toggle-group button.active {
  background: white;
  color: #2563eb;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.source-select {
  margin-bottom: 20px;
  text-align: left;
}

.radio-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: 0.2s;
}

.radio-label:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
}

.radio-label input {
  margin-right: 12px;
  transform: scale(1.2);
  accent-color: #2563eb;
}

.radio-label.selected {
  border-color: #2563eb;
  background: #eff6ff;
}

.radio-content {
  display: flex;
  align-items: center;
}

.account-name {
  font-weight: 500;
  color: #1e293b;
  margin-right: 8px;
}

.account-bal {
  color: #64748b;
  font-size: 0.9rem;
}

.exchange-info {
  background: #f8fafc;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 20px;
  border: 1px solid #e2e8f0;
  text-align: left;
}

.exchange-info p {
  margin: 5px 0;
  color: #334155;
  font-size: 0.95rem;
}

.exchange-info strong {
  color: #0f172a;
}

.sub-text {
  font-size: 0.8rem;
  color: #64748b;
}

.result-highlight {
  color: #2563eb;
  font-size: 1.1rem;
  margin-left: 5px;
  font-weight: bold;
}
</style>
