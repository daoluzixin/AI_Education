<script setup>
import { ref, onMounted } from 'vue'
import { teacherApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const loading = ref(true)
const hasProfile = ref(false)
const profile = ref(null)

// 审核状态：0-待审核 1-已通过 2-已拒绝
const authStatus = ref(null)
const rejectReason = ref('')

onMounted(async () => {
  try {
    const userId = userStore.user?.id
    if (!userId) {
      loading.value = false
      return
    }
    const data = await teacherApi.getProfile(userId)
    hasProfile.value = true
    profile.value = data
    authStatus.value = data.authStatus ?? 0
    rejectReason.value = data.rejectReason || ''
  } catch (err) {
    // 404 表示还没提交过资料
    if (err.response?.status === 404) {
      hasProfile.value = false
    }
  } finally {
    loading.value = false
  }
})

function goToProfile() {
  router.push('/teacher/profile')
}
</script>

<template>
  <div class="max-w-3xl mx-auto py-10 px-4">
    <h1 class="text-2xl font-bold mb-8" style="color: var(--color-text)">教师工作台</h1>

    <!-- 加载中 -->
    <div v-if="loading" class="flex justify-center py-20">
      <div class="spinner"></div>
    </div>

    <!-- 未提交资料 — 引导卡片 -->
    <div v-else-if="!hasProfile" class="soft-card text-center py-12">
      <div class="mb-4">
        <svg class="mx-auto" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor"
             stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"
             style="color: var(--color-primary); opacity: 0.7">
          <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
          <circle cx="9" cy="7" r="4"/>
          <line x1="19" y1="8" x2="19" y2="14"/>
          <line x1="22" y1="11" x2="16" y2="11"/>
        </svg>
      </div>
      <h2 class="text-lg font-semibold mb-2" style="color: var(--color-text)">完善您的个人资料开始接单</h2>
      <p class="mb-6" style="color: var(--color-text-secondary)">
        提交您的教学资质和个人信息，通过审核后即可浏览需求、接受家教订单。
      </p>
      <button class="soft-btn soft-btn-primary soft-btn-lg" @click="goToProfile">
        填写个人资料
      </button>
    </div>

    <!-- 已提交 — 审核状态卡片 -->
    <template v-else>
      <!-- 待审核 -->
      <div v-if="authStatus === 0" class="soft-card" style="border-left: 4px solid var(--color-warning)">
        <div class="flex items-start gap-4">
          <div class="flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center"
               style="background: #FEF3C7">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#D97706"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
          </div>
          <div>
            <span class="soft-badge soft-badge-pending mb-3 inline-block">待审核</span>
            <h2 class="text-lg font-semibold mb-1" style="color: var(--color-text)">资料正在审核中</h2>
            <p style="color: var(--color-text-secondary)">
              您的资料已提交成功，工作人员正在审核中，请耐心等待。通常审核会在 1-2 个工作日内完成。
            </p>
          </div>
        </div>
      </div>

      <!-- 已通过 -->
      <div v-else-if="authStatus === 1" class="soft-card" style="border-left: 4px solid var(--color-success)">
        <div class="flex items-start gap-4">
          <div class="flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center"
               style="background: #DCFCE7">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#16A34A"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div>
            <span class="soft-badge soft-badge-approved mb-3 inline-block">已通过</span>
            <h2 class="text-lg font-semibold mb-1" style="color: var(--color-text)">恭喜！您的资质已通过审核</h2>
            <p class="mb-4" style="color: var(--color-text-secondary)">
              您现在可以浏览家长发布的需求，开始接单了。
            </p>
            <button class="soft-btn soft-btn-primary" @click="router.push('/teacher/demands')">
              浏览需求大厅
            </button>
          </div>
        </div>
      </div>

      <!-- 已拒绝 -->
      <div v-else-if="authStatus === 2" class="soft-card" style="border-left: 4px solid var(--color-danger)">
        <div class="flex items-start gap-4">
          <div class="flex-shrink-0 w-12 h-12 rounded-full flex items-center justify-center"
               style="background: #FEE2E2">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#DC2626"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="15" y1="9" x2="9" y2="15"/>
              <line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
          </div>
          <div>
            <span class="soft-badge soft-badge-rejected mb-3 inline-block">未通过</span>
            <h2 class="text-lg font-semibold mb-1" style="color: var(--color-text)">很遗憾，您的资料未通过审核</h2>
            <div v-if="rejectReason" class="my-3 p-3 rounded-lg" style="background: #FEF2F2; color: #991B1B; font-size: 14px">
              <span class="font-medium">拒绝原因：</span>{{ rejectReason }}
            </div>
            <p class="mb-4" style="color: var(--color-text-secondary)">
              请根据以上原因修改资料后重新提交。
            </p>
            <button class="soft-btn soft-btn-primary" @click="goToProfile">
              重新提交资料
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
