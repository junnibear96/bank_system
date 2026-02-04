import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import SignupView from '../views/SignupView.vue'
import HistoryView from '../views/HistoryView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView
        },
        {
            path: '/login',
            name: 'login',
            component: LoginView
        },
        {
            path: '/signup',
            name: 'signup',
            component: SignupView
        },
        {
            path: '/history',
            name: 'history',
            component: HistoryView
        }
    ]
})

// 네비게이션 가드 (로그인 안 된 상태로 중요한 페이지 접근 시 차단 기능)
// 필요하다면 주석 해제하여 사용
/*
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  // 만약 인증이 필요한 페이지 목록이 있다면 여기서 체크
  // if (to.name !== 'login' && !authStore.isAuthenticated) next({ name: 'login' })
  // else next()
  next()
})
*/

export default router
