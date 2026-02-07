import { defineStore } from 'pinia';
import api from '../api'; // Use the configured API instance
import axios from 'axios'; // Still needed for defaults, or better, use api.defaults?

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        user: null,
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
    },
    actions: {
        async login(username, password) {
            try {
                // Use api instance (relative path)
                const response = await api.post('/auth/login', {
                    username,
                    password
                });

                this.token = response.data;
                localStorage.setItem('token', this.token);

                // Set default header for future requests on the api instance?
                // The api interceptor already handles this!
                // But if we use other axios instances, we might need it.
                // For now, let's rely on the interceptor in api/index.js
                return true;
            } catch (error) {
                console.error("Login failed", error);
                return false;
            }
        },
        async signup(username, password, name) {
            try {
                await api.post('/auth/signup', {
                    username,
                    password,
                    name
                });
                return true;
            } catch (error) {
                console.error('Signup failed', error)
                throw error
            }
        },
        logout() {
            this.token = '';
            this.user = null;
            localStorage.removeItem('token');
            // delete axios.defaults.headers.common['Authorization']; 
            // No need to delete from axios if we use interceptor
        }
    }
});
