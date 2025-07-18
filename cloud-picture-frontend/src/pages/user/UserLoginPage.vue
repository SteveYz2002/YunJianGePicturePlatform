<template>
  <div class="login-container">
    <div class="login-layout">
      <div class="login-left">
        <div class="welcome-content">
          <h1>欢迎使用云笺阁云图库平台</h1>
          <p>企业级智能协同云图库，高效管理您的图片资源</p>
        </div>
      </div>
      <div class="login-right">
        <div id="userLoginPage" class="login-box">
          <h2 class="title">用户登录</h2>
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
        <a-form-item name="code" :rules="[{ required: true, message: '请输入验证码' }]">
          <a-input v-model:value="formState.code" placeholder="请输入验证码" />
        </a-form-item>
        <div class="captcha-container">
          <img :src="picURL" @click="getKaptcha" class="captcha-image" alt="验证码" />
          <div class="captcha-tip">点击图片刷新验证码</div>
        </div>
        <div class="tips">
          没有账号？
          <RouterLink to="/user/register">去注册</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">登录</a-button>
        </a-form-item>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { getCaptchaUsingGet, userLoginUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import { useRoute } from 'vue-router'

// 获取redirect参数并处理成正确的路径格式
const redirectParam = useRoute().query.redirect?.toString() || '/'
let redirect = '/'

try {
  // 如果是完整URL，则提取路径部分
  if (redirectParam.startsWith('http')) {
    const url = new URL(redirectParam)
    redirect = url.pathname
  } else {
    redirect = redirectParam
  }
} catch (error) {
  console.error('Invalid redirect URL:', error)
  redirect = '/'
}

// 用于接受表单输入的值
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
  uuid: '',
  code: ''
})

const loginUserStore = useLoginUserStore()

const picURL = ref<string>('')

/**
 * 表单提交
 * @param values
 */
const handleSubmit = async (values: any) => {
  // 确保使用formState中的uuid
  const loginData = {
    ...values,
    uuid: formState.uuid,
    code: formState.code
  }

  const res = await userLoginUsingPost(loginData)
  // 登录成功，把登录态保存到全局状态中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser()
    message.success('登录成功')
    await router.replace(redirect)
  } else {
    message.error('登录失败: ' + res.data.message)
    // 登录失败时刷新验证码
    getKaptcha()
  }
}

const getKaptcha = async () => {
  const res = await getCaptchaUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 设置uuid，这是验证码的唯一标识符
    if (res.data.data.uuid) {
      formState.uuid = res.data.data.uuid
    }

    // 检查图片数据格式
    if (res.data.data.img) {
      // 如果img是Base64编码的图片数据，但没有前缀，添加前缀
      if (res.data.data.img.startsWith('/9j/') || res.data.data.img.startsWith('iVBOR')) {
        picURL.value = 'data:image/jpeg;base64,' + res.data.data.img
      } else {
        picURL.value = res.data.data.img
      }
    } else {
      message.error('验证码图片数据为空')
    }
  } else {
    message.error('获取验证码失败' + res.data.message)
  }
}

onMounted(() => {
  getKaptcha()

  // 动态加载Google Fonts
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = 'https://fonts.googleapis.com/css2?family=Dancing+Script:wght@700&family=Pacifico&family=Poppins:wght@400;700&display=swap'
  document.head.appendChild(link)
})

</script>

<style scoped>
#userLoginPage {
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

.captcha-container {
  text-align: center;
  margin-bottom: 16px;
  margin-top: 5px;
}

.captcha-image {
  cursor: pointer;
  width: 120px;
  height: 40px;
  margin-bottom: 8px;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
}

.captcha-image:hover {
  border-color: #40a9ff;
}

.captcha-tip {
  font-size: 12px;
  color: #999;
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
