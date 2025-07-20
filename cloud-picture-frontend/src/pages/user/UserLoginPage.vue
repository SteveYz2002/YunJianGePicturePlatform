<template>
  <div class="login-container">
    <div class="login-layout">
      <div class="login-left">
        <div class="tech-bg">
          <div class="circle circle-1"></div>
          <div class="circle circle-2"></div>
          <div class="circle circle-3"></div>
          <div class="tech-lines"></div>
        </div>
        <div class="welcome-content">
          <h1>欢迎使用云笺阁</h1>
          <div class="tagline">
            <span class="highlight">企业级智能协同云图库</span>
            <br>高效管理您的图片资源
          </div>
          <div class="features">
            <div class="feature">
              <CheckCircleOutlined />
              <span>安全可靠的图片存储</span>
            </div>
            <div class="feature">
              <CheckCircleOutlined />
              <span>强大的智能图片搜索</span>
            </div>
            <div class="feature">
              <CheckCircleOutlined />
              <span>团队协作与权限管理</span>
            </div>
          </div>
        </div>
      </div>
      <div class="login-right">
        <div id="userLoginPage" class="login-box">
          <div class="login-header">
            <h2 class="title">用户登录</h2>
            <div class="logo-text">云笺阁</div>
          </div>
          
          <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit" class="login-form">
            <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
              <a-input 
                v-model:value="formState.userAccount" 
                placeholder="请输入账号" 
                :prefix="h(UserOutlined)"
                class="input-field" 
              />
            </a-form-item>
            <a-form-item
              name="userPassword"
              :rules="[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码不能小于 8 位' },
            ]"
            >
              <a-input-password 
                v-model:value="formState.userPassword" 
                placeholder="请输入密码" 
                :prefix="h(LockOutlined)"
                class="input-field" 
              />
            </a-form-item>
            <a-form-item name="code" :rules="[{ required: true, message: '请输入验证码' }]">
              <div class="captcha-input-group">
                <a-input 
                  v-model:value="formState.code" 
                  placeholder="请输入验证码" 
                  :prefix="h(SafetyCertificateOutlined)"
                  class="input-field captcha-input" 
                />
                <div class="captcha-img-container" @click="getKaptcha">
                  <img :src="picURL" class="captcha-image" alt="验证码" />
                  <div class="captcha-overlay">
                    <SyncOutlined spin />
                  </div>
                </div>
              </div>
            </a-form-item>
            
            <div class="form-actions">
              <a-button 
                type="primary" 
                html-type="submit" 
                class="login-button" 
                :loading="loading"
              >
                登录
              </a-button>
              <div class="register-link">
                没有账号？
                <RouterLink to="/user/register" class="link">立即注册</RouterLink>
              </div>
            </div>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup lang="ts">
import { onMounted, reactive, ref, h } from 'vue'
import { getCaptchaUsingGet, userLoginUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import { useRoute } from 'vue-router'
import { 
  UserOutlined, 
  LockOutlined, 
  SafetyCertificateOutlined,
  CheckCircleOutlined,
  SyncOutlined
} from '@ant-design/icons-vue'

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
const loading = ref<boolean>(false)

/**
 * 表单提交
 * @param values
 */
const handleSubmit = async (values: any) => {
  loading.value = true
  
  try {
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
  } catch (error: any) {
    message.error('登录失败: ' + error.message)
    getKaptcha()
  } finally {
    loading.value = false
  }
}

const getKaptcha = async () => {
  try {
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
  } catch (error: any) {
    message.error('获取验证码失败: ' + error.message)
  }
}

onMounted(() => {
  getKaptcha()

  // 动态加载Google Fonts
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = 'https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700&family=Poppins:wght@400;600&display=swap'
  document.head.appendChild(link)
})

</script>

<style scoped>
.login-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f8fa;
  padding: 20px;
}

.login-layout {
  display: flex;
  width: 85%;
  max-width: 1200px;
  min-height: 600px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Left side styling */
.login-left {
  flex: 1.2;
  position: relative;
  background: linear-gradient(135deg, #1677ff, #4096ff);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: white;
  overflow: hidden;
}

.tech-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  overflow: hidden;
}

.tech-lines {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(to right, rgba(255,255,255,0.1) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255,255,255,0.1) 1px, transparent 1px);
  background-size: 30px 30px;
  opacity: 0.4;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -80px;
  background: radial-gradient(circle at center, rgba(255,255,255,0.2), rgba(255,255,255,0.05));
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  left: -50px;
  background: radial-gradient(circle at center, rgba(255,255,255,0.15), rgba(255,255,255,0.05));
}

.circle-3 {
  width: 100px;
  height: 100px;
  top: 50%;
  left: 30%;
  background: radial-gradient(circle at center, rgba(255,255,255,0.1), rgba(255,255,255,0.05));
}

.welcome-content {
  position: relative;
  z-index: 2;
  text-align: left;
  max-width: 440px;
}

.welcome-content h1 {
  font-family: 'Montserrat', sans-serif;
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 20px;
  color: white;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  letter-spacing: 0.5px;
}

.tagline {
  font-family: 'Poppins', sans-serif;
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 30px;
}

.highlight {
  position: relative;
  font-weight: 600;
}

.highlight::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 2px;
  background: rgba(255, 255, 255, 0.6);
}

.features {
  margin-top: 30px;
}

.feature {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.feature :deep(svg) {
  font-size: 18px;
  margin-right: 12px;
  color: #ffffff;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

/* Right side styling */
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
  padding: 0;
}

.login-header {
  margin-bottom: 30px;
  text-align: center;
}

.title {
  font-family: 'Montserrat', sans-serif;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #1e293b;
}

.logo-text {
  font-family: 'Poppins', sans-serif;
  font-size: 16px;
  color: #64748b;
  margin-bottom: 20px;
  letter-spacing: 1px;
}

.login-form {
  padding: 0;
}

.input-field {
  height: 50px;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.03);
  transition: all 0.3s;
  border: 1px solid #e2e8f0;
}

.input-field:hover {
  border-color: #94a3b8;
}

.input-field:focus {
  border-color: #1677ff;
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
}

.captcha-input-group {
  display: flex;
  gap: 10px;
}

.captcha-input {
  flex: 1;
}

.captcha-img-container {
  position: relative;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.captcha-img-container:hover .captcha-overlay {
  opacity: 1;
}

.captcha-image {
  display: block;
  width: 120px;
  height: 50px;
  border-radius: 8px;
  object-fit: cover;
  border: 1px solid #e2e8f0;
}

.captcha-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.form-actions {
  margin-top: 24px;
}

.login-button {
  height: 50px;
  width: 100%;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
  background: linear-gradient(to right, #1677ff, #4096ff);
  border: none;
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.3);
}

.register-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #64748b;
}

.link {
  color: #1677ff;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.3s;
}

.link:hover {
  color: #4096ff;
  text-decoration: underline;
}

/* Responsive styles */
@media (max-width: 992px) {
  .login-layout {
    width: 95%;
  }
}

@media (max-width: 768px) {
  .login-layout {
    flex-direction: column;
    min-height: auto;
  }

  .login-left {
    padding: 40px 20px;
  }

  .login-right {
    padding: 30px 20px;
  }

  .welcome-content {
    text-align: center;
    margin-top: 0;
  }

  .welcome-content h1 {
    font-size: 32px;
  }

  .feature {
    justify-content: center;
  }
  
  .tagline {
    font-size: 16px;
  }
}
</style>
