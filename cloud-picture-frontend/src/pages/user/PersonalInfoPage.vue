<template>
  <div id="personalInfoPage">
    <div class="profile-container">
      <!-- 页面顶部：背景封面和个人头像 -->
      <div class="profile-header">
        <div class="cover-photo"></div>
        <div class="profile-info-bar">
          <div class="avatar-wrapper">
            <div class="avatar-container">
              <a-upload
                list-type="picture-card"
                :show-upload-list="false"
                :custom-request="handleUpload"
                :before-upload="beforeUpload"
                class="avatar-uploader"
              >
                <div class="avatar-inner">
                  <img
                    v-if="userForm.userAvatar"
                    :src="userForm.userAvatar"
                    alt="avatar"
                    class="avatar-image"
                  />
                  <div v-else class="avatar-placeholder">
                    <LoadingOutlined v-if="loading" />
                    <UserOutlined v-else class="avatar-icon" />
                  </div>
                  <div class="avatar-overlay">
                    <CameraOutlined />
                    <span class="edit-text">编辑</span>
                  </div>
                </div>
              </a-upload>
            </div>
            <h2 class="user-name">{{ userForm.userName || '用户名' }}</h2>
            <div class="user-id">ID: {{ userForm.id || '未设置' }}</div>
          </div>
        </div>
      </div>
      
      <!-- 页面内容：个人信息表单 -->
      <div class="profile-content">
        <div class="content-card">
          <h3 class="section-title">基本信息</h3>
          
          <a-form
            :model="userForm"
            layout="vertical"
            class="form-custom"
          >
            <a-row :gutter="[24, 0]">
              <a-col :span="12">
                <a-form-item
                  name="userAccount"
                  label="账号"
                  :rules="[{ required: true, message: '请输入账号' }]"
                >
                  <a-input
                    v-model:value="userForm.userAccount"
                    placeholder="请输入账号"
                  >
                    <template #prefix>
                      <UserOutlined />
                    </template>
                  </a-input>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item
                  name="userName"
                  label="用户名"
                  :rules="[{ required: true, message: '请输入用户名' }]"
                >
                  <a-input 
                    v-model:value="userForm.userName" 
                    placeholder="请输入用户名"
                  >
                    <template #prefix>
                      <EditOutlined />
                    </template>
                  </a-input>
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item name="userProfile" label="个人简介" class="profile-textarea-item">
              <a-textarea
                v-model:value="userForm.userProfile"
                :auto-size="{ minRows: 4, maxRows: 8 }"
                placeholder="介绍一下你自己..."
                allow-clear
                class="profile-textarea"
              />
            </a-form-item>

            <div class="form-actions">
              <a-button 
                type="primary" 
                @click="save" 
                :loading="saveLoading"
                class="save-button"
              >
                <template #icon><CheckOutlined /></template>
                保存修改
              </a-button>
              <a-button @click="resetForm" class="reset-button">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
            </div>
          </a-form>
        </div>

        <div class="content-card security-card">
          <h3 class="section-title">账户安全</h3>
          <div class="security-options">
            <div class="security-item">
              <div class="security-item-info">
                <LockOutlined class="security-icon" />
                <div class="security-details">
                  <div class="security-title">账户密码</div>
                  <div class="security-desc">定期修改密码可以提高账户安全性</div>
                </div>
              </div>
              <router-link to="/editPassword">
                <a-button type="link">修改密码</a-button>
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 成功提示 -->
    <a-modal
      v-model:visible="showSuccessModal"
      :footer="null"
      :closable="false"
      :width="400"
      :mask-closable="true"
      class="success-modal"
    >
      <div class="success-content">
        <CheckCircleFilled class="success-icon" />
        <h3>保存成功!</h3>
        <p>您的个人信息已更新</p>
        <a-button type="primary" @click="showSuccessModal = false">确定</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { updateUserSelfUsingPost } from '@/api/userController'
