<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminApi } from '@/api'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()

const id = route.params.id
const teacher = ref(null)
const loading = ref(true)

/* ---- 拒绝模态框 ---- */
const showRejectModal = ref(false)
const rejectReason = ref('')
const submitting = ref(false)

/* ---- 加载详情 ---- */
async function fetchDetail() {
  loading.value = true
  try {
    teacher.value = await adminApi.auditDetail(id)
  } catch {
    teacher.value = null
  } finally {
    loading.value = false
  }
}

onMounted(fetchDetail)

/* ---- 通过审核 ---- */
async function handleApprove() {
  submitting.value = true
  try {
    await adminApi.approve(id)
    showToast('审核已通过', 'success')
    await fetchDetail()
  } finally {
    submitting.value = false
  }
}

/* ---- 拒绝审核 ---- */
async function handleReject() {
  if (!rejectReason.value.trim()) {
    showToast('请输入拒绝原因', 'warning')
    return
  }
  submitting.value = true
  try {
    await adminApi.reject(id, rejectReason.value.trim())
    showToast('已拒绝该教师', 'success')
    showRejectModal.value = false
    rejectReason.value = ''
    await fetchDetail()
  } finally {
    submitting.value = false
  }
}

/* ---- 返回列表 ---- */
function goBack() {
  router.push({ name: 'AdminHome' })
}

