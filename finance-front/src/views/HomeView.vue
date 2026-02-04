<template>
  <div class="dashboard">
    <div class="header-section">
      <h2>👋 안녕하세요, {{ username }}님</h2>
      <p class="subtitle">오늘의 금융 자산 현황입니다.</p>
    </div>

    <div v-if="!authStore.isAuthenticated" class="card center-msg">
      <p>로그인이 필요합니다.</p>
      <button class="btn-primary" @click="$router.push('/login')">로그인 하러가기</button>
    </div>

    <div v-else-if="!account" class="card center-msg">
      <p>아직 계좌가 없으시군요!</p>
      <button class="btn-primary" @click="createAccount">계좌 개설하기</button>
    </div>

    <div v-else class="grid-container">
      <div class="card balance-card">
        <h3>💰 총 보유 자산</h3>
        <div class="balance-amount">₩ {{ Number(account.balance).toLocaleString() }}</div>
        <p class="acc-num">계좌: {{ account.accountNumber }}</p>
        <div class="card-actions">
          <button class="btn-secondary" @click="openDepositModal">입금</button>
          <button class="btn-secondary" @click="openTransferModal">송금</button>
          <button class="btn-secondary" @click="$router.push('/history')">내역</button>
        </div>
      </div>

      <div class="card status-card">
        <div class="card-header">
          <h3>🖥️ 시스템 상태</h3>
          <span class="status-badge online">정상</span>
        </div>
        <p class="status-text">안전하게 보호되고 있습니다.</p>
      </div>
    </div>

    <!-- 입금 모달 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3>입금하기</h3>
        <p>충전할 금액을 입력해주세요.</p>
        
        <input 
          v-model="depositAmount" 
          type="number" 
          placeholder="금액 입력" 
          class="modal-input"
          @keyup.enter="handleDeposit"
        />
        
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeModal">취소</button>
          <button class="btn-confirm" @click="handleDeposit">충전하기</button>
        </div>
      </div>
    </div>

    <!-- 송금 모달 (Placeholder for now) -->
    <!-- 송금 모달 -->
    <div v-if="showTransferModal" class="modal-overlay" @click.self="closeTransferModal">
      <div class="modal-content">
        <h3>송금하기</h3>
        <p>누구에게 얼마를 보낼까요?</p>
        
        <div class="input-group">
          <input 
            v-model="transferTarget" 
            type="text" 
            placeholder="받는 사람 계좌번호 (예: 100-xxx)" 
            class="modal-input"
          />
          <input 
            v-model="transferAmount" 
            type="number" 
            placeholder="보낼 금액" 
            class="modal-input"
            @keyup.enter="handleTransfer"
          />
        </div>
        
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeTransferModal">취소</button>
          <button class="btn-confirm send-btn" @click="handleTransfer">보내기</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '../stores/auth';
import api from '@/api'; // Centralized Axios import

const authStore = useAuthStore();
const account = ref(null);
const username = computed(() => authStore.user?.name || '사용자');

// 모달 상태
const showModal = ref(false); // 입금
const showTransferModal = ref(false); // 송금

const depositAmount = ref('');
const transferTarget = ref('');
const transferAmount = ref('');

// 계좌 조회
const loadAccount = async () => {
  if (!authStore.isAuthenticated) return;
  try {
    const res = await api.get('/account/my');
    account.value = res.data;
  } catch (e) {
    console.log("계좌 없음");
  }
};

// 계좌 생성
const createAccount = async () => {
  try {
    await api.post('/account/create');
    loadAccount();
  } catch (e) {
    alert("계좌 생성 실패");
  }
};

// --- 입금 관련 ---
const openDepositModal = () => {
  depositAmount.value = '';
  showModal.value = true;
};
const closeModal = () => {
  showModal.value = false;
};
const handleDeposit = async () => {
  if (!depositAmount.value || depositAmount.value <= 0) {
    alert("올바른 금액을 입력하세요.");
    return;
  }
  try {
    // DTO format handled by centralized exception handler on error
    await api.post('/account/deposit', {
      amount: Number(depositAmount.value)
    });
    alert(`₩${Number(depositAmount.value).toLocaleString()} 입금 완료!`);
    closeModal();
    loadAccount(); 
  } catch (error) {
    alert("입금 실패: " + (error.message || "오류"));
  }
};

