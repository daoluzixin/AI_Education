<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { teacherApi } from '@/api'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { showToast } = useToast()

const loading = ref(false)
const teachers = ref([])
const page = ref(1)
const size = 10
const total = ref(0)

const districts = [
  '雁塔区', '碑林区', '莲湖区', '新城区', '灞桥区',
  '未央区', '长安区', '临潼区', '高新区', '曲江新区'
]

const filters = ref({
  district: '',
  subject: '',
  gender: ''
})

const teachModeMap = { 1: '上门', 2: '线上', 3: '均可' }

onMounted(() => {
  doSearch()
})

async function doSearch() {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size
    }
    if (filters.value.district) params.district = filters.value.district
    if (filters.value.subject) params.subject = filters.value.subject
    if (filters.value.gender) params.gender = Number(filters.value.gender)

    const res = await teacherApi.search(params)
    teachers.value = res.records || res.list || res || []
    total.value = res.total || teachers.value.length
  } catch {
    showToast('搜索失败，请重试', 'error')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  doSearch()
}

function prevPage() {
  if (page.value > 1) {
    page.value--
    doSearch()
  }
}

function nextPage() {
  if (page.value * size < total.value) {
    page.value++
    doSearch()
  }
}
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-semibold text-gray-800 mb-8">找老师</h1>

    <!-- 筛选栏 -->
    <div class="soft-card mb-6">
      <div class="flex flex-wrap items-end gap-4">
        <div class="flex-1 min-w-[140px]">
          <label class="form-label">区域</label>
          <select v-model="filters.district" class="soft-select">
            <option value="">全部区域</option>
            <option v-for="d in districts" :key="d" :value="d">{{ d }}</option>
          </select>
        </div>
        <div class="flex-1 min-w-[140px]">
          <label class="form-label">科目</label>
          <input v-model="filters.subject" class="soft-input" placeholder="如：数学" />
        </div>
        <div class="flex-1 min-w-[140px]">
          <label class="form-label">性别</label>
          <select v-model="filters.gender" class="soft-select">
            <option value="">不限</option>
            <option value="1">男</option>
            <option value="2">女</option>
          </select>
        </div>
        <button class="soft-btn soft-btn-primary" @click="handleSearch">搜索</button>
      </div>
    </div>

    <!-- 加载 -->
    <div v-if="loading" class="flex justify-center py-20">
      <div class="spinner"></div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="teachers.length === 0" class="empty-state">
      <p class="text-lg mb-2">暂无匹配的教师</p>
      <p class="text-sm">试试调整筛选条件</p>
    </div>

    <!-- 教师列表 -->
    <template v-else>
      <div class="grid gap-4">
        <div
          v-for="t in teachers"
          :key="t.id"
          class="soft-card cursor-pointer"
          @click="router.push(`/parent/teachers/${t.id}`)"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <h3 class="text-lg font-semibold text-gray-800 mb-1">
                {{ t.realName || '教师' }}
                <span class="text-sm font-normal text-gray-400 ml-2">{{ t.university || '' }}</span>
              </h3>
              <p class="text-sm text-gray-500 mb-2">
                擅长科目：{{ t.subjects || '-' }}
                <span class="mx-2">|</span>
                可教年级：{{ t.teachGrades || '-' }}
              </p>
              <div class="flex flex-wrap gap-3 text-sm text-gray-500">
                <span>区域：{{ t.district || '-' }}</span>
                <span>授课方式：{{ teachModeMap[t.teachMode] || '-' }}</span>
                <span>时薪：{{ t.pricePerHour ? t.pricePerHour + ' 元' : '面议' }}</span>
              </div>
            </div>
            <div class="text-right ml-4 shrink-0">
              <div v-if="t.rating" class="text-lg font-semibold text-amber-500">{{ t.rating }} 分</div>
              <div class="text-xs text-gray-400 mt-1">查看详情 →</div>
            </div>
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
