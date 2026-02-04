import { defineStore } from 'pinia';
import axios from 'axios';

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
                const response = await axios.post('http://localhost:8080/api/auth/login', {
                    username,
                    password
                });

                this.token = response.data; // The backend returns the raw string token
                localStorage.setItem('token', this.token); // Save to browser

                // Set default header for future requests
                axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`;
                return true;
            } catch (error) {
                console.error("Login failed", error);
                return false;
            }
        },
        async signup(username, password, name) {
            try {
                await axios.post('http://localhost:8080/api/auth/signup', {
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
            delete axios.defaults.headers.common['Authorization'];
        }
    }
});