// --- 송금 관련 ---
const openTransferModal = () => {
  transferTarget.value = '';
  transferAmount.value = '';
  showTransferModal.value = true;
};
const closeTransferModal = () => {
  showTransferModal.value = false;
};
const handleTransfer = async () => {
  // 간단한 유효성 검사
  if (!transferTarget.value) {
    alert("받는 사람 계좌번호를 입력해주세요.");
    return;
  }
  if (!transferAmount.value || transferAmount.value <= 0) {
    alert("올바른 금액을 입력해주세요.");
    return;
  }
  // 나한테 보내는지 체크
  if (account.value && transferTarget.value === account.value.accountNumber) {
    alert("내 계좌로는 송금할 수 없습니다. 입금 기능을 이용하세요.");
    return;
  }

  if (!confirm(`${transferTarget.value}님에게\n₩${Number(transferAmount.value).toLocaleString()}원을 보내시겠습니까?`)) {
    return;
  }

  try {
    await api.post('/account/transfer', {
      toAccount: transferTarget.value,
      amount: Number(transferAmount.value)
    });
    alert("송금 성공! 💸");
    closeTransferModal();
    loadAccount(); // 잔액 갱신
  } catch (error) {
    // Centralized error handling returns error.message directly
    alert("에러: " + (error.message || "송금 실패"));
  }
};

onMounted(() => {
  loadAccount();
});
</script>

<style scoped>
/* 기존 스타일 유지 */
.header-section { margin-bottom: 30px; }
h2 { font-size: 2rem; margin-bottom: 8px; color: var(--secondary-color); }
.subtitle { color: var(--text-sub); font-size: 1.1rem; }
.grid-container { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 24px; }
.card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); border: 1px solid #e5e7eb; transition: transform 0.2s; }
.card:hover { transform: translateY(-2px); }
.status-badge { padding: 4px 12px; border-radius: 9999px; font-size: 0.875rem; font-weight: 600; }
.status-badge.online { background-color: #d1fae5; color: #059669; }
.balance-card { background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%); color: white; }
.balance-card h3 { color: rgba(255,255,255,0.9); margin: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.card-header h3 { font-size: 1.25rem; color: var(--secondary-color); margin: 0; font-weight: 600; }

.balance-amount { font-size: 2.5rem; font-weight: 800; margin: 10px 0; }
.acc-num { opacity: 0.8; margin-bottom: 20px; font-size: 0.9rem; }
.card-actions { display: flex; gap: 10px; }

.btn-primary { padding: 12px 20px; background-color: var(--primary-color); color: white; border-radius: 8px; font-weight: 600; }
.btn-primary:hover { background-color: #1d4ed8; }
.btn-secondary { flex: 1; padding: 10px; background-color: rgba(255,255,255,0.2); color: white; border-radius: 8px; font-weight: 600; backdrop-filter: blur(4px); }
.btn-secondary:hover { background-color: rgba(255,255,255,0.3); }

.center-msg { text-align: center; padding: 40px; }
.center-msg p { margin-bottom: 20px; color: var(--text-sub); font-size: 1.1rem; }

/* 모달 스타일 */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 1000; backdrop-filter: blur(5px);
}
.modal-content {
  background: white;
  padding: 40px; /* More breathing room */
  border-radius: 20px; /* Rounder corners */
  width: 90%;
  max-width: 420px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  text-align: center;
  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
.modal-content h3 { margin-top: 0; color: var(--secondary-color); font-size: 1.5rem; }
.modal-content p { color: var(--text-sub); margin-bottom: 20px; }
.modal-input {
  width: 100%;
  padding: 15px;
  font-size: 1.2rem;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  margin-bottom: 24px; /* Increased margin */
  text-align: center;
  box-sizing: border-box; /* Fix width overflow */
  transition: all 0.2s;
}
.modal-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.1); /* Add focus ring */
  outline: none;
}
.modal-actions { display: flex; gap: 10px; }
.btn-confirm { flex: 1; padding: 12px; background-color: var(--primary-color); color: white; border: none; border-radius: 8px; font-weight: 600; }
.btn-confirm:hover { background-color: #1d4ed8; }
.btn-cancel { flex: 1; padding: 12px; background-color: #f3f4f6; color: #4b5563; border: none; border-radius: 8px; font-weight: 600; }
.btn-cancel:hover { background-color: #e5e7eb; }

/* 송금 모달 전용 스타일 추가 */
.input-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}
.send-btn {
  background-color: #ef4444; /* 송금은 돈이 나가니까 빨간색 계열 포인트 */
}
.send-btn:hover {
  background-color: #dc2626;
}
</style>
