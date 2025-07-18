import { defineStore } from 'pinia'
import { ref } from 'vue'
import { listSpaceVoByPageUsingPost } from '@/api/spaceController.ts'
import { useLoginUserStore } from './useLoginUserStore'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'

export const useUserSpaceStore = defineStore('userSpace', () => {
  // 用户的个人空间ID
  const personalSpaceId = ref<number | null>(null)

  /**
   * 获取用户的个人空间ID
   */
  async function fetchPersonalSpaceId() {
    const loginUserStore = useLoginUserStore()
    const loginUser = loginUserStore.loginUser
    
    if (!loginUser?.id) {
      return null
    }
    
    try {
      const res = await listSpaceVoByPageUsingPost({
        userId: loginUser.id,
        current: 1,
        pageSize: 1,
        spaceType: SPACE_TYPE_ENUM.PRIVATE
      })
      
      if (res.data.code === 0 && res.data.data?.records?.length > 0) {
        const space = res.data.data.records[0]
        personalSpaceId.value = space.id
        return space.id
      }
    } catch (error) {
      console.error('获取个人空间失败', error)
    }
    
    return null
  }

  /**
   * 判断给定的空间ID是否是用户的个人空间
   */
  function isPersonalSpace(spaceId: string | number | null): boolean {
    if (!spaceId || !personalSpaceId.value) return false
    return Number(spaceId) === Number(personalSpaceId.value)
  }

  return { 
    personalSpaceId,
    fetchPersonalSpaceId,
    isPersonalSpace
  }
}) 