<template>
  <div class="register-container">
    <div class="register-layout">
      <div class="register-left">
        <div class="tech-bg">
          <div class="circle circle-1"></div>
          <div class="circle circle-2"></div>
          <div class="circle circle-3"></div>
          <div class="tech-lines"></div>
        </div>
        <div class="welcome-content">
          <h1>欢迎加入云笺阁</h1>
          <div class="tagline">
            <span class="highlight">开启您的图片管理之旅</span>
            <br>智能、便捷、安全的企业级云图库
          </div>
          <div class="features">
            <div class="feature">
              <PictureOutlined />
              <span>超过10万张图片的管理能力</span>
            </div>
            <div class="feature">
              <CloudOutlined />
              <span>安全可靠的云端存储技术</span>
            </div>
            <div class="feature">
              <TeamOutlined />
              <span>多人协作的权限管理体系</span>
            </div>
          </div>
        </div>
      </div>
      <div class="register-right">
        <div id="userRegisterPage" class="register-box">
          <div class="register-header">
            <h2 class="title">用户注册</h2>
            <div class="logo-text">云笺阁</div>
          </div>
          
          <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit" class="register-form">
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
            <a-form-item
              name="checkPassword"
              :rules="[
                { required: true, message: '请确认密码' },
                { min: 8, message: '确认密码不能小于 8 位' },
                { validator: validatePassword, message: '两次密码输入不一致' }
              ]"
            >
              <a-input-password 
                v-model:value="formState.checkPassword" 
                placeholder="请确认密码" 
                :prefix="h(SafetyCertificateOutlined)"
                class="input-field" 
              />
            </a-form-item>
            
            <div class="form-actions">
              <a-button 
                type="primary" 
                html-type="submit" 
                class="register-button"
                :loading="loading"
              >
                注册
              </a-button>
              <div class="login-link">
                已有账号？
                <RouterLink to="/user/login" class="link">立即登录</RouterLink>
              </div>
            </div>
          </a-form>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup lang="ts">
import { reactive, onMounted, ref, h } from 'vue'
import { userRegisterUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'
import { 
  UserOutlined, 
  LockOutlined, 
  SafetyCertificateOutlined,
  PictureOutlined,
  CloudOutlined,
  TeamOutlined
} from '@ant-design/icons-vue'

// 用于接受表单输入的值
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const loading = ref<boolean>(false)

// 验证密码一致性的函数
const validatePassword = async (_rule: any, value: string) => {
  if (value && value !== formState.userPassword) {
    return Promise.reject('两次输入的密码不一致');
  }
  return Promise.resolve();
};

const loginUserStore = useLoginUserStore()

/**
 * 表单提交
 * @param values
 */
const handleSubmit = async (values: any) => {
  loading.value = true;
  
  try {
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
  } catch (error: any) {
    message.error('注册失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // 动态加载Google Fonts
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = 'https://fonts.googleapis.com/css2?family=Montserrat:wght@400;500;600;700&family=Poppins:wght@400;600&display=swap'
  document.head.appendChild(link)
})

</script>

<style scoped>
.register-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f8fa;
  padding: 20px;
}

.register-layout {
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
.register-left {
  flex: 1.2;
  position: relative;
  background: linear-gradient(135deg, #8553E2, #722ed1);
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
.register-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.register-box {
  width: 100%;
  max-width: 400px;
  padding: 0;
}

.register-header {
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

.register-form {
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
  border-color: #722ed1;
  box-shadow: 0 0 0 2px rgba(114, 46, 209, 0.1);
}

.form-actions {
  margin-top: 24px;
}

.register-button {
  height: 50px;
  width: 100%;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(114, 46, 209, 0.2);
  background: linear-gradient(to right, #722ed1, #8553E2);
  border: none;
  transition: all 0.3s;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(114, 46, 209, 0.3);
}

.login-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #64748b;
}

.link {
  color: #722ed1;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.3s;
}

.link:hover {
  color: #8553E2;
  text-decoration: underline;
}

/* Responsive styles */
@media (max-width: 992px) {
  .register-layout {
    width: 95%;
  }
}

@media (max-width: 768px) {
  .register-layout {
    flex-direction: column;
    min-height: auto;
  }

  .register-left {
    padding: 40px 20px;
  }

  .register-right {
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