import type { UploadProps } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { 
  LoadingOutlined, 
  UserOutlined, 
  EditOutlined,
  CameraOutlined,
  CheckOutlined,
  ReloadOutlined,
  LockOutlined,
  CheckCircleFilled
} from '@ant-design/icons-vue'
import { uploadAvatarUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()

// 保存原始用户数据，用于重置
const originalUserData = ref<API.UserVO>({
  id: loginUserStore.loginUser.id,
  userAccount: loginUserStore.loginUser.userAccount,
  userAvatar: loginUserStore.loginUser.userAvatar,
  userName: loginUserStore.loginUser.userName,
  userProfile: loginUserStore.loginUser.userProfile,
})

let userForm = ref<API.UserVO>({...originalUserData.value})

const loading = ref(false)
const saveLoading = ref(false)
const showSuccessModal = ref(false)
const picture = ref<API.PictureVO>({})

/**
 * 上传图片
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = picture.value ? { id: picture.value.id } : {}
    const res = await uploadAvatarUsingPost(params, {}, file)
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data

      // 确保使用正确的URL字段
      const avatarUrl = picture.value.url

      // 更新用户头像
      const res2 = await updateUserSelfUsingPost({
        id: userForm.value.id,
        userAvatar: avatarUrl
      })

      if (res2.data.code === 0 && res2.data.data) {
        message.success('头像更新成功')

        // 更新本地userForm对象
        userForm.value.userAvatar = avatarUrl

        // 更新登录用户信息
        await loginUserStore.fetchLoginUser()
        // 同时更新原始数据
        originalUserData.value = {...userForm.value}
      } else {
        message.error('头像更新失败，' + (res2.data.message || '未知错误'))
      }
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error: any) {
    console.error('图片上传失败', error)
    message.error('图片上传失败，' + error.message)
  }
  loading.value = false
}

/**
 * 上传前的校验
 * @param file
 */
const beforeUpload = (file: UploadProps['fileList'][number]) => {
  // 校验图片格式
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    message.error('不支持上传该格式的图片，推荐 jpg 或 png')
  }
  // 校验图片大小
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    message.error('不能上传超过 2M 的图片')
  }
  return isJpgOrPng && isLt2M
}

/**
 * 保存更改
 */
const save = async () => {
  saveLoading.value = true
  try {
    const res = await updateUserSelfUsingPost(userForm.value)
    if (res.data.code === 0 && res.data.data) {
      // 使用模态框替代简单的消息提示
      showSuccessModal.value = true
      
      await loginUserStore.fetchLoginUser()
      // 更新原始数据，使其与当前数据一致
      originalUserData.value = {...userForm.value}
    } else {
      message.error('保存失败，' + res.data.message)
    }
  } catch(error: any) {
    message.error('保存失败: ' + error.message)
  } finally {
    saveLoading.value = false
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  // 重置为原始数据
  userForm.value = {...originalUserData.value}
  message.info('表单已重置')
}

// 页面加载时
onMounted(() => {
  // 确保数据与登录用户信息同步
  userForm.value = {
    id: loginUserStore.loginUser.id,
    userAccount: loginUserStore.loginUser.userAccount,
    userAvatar: loginUserStore.loginUser.userAvatar,
    userName: loginUserStore.loginUser.userName,
    userProfile: loginUserStore.loginUser.userProfile,
  }
  
  // 同时更新原始数据备份
  originalUserData.value = {...userForm.value}
})
</script>

<style scoped>
#personalInfoPage {
  min-height: 100%;
  background-color: #f5f7fa;
}

.profile-container {
  display: flex;
  flex-direction: column;
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: 40px;
}

/* 顶部区域样式 */
.profile-header {
  position: relative;
  margin-bottom: 90px;
}

