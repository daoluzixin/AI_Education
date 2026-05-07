<script setup>
import { ref, onMounted } from 'vue'
import { parentApi, studentApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'

const userStore = useUserStore()
const { showToast } = useToast()

const loading = ref(true)
const students = ref([])
const parentId = ref(null)
const showModal = ref(false)
const editing = ref(false)
const submitting = ref(false)

const gradeOptions = [
  '一年级', '二年级', '三年级', '四年级', '五年级', '六年级',
  '初一', '初二', '初三', '高一', '高二', '高三'
]

const defaultForm = { name: '', gender: 1, grade: '', school: '', remark: '' }
const form = ref({ ...defaultForm })
const editingId = ref(null)

onMounted(async () => {
  try {
    const profile = await parentApi.getProfile(userStore.user.id)
    parentId.value = profile.id
    await loadStudents()
  } catch {
    showToast('请先完善家长资料', 'warning')
  } finally {
    loading.value = false
  }
})

async function loadStudents() {
  if (!parentId.value) return
  students.value = await studentApi.list(parentId.value) || []
}

function openAdd() {
  editing.value = false
  editingId.value = null
  form.value = { ...defaultForm }
  showModal.value = true
}

function openEdit(student) {
  editing.value = true
  editingId.value = student.id
  form.value = {
    name: student.name,
    gender: student.gender,
    grade: student.grade,
    school: student.school || '',
    remark: student.remark || ''
  }
  showModal.value = true
}

async function handleSubmit() {
  if (!form.value.name || !form.value.grade) {
    showToast('请填写姓名和年级', 'warning')
    return
  }
  submitting.value = true
  try {
    if (editing.value) {
      await studentApi.update(editingId.value, parentId.value, form.value)
      showToast('修改成功')
    } else {
      await studentApi.add(parentId.value, form.value)
      showToast('添加成功')
    }
    showModal.value = false
    await loadStudents()
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-2xl font-semibold text-gray-800">孩子管理</h1>
      <button class="soft-btn soft-btn-primary" @click="openAdd">添加孩子</button>
    </div>

    <div v-if="loading" class="flex justify-center py-20">
      <div class="spinner"></div>
    </div>

    <div v-else-if="students.length === 0" class="empty-state">
      <p class="text-lg mb-2">还没有添加孩子</p>
      <p class="text-sm">点击上方按钮添加孩子信息</p>
    </div>

    <div v-else class="grid gap-4">
      <div v-for="s in students" :key="s.id" class="soft-card">
        <div class="flex items-start justify-between">
          <div>
            <h3 class="text-lg font-semibold text-gray-800 mb-2">
              {{ s.name }}
              <span class="text-sm font-normal text-gray-500 ml-2">{{ s.gender === 1 ? '男' : '女' }}</span>
            </h3>
            <p class="text-sm text-gray-500 mb-1">年级：{{ s.grade }}</p>
            <p v-if="s.school" class="text-sm text-gray-500 mb-1">学校：{{ s.school }}</p>
            <p v-if="s.remark" class="text-sm text-gray-400 mt-2">备注：{{ s.remark }}</p>
          </div>
          <button class="soft-btn soft-btn-outline soft-btn-sm" @click="openEdit(s)">编辑</button>
        </div>
      </div>
    </div>

    <!-- 模态框 -->
    <Teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal-content">
          <h2 class="text-xl font-semibold text-gray-800 mb-6">{{ editing ? '编辑孩子' : '添加孩子' }}</h2>

          <form @submit.prevent="handleSubmit">
            <div class="form-group">
              <label class="form-label">姓名 <span class="text-red-400">*</span></label>
              <input v-model="form.name" class="soft-input" placeholder="请输入孩子姓名" />
            </div>

            <div class="form-group">
              <label class="form-label">性别</label>
              <select v-model="form.gender" class="soft-select">
                <option :value="1">男</option>
                <option :value="2">女</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">年级 <span class="text-red-400">*</span></label>
              <select v-model="form.grade" class="soft-select">
                <option value="">请选择年级</option>
                <option v-for="g in gradeOptions" :key="g" :value="g">{{ g }}</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">学校</label>
              <input v-model="form.school" class="soft-input" placeholder="请输入学校名称" />
            </div>

            <div class="form-group">
              <label class="form-label">备注</label>
              <input v-model="form.remark" class="soft-input" placeholder="如学习情况、性格特点等" />
            </div>

            <div class="flex gap-3 mt-6">
              <button type="button" class="soft-btn soft-btn-outline flex-1" @click="showModal = false">取消</button>
              <button type="submit" class="soft-btn soft-btn-primary flex-1" :disabled="submitting">
                {{ submitting ? '提交中...' : '确认' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>
