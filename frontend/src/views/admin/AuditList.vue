<script setup>
import { ref, onMounted, watch } from 'vue'
import { adminApi } from '@/api'
import { useRouter } from 'vue-router'

const router = useRouter()

/* ---- 筛选 & 分页状态 ---- */
const authStatus = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const pages = ref(0)
const loading = ref(false)

/* ---- 数据 ---- */
const records = ref([])

/* ---- 加载列表 ---- */
async function fetchList() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (authStatus.value !== '') params.authStatus = authStatus.value
    const res = await adminApi.auditList(params)
    records.value = res.records || []
    total.value = res.total || 0
    pages.value = res.pages || 0
  } catch {
    records.value = []
  } finally {
    loading.value = false
  }
}

/* ---- 筛选变化时重置到第一页 ---- */
watch(authStatus, () => {
  page.value = 1
  fetchList()
})

onMounted(fetchList)

/* ---- 分页 ---- */
function goPage(p) {
  if (p < 1 || p > pages.value) return
  page.value = p
  fetchList()
}

/* ---- 状态映射 ---- */
const statusMap = {
  0: { label: '待审核', cls: 'soft-badge soft-badge-pending' },
  1: { label: '已通过', cls: 'soft-badge soft-badge-approved' },
  2: { label: '已拒绝', cls: 'soft-badge soft-badge-rejected' }
}

function statusInfo(val) {
  return statusMap[val] || { label: '未知', cls: 'soft-badge' }
}

/* ---- 格式化时间 ---- */
function formatTime(ts) {
  if (!ts) return '-'
  const d = new Date(ts)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/* ---- 跳转详情 ---- */
function goDetail(id) {
  router.push({ name: 'AuditDetail', params: { id } })
}
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 py-8">
    <!-- 标题 -->
    <h1 class="text-2xl font-bold mb-6" style="color: var(--color-text)">教师资质审核</h1>

    <!-- 筛选栏 -->
    <div class="soft-card mb-6 !p-4 flex items-center gap-4 flex-wrap" style="cursor:default"
         @mouseenter="$event.currentTarget.style.transform='none'">
      <label class="text-sm font-medium" style="color: var(--color-text-secondary)">审核状态</label>
      <select v-model="authStatus" class="soft-select" style="width: 180px">
        <option value="">全部</option>
        <option value="0">待审核</option>
        <option value="1">已通过</option>
        <option value="2">已拒绝</option>
      </select>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="flex justify-center py-16">
      <div class="spinner"></div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="records.length === 0" class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round"
              d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m5.231 13.481L15 17.25m-4.5-15H5.625c-.621 0-1.125.504-1.125 1.125v16.5c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
      </svg>
      <p class="text-base">暂无审核记录</p>
    </div>

    <!-- 表格 -->
    <div v-else class="soft-card !p-0 overflow-hidden" style="cursor:default"
         @mouseenter="$event.currentTarget.style.transform='none'">
      <table class="w-full text-sm">
        <thead>
          <tr style="background: var(--color-primary-50)">
            <th class="text-left px-5 py-3 font-medium" style="color: var(--color-text-secondary)">姓名</th>
            <th class="text-left px-5 py-3 font-medium" style="color: var(--color-text-secondary)">大学</th>
            <th class="text-left px-5 py-3 font-medium" style="color: var(--color-text-secondary)">擅长科目</th>
            <th class="text-left px-5 py-3 font-medium" style="color: var(--color-text-secondary)">提交时间</th>
            <th class="text-left px-5 py-3 font-medium" style="color: var(--color-text-secondary)">审核状态</th>
            <th class="text-center px-5 py-3 font-medium" style="color: var(--color-text-secondary)">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in records" :key="item.id"
              class="border-t transition-colors duration-200"
              style="border-color: var(--color-border-light)"
              @mouseenter="$event.currentTarget.style.background='var(--color-primary-50)'"
              @mouseleave="$event.currentTarget.style.background='transparent'">
            <td class="px-5 py-4 font-medium">{{ item.realName || '-' }}</td>
            <td class="px-5 py-4" style="color: var(--color-text-secondary)">{{ item.university || '-' }}</td>
            <td class="px-5 py-4" style="color: var(--color-text-secondary)">{{ item.subjects || '-' }}</td>
            <td class="px-5 py-4" style="color: var(--color-text-muted)">{{ formatTime(item.createTime) }}</td>
            <td class="px-5 py-4">
              <span :class="statusInfo(item.authStatus).cls">{{ statusInfo(item.authStatus).label }}</span>
            </td>
            <td class="px-5 py-4 text-center">
              <button class="soft-btn soft-btn-secondary soft-btn-sm" @click="goDetail(item.id)">
                查看详情
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div v-if="pages > 1" class="pagination">
      <button class="pagination-btn" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
      <template v-for="p in pages" :key="p">
        <button class="pagination-btn" :class="{ active: p === page }" @click="goPage(p)">{{ p }}</button>
      </template>
      <button class="pagination-btn" :disabled="page >= pages" @click="goPage(page + 1)">下一页</button>
    </div>
  </div>
</template>
