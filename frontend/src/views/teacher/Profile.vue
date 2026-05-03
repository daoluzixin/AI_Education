<script setup>
import { ref, onMounted } from 'vue'
import { teacherApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'

const userStore = useUserStore()
const { showToast } = useToast()

const loading = ref(true)
const submitting = ref(false)
const isEdit = ref(false)

const form = ref({
  realName: '',
  gender: '',
  university: '',
  major: '',
  educationLevel: '',
  grade: '',
  selfIntro: '',
  teachingExperience: '',
  subjects: '',
  gradeRange: '',
  district: '',
  detailAddress: '',
  teachMode: '',
  pricePerHour: null
})

const genderOptions = [
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]

const educationOptions = ['本科', '硕士', '博士']

const gradeOptions = ['大一', '大二', '大三', '大四', '研一', '研二', '研三']

const districtOptions = [
  '雁塔区', '碑林区', '莲湖区', '新城区', '灞桥区',
  '未央区', '长安区', '临潼区', '高新区', '曲江新区'
]

const teachModeOptions = [
  { label: '上门', value: 1 },
  { label: '线上', value: 2 },
  { label: '均可', value: 3 }
]

onMounted(async () => {
  try {
    const userId = userStore.user?.id
    if (!userId) {
      loading.value = false
      return
    }
    const data = await teacherApi.getProfile(userId)
    if (data) {
      isEdit.value = true
      Object.keys(form.value).forEach((key) => {
        if (data[key] !== undefined && data[key] !== null) {
          form.value[key] = data[key]
        }
      })
    }
  } catch (err) {
    // 404 表示还没提交过，保持空表单
    if (err.response?.status !== 404) {
      console.error('获取资料失败', err)
    }
  } finally {
    loading.value = false
  }
})

async function handleSubmit() {
  // 基础校验
  if (!form.value.realName?.trim()) {
    showToast('请填写真实姓名', 'warning')
    return
  }
  if (!form.value.gender) {
    showToast('请选择性别', 'warning')
    return
  }
  if (!form.value.university?.trim()) {
    showToast('请填写大学名称', 'warning')
    return
  }
  if (!form.value.district) {
    showToast('请选择服务区域', 'warning')
    return
  }

  submitting.value = true
  try {
    const userId = userStore.user?.id
    await teacherApi.submitProfile(userId, form.value)
    showToast(isEdit.value ? '资料更新成功' : '资料提交成功，请等待审核', 'success')
  } catch (err) {
    // 错误已由拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto py-10 px-4">
    <h1 class="text-2xl font-bold mb-2" style="color: var(--color-text)">
      {{ isEdit ? '编辑个人资料' : '填写个人资料' }}
    </h1>
    <p class="mb-8" style="color: var(--color-text-secondary)">
      请认真填写以下信息，提交后将由管理员审核。
    </p>

    <!-- 加载中 -->
    <div v-if="loading" class="flex justify-center py-20">
      <div class="spinner"></div>
    </div>

    <!-- 表单 -->
    <form v-else class="soft-card" @submit.prevent="handleSubmit">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- 真实姓名 -->
        <div class="form-group">
          <label class="form-label">真实姓名 <span style="color: var(--color-danger)">*</span></label>
          <input v-model="form.realName" type="text" class="soft-input" placeholder="请输入真实姓名" />
        </div>

        <!-- 性别 -->
        <div class="form-group">
          <label class="form-label">性别 <span style="color: var(--color-danger)">*</span></label>
          <select v-model="form.gender" class="soft-select">
            <option value="" disabled>请选择性别</option>
            <option v-for="opt in genderOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>

        <!-- 大学 -->
        <div class="form-group">
          <label class="form-label">大学 <span style="color: var(--color-danger)">*</span></label>
          <input v-model="form.university" type="text" class="soft-input" placeholder="请输入大学名称" />
        </div>

        <!-- 专业 -->
        <div class="form-group">
          <label class="form-label">专业</label>
          <input v-model="form.major" type="text" class="soft-input" placeholder="请输入专业" />
        </div>

        <!-- 学历 -->
        <div class="form-group">
          <label class="form-label">学历</label>
          <select v-model="form.educationLevel" class="soft-select">
            <option value="" disabled>请选择学历</option>
            <option v-for="opt in educationOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
        </div>

        <!-- 年级 -->
        <div class="form-group">
          <label class="form-label">年级</label>
          <select v-model="form.grade" class="soft-select">
            <option value="" disabled>请选择年级</option>
            <option v-for="opt in gradeOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
        </div>

        <!-- 擅长科目 -->
        <div class="form-group">
          <label class="form-label">擅长科目</label>
          <input v-model="form.subjects" type="text" class="soft-input" placeholder="如：数学,英语,物理（逗号分隔）" />
          <p class="form-hint">多个科目请用逗号分隔</p>
        </div>

        <!-- 可教年级 -->
        <div class="form-group">
          <label class="form-label">可教年级</label>
          <input v-model="form.gradeRange" type="text" class="soft-input" placeholder="如：初一,初二,高一（逗号分隔）" />
          <p class="form-hint">多个年级请用逗号分隔</p>
        </div>

        <!-- 服务区域 -->
        <div class="form-group">
          <label class="form-label">服务区域 <span style="color: var(--color-danger)">*</span></label>
          <select v-model="form.district" class="soft-select">
            <option value="" disabled>请选择区域</option>
            <option v-for="opt in districtOptions" :key="opt" :value="opt">{{ opt }}</option>
          </select>
        </div>

        <!-- 详细地址 -->
        <div class="form-group">
          <label class="form-label">详细地址</label>
          <input v-model="form.detailAddress" type="text" class="soft-input" placeholder="请输入详细地址" />
        </div>

        <!-- 授课方式 -->
        <div class="form-group">
          <label class="form-label">授课方式</label>
          <select v-model="form.teachMode" class="soft-select">
            <option value="" disabled>请选择授课方式</option>
            <option v-for="opt in teachModeOptions" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </option>
          </select>
        </div>

        <!-- 期望时薪 -->
        <div class="form-group">
          <label class="form-label">期望时薪（元/小时）</label>
          <input v-model.number="form.pricePerHour" type="number" class="soft-input"
                 placeholder="请输入期望时薪" min="0" />
        </div>

        <!-- 个人简介 — 跨两列 -->
        <div class="form-group md:col-span-2">
          <label class="form-label">个人简介</label>
          <textarea v-model="form.selfIntro" class="soft-textarea" rows="4"
                    placeholder="介绍一下自己，让家长更了解您"></textarea>
        </div>

        <!-- 家教经验 — 跨两列 -->
        <div class="form-group md:col-span-2">
          <label class="form-label">家教经验</label>
          <textarea v-model="form.teachingExperience" class="soft-textarea" rows="4"
                    placeholder="描述您的家教经历、教学成果等"></textarea>
        </div>

        <!-- 提交按钮 — 跨两列 -->
        <div class="md:col-span-2 pt-2">
          <button type="submit" class="soft-btn soft-btn-primary soft-btn-lg w-full" :disabled="submitting">
            {{ submitting ? '提交中...' : (isEdit ? '更新资料' : '提交资料') }}
          </button>
        </div>
      </div>
    </form>
  </div>
</template>
