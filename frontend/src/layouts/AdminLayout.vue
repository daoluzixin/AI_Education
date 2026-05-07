<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { computed } from 'vue'

const userStore = useUserStore()
const router = useRouter()

const navItems = [
  { path: '/admin', label: '审核管理', icon: 'audit', exact: true },
]

const userName = computed(() => userStore.user?.nickname || userStore.user?.username || '管理员')

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="layout-wrapper">
    <!-- 侧边导航栏 -->
    <aside class="sidebar">
      <!-- 顶部品牌 -->
      <div class="sidebar-brand">
        <div class="brand-logo">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="white"
               stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"/>
            <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"/>
          </svg>
        </div>
        <div>
          <h1 class="brand-title">西安家教通</h1>
          <p class="brand-sub">管理后台</p>
        </div>
      </div>

      <!-- 用户信息 -->
      <div class="sidebar-user">
        <div class="user-avatar">{{ userName.charAt(0) }}</div>
        <div class="user-info">
          <p class="user-name">{{ userName }}</p>
          <p class="user-role">管理员账号</p>
        </div>
      </div>

      <!-- 导航列表 -->
      <nav class="sidebar-nav">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          custom
          v-slot="{ isActive, isExactActive, navigate }"
        >
          <button
            @click="navigate"
            :class="['nav-item', { active: item.exact ? isExactActive : isActive }]"
          >
            <!-- Audit / Shield -->
            <svg v-if="item.icon === 'audit'" class="nav-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
            <span>{{ item.label }}</span>
          </button>
        </router-link>
      </nav>

      <!-- 底部登出 -->
      <div class="sidebar-footer">
        <button class="logout-btn" @click="handleLogout">
          <svg class="nav-icon" width="20" height="20" fill="none" stroke="currentColor" viewBox="0 0 24 24" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          <span>退出登录</span>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout-wrapper {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg);
}

/* ===== 侧边栏 ===== */
.sidebar {
  width: 260px;
  background: linear-gradient(180deg, #1E293B 0%, #0F172A 100%);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 50;
  overflow-y: auto;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px 20px;
}

.brand-logo {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #F59E0B, #D97706);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);
  flex-shrink: 0;
}

.brand-title {
  font-size: 17px;
  font-weight: 700;
  color: #F1F5F9;
  letter-spacing: 0.3px;
}

.brand-sub {
  font-size: 12px;
  color: #64748B;
  margin-top: 2px;
}

/* 用户信息 */
.sidebar-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 4px 16px 16px;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #F59E0B, #D97706);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #E2E8F0;
}

.user-role {
  font-size: 12px;
  color: #64748B;
  margin-top: 1px;
}

/* 导航 */
.sidebar-nav {
  flex: 1;
  padding: 0 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #94A3B8;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.nav-item:hover {
  color: #E2E8F0;
  background: rgba(255, 255, 255, 0.06);
}

.nav-item.active {
  color: #FFFFFF;
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.9), rgba(245, 158, 11, 0.7));
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
}

.nav-icon {
  flex-shrink: 0;
  opacity: 0.85;
}

.nav-item.active .nav-icon {
  opacity: 1;
}

/* 底部 */
.sidebar-footer {
  padding: 16px 12px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #64748B;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  color: #F87171;
  background: rgba(248, 113, 113, 0.1);
}

/* ===== 主内容区 ===== */
.main-content {
  flex: 1;
  margin-left: 260px;
  padding: 32px;
  overflow-y: auto;
  min-height: 100vh;
}
</style>
