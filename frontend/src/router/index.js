import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { guest: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { guest: true }
  },
  /* ---- 教师端 ---- */
  {
    path: '/teacher',
    component: () => import('@/layouts/TeacherLayout.vue'),
    meta: { requiresAuth: true, role: 'teacher' },
    children: [
      { path: '', name: 'TeacherHome', component: () => import('@/views/teacher/Dashboard.vue') },
      { path: 'profile', name: 'TeacherProfile', component: () => import('@/views/teacher/Profile.vue') },
      { path: 'demands', name: 'TeacherDemands', component: () => import('@/views/teacher/Demands.vue') }
    ]
  },
  /* ---- 家长端 ---- */
  {
    path: '/parent',
    component: () => import('@/layouts/ParentLayout.vue'),
    meta: { requiresAuth: true, role: 'parent' },
    children: [
      { path: '', name: 'ParentHome', component: () => import('@/views/parent/Dashboard.vue') },
      { path: 'profile', name: 'ParentProfile', component: () => import('@/views/parent/Profile.vue') },
      { path: 'students', name: 'StudentManage', component: () => import('@/views/parent/Students.vue') },
      { path: 'demands', name: 'ParentDemands', component: () => import('@/views/parent/Demands.vue') },
      { path: 'demands/create', name: 'CreateDemand', component: () => import('@/views/parent/CreateDemand.vue') },
      { path: 'teachers', name: 'SearchTeachers', component: () => import('@/views/parent/TeacherList.vue') },
      { path: 'teachers/:id', name: 'TeacherDetail', component: () => import('@/views/parent/TeacherDetail.vue') }
    ]
  },
  /* ---- 管理员端 ---- */
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      { path: '', name: 'AdminHome', component: () => import('@/views/admin/AuditList.vue') },
      { path: 'audit/:id', name: 'AuditDetail', component: () => import('@/views/admin/AuditDetail.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

// 全局路由守卫
router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  const role = localStorage.getItem('role') || ''
  const isLoggedIn = !!user

  // 需要认证的页面 — 未登录则跳转登录页
  if (to.matched.some(r => r.meta.requiresAuth)) {
    if (!isLoggedIn) {
      return next({ name: 'Login' })
    }
    // 角色不匹配 — 跳转到对应角色首页
    const requiredRole = to.matched.find(r => r.meta.role)?.meta.role
    if (requiredRole && requiredRole !== role) {
      return next(`/${role}`)
    }
  }

  // 已登录用户访问登录页 — 跳转到对应角色首页
  if (to.name === 'Login' && isLoggedIn && role) {
    return next(`/${role}`)
  }

  next()
})

export default router
