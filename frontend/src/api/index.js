import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  login: data => request.post('/auth/login', data),
  register: data => request.post('/auth/register', data)
}

export const booksApi = {
  getList: params => request.get('/books', { params }),
  getDetail: id => request.get(`/books/${id}`),
  create: data => request.post('/books', data),
  update: (id, data) => request.put(`/books/${id}`, data),
  delete: id => request.delete(`/books/${id}`)
}

export const borrowApi = {
  getMyRecords: params => request.get('/borrow/my', { params }),
  getAllRecords: params => request.get('/borrow/all', { params }),
  borrow: bookId => request.post(`/borrow/${bookId}`),
  return: recordId => request.post(`/borrow/return/${recordId}`)
}

export const usersApi = {
  getList: params => request.get('/users', { params }),
  getDetail: id => request.get(`/users/${id}`),
  update: (id, data) => request.put(`/users/${id}`, data),
  delete: id => request.delete(`/users/${id}`)
}

export default request
