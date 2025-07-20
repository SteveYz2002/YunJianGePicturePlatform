<template>
  <div id="editPasswordPage" class="edit-password-page">
    <div class="page-header glass-effect">
      <div class="page-title">
        <h2 class="gradient-text">修改密码</h2>
        <div class="title-underline"></div>
      </div>
      <div class="page-description">
        请输入您的原密码和新密码，确保您的账户安全
      </div>
    </div>
    
    <div class="card-container">
      <div class="password-form-card glass-effect">
        <div class="card-content">
          <a-form 
            :model="formState" 
            name="basic" 
            autocomplete="off" 
            layout="vertical"
            class="password-form"
          >
            <a-form-item
              :rules="[{ required: true, message: '请输入原密码', trigger: 'blur' }]"
              label="原密码"
              name="userPassword"
            >
              <a-input-password 
                v-model:value="formState.userPassword" 
                autocomplete="off" 
                placeholder="请输入原密码"
                class="custom-input" 
                :maxLength="20"
              />
            </a-form-item>
            
            <a-form-item
              :rules="[
                { required: true, message: '请输入新密码', trigger: 'blur' },
                { min: 8, message: '密码不能小于 8 位' },
              ]"
              label="新密码"
              name="newPassword"
            >
              <a-input-password 
                v-model:value="formState.newPassword" 
                autocomplete="off" 
                placeholder="请输入新密码" 
                class="custom-input"
                :maxLength="20"
              />
              <div class="password-hint">密码长度至少为 8 位，建议使用字母、数字和特殊符号的组合</div>
            </a-form-item>
            
            <a-form-item
              :rules="[
                { required: true, message: '请输入确认密码', trigger: 'blur' },
                { min: 8, message: '密码不能小于 8 位' },
                { validator: validateConfirmPassword }
              ]"
              label="确认密码"
              name="checkPassword"
            >
              <a-input-password 
                v-model:value="formState.checkPassword" 
                autocomplete="off" 
                placeholder="请确认密码" 
                class="custom-input"
                :maxLength="20"
              />
            </a-form-item>
            
            <div class="form-actions">
              <a-button 
                type="primary" 
                @click="save"
                class="save-button"
                :loading="loading"
              >
                <template #icon><LockOutlined /></template>
                确认修改
              </a-button>
              <a-button 
                @click="resetForm"
                class="reset-button"
              >
                重置
              </a-button>
            </div>
          </a-form>
        </div>
        
        <div class="security-tips">
          <div class="tips-title">
            <SafetyOutlined /> 安全提示
          </div>
          <ul class="tips-list">
            <li>定期更换密码可以提高账户安全性</li>
            <li>不要使用与其他网站相同的密码</li>
            <li>不要将密码分享给他人</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { updatePasswordUsingPost, userLogoutUsingPost } from '@/api/userController'
import { useRouter } from 'vue-router'
import { LockOutlined, SafetyOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const loading = ref(false)

const formState = reactive<API.UserUpdateRequest>({
  userPassword: '',
  newPassword: '',
  checkPassword: ''
})

// 验证确认密码是否与新密码一致
const validateConfirmPassword = async (_rule: any, value: string) => {
  if (value && value !== formState.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

// 重置表单
const resetForm = () => {
  formState.userPassword = ''
  formState.newPassword = ''
  formState.checkPassword = ''
}

const save = async () => {
  // 基本验证
  if (!formState.userPassword) {
    message.warning('请输入原密码')
    return
  }
  if (!formState.newPassword) {
    message.warning('请输入新密码')
    return
  }
  if (!formState.checkPassword) {
    message.warning('请输入确认密码')
    return
  }
  if (formState.newPassword !== formState.checkPassword) {
    message.warning('两次输入的密码不一致')
    return
  }
  if (formState.newPassword.length < 8) {
    message.warning('密码不能小于 8 位')
    return
  }
  
  loading.value = true
  try {
    const res = await updatePasswordUsingPost({
      userPassword: formState.userPassword,
      newPassword: formState.newPassword,
      checkPassword: formState.checkPassword
    })

    if (res.data.code === 0) {
      message.success('密码修改成功，请重新登录')
      // 跳转到登录页
      window.location.replace('/user/login')
    } else {
      // 修改失败，显示错误信息
      message.error("修改密码失败，" + res.data.message)
    }
  } catch (error) {
    message.error('请求发生错误，请稍后重试')
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.edit-password-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 0 40px;
  animation: fadeIn 0.5s ease;
}

.page-header {
  padding: 20px 24px;
  border-radius: var(--border-radius-lg);
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
}

.page-title {
  position: relative;
  margin-bottom: 16px;
}

.page-title h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.title-underline {
  position: absolute;
  bottom: -8px;
  left: 0;
  height: 3px;
  width: 40px;
  background: var(--primary-gradient);
  border-radius: var(--border-radius-full);
}

.page-description {
  color: var(--text-secondary);
  font-size: 14px;
  margin-top: 16px;
}

.card-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.password-form-card {
  width: 100%;
  max-width: 600px;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.card-content {
  padding: 30px;
}

.password-form {
  width: 100%;
}

.custom-input {
  height: 42px;
  border-radius: var(--border-radius-md);
  transition: all var(--transition-fast);
}

.custom-input:hover {
  border-color: var(--primary-light);
}

.custom-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(37, 100, 235, 0.1);
}

.password-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 30px;
}

.save-button {
  min-width: 140px;
  height: 42px;
  border-radius: var(--border-radius-md);
  background: var(--primary-gradient);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.save-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  opacity: 0.95;
}

.save-button .anticon {
  margin-right: 6px;
}

.reset-button {
  min-width: 100px;
  height: 42px;
  border-radius: var(--border-radius-md);
  transition: all var(--transition-normal);
}

.security-tips {
  background-color: rgba(37, 100, 235, 0.03);
  border-top: 1px solid var(--border-color);
  padding: 16px 30px;
}

.tips-title {
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tips-list {
  list-style-type: none;
  margin: 0;
  padding: 0;
}

.tips-list li {
  position: relative;
  padding-left: 16px;
  margin-bottom: 6px;
  color: var(--text-secondary);
  font-size: 13px;
}

.tips-list li::before {
  content: "";
  position: absolute;
  left: 0;
  top: 8px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--primary-color);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .edit-password-page {
    padding: 0 16px 30px;
  }
  
  .card-content {
    padding: 20px;
  }
  
  .form-actions {
    flex-direction: column;
  }
  
  .save-button,
  .reset-button {
    width: 100%;
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
