<template>
  <div class="auth-container">
    <div class="auth-card">
      <div class="auth-header">
        <h2>회원가입</h2>
        <p>간편하게 계정을 생성하세요.</p>
      </div>

      <form @submit.prevent="handleSignup">
        <div class="form-group">
          <label>이름</label>
          <input v-model="name" type="text" placeholder="실명을 입력하세요" required />
        </div>

        <div class="form-group">
          <label>아이디</label>
          <input v-model="username" type="text" placeholder="사용할 아이디" required />
        </div>

        <div class="form-group">
          <label>비밀번호</label>
          <input v-model="password" type="password" placeholder="비밀번호" required />
        </div>

        <button type="submit" class="btn-primary">가입하기</button>
      </form>

      <div class="auth-footer">
        <p>이미 계정이 있으신가요? <a @click="$router.push('/login')">로그인</a></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const username = ref('');
const password = ref('');
const name = ref('');

const handleSignup = async () => {
  try {
    // 백엔드 회원가입 API 호출
    await axios.post('http://localhost:8080/api/auth/signup', {
      username: username.value,
      password: password.value,
      name: name.value
    });
    alert('가입 성공! 로그인해주세요.');
    router.push('/login');
  } catch (error) {
    alert('가입 실패: ' + (error.response?.data || error.message));
  }
};
</script>

<style scoped>
/* LoginView와 동일한 스타일 사용 (재사용성을 위해 별도 CSS 파일로 분리하는 것이 좋으나, 지금은 복붙) */
.auth-container { display: flex; justify-content: center; align-items: center; min-height: 80vh; }
.auth-card { background: white; width: 100%; max-width: 400px; padding: 40px; border-radius: 16px; box-shadow: 0 10px 25px rgba(0,0,0,0.05); border: 1px solid #e5e7eb; }
.auth-header { text-align: center; margin-bottom: 30px; }
.auth-header h2 { font-size: 1.8rem; color: var(--secondary-color); margin-bottom: 10px; }
.auth-header p { color: var(--text-sub); }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; margin-bottom: 8px; font-weight: 500; color: var(--text-main); }
input { width: 100%; padding: 12px; border: 1px solid #d1d5db; border-radius: 8px; font-size: 1rem; box-sizing: border-box; transition: border-color 0.2s; }
input:focus { outline: none; border-color: var(--primary-color); box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1); }
.btn-primary { width: 100%; padding: 14px; background-color: var(--primary-color); color: white; border-radius: 8px; font-weight: 600; font-size: 1rem; margin-top: 10px; }
.btn-primary:hover { background-color: #1d4ed8; }
.auth-footer { margin-top: 20px; text-align: center; font-size: 0.9rem; color: var(--text-sub); }
.auth-footer a { color: var(--primary-color); font-weight: 600; cursor: pointer; text-decoration: underline; }
</style>
