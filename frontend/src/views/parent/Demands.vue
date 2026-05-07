<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { parentApi, demandApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const userStore = useUserStore()
const { showToast } = useToast()

const loading = ref(true)
const demands = ref([])
const parentId = ref(null)
const page = ref(1)
const size = 10
const total = ref(0)

const statusMap = {
  0: { label: '进行中', cls: 'soft-badge-published' },
  1: { label: '已关闭', cls: 'soft-badge-closed' },
  2: { label: '已完成', cls: 'soft-badge-approved' }
}

onMounted(async () => {
  try {
    const profile = await parentApi.getProfile(userStore.user.id)
    parentId.value = profile.id
    await loadDemands()
  } catch {
    showToast('请先完善家长资料', 'warning')
  } finally {
    loading.value = false
  }
})

async function loadDemands() {
  if (!parentId.value) return
  const res = await demandApi.myList(parentId.value, page.value, size)
  demands.value = res.records || res.list || res || []
  total.value = res.total || demands.value.length
}

async function closeDemand(id) {
  if (!confirm('确定要关闭此需求吗？')) return
  await demandApi.close(id, parentId.value)
  showToast('需求已关闭')
  await loadDemands()
}

function prevPage() {
  if (page.value > 1) {
    page.value--
    loadDemands()
  }
}

function nextPage() {
  if (page.value * size < total.value) {
    page.value++
    loadDemands()
  }
}

function formatTime(ts) {
  if (!ts) return ''
  return new Date(ts).toLocaleDateString('zh-CN')
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-2xl font-semibold text-gray-800">我的辅导需求</h1>
      <button class="soft-btn soft-btn-primary" @click="router.push('/parent/demands/create')">发布新需求</button>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="spinner"></div>
    </div>

    <div v-else-if="demands.length === 0" class="empty-state">
      <p class="text-lg mb-2">暂无辅导需求</p>
      <p class="text-sm">点击右上角按钮发布您的第一条需求</p>
    </div>

    <template v-else>
      <div class="grid gap-4">
        <div v-for="d in demands" :key="d.id" class="soft-card">
          <div class="flex items-start justify-between mb-3">
            <div>
              <span class="text-xs text-gray-400">需求编号 #{{ d.id }}</span>
              <h3 class="text-lg font-semibold text-gray-800 mt-1">{{ d.subject }}</h3>
            </div>
            <span class="soft-badge" :class="statusMap[d.status]?.cls || 'soft-badge-pending'">
              {{ statusMap[d.status]?.label || '未知' }}
            </span>
          </div>

          <div class="grid grid-cols-2 gap-x-6 gap-y-2 text-sm text-gray-500 mb-4">
            <p>频次：{{ d.frequency || '-' }}</p>
            <p>时薪：{{ d.pricePerHour ? d.pricePerHour + ' 元/小时' : '-' }}</p>
            <p>区域：{{ d.district || '-' }}</p>
            <p>创建时间：{{ formatTime(d.createTime || d.createdAt) }}</p>
          </div>

          <div v-if="d.status === 0" class="flex justify-end">
            <button class="soft-btn soft-btn-outline soft-btn-sm" @click="closeDemand(d.id)">关闭需求</button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <button class="pagination-btn" :disabled="page <= 1" @click="prevPage">上一页</button>
        <span class="text-sm text-gray-500">第 {{ page }} 页</span>
        <button class="pagination-btn" :disabled="page * size >= total" @click="nextPage">下一页</button>
      </div>
    </template>
  </div>
</template>
