<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const userStore = useUserStore()
const { showToast } = useToast()

// 登录/注册 模式切换
const isRegister = ref(false)

// 表单数据
const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  role: 1,       // 1=家长 2=教师
  nickname: ''
})

const loading = ref(false)
const errorMsg = ref('')

// 后端 role: 1=家长 2=老师 3=管理员
const roleMap = { 1: 'parent', 2: 'teacher', 3: 'admin' }
const roleLabelMap = { 1: '家长', 2: '教师', 3: '管理员' }

function switchMode() {
  isRegister.value = !isRegister.value
  errorMsg.value = ''
}

async function handleSubmit() {
  errorMsg.value = ''

  // 基础校验
  if (!form.value.username.trim()) {
    errorMsg.value = '请输入用户名'
    return
  }
  if (!form.value.password) {
    errorMsg.value = '请输入密码'
    return
  }

  if (isRegister.value) {
    if (form.value.password.length < 6) {
      errorMsg.value = '密码至少 6 位'
      return
    }
    if (form.value.password !== form.value.confirmPassword) {
      errorMsg.value = '两次密码输入不一致'
      return
    }
  }

  loading.value = true

  try {
    let user
    if (isRegister.value) {
      // 注册
      user = await userApi.register({
        username: form.value.username.trim(),
        password: form.value.password,
        role: form.value.role,
        nickname: form.value.nickname.trim() || undefined
      })
      showToast('注册成功，欢迎加入！', 'success')
    } else {
      // 登录
      user = await userApi.login({
        username: form.value.username.trim(),
        password: form.value.password
      })
      showToast(`欢迎回来，${roleLabelMap[user.role]}！`, 'success')
    }

    // 存储用户信息并跳转
    const frontRole = roleMap[user.role]
    userStore.setUser(user, frontRole)
    router.push(`/${frontRole}`)
  } catch (err) {
    errorMsg.value = isRegister.value ? '注册失败，请重试' : '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page min-h-screen flex items-center justify-center px-4">
    <!-- 背景装饰 -->
    <div class="login-bg">
      <div class="login-orb login-orb-1"></div>
      <div class="login-orb login-orb-2"></div>
    </div>

    <div class="login-card w-full max-w-md py-12 px-10">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl mb-4"
             style="background: linear-gradient(135deg, #8B734A, #A6905C); box-shadow: 0 4px 12px rgba(139,115,74,0.25)">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white"
               stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
          </svg>
        </div>
        <h1 class="text-2xl font-bold" style="color: #2C2418">
          {{ isRegister ? '注册账号' : '欢迎回来' }}
        </h1>
        <p class="mt-2 text-sm" style="color: #7A6E5D">
          {{ isRegister ? '创建您的西安家教通账号' : '登录西安家教通' }}
        </p>
      </div>

      <!-- 表单 -->
      <div class="space-y-4">
        <!-- 用户名 -->
        <div class="form-group" style="margin-bottom: 0">
          <label class="form-label">用户名</label>
          <input
            v-model="form.username"
            type="text"
            class="soft-input"
            placeholder="请输入用户名"
            @keyup.enter="handleSubmit"
            @input="errorMsg = ''"
          />
        </div>

        <!-- 密码 -->
        <div class="form-group" style="margin-bottom: 0">
          <label class="form-label">密码</label>
          <input
            v-model="form.password"
            type="password"
            class="soft-input"
            :placeholder="isRegister ? '请设置密码（至少6位）' : '请输入密码'"
            @keyup.enter="handleSubmit"
            @input="errorMsg = ''"
          />
        </div>

        <!-- 注册模式额外字段 -->
        <template v-if="isRegister">
          <!-- 确认密码 -->
          <div class="form-group" style="margin-bottom: 0">
            <label class="form-label">确认密码</label>
            <input
              v-model="form.confirmPassword"
              type="password"
              class="soft-input"
              placeholder="请再次输入密码"
              @keyup.enter="handleSubmit"
              @input="errorMsg = ''"
            />
          </div>

          <!-- 选择身份 -->
          <div class="form-group" style="margin-bottom: 0">
            <label class="form-label">选择身份</label>
            <div class="flex gap-3">
              <label
                class="flex-1 flex items-center justify-center gap-2 py-3 rounded-xl cursor-pointer transition-all duration-200 border-2"
                :class="form.role === 1
                  ? 'border-blue-400 bg-blue-50 text-blue-700'
                  : 'border-gray-200 bg-white text-gray-500 hover:border-gray-300'"
              >
                <input type="radio" v-model="form.role" :value="1" class="sr-only" />
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
                <span class="text-sm font-medium">我是家长</span>
              </label>
              <label
                class="flex-1 flex items-center justify-center gap-2 py-3 rounded-xl cursor-pointer transition-all duration-200 border-2"
                :class="form.role === 2
                  ? 'border-green-400 bg-green-50 text-green-700'
                  : 'border-gray-200 bg-white text-gray-500 hover:border-gray-300'"
              >
                <input type="radio" v-model="form.role" :value="2" class="sr-only" />
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M22 10v6M2 10l10-5 10 5-10 5z"/>
                  <path d="M6 12v5c3 3 6 3 6 3s3 0 6-3v-5"/>
                </svg>
                <span class="text-sm font-medium">我是教师</span>
              </label>
            </div>
          </div>

          <!-- 昵称(可选) -->
          <div class="form-group" style="margin-bottom: 0">
            <label class="form-label">昵称 <span class="text-xs" style="color: var(--color-text-muted)">（选填）</span></label>
            <input
              v-model="form.nickname"
              type="text"
              class="soft-input"
              placeholder="给自己取个名字吧"
            />
          </div>
        </template>

        <!-- 错误提示 -->
        <p v-if="errorMsg" class="text-sm" style="color: var(--color-danger)">
          {{ errorMsg }}
        </p>

        <!-- 提交按钮 -->
        <button
          class="login-submit-btn w-full"
          :disabled="loading"
          @click="handleSubmit"
        >
          <template v-if="loading">
            <div class="spinner" style="width: 18px; height: 18px; border-width: 2px"></div>
            {{ isRegister ? '注册中...' : '登录中...' }}
          </template>
          <template v-else>{{ isRegister ? '注 册' : '登 录' }}</template>
        </button>
      </div>

      <!-- 切换登录/注册 -->
      <div class="text-center mt-6">
        <span class="text-sm" style="color: var(--color-text-muted)">
          {{ isRegister ? '已有账号？' : '还没有账号？' }}
        </span>
        <button
          class="text-sm font-medium ml-1 bg-transparent border-none cursor-pointer"
          style="color: #8B734A"
          @click="switchMode"
        >
          {{ isRegister ? '立即登录' : '立即注册' }}
        </button>
      </div>

      <!-- 返回首页 -->
      <div class="text-center mt-4">
        <router-link to="/" class="text-sm" style="color: #9A8E7E">
          ← 返回首页
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  background: linear-gradient(170deg, #FAF6F0 0%, #F5EDE4 50%, #F0EBE3 100%);
}
.login-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
}
.login-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
}
.login-orb-1 {
  width: 450px; height: 450px;
  top: -80px; right: -100px;
  background: radial-gradient(circle, #E8D5B8 0%, transparent 70%);
  opacity: 0.45;
}
.login-orb-2 {
  width: 350px; height: 350px;
  bottom: -60px; left: -80px;
  background: radial-gradient(circle, #D4C4A8 0%, transparent 70%);
  opacity: 0.3;
}
.login-card {
  position: relative;
  z-index: 10;
  border-radius: 24px;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(201,184,150,0.2);
  backdrop-filter: blur(16px);
  box-shadow: 0 8px 32px rgba(139,115,74,0.08);
}
.login-submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 0;
  border-radius: 14px;
  border: none;
  font-size: 16px;
  font-weight: 600;
  color: #FFF;
  background: linear-gradient(135deg, #7A6340, #9E8A5E);
  box-shadow: 0 4px 16px rgba(122,99,64,0.25);
  cursor: pointer;
  transition: all 0.25s ease;
}
.login-submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(122,99,64,0.35);
}
.login-submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
