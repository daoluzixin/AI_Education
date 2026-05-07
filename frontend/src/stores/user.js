import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const role = ref(localStorage.getItem('role') || '')
  const profileId = ref(localStorage.getItem('profileId') || null)

  const isLoggedIn = computed(() => !!user.value)
  const isParent = computed(() => role.value === 'parent')
  const isTeacher = computed(() => role.value === 'teacher')
  const isAdmin = computed(() => role.value === 'admin')

  function setUser(userData, userRole, profId = null) {
    user.value = userData
    role.value = userRole
    profileId.value = profId
    localStorage.setItem('user', JSON.stringify(userData))
    localStorage.setItem('role', userRole)
    if (profId) localStorage.setItem('profileId', profId)
  }

  function logout() {
    user.value = null
    role.value = ''
    profileId.value = null
    localStorage.removeItem('user')
    localStorage.removeItem('role')
    localStorage.removeItem('profileId')
  }

  return { user, role, profileId, isLoggedIn, isParent, isTeacher, isAdmin, setUser, logout }
})
