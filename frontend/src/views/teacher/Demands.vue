<script setup>
import { ref, onMounted } from 'vue'
import { demandApi } from '@/api'

const loading = ref(false)
const demands = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(0)

// 筛选条件
const filterDistrict = ref('')
const filterSubject = ref('')

const districtOptions = [
  '雁塔区', '碑林区', '莲湖区', '新城区', '灞桥区',
  '未央区', '长安区', '临潼区', '高新区', '曲江新区'
]

const statusMap = {
  0: { label: '待发布', class: 'soft-badge-pending' },
  1: { label: '已发布', class: 'soft-badge-published' },
  2: { label: '已关闭', class: 'soft-badge-closed' }
}

onMounted(() => {
  fetchDemands()
})

async function fetchDemands() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (filterDistrict.value) params.district = filterDistrict.value
    if (filterSubject.value.trim()) params.subject = filterSubject.value.trim()

    const data = await demandApi.publicList(params)
    demands.value = data.records || []
    total.value = data.total || 0
    currentPage.value = data.current || 1
    totalPages.value = data.pages || 0
  } catch (err) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchDemands()
}

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchDemands()
}

// 生成页码数组
function getPageNumbers() {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)
  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
}
</script>

<template>
  <div class="max-w-5xl mx-auto py-10 px-4">
    <h1 class="text-2xl font-bold mb-8" style="color: var(--color-text)">需求大厅</h1>

    <!-- 筛选栏 -->
    <div class="soft-card mb-8">
      <div class="flex flex-wrap items-end gap-4">
        <div class="form-group mb-0 flex-1 min-w-[180px]">
          <label class="form-label">区域</label>
          <select v-model="filterDistrict" class="soft-select">
            <option value="">全部区域</option>
            <option v-for="opt in districtOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
        </div>
        <div class="form-group mb-0 flex-1 min-w-[180px]">
          <label class="form-label">科目</label>
          <input v-model="filterSubject" type="text" class="soft-input" placeholder="输入科目关键词"
                 @keyup.enter="handleSearch" />
        </div>
        <button class="soft-btn soft-btn-primary" @click="handleSearch">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
               stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          搜索
        </button>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="flex justify-center py-20">
      <div class="spinner"></div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="demands.length === 0" class="empty-state">
      <svg xmlns="http://www.w3.org/2000/svg" class="mx-auto" width="64" height="64" viewBox="0 0 24 24"
           fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
        <polyline points="14 2 14 8 20 8"/>
        <line x1="16" y1="13" x2="8" y2="13"/>
        <line x1="16" y1="17" x2="8" y2="17"/>
        <polyline points="10 9 9 9 8 9"/>
      </svg>
      <p class="text-base">暂无需求数据</p>
    </div>

    <!-- 需求卡片列表 -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div v-for="item in demands" :key="item.id" class="soft-card">
        <div class="flex items-center justify-between mb-3">
          <span style="color: var(--color-text-muted); font-size: 13px">
            需求编号：{{ item.id }}
          </span>
          <span class="soft-badge" :class="statusMap[item.status]?.class || 'soft-badge-pending'">
            {{ statusMap[item.status]?.label || '未知' }}
          </span>
        </div>

        <h3 class="text-base font-semibold mb-3" style="color: var(--color-text)">
          {{ item.subject || '未指定科目' }}
        </h3>

        <div class="space-y-2" style="font-size: 14px; color: var(--color-text-secondary)">
          <div class="flex items-center gap-2" v-if="item.studentGrade">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 10v6M2 10l10-5 10 5-10 5z"/>
              <path d="M6 12v5c0 2 4 3 6 3s6-1 6-3v-5"/>
            </svg>
            <span>年级：{{ item.studentGrade }}</span>
          </div>
          <div class="flex items-center gap-2" v-if="item.frequency">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
            <span>频次：{{ item.frequency }}</span>
          </div>
          <div class="flex items-center gap-2" v-if="item.budget">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="1" x2="12" y2="23"/>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
            </svg>
            <span>时薪：{{ item.budget }} 元/小时</span>
          </div>
          <div class="flex items-center gap-2" v-if="item.district">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                 stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
            <span>区域：{{ item.district }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="pagination">
      <button class="pagination-btn" :disabled="currentPage <= 1" @click="goToPage(currentPage - 1)">
        上一页
      </button>
      <button v-for="page in getPageNumbers()" :key="page"
              class="pagination-btn" :class="{ active: page === currentPage }"
              @click="goToPage(page)">
        {{ page }}
      </button>
      <button class="pagination-btn" :disabled="currentPage >= totalPages" @click="goToPage(currentPage + 1)">
        下一页
      </button>
      <span style="color: var(--color-text-muted); font-size: 13px; margin-left: 8px">
        共 {{ total }} 条
      </span>
    </div>
  </div>
</template>
