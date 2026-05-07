<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { parentApi, studentApi, demandApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const userStore = useUserStore()
const { showToast } = useToast()

const loading = ref(true)
const submitting = ref(false)
const parentId = ref(null)
const studentOptions = ref([])

const districts = [
  '雁塔区', '碑林区', '莲湖区', '新城区', '灞桥区',
  '未央区', '长安区', '临潼区', '高新区', '曲江新区'
]

const form = ref({
  studentId: '',
  subject: '',
  teacherCount: 1,
  currentLevel: '',
  frequency: '',
  durationHours: 2,
  preferWeekday: '',
  preferTimeSlot: '',
  pricePerHour: null,
  district: '',
  address: '',
  teachMode: 1,
  teacherGenderReq: null,
  teacherRequirement: ''
})

onMounted(async () => {
  try {
    const profile = await parentApi.getProfile(userStore.user.id)
    parentId.value = profile.id
    const list = await studentApi.list(profile.id)
    studentOptions.value = list || []
  } catch {
    showToast('请先完善家长资料并添加孩子', 'warning')
  } finally {
    loading.value = false
  }
})

async function handleSubmit() {
  if (!form.value.studentId) {
    showToast('请选择学生', 'warning')
    return
  }
  if (!form.value.subject) {
    showToast('请填写辅导科目', 'warning')
    return
  }
  submitting.value = true
  try {
    const data = { ...form.value }
    // teacherGenderReq 为 "null" 字符串时转为 null
    if (!data.teacherGenderReq || data.teacherGenderReq === 'null') {
      data.teacherGenderReq = null
    } else {
      data.teacherGenderReq = Number(data.teacherGenderReq)
    }
    data.teachMode = Number(data.teachMode)
    data.studentId = Number(data.studentId)

    await demandApi.create(parentId.value, data)
    showToast('需求发布成功')
    router.push('/parent/demands')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-semibold text-gray-800 mb-8">发布辅导需求</h1>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="spinner"></div>
    </div>

    <form v-else class="soft-card" @submit.prevent="handleSubmit">
      <!-- 学生选择 -->
      <div class="form-group">
        <label class="form-label">选择学生 <span class="text-red-400">*</span></label>
        <select v-model="form.studentId" class="soft-select">
          <option value="">请选择孩子</option>
          <option v-for="s in studentOptions" :key="s.id" :value="s.id">{{ s.name }}（{{ s.grade }}）</option>
        </select>
      </div>

      <!-- 科目 -->
      <div class="form-group">
        <label class="form-label">辅导科目 <span class="text-red-400">*</span></label>
        <input v-model="form.subject" class="soft-input" placeholder="如：数学，英语（多科目用逗号分隔）" />
      </div>

      <!-- 两列布局 -->
      <div class="grid grid-cols-2 gap-4">
        <div class="form-group">
          <label class="form-label">需要老师数</label>
          <input v-model.number="form.teacherCount" type="number" min="1" class="soft-input" />
        </div>
        <div class="form-group">
          <label class="form-label">当前水平</label>
          <input v-model="form.currentLevel" class="soft-input" placeholder="如：中等偏上" />
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div class="form-group">
          <label class="form-label">辅导频次</label>
          <input v-model="form.frequency" class="soft-input" placeholder="如：每周2次" />
        </div>
        <div class="form-group">
          <label class="form-label">每次时长（小时）</label>
          <input v-model.number="form.durationHours" type="number" min="0.5" step="0.5" class="soft-input" />
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div class="form-group">
          <label class="form-label">偏好星期</label>
          <input v-model="form.preferWeekday" class="soft-input" placeholder="如：周六周日" />
        </div>
        <div class="form-group">
          <label class="form-label">偏好时段</label>
          <input v-model="form.preferTimeSlot" class="soft-input" placeholder="如：下午14:00-16:00" />
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div class="form-group">
          <label class="form-label">期望时薪（元）</label>
          <input v-model.number="form.pricePerHour" type="number" min="0" class="soft-input" placeholder="如：150" />
        </div>
        <div class="form-group">
          <label class="form-label">授课方式</label>
          <select v-model="form.teachMode" class="soft-select">
            <option :value="1">上门辅导</option>
            <option :value="2">线上辅导</option>
            <option :value="3">均可</option>
          </select>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div class="form-group">
          <label class="form-label">所在区域</label>
          <select v-model="form.district" class="soft-select">
            <option value="">请选择区域</option>
            <option v-for="d in districts" :key="d" :value="d">{{ d }}</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">教师性别要求</label>
          <select v-model="form.teacherGenderReq" class="soft-select">
            <option :value="null">不限</option>
            <option :value="1">男</option>
            <option :value="2">女</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">详细地址</label>
        <input v-model="form.address" class="soft-input" placeholder="请输入上课地址" />
      </div>

      <div class="form-group">
        <label class="form-label">对老师的其他要求</label>
        <textarea v-model="form.teacherRequirement" class="soft-textarea" placeholder="如：有耐心、有相关教学经验等"></textarea>
      </div>

      <div class="flex gap-3 mt-6">
        <button type="button" class="soft-btn soft-btn-outline flex-1" @click="router.back()">取消</button>
        <button type="submit" class="soft-btn soft-btn-primary flex-1 soft-btn-lg" :disabled="submitting">
          {{ submitting ? '提交中...' : '发布需求' }}
        </button>
      </div>
    </form>
  </div>
</template>
