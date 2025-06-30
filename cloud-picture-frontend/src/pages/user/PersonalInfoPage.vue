<template>
  <div id="personalInfoPage">
    <h2 style="margin-top: 20px; margin-left: 50px">个人信息</h2>
    <a-form
      :model="userForm"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 16 }"
      style="padding: 20px; margin-top: 20px; max-width: 800px; margin-left: auto; margin-right: auto"
      class="card"
    >
      <a-form-item name="avatar" label="头像">
        <div class="avatar-container">
          <a-upload
            list-type="picture-card"
            :show-upload-list="false"
            :custom-request="handleUpload"
            :before-upload="beforeUpload"
          >
            <img
              v-if="userForm.userAvatar"
              :src="userForm.userAvatar"
              alt="avatar"
              class="avatar-image"
            />
            <div v-else>
              <loading-outlined v-if="loading"></loading-outlined>
              <plus-outlined v-else></plus-outlined>
              <div class="ant-upload-text">点击或拖拽上传图片</div>
            </div>
          </a-upload>
        </div>
      </a-form-item>
      <a-form-item name="id" label="用户ID">
        <div style="margin-top: 5px">{{ userForm.id }}</div>
      </a-form-item>
      <a-form-item
        name="userAccount"
        label="账号"
        :rules="[{ required: true, message: '请输入账号', trigger: 'blur' }]"
      >
        <a-input v-model:value="userForm.userAccount" />
      </a-form-item>
      <a-form-item
        name="userName"
        label="用户名"
        :rules="[{ required: true, message: '请输入用户名', trigger: 'blur' }]"
      >
        <a-input v-model:value="userForm.userName" />
      </a-form-item>
      <a-form-item name="userProfile" label="个人简介">
        <a-textarea
          v-model:value="userForm.userProfile"
          :auto-size="{ minRows: 2, maxRows: 5 }"
          allow-clear
        />
      </a-form-item>
      <a-form-item :wrapper-col="{ span: 16, offset: 4 }">
        <a-button type="primary" @click="save">保 存</a-button>
      </a-form-item>
    </a-form>


  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { updateUserSelfUsingPost, updateUserUsingPost } from '@/api/userController'
import type { UploadProps } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { uploadAvatarUsingPost, uploadPictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()

let userForm = ref<API.UserVO>({
  id: loginUserStore.loginUser.id,
  userAccount: loginUserStore.loginUser.userAccount,
  userAvatar: loginUserStore.loginUser.userAvatar,
  userName: loginUserStore.loginUser.userName,
  userProfile: loginUserStore.loginUser.userProfile,
  userAccount: loginUserStore.loginUser.userAccount,
})
const loading = ref(false)

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
      message.success('图片上传成功')
      console.log(res.data.data)
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
      } else {
        message.error('头像更新失败，' + (res2.data.message || '未知错误'))
      }
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
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
  const res = await updateUserSelfUsingPost(userForm.value)
  if (res.data.code === 0 && res.data.data) {
    message.success('保存成功')
    await loginUserStore.fetchLoginUser()
  } else {
    message.error('保存失败，' + res.data.message)
  }
}


</script>

<style scoped>
.card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar-container {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.avatar-container :deep(.ant-upload) {
  width: 100px !important;
  height: 100px !important;
  margin-bottom: 0;
}

.avatar-image {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

:deep(.ant-form-item-label) {
  text-align: right;
}

:deep(.ant-upload-select-picture-card) {
  border-radius: 50%;
  overflow: hidden;
  margin: 0;
}

:deep(.ant-form-item) {
  margin-bottom: 24px;
}

:deep(.ant-upload-text) {
  margin-top: 8px;
  font-size: 14px;
  color: #666;
}

:deep(.ant-form-item-control-input) {
  min-height: auto;
}
</style>
