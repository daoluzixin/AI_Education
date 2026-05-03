<script setup>
import { useToast } from '@/composables/useToast'
const { toasts } = useToast()
</script>

<template>
  <router-view />
  <!-- 全局 Toast 通知 -->
  <div class="toast-container">
    <transition-group name="fade">
      <div v-for="t in toasts" :key="t.id" :class="['toast', `toast-${t.type}`]">
        {{ t.message }}
      </div>
    </transition-group>
  </div>
</template>
