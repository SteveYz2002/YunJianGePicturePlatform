<template>
  <div class="url-picture-upload">
    <div class="url-input-container">
      <a-input-group compact class="url-input-group">
        <a-input
          v-model:value="fileUrl"
          placeholder="请输入图片地址"
          class="custom-input url-input"
        />
        <a-button 
          type="primary" 
          :loading="loading" 
          @click="handleUpload"
          class="submit-button"
        >
          <template #icon><CloudUploadOutlined /></template>
          提交
        </a-button>
      </a-input-group>
      
      <div class="url-hint">支持常见图片链接，确保链接以 http(s):// 开头</div>
    </div>

    <div class="preview-container" v-if="picture?.url">
      <div class="preview-title">图片预览</div>
      <div class="img-wrapper">
        <img :src="picture?.url" alt="图片预览" class="preview-image" />
      </div>
    </div>

    <div class="placeholder-container" v-else>
      <div class="placeholder-content">
        <div class="placeholder-icon">
          <LinkOutlined class="icon" />
        </div>
        <div class="placeholder-text">输入URL并提交后查看图片预览</div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { uploadPictureByUrlUsingPost } from '@/api/pictureController.ts'
import { CloudUploadOutlined, LinkOutlined } from '@ant-design/icons-vue'

interface Props {
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()
const fileUrl = ref<string>()
const loading = ref<boolean>(false)

/**
 * 上传图片
 * @param file
 */
const handleUpload = async () => {
  if (!fileUrl.value) {
    message.warning('请输入有效的图片URL')
    return
  }
  
  if (!fileUrl.value.startsWith('http')) {
    message.warning('图片URL应以 http:// 或 https:// 开头')
    return
  }
  
  loading.value = true
  try {
    const params: API.PictureUploadRequest = { url: fileUrl.value }
    params.spaceId = props.spaceId;
    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
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
</script>
<style scoped>
.url-picture-upload {
  width: 100%;
  margin-bottom: 20px;
}

.url-input-container {
  margin-bottom: 16px;
}

.url-input-group {
  display: flex;
  width: 100%;
}

.url-input {
  flex: 1;
  border-top-left-radius: var(--border-radius-md) !important;
  border-bottom-left-radius: var(--border-radius-md) !important;
  border: 1px solid var(--border-color);
  transition: all var(--transition-fast);
  height: 42px;
}

.url-input:hover {
  border-color: var(--primary-light);
}

.url-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(37, 100, 235, 0.1);
}

.submit-button {
  width: 120px;
  height: 42px;
  border-top-right-radius: var(--border-radius-md) !important;
  border-bottom-right-radius: var(--border-radius-md) !important;
  background: var(--primary-gradient);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-normal);
}

.submit-button:hover {
  opacity: 0.95;
  box-shadow: var(--shadow-sm);
}

.submit-button .anticon {
  margin-right: 6px;
}

.url-hint {
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  padding-left: 4px;
}

.preview-container {
  margin-top: 20px;
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  background-color: rgba(37, 100, 235, 0.02);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  animation: fadeIn var(--transition-normal);
}

.preview-title {
  padding: 12px 16px;
  font-weight: 500;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
  background-color: rgba(37, 100, 235, 0.03);
}

.img-wrapper {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.preview-image {
  max-width: 100%;
  max-height: 480px;
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-normal);
  animation: fadeIn var(--transition-normal), slideUp var(--transition-normal);
}

.preview-image:hover {
  transform: scale(1.02);
  box-shadow: var(--shadow-md);
}

.placeholder-container {
  margin-top: 20px;
  border-radius: var(--border-radius-lg);
  border: 2px dashed var(--border-color);
  background-color: rgba(37, 100, 235, 0.02);
  height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-normal);
}

.placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  text-align: center;
}

.placeholder-icon {
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

.placeholder-icon .icon {
  font-size: 24px;
  color: var(--primary-color);
}

.placeholder-text {
  color: var(--text-secondary);
  font-size: 14px;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { transform: translateY(10px); }
  to { transform: translateY(0); }
}
</style>