/* ---- 辅助 ---- */
const genderMap = { 0: '女', 1: '男' }
const degreeMap = { 1: '专科', 2: '本科', 3: '硕士', 4: '博士' }
const modeMap = { 1: '线上', 2: '线下', 3: '线上+线下' }
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <!-- 返回 -->
    <button class="soft-btn soft-btn-outline soft-btn-sm mb-6" @click="goBack">
      ← 返回列表
    </button>

    <!-- 加载中 -->
    <div v-if="loading" class="flex justify-center py-20">
      <div class="spinner"></div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!teacher" class="empty-state">
      <p class="text-base">未找到该教师信息</p>
    </div>

    <template v-else>
      <!-- 审核状态提示 -->
      <div v-if="teacher.authStatus === 1"
           class="mb-5 px-5 py-3 rounded-xl text-sm font-medium"
           style="background: #DCFCE7; color: #16A34A">
        ✓ 已通过审核
      </div>
      <div v-else-if="teacher.authStatus === 2"
           class="mb-5 px-5 py-3 rounded-xl text-sm font-medium"
           style="background: #FEE2E2; color: #DC2626">
        ✗ 已拒绝 — 原因: {{ teacher.rejectReason || '未填写' }}
      </div>

      <!-- 基本信息卡片 -->
      <div class="soft-card mb-5" style="cursor:default"
           @mouseenter="$event.currentTarget.style.transform='none'">
        <h2 class="text-lg font-bold mb-4" style="color: var(--color-text)">基本信息</h2>
        <div class="grid grid-cols-2 gap-x-8 gap-y-3 text-sm">
          <div>
            <span style="color: var(--color-text-muted)">姓名</span>
            <p class="mt-1 font-medium">{{ teacher.realName || '-' }}</p>
          </div>
          <div>
            <span style="color: var(--color-text-muted)">性别</span>
            <p class="mt-1 font-medium">{{ genderMap[teacher.gender] || '-' }}</p>
          </div>
          <div>
            <span style="color: var(--color-text-muted)">大学</span>
            <p class="mt-1 font-medium">{{ teacher.university || '-' }}</p>
          </div>
          <div>
            <span style="color: var(--color-text-muted)">专业</span>
            <p class="mt-1 font-medium">{{ teacher.major || '-' }}</p>
          </div>
          <div>
            <span style="color: var(--color-text-muted)">学历</span>
            <p class="mt-1 font-medium">{{ degreeMap[teacher.education] || '-' }}</p>
          </div>
          <div>
            <span style="color: var(--color-text-muted)">年级</span>
            <p class="mt-1 font-medium">{{ teacher.grade || '-' }}</p>
          </div>
        </div>
      </div>

      <!-- 个人简介 & 家教经验 -->
      <div class="soft-card mb-5" style="cursor:default"
           @mouseenter="$event.currentTarget.style.transform='none'">
        <h2 class="text-lg font-bold mb-4" style="color: var(--color-text)">个人简介</h2>
        <p class="text-sm leading-relaxed mb-6" style="color: var(--color-text-secondary)">
          {{ teacher.introduction || '暂无' }}
        </p>
        <h2 class="text-lg font-bold mb-4" style="color: var(--color-text)">家教经验</h2>
        <p class="text-sm leading-relaxed" style="color: var(--color-text-secondary)">
          {{ teacher.experience || '暂无' }}
        </p>
      </div>

      <!-- 教学信息 -->
      <div class="soft-card mb-5" style="cursor:default"
           @mouseenter="$event.currentTarget.style.transform='none'">
        <h2 class="text-lg font-bold mb-4" style="color: var(--color-text)">教学信息</h2>

        <!-- 擅长科目 -->
        <div class="mb-4">
          <span class="text-sm" style="color: var(--color-text-muted)">擅长科目</span>
          <div class="flex flex-wrap gap-2 mt-2">
            <span v-for="s in (teacher.subjects || '').split(',')" :key="s"
                  class="soft-badge soft-badge-published">{{ s }}</span>
          </div>
        </div>

        <!-- 可教年级 -->
        <div class="mb-4">
          <span class="text-sm" style="color: var(--color-text-muted)">可教年级</span>
          <div class="flex flex-wrap gap-2 mt-2">
            <span v-for="g in (teacher.teachGrades || '').split(',')" :key="g"
                  class="soft-badge soft-badge-published">{{ g }}</span>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-x-8 gap-y-3 text-sm">
          <div>
            <span style="color: var(--color-text-muted)">服务区域</span>
            <p class="mt-1 font-medium">{{ teacher.serviceArea || '-' }}</p>
          </div>
          <div>
            <span style="color: var(--color-text-muted)">授课方式</span>
            <p class="mt-1 font-medium">{{ modeMap[teacher.teachMode] || '-' }}</p>
          </div>
          <div>
            <span style="color: var(--color-text-muted)">期望时薪</span>
            <p class="mt-1 font-medium" style="color: var(--color-primary)">
              {{ teacher.expectedPrice ? `¥${teacher.expectedPrice}/小时` : '-' }}
            </p>
          </div>
        </div>
      </div>

      <!-- 审核操作区（仅待审核时显示） -->
      <div v-if="teacher.authStatus === 0"
           class="soft-card flex items-center justify-end gap-4" style="cursor:default"
           @mouseenter="$event.currentTarget.style.transform='none'">
        <button class="soft-btn soft-btn-success"
                :disabled="submitting"
                @click="handleApprove">
          {{ submitting ? '处理中...' : '通过' }}
        </button>
        <button class="soft-btn soft-btn-danger"
                :disabled="submitting"
                @click="showRejectModal = true">
          拒绝
        </button>
      </div>

      <!-- 底部返回 -->
      <div class="mt-8 text-center">
        <button class="soft-btn soft-btn-outline" @click="goBack">返回列表</button>
      </div>
    </template>

    <!-- 拒绝原因模态框 -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showRejectModal" class="modal-overlay" @click.self="showRejectModal = false">
          <div class="modal-content">
            <h3 class="text-lg font-bold mb-4" style="color: var(--color-text)">拒绝原因</h3>
            <textarea
              v-model="rejectReason"
              class="soft-textarea"
              placeholder="请输入拒绝原因..."
              rows="4"
            ></textarea>
            <div class="flex justify-end gap-3 mt-6">
              <button class="soft-btn soft-btn-outline" @click="showRejectModal = false">取消</button>
              <button class="soft-btn soft-btn-danger"
                      :disabled="submitting"
                      @click="handleReject">
                {{ submitting ? '提交中...' : '确认拒绝' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>
