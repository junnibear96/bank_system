<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <h2>로그인</h2>
        <p>금융 서비스 이용을 위해 로그인해주세요.</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>아이디</label>
          <input v-model="username" type="text" placeholder="아이디를 입력하세요" required />
        </div>

        <div class="form-group">
          <label>비밀번호</label>
          <input v-model="password" type="password" placeholder="비밀번호를 입력하세요" required />
        </div>

        <p v-if="errorMsg" class="error-text">{{ errorMsg }}</p>

        <button type="submit" class="btn-primary" :disabled="isLoading">
          {{ isLoading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <div class="auth-footer">
        <p>계정이 없으신가요? <a @click="$router.push('/signup')">회원가입</a></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const username = ref('');
const password = ref('');
const errorMsg = ref('');
const isLoading = ref(false);

const handleLogin = async () => {
  isLoading.value = true;
  errorMsg.value = '';
  
  const success = await authStore.login(username.value, password.value);
  
  if (success) {
    router.push('/'); // 로그인 성공 시 메인으로 이동
  } else {
    errorMsg.value = '아이디 또는 비밀번호가 올바르지 않습니다.';
  }
  isLoading.value = false;
};
</script>

<style scoped>
/* 화면 중앙 정렬 */
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh; /* 화면 높이의 80% */
}

.auth-card {
  background: white;
  width: 100%;
  max-width: 400px;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
  border: 1px solid #e5e7eb;
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;
}

.auth-header h2 {
  font-size: 1.8rem;
  color: var(--secondary-color);
  margin-bottom: 10px;
}

.auth-header p {
  color: var(--text-sub);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: var(--text-main);
}

input {
  width: 100%;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  box-sizing: border-box; /* 패딩 포함 크기 계산 */
  transition: border-color 0.2s;
}

input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.btn-primary {
  width: 100%;
  padding: 14px;
  background-color: var(--primary-color);
  color: white;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  margin-top: 10px;
}

.btn-primary:hover {
  background-color: #1d4ed8;
}

.error-text {
  color: #ef4444;
  font-size: 0.9rem;
  margin-bottom: 15px;
  text-align: center;
}

.auth-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 0.9rem;
  color: var(--text-sub);
}

.auth-footer a {
  color: var(--primary-color);
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
}
</style>
