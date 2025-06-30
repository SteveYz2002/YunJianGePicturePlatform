<template>
  <div id="mySpacePage">
    <p>...加载中</p>
  </div>
</template>

<script setup lang="ts">

import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { listSpaceVoByPageUsingPost } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { onMounted } from 'vue'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'

const router = useRouter()

const loginUserStore = useLoginUserStore()

// 检查用户是否存在个人空间
const checkUserSpace = async () => {
  // 检查用户是否登录
  const loginUser = loginUserStore.loginUser
  if (!loginUser?.id) {
    // 跳转到登录页
    await router.replace('/user/login')
    return
  }
  // 如果已经登录，会获取此用户自己创建的空间
  const res = await listSpaceVoByPageUsingPost({
    userId: loginUser.id,
    current: 1,
    pageSize: 1,
    spaceType: SPACE_TYPE_ENUM.PRIVATE
  })
  if (res.data.code === 0) {
    // 如果有，进入
    if (res.data.data?.records?.length > 0) {
      const space = res.data.data.records[0]
      await router.replace(`/space/${space.id}`)
    } else {
      // 没有，跳转到创建页面
      await router.replace('/add_space')
      message.warn('请先创建个人空间')
    }
  } else {
    message.error('获取个人空间失败，' + res.data.message)
  }
}

onMounted(() => {
  checkUserSpace()
})

</script>

<style scoped>
#addSpacePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