.cover-photo {
  height: 240px;
  background: linear-gradient(135deg, #42a5f5, #2196f3, #1976d2);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
  border-radius: 0 0 15px 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.profile-info-bar {
  position: absolute;
  bottom: -80px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
}

.avatar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: white;
  border-radius: 20px;
  padding: 0 30px 20px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.avatar-container {
  position: relative;
  margin-top: -60px;
  margin-bottom: 15px;
}

/* 完全移除ant-design上传组件的默认样式 */
.avatar-container :deep(.ant-upload-select) {
  margin: 0;
  border: none;
  width: auto;
  height: auto;
}

.avatar-container :deep(.ant-upload-select:hover) {
  border-color: transparent;
}

.avatar-uploader {
  border: none !important;
  background: transparent !important;
}

.avatar-uploader :deep(.ant-upload) {
  border: none !important;
  background: transparent !important;
  padding: 0 !important;
  width: 120px !important;
  height: 120px !important;
}

.avatar-inner {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  border: 3px solid white;
  aspect-ratio: 1/1;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #e0e0e0;
}

.avatar-icon {
  font-size: 60px;
  color: #999;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
  cursor: pointer;
}

.avatar-inner:hover .avatar-overlay {
  opacity: 1;
}

.edit-text {
  margin-top: 5px;
  font-size: 14px;
}

.user-name {
  font-size: 22px;
  font-weight: 600;
  margin: 0 0 5px;
}

.user-id {
  color: #666;
  font-size: 14px;
}

/* 内容区域样式 */
.profile-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
  padding: 0 20px;
}

.content-card {
  background: white;
  border-radius: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 30px;
}

.section-title {
  margin-top: 0;
  margin-bottom: 25px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  position: relative;
  padding-left: 15px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  height: 18px;
  width: 4px;
  background: linear-gradient(to bottom, #1890ff, #096dd9);
  border-radius: 2px;
}

.form-custom :deep(.ant-form-item-label) {
  font-weight: 500;
}

.form-custom :deep(.ant-form-item) {
  margin-bottom: 24px;
}

.form-custom :deep(.ant-input),
.form-custom :deep(.ant-input-affix-wrapper),
.form-custom :deep(.ant-input-number),
.form-custom :deep(.ant-picker) {
  border-radius: 6px;
  padding: 8px 12px;
  height: auto;
}

.form-custom :deep(.ant-input-affix-wrapper .ant-input) {
  padding: 0;
}

.form-custom :deep(.ant-input-textarea textarea) {
  padding: 16px;
  border-radius: 6px;
  line-height: 1.6;
}

.profile-textarea-item {
  margin-top: 10px;
}

.profile-textarea {
  font-size: 15px;
}

.form-custom :deep(.profile-textarea .ant-input) {
  padding: 16px 20px;
  background-color: #fafafa;
  border-color: #e8e8e8;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.02);
  transition: all 0.3s ease;
}

.form-custom :deep(.profile-textarea .ant-input:focus) {
  background-color: #fff;
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.form-actions {
  display: flex;
  justify-content: flex-start;
  gap: 10px;
  margin-top: 10px;
}

.save-button {
  background: linear-gradient(45deg, #1890ff, #096dd9);
  border: none;
  padding: 0 25px;
  height: 40px;
  border-radius: 6px;
  font-weight: 500;
  box-shadow: 0 4px 10px rgba(24, 144, 255, 0.2);
  transition: all 0.3s ease;
}

.save-button:hover {
  background: linear-gradient(45deg, #40a9ff, #1890ff);
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(24, 144, 255, 0.3);
}

.reset-button {
  border-radius: 6px;
  height: 40px;
  padding: 0 20px;
}

/* 安全设置卡片 */
.security-card {
  margin-bottom: 20px;
}

.security-options {
  display: flex;
  flex-direction: column;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.security-item:last-child {
  border-bottom: none;
}

.security-item-info {
  display: flex;
  align-items: center;
}

.security-icon {
  font-size: 24px;
  margin-right: 15px;
  color: #1890ff;
}

.security-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 5px;
}

.security-desc {
  font-size: 14px;
  color: #888;
}

/* 成功模态框 */
.success-modal :deep(.ant-modal-content) {
  border-radius: 12px;
  overflow: hidden;
}

.success-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.success-icon {
  font-size: 64px;
  color: #52c41a;
  margin-bottom: 20px;
}

.success-content h3 {
  margin: 0 0 10px;
  font-size: 24px;
}

.success-content p {
  margin-bottom: 20px;
  color: #666;
}

.success-content button {
  padding: 0 30px;
}

/* 响应式调整 */
@media (min-width: 768px) {
  .profile-content {
    padding: 0 30px;
  }
  
  .content-card {
    padding: 35px;
  }
}

@media (max-width: 767px) {
  .cover-photo {
    height: 180px;
  }
  
  .avatar-wrapper {
    padding: 0 20px 15px;
  }
  
  .avatar-inner {
    width: 100px;
    height: 100px;
  }
  
  .avatar-uploader :deep(.ant-upload) {
    width: 100px !important;
    height: 100px !important;
  }
  
  .profile-info-bar {
    bottom: -70px;
  }
  
  .profile-header {
    margin-bottom: 80px;
  }
}

@media (max-width: 576px) {
  .content-card {
    padding: 20px;
  }
  
  .form-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .save-button, .reset-button {
    width: 100%;
  }
}
</style>
