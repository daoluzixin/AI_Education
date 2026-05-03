import axios from 'axios'
import { useToast } from '@/composables/useToast'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 响应拦截器 — 统一处理后端 Result<T> 结构
http.interceptors.response.use(
  (res) => {
    const data = res.data
    if (data.code === 200) {
      return data.data
    }
    const { showToast } = useToast()
    showToast(data.message || '操作失败', 'error')
    return Promise.reject(new Error(data.message))
  },
  (err) => {
    const { showToast } = useToast()
    if (err.response) {
      const msg = err.response.data?.message || `请求失败 (${err.response.status})`
      showToast(msg, 'error')
    } else {
      showToast('网络异常，请检查网络连接', 'error')
    }
    return Promise.reject(err)
  }
)

/* ========== 用户模块 ========== */
export const userApi = {
  login: (data) => http.post('/user/login', data),
  register: (data) => http.post('/user/register', data),
  getById: (id) => http.get(`/user/${id}`),
  getByOpenid: (openid) => http.get(`/user/openid/${openid}`)
}

/* ========== 教师模块 ========== */
export const teacherApi = {
  submitProfile: (userId, data) => http.post(`/teacher/profile?userId=${userId}`, data),
  getProfile: (userId) => http.get('/teacher/profile', { params: { userId } }),
  getDetail: (id) => http.get(`/teacher/profile/${id}`),
  search: (params) => http.get('/teacher/list', { params })
}

/* ========== 家长模块 ========== */
export const parentApi = {
  submitProfile: (userId, data) => http.post(`/parent/profile?userId=${userId}`, data),
  getProfile: (userId) => http.get('/parent/profile', { params: { userId } })
}

/* ========== 学生模块 ========== */
export const studentApi = {
  add: (parentId, data) => http.post(`/student?parentId=${parentId}`, data),
  update: (id, parentId, data) => http.put(`/student/${id}?parentId=${parentId}`, data),
  list: (parentId) => http.get('/student/list', { params: { parentId } })
}

/* ========== 需求模块 ========== */
export const demandApi = {
  create: (parentId, data) => http.post(`/demand?parentId=${parentId}`, data),
  myList: (parentId, page = 1, size = 10) =>
    http.get('/demand/my', { params: { parentId, page, size } }),
  publicList: (params) => http.get('/demand/list', { params }),
  getDetail: (id) => http.get(`/demand/${id}`),
  close: (id, parentId) => http.put(`/demand/${id}/close?parentId=${parentId}`)
}

/* ========== 管理员模块 ========== */
export const adminApi = {
  auditList: (params) => http.get('/admin/teacher/audit/list', { params }),
  auditDetail: (id) => http.get(`/admin/teacher/audit/${id}`),
  approve: (id) => http.put(`/admin/teacher/audit/${id}/approve`),
  reject: (id, reason) => http.put(`/admin/teacher/audit/${id}/reject`, { reason })
}

export default http
