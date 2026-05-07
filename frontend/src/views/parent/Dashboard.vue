<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { computed } from 'vue'

const router = useRouter()
const userStore = useUserStore()

const userName = computed(() => userStore.user?.nickname || userStore.user?.username || '家长')

const cards = [
  {
    icon: 'profile',
    title: '我的资料',
    desc: '完善个人信息，方便老师了解您的需求',
    path: '/parent/profile',
    color: '#4F6AF6',
    bg: 'linear-gradient(135deg, #EEF0FE, #D9DDFB)'
  },
  {
    icon: 'students',
    title: '孩子管理',
    desc: '添加和管理孩子的基本信息与学习情况',
    path: '/parent/students',
    color: '#22C55E',
    bg: 'linear-gradient(135deg, #DCFCE7, #BBF7D0)'
  },
  {
    icon: 'demand',
    title: '发布需求',
    desc: '发布家教辅导需求，匹配合适的老师',
    path: '/parent/demands/create',
    color: '#F59E0B',
    bg: 'linear-gradient(135deg, #FEF3C7, #FDE68A)'
  },
  {
    icon: 'search',
    title: '找老师',
    desc: '按科目、区域等条件搜索优质教师',
    path: '/parent/teachers',
    color: '#8B5CF6',
    bg: 'linear-gradient(135deg, #EDE9FE, #DDD6FE)'
  }
]

function getGreeting() {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
}
</script>

<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div>
        <h1 class="welcome-title">{{ getGreeting() }}，{{ userName }}</h1>
        <p class="welcome-desc">欢迎回到家长中心，请选择您要进行的操作</p>
      </div>
    </div>

    <!-- 功能卡片 -->
    <div class="card-grid">
      <div
        v-for="card in cards"
        :key="card.path"
        class="feature-card"
        @click="router.push(card.path)"
      >
        <div class="card-icon-wrap" :style="{ background: card.bg }">
          <!-- Profile -->
          <svg v-if="card.icon === 'profile'" width="28" height="28" viewBox="0 0 24 24" fill="none" :stroke="card.color" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
            <circle cx="12" cy="7" r="4"/>
          </svg>
          <!-- Students -->
          <svg v-else-if="card.icon === 'students'" width="28" height="28" viewBox="0 0 24 24" fill="none" :stroke="card.color" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
          <!-- Demand -->
          <svg v-else-if="card.icon === 'demand'" width="28" height="28" viewBox="0 0 24 24" fill="none" :stroke="card.color" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
          <!-- Search -->
          <svg v-else-if="card.icon === 'search'" width="28" height="28" viewBox="0 0 24 24" fill="none" :stroke="card.color" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ card.title }}</h3>
          <p class="card-desc">{{ card.desc }}</p>
        </div>
        <div class="card-arrow">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 900px;
  margin: 0 auto;
}

.welcome-section {
  margin-bottom: 32px;
}

.welcome-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 6px;
}

.welcome-desc {
  font-size: 15px;
  color: var(--color-text-secondary);
}

/* 卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 24px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-soft-sm);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.feature-card:hover {
  box-shadow: var(--shadow-soft-lg);
  transform: translateY(-3px);
  border-color: var(--color-border);
}

.card-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
}

.card-desc {
  font-size: 13px;
  color: var(--color-text-muted);
  line-height: 1.5;
}

.card-arrow {
  color: var(--color-text-muted);
  opacity: 0;
  transform: translateX(-4px);
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.feature-card:hover .card-arrow {
  opacity: 1;
  transform: translateX(0);
}

@media (max-width: 768px) {
  .card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
