<template>
  <div class="app-container">
    <header class="navbar">
      <div class="nav-content">
        <h1 class="logo" @click="$router.push('/')">💸 FinTech Pro</h1>
        <nav>
          <span class="nav-item" @click="$router.push('/')">포트폴리오</span>
          <span class="nav-item" @click="$router.push('/stock')">주식거래</span>
          <button v-if="!authStore.isAuthenticated" class="nav-btn login-btn" @click="$router.push('/login')">로그인</button>
          <button v-else class="nav-btn logout-btn" @click="logout">로그아웃</button>
        </nav>
      </div>
    </header>
    
    <main class="main-content">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterView, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const logout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style>
/* 전역 설정 (Global Styles) */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&family=Noto+Sans+KR:wght@400;500;700&display=swap');

:root {
  --primary-color: #2563eb; /* 쨍한 파란색 */
  --secondary-color: #1e293b; /* 짙은 네이비 */
  --accent-color: #10b981; /* 성공/돈 색상 (초록) */
  --bg-color: #f3f4f6; /* 아주 연한 회색 배경 */
  --card-bg: #ffffff;
  --text-main: #1f2937;
  --text-sub: #6b7280;
}

body {
  margin: 0;
  font-family: 'Inter', 'Noto Sans KR', sans-serif;
  background-color: var(--bg-color);
  color: var(--text-main);
  -webkit-font-smoothing: antialiased;
}

.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

/* 네비게이션 바 스타일 */
.navbar {
  background-color: var(--card-bg);
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-content {
  width: 100%;
  max-width: 1200px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--primary-color);
  cursor: pointer;
  margin: 0;
}

.nav-item {
  margin-left: 20px;
  font-weight: 500;
  color: var(--text-sub);
  cursor: pointer;
  transition: color 0.2s;
}

.nav-item:hover {
  color: var(--primary-color);
}

.nav-btn {
  margin-left: 20px;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 600;
  font-size: 0.9rem;
}

.login-btn {
  background-color: var(--primary-color);
  color: white;
}

.login-btn:hover {
  background-color: #1d4ed8;
}

.logout-btn {
  background-color: transparent;
  border: 1px solid #e5e7eb;
  color: var(--text-sub);
}

.logout-btn:hover {
  background-color: #f3f4f6;
  color: var(--text-main);
}

.main-content {
  flex: 1;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* 버튼 공통 스타일 */
button {
  cursor: pointer;
  border: none;
  font-family: inherit;
  transition: all 0.2s ease;
}
</style>
