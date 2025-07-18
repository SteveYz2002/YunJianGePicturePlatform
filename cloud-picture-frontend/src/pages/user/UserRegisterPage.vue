<template>
  <div class="login-container">
    <div class="login-layout">
      <div class="login-left">
        <div class="welcome-content">
          <h1>欢迎加入云笺阁云图库平台</h1>
          <p>注册账号，开启您的图片管理之旅</p>
        </div>
      </div>
      <div class="login-right">
        <div id="userRegisterPage" class="login-box">
          <h2 class="title">用户注册</h2>
          <div class="desc">云笺阁</div>
          <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
        <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item
          name="userPassword"
          :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码不能小于 8 位' },
        ]"
        >
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item
          name="checkPassword"
          :rules="[
          { required: true, message: '请确认密码' },
          { min: 8, message: '确认密码不能小于 8 位' },
        ]"
        >
          <a-input-password v-model:value="formState.checkPassword" placeholder="请确认密码" />
        </a-form-item>
        <div class="tips">
          已有账号？
          <RouterLink to="/user/login">去登录</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
        </a-form-item>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { userRegisterUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

// 用于接受表单输入的值
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const loginUserStore = useLoginUserStore()

/**
 * 表单提交
 * @param values
 */
const handleSubmit = async (values: any) => {
  //校验两次输入的代码是否一致
  if(formState.userPassword !== formState.checkPassword){
    message.error('两次输入的密码不一致')
    return
  }
  const res = await userRegisterUsingPost(values)
  // 注册成功，跳转到登录页
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    await router.push({ path: '/user/login', replace: true })
  } else {
    message.error('注册失败,' + res.data.message)
  }
}

onMounted(() => {
  // 动态加载Google Fonts
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = 'https://fonts.googleapis.com/css2?family=Dancing+Script:wght@700&family=Pacifico&family=Poppins:wght@400;700&display=swap'
  document.head.appendChild(link)
})

</script>

<style scoped>
#userRegisterPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  margin-bottom: 16px;
}

.desc {
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}

.login-container {
  min-height: calc(100vh - 41px); /* 减去页脚的高度 */
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
}

.login-layout {
  display: flex;
  width: 80%;
  max-width: 1200px;
  min-height: 600px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: white;
}

.welcome-content {
  text-align: left;
  max-width: 400px;
  margin-top: -50px;
}

.welcome-content h1 {
  font-family: 'Dancing Script', cursive;
  font-size: 38px;
  margin-bottom: 20px;
  color: white;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
  letter-spacing: 1px;
}

.welcome-content p {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  opacity: 0.9;
  line-height: 1.6;
  letter-spacing: 0.5px;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: 30px;
  border-radius: 5px;
  background-color: #fff;
}

@media (max-width: 768px) {
  .login-layout {
    width: 95%;
    flex-direction: column;
  }

  .login-left {
    padding: 20px;
  }

  .login-right {
    padding: 20px;
  }

  .welcome-content {
    text-align: center;
  }
}
</style>
