<script setup>
import { ref, onMounted } from 'vue'
import { parentApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'

const userStore = useUserStore()
const { showToast } = useToast()

const loading = ref(true)
const submitting = ref(false)

const districts = [
  '雁塔区', '碑林区', '莲湖区', '新城区', '灞桥区',
  '未央区', '长安区', '临潼区', '高新区', '曲江新区'
]

const form = ref({
  realName: '',
  phone: '',
  district: '',
  address: ''
})

onMounted(async () => {
  try {
    const profile = await parentApi.getProfile(userStore.user.id)
    if (profile) {
      form.value.realName = profile.realName || ''
      form.value.phone = profile.phone || ''
      form.value.district = profile.district || ''
      form.value.address = profile.address || ''
    }
  } catch {
    /* 首次访问可能无资料 */
  } finally {
    loading.value = false
  }
})

async function handleSubmit() {
  if (!form.value.realName || !form.value.phone) {
    showToast('请填写姓名和手机号', 'warning')
    return
  }
  submitting.value = true
  try {
    await parentApi.submitProfile(userStore.user.id, form.value)
    showToast('资料保存成功')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-2xl mx-auto px-4 py-8">
    <h1 class="text-2xl font-semibold text-gray-800 mb-8">我的资料</h1>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="spinner"></div>
    </div>

    <form v-else class="soft-card" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label class="form-label">真实姓名 <span class="text-red-400">*</span></label>
        <input v-model="form.realName" class="soft-input" placeholder="请输入真实姓名" />
      </div>

      <div class="form-group">
        <label class="form-label">手机号 <span class="text-red-400">*</span></label>
        <input v-model="form.phone" class="soft-input" placeholder="请输入手机号" />
      </div>

      <div class="form-group">
        <label class="form-label">所在区域</label>
        <select v-model="form.district" class="soft-select">
          <option value="">请选择区域</option>
          <option v-for="d in districts" :key="d" :value="d">{{ d }}</option>
        </select>
      </div>

      <div class="form-group">
        <label class="form-label">详细地址</label>
        <input v-model="form.address" class="soft-input" placeholder="请输入详细地址" />
      </div>

      <button type="submit" class="soft-btn soft-btn-primary soft-btn-lg w-full mt-4" :disabled="submitting">
        {{ submitting ? '保存中...' : '保存资料' }}
      </button>
    </form>
  </div>
</template>
