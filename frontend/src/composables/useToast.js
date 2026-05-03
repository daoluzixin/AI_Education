import { reactive } from 'vue'

const state = reactive({
  toasts: []
})

let toastId = 0

export function useToast() {
  function showToast(message, type = 'success', duration = 3000) {
    const id = ++toastId
    state.toasts.push({ id, message, type })
    setTimeout(() => {
      const idx = state.toasts.findIndex(t => t.id === id)
      if (idx > -1) state.toasts.splice(idx, 1)
    }, duration)
  }

  return {
    toasts: state.toasts,
    showToast
  }
}
