<template>
  <div id="mySpacePage" class="my-space-page">
    <div class="loading-container glass-effect">
      <div class="loading-content">
        <div class="logo-container">
          <CloudOutlined class="space-icon pulse" />
        </div>
        
        <h2 class="gradient-text">正在前往我的空间</h2>
        
        <div class="loading-bar-container">
          <div class="loading-bar"></div>
        </div>
        
        <p class="loading-text">{{ loadingMessage }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { listSpaceVoByPageUsingPost } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { onMounted, ref } from 'vue'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'
import { CloudOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const loadingMessage = ref('检查您的个人空间...')

// 检查用户是否存在个人空间
const checkUserSpace = async () => {
  // 检查用户是否登录
  const loginUser = loginUserStore.loginUser
  if (!loginUser?.id) {
    loadingMessage.value = '未检测到登录信息，正在跳转至登录页面...'
    // 延迟跳转，让用户看到提示
    setTimeout(async () => {
      // 跳转到登录页
      await router.replace('/user/login')
    }, 1500)
    return
  }
  
  loadingMessage.value = '正在获取您的空间信息...'
  
  try {
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
        loadingMessage.value = '找到您的个人空间，正在进入...'
        
        // 延迟跳转，让用户看到提示
        setTimeout(async () => {
          await router.replace(`/space/${space.id}`)
        }, 1000)
      } else {
        // 没有，跳转到创建页面
        loadingMessage.value = '您还没有创建个人空间，正在跳转至创建页面...'
        
        // 延迟跳转，让用户看到提示
        setTimeout(async () => {
          await router.replace('/add_space')
          message.warn('请先创建个人空间')
        }, 1500)
      }
    } else {
      loadingMessage.value = '获取个人空间信息失败'
      message.error('获取个人空间失败，' + res.data.message)
    }
  } catch (error) {
    loadingMessage.value = '发生错误，请重试'
    message.error('系统异常，请稍后重试')
  }
}

onMounted(() => {
  checkUserSpace()
})
</script>

<style scoped>
.my-space-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px - 48px); /* Adjust based on your header and footer height */
  padding: 20px;
  background: linear-gradient(135deg, rgba(245, 247, 250, 0.5) 0%, rgba(237, 241, 255, 0.5) 100%);
}

.loading-container {
  width: 100%;
  max-width: 500px;
  padding: 40px 30px;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-md);
  text-align: center;
  animation: fadeIn 0.8s ease;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-container {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, var(--primary-light) 0%, var(--primary-color) 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.2);
}

.space-icon {
  font-size: 36px;
  color: white;
}

.pulse {
  animation: pulse 2s infinite;
}

.gradient-text {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 30px;
  background: linear-gradient(to right, var(--primary-color), #4f46e5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.loading-bar-container {
  width: 100%;
  height: 6px;
  background-color: rgba(229, 231, 235, 0.5);
  border-radius: var(--border-radius-full);
  overflow: hidden;
  margin-bottom: 20px;
  position: relative;
}

.loading-bar {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 20%;
  background: linear-gradient(to right, var(--primary-light), var(--primary-color));
  border-radius: var(--border-radius-full);
  animation: loading 1.5s infinite ease-in-out;
}

.loading-text {
  color: var(--text-secondary);
  font-size: 16px;
  margin: 0;
  min-height: 24px;
}

@keyframes loading {
  0% {
    left: -30%;
    width: 30%;
  }
  50% {
    width: 30%;
  }
  70% {
    width: 70%;
  }
  80% {
    left: 50%;
  }
  95% {
    left: 120%;
  }
  100% {
    left: 100%;
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 响应式调整 */
@media (max-width: 768px) {
  .loading-container {
    padding: 30px 20px;
  }
  
  .logo-container {
    width: 60px;
    height: 60px;
  }
  
  .space-icon {
    font-size: 28px;
  }
  
  .gradient-text {
    font-size: 20px;
    margin-bottom: 24px;
  }
}
</style>
