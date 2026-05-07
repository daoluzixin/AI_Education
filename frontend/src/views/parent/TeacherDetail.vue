<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { teacherApi } from '@/api'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()

const loading = ref(true)
const teacher = ref(null)

const teachModeMap = { 1: '上门辅导', 2: '线上辅导', 3: '上门/线上均可' }
const genderMap = { 1: '男', 2: '女' }
const educationMap = { 1: '专科', 2: '本科', 3: '硕士', 4: '博士' }

onMounted(async () => {
  try {
    teacher.value = await teacherApi.getDetail(route.params.id)
  } catch {
    showToast('获取教师信息失败', 'error')
  } finally {
    loading.value = false
  }
})

function splitTags(str) {
  if (!str) return []
  return str.split(/[,，、]/).map(s => s.trim()).filter(Boolean)
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <button class="soft-btn soft-btn-outline soft-btn-sm mb-6" @click="router.push('/parent/teachers')">
      ← 返回列表
    </button>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="spinner"></div>
    </div>

    <div v-else-if="!teacher" class="empty-state">
      <p class="text-lg">未找到该教师信息</p>
    </div>

    <template v-else>
      <!-- 头部卡片 -->
      <div class="soft-card mb-6">
        <div class="flex items-start justify-between">
          <div>
            <h1 class="text-2xl font-semibold text-gray-800 mb-2">{{ teacher.realName || '教师' }}</h1>
            <div class="flex flex-wrap gap-3 text-sm text-gray-500">
              <span v-if="teacher.gender">{{ genderMap[teacher.gender] || '' }}</span>
              <span v-if="teacher.university">{{ teacher.university }}</span>
              <span v-if="teacher.major">{{ teacher.major }}</span>
              <span v-if="teacher.education">{{ educationMap[teacher.education] || teacher.education }}</span>
              <span v-if="teacher.grade">{{ teacher.grade }}</span>
            </div>
          </div>
          <div v-if="teacher.rating" class="text-center ml-6 shrink-0">
            <div class="text-3xl font-bold text-amber-500">{{ teacher.rating }}</div>
            <div class="text-xs text-gray-400 mt-1">评分</div>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- 左侧详情 -->
        <div class="md:col-span-2 space-y-6">
          <!-- 个人简介 -->
          <div v-if="teacher.introduction" class="soft-card">
            <h2 class="text-lg font-semibold text-gray-800 mb-3">个人简介</h2>
            <p class="text-sm text-gray-600 leading-relaxed whitespace-pre-line">{{ teacher.introduction }}</p>
          </div>

          <!-- 家教经验 -->
          <div v-if="teacher.experience" class="soft-card">
            <h2 class="text-lg font-semibold text-gray-800 mb-3">家教经验</h2>
            <p class="text-sm text-gray-600 leading-relaxed whitespace-pre-line">{{ teacher.experience }}</p>
          </div>

          <!-- 擅长科目 -->
          <div v-if="teacher.subjects" class="soft-card">
            <h2 class="text-lg font-semibold text-gray-800 mb-3">擅长科目</h2>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="tag in splitTags(teacher.subjects)"
                :key="tag"
                class="soft-badge soft-badge-published"
              >{{ tag }}</span>
            </div>
          </div>

          <!-- 可教年级 -->
          <div v-if="teacher.teachGrades" class="soft-card">
            <h2 class="text-lg font-semibold text-gray-800 mb-3">可教年级</h2>
            <div class="flex flex-wrap gap-2">
              <span
                v-for="tag in splitTags(teacher.teachGrades)"
                :key="tag"
                class="soft-badge soft-badge-approved"
              >{{ tag }}</span>
            </div>
          </div>
        </div>

        <!-- 右侧信息 -->
        <div class="space-y-6">
          <div class="soft-card">
            <h2 class="text-lg font-semibold text-gray-800 mb-4">服务信息</h2>
            <div class="space-y-3 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-500">服务区域</span>
                <span class="text-gray-800 font-medium">{{ teacher.district || '-' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">授课方式</span>
                <span class="text-gray-800 font-medium">{{ teachModeMap[teacher.teachMode] || '-' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">期望时薪</span>
                <span class="text-gray-800 font-medium">{{ teacher.pricePerHour ? teacher.pricePerHour + ' 元/小时' : '面议' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">评分</span>
                <span class="text-amber-500 font-medium">{{ teacher.rating || '-' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500">完成订单</span>
                <span class="text-gray-800 font-medium">{{ teacher.completedOrders ?? '-' }} 单</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="mt-8 flex justify-center">
        <button class="soft-btn soft-btn-outline soft-btn-lg" @click="router.push('/parent/teachers')">
          ← 返回教师列表
        </button>
      </div>
    </template>
  </div>
</template>
