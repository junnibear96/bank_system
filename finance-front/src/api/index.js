import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const api = axios.create({
    baseURL: '/api', // Relative path for both local and prod
    timeout: 5000,
});

// 요청 보내기 전(Request Interceptor): 토큰이 있으면 자동으로 헤더에 넣기
api.interceptors.request.use(
    (config) => {
        const authStore = useAuthStore();
        if (authStore.token) {
            config.headers.Authorization = `Bearer ${authStore.token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// 응답 받은 후(Response Interceptor): 에러 처리 편하게 하기
api.interceptors.response.use(
    (response) => response,
    (error) => {
        // 401 에러(인증 실패) 시 로그아웃 처리 등 가능
        if (error.response?.data?.message) {
            // 백엔드에서 보낸 에러 메시지만 뽑아서 넘겨줌
            return Promise.reject(new Error(error.response.data.message));
        }
        return Promise.reject(error);
    }
);

export default api;
