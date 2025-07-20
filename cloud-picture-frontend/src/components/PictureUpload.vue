<template>
  <div class="picture-upload">
    <a-upload
      list-type="picture-card"
      :show-upload-list="false"
      :custom-request="handleUpload"
      :before-upload="beforeUpload"
      class="upload-container"
    >
      <div class="upload-content">
        <img v-if="picture?.url" :src="picture?.url" alt="avatar" class="uploaded-image" />
        <div v-else class="upload-placeholder">
          <div class="upload-icon-container">
            <loading-outlined v-if="loading" class="loading-icon spin" />
            <plus-outlined v-else class="upload-icon" />
          </div>
          <div class="upload-text">点击或拖拽上传图片</div>
          <div class="upload-hint">支持 JPG/PNG 格式，小于 2MB</div>
        </div>
      </div>
    </a-upload>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons-vue'
import type { UploadProps } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { uploadPictureUsingPost } from '@/api/pictureController.ts'

interface Props {
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

/**
 * 上传图片
 * @param file
 */
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = props.picture ? { id: props.picture.id } : {}
    params.spaceId = props.spaceId;
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    console.error('图片上传失败', error)
    message.error('图片上传失败，' + error.message)
  }
  loading.value = false
}

const loading = ref<boolean>(false)

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
</script>
<style scoped>
.picture-upload {
  width: 100%;
  margin-bottom: 20px;
}

.upload-container {
  width: 100%;
  cursor: pointer;
}

.picture-upload :deep(.ant-upload) {
  width: 100% !important;
  height: auto !important;
  min-width: 200px;
  min-height: 200px;
  background-color: rgba(37, 100, 235, 0.02);
  border: 2px dashed var(--border-color);
  border-radius: var(--border-radius-lg);
  transition: all var(--transition-normal);
  overflow: hidden;
}

.picture-upload :deep(.ant-upload:hover) {
  border-color: var(--primary-color);
  background-color: rgba(37, 100, 235, 0.05);
  box-shadow: var(--shadow-sm);
}

.upload-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  min-height: 240px;
  padding: 20px;
}

.uploaded-image {
  max-width: 100%;
  max-height: 480px;
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-normal);
}

.uploaded-image:hover {
  transform: scale(1.02);
  box-shadow: var(--shadow-md);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 40px 20px;
}

.upload-icon-container {
  height: 64px;
  width: 64px;
  border-radius: 50%;
  background: rgba(37, 100, 235, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  transition: all var(--transition-normal);
}

.upload-container:hover .upload-icon-container {
  transform: translateY(-5px);
  background: rgba(37, 100, 235, 0.1);
}

.upload-icon {
  font-size: 28px;
  color: var(--primary-color);
}

.loading-icon {
  font-size: 24px;
  color: var(--primary-color);
}

.spin {
  animation: spin 1.2s infinite linear;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.upload-text {
  margin-bottom: 8px;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 500;
}

.upload-hint {
  color: var(--text-secondary);
  font-size: 13px;
}
</style>
