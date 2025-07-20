<template>
  <a-modal
    class="image-out-painting"
    v-model:visible="visible"
    :footer="false"
    :width="1000"
    @cancel="closeModal"
    :mask-closable="false"
    :closable="true"
  >
    <template #title>
      <div class="modal-title">
        <span class="title-icon"><ExperimentOutlined /></span>
        <span>AI 智能扩图</span>
      </div>
    </template>
    
    <a-row class="content-container" :gutter="24">
      <a-col :span="8">
        <div class="section-card original-image">
          <h4 class="section-title">
            <PictureOutlined /> 原始图片
          </h4>
          <div class="image-container">
            <img :src="picture?.url" :alt="picture?.name" class="preview-image" />
          </div>
          <div class="image-info">
            <div class="info-item">
              <InfoCircleOutlined />
              <span>图像大小：{{ formatSize(picture?.picSize) }}</span>
            </div>
            <div class="info-item">
              <ColumnWidthOutlined />
              <span>尺寸：{{ picture?.picHeight ?? '-' }}px × {{ picture?.picWidth ?? '-' }}px</span>
            </div>
            <div class="info-item">
              <FileImageOutlined />
              <span>格式：{{ picture?.picFormat }}</span>
            </div>
          </div>
          <div class="alerts-container">
            <a-alert
              v-if="(picture?.picFormat != 'webp' && picture?.picFormat != 'jpg' && picture?.picFormat != 'jpeg' && picture?.picFormat != 'png' )"
              type="warning"
              message="图片格式不符，无法扩图"
              show-icon
              class="status-alert"
            />
            <a-alert
              v-if="picture?.picSize > 10 * 1024 * 1024"
              type="warning"
              message="图片大小超过10MB，无法扩图"
              show-icon
              class="status-alert"
            />
            <a-alert
              v-if="picture?.picWidth < 512 || picture?.picHeight < 512"
              type="warning"
              message="图片尺寸小于512×512，无法扩图"
              show-icon
              class="status-alert"
            />
          </div>
        </div>
      </a-col>
      
      <a-col :span="8">
        <div class="section-card result-image">
          <h4 class="section-title">
            <RocketOutlined /> 扩图结果
          </h4>
          <div class="image-container result-container" :class="{'has-result': resultImageUrl}">
            <img
              v-if="resultImageUrl"
              :src="resultImageUrl"
              :alt="picture?.name"
              class="preview-image"
            />
            <div v-else class="no-result">
              <InboxOutlined />
              <p>扩图结果将在这里显示</p>
            </div>
            <div v-if="taskId && !resultImageUrl" class="processing-overlay">
              <div class="processing-content">
                <LoadingOutlined spin />
                <p>正在扩图中...</p>
              </div>
            </div>
          </div>
        </div>
      </a-col>
      
      <a-col :span="8">
        <div class="section-card requirement-card">
          <h4 class="section-title">
            <SettingOutlined /> 扩图设置
          </h4>
          
          <div class="requirement-section">
            <h5><CheckCircleOutlined /> 图像要求：</h5>
            <ul class="requirement-list">
              <li>图像格式：JPG、JPEG、PNG、HEIF、WEBP</li>
              <li>图像大小：不超过10MB</li>
              <li>图像分辨率：不低于512×512像素且不超过4096×4096像素</li>
              <li>图像单边长度范围：[512, 4096]，单位像素</li>
            </ul>
          </div>
          
          <div class="parameters-section">
            <h5><SlidersFilled /> 扩图参数：</h5>
            <a-form layout="vertical">
              <a-form-item label="横向扩展比例">
                <div class="parameter-row">
                  <a-slider 
                    v-model:value="xScale" 
                    :min="1" 
                    :max="4" 
                    :step="0.5" 
                    class="scale-slider"
                  />
                  <div class="scale-badge">{{ xScale }}x</div>
                </div>
              </a-form-item>
              <a-form-item label="纵向扩展比例">
                <div class="parameter-row">
                  <a-slider 
                    v-model:value="yScale" 
                    :min="1" 
                    :max="4" 
                    :step="0.5"
                    class="scale-slider"
                  />
                  <div class="scale-badge">{{ yScale }}x</div>
                </div>
              </a-form-item>
            </a-form>
          </div>
        </div>
      </a-col>
    </a-row>
    
    <div class="footer-actions">
      <a-button
        :disabled="(picture?.picFormat != 'webp' && picture?.picFormat != 'jpg' && picture?.picFormat != 'jpeg' && picture?.picFormat != 'png' ) || (picture?.picSize > 10 * 1024 * 1024) || (picture?.picWidth < 512 || picture?.picHeight < 512) "
        type="primary"
        :loading="!!taskId"
        class="action-button generate-button"
        @click="createTask"
      >
        <ThunderboltOutlined /> 生成图片
      </a-button>
      <a-button 
        v-if="resultImageUrl" 
        type="primary" 
        :loading="uploadLoading" 
        class="action-button apply-button" 
        @click="handleUpload"
      >
        <CheckOutlined /> 应用结果
      </a-button>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import {
  createPictureOutPaintingTaskUsingPost,
  getPictureOutPaintingTaskUsingGet,
  uploadPictureByUrlUsingPost
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { formatSize } from '@/utils'
import {
  ExperimentOutlined,
  PictureOutlined,
  RocketOutlined,
  SettingOutlined,
  InfoCircleOutlined,
  FileImageOutlined,
  ColumnWidthOutlined,
  CheckCircleOutlined,
  SlidersFilled,
  ThunderboltOutlined,
  CheckOutlined,
  LoadingOutlined,
  InboxOutlined
} from '@ant-design/icons-vue'

interface Props {
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

const resultImageUrl = ref<string>('')

// 任务 id
const taskId = ref<string>()

// 错误信息与中文说明的映射关系
const errorMessageMap: Record<string, string> = {
  'input json error': '输入 json 错误',
  'oss download error': '输入图像下载失败',
  'read image error': '读取图像失败',
  'The image content does not comply with green network verification': '图像内容不合规',
  'the parameters must conform to the specification: xxx': '输入参数值超出范围',
  'The image size is not supported for the data inspection.': '输出图像尺寸超限（大于 10M）',
  'algorithm process error': '算法错误',
  'oss upload error': '文件上传失败',
  'the size of input image is too small or to large': '文件大小不合规'
}

// 方法：根据 message 获取对应说明
function getErrorDescription(message: string): string {
  return errorMessageMap[message] || '未知错误'
}

// 扩图参数
const xScale = ref(2)
const yScale = ref(2)

/**
 * 创建任务
 */
const createTask = async () => {
  if (!props.picture?.id) {
    return
  }
  try {
    const res = await createPictureOutPaintingTaskUsingPost({
      pictureId: props.picture.id,
      // 使用用户设置的扩图参数
      parameters: {
        xScale: xScale.value,
        yScale: yScale.value
      }
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('创建任务成功，请耐心等待')
      console.log(res.data.data.output?.taskId)
      taskId.value = res.data.data.output?.taskId
      // 开启轮询
      startPolling()
    } else {
      message.error('图片任务失败，' + res.data.message)
    }
  } catch (error) {
    message.error('创建扩图任务失败')
  }
}

// 轮询定时器
let pollingTimer: NodeJS.Timeout = null

// 开始轮询
const startPolling = () => {
  if (!taskId.value) {
    return
  }

  pollingTimer = setInterval(async () => {
    try {
      const res = await getPictureOutPaintingTaskUsingGet({
        taskId: taskId.value
      })
      if (res.data.code === 0 && res.data.data) {
        const taskResult = res.data.data.output
        if (taskResult?.taskStatus === 'SUCCEEDED') {
          message.success('扩图任务执行成功')
          resultImageUrl.value = taskResult?.outputImageUrl
          // 清理轮询
          clearPolling()
        } else if (taskResult?.taskStatus === 'FAILED') {
          message.error('扩图任务执行失败，' + getErrorDescription(taskResult?.message))

          // 清理轮询
          clearPolling()
        }
      }
    } catch (error) {
      console.error('扩图任务轮询失败', error)
      message.error('扩图任务轮询失败，' + error.message)
      // 清理轮询
      clearPolling()
    }
  }, 5000) // 每 5 秒轮询一次
}

// 清理轮询
const clearPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
    taskId.value = null
  }
}

// 是否正在上传
const uploadLoading = ref(false)

/**
 * 上传图片
 * @param file
 */
const handleUpload = async () => {
  uploadLoading.value = true
  try {
    const params: API.PictureUploadRequest = {
      url: resultImageUrl.value,
      spaceId: props.spaceId
    }
    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      // 关闭弹窗
      closeModal()
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    console.error('图片上传失败', error)
    message.error('图片上传失败，' + error.message)
  }
  uploadLoading.value = false
}

// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
  // 重置所有状态
  resultImageUrl.value = ''
  taskId.value = null
  xScale.value = 2
  yScale.value = 2
  clearPolling()
}

// 暴露函数给父组件
defineExpose({
  openModal
})
</script>

<style scoped>
.image-out-painting {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.image-out-painting :deep(.ant-modal-content) {
  border-radius: 12px;
  overflow: hidden;
}

.image-out-painting :deep(.ant-modal-header) {
  padding: 16px 24px;
  background: linear-gradient(135deg, #1677ff, #69b1ff);
  border-bottom: none;
}

.image-out-painting :deep(.ant-modal-body) {
  padding: 24px;
}

.image-out-painting :deep(.ant-modal-close) {
  color: white;
}

.image-out-painting :deep(.ant-modal-close:hover) {
  color: rgba(255, 255, 255, 0.85);
}

.modal-title {
  color: white;
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.title-icon {
  margin-right: 10px;
  font-size: 20px;
}

.content-container {
  margin-bottom: 20px;
}

.section-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  height: 100%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  position: relative;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  gap: 8px;
}

.image-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f8ff;
  border-radius: 8px;
  overflow: hidden;
  min-height: 200px;
  position: relative;
}

.preview-image {
  max-width: 100%;
  max-height: 200px;
  object-fit: contain;
  border-radius: 4px;
}

.image-info {
  margin-top: 16px;
  padding: 12px;
  background-color: #f8fafc;
  border-radius: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #475569;
}

.info-item:last-child {
  margin-bottom: 0;
}

.alerts-container {
  margin-top: 16px;
}

.status-alert {
  margin-bottom: 8px;
}

.status-alert:last-child {
  margin-bottom: 0;
}

.requirement-section, 
.parameters-section {
  margin-bottom: 20px;
}

.requirement-section h5,
.parameters-section h5 {
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.requirement-list {
  padding-left: 20px;
  margin-bottom: 16px;
  list-style-type: none;
}

.requirement-list li {
  position: relative;
  padding-left: 16px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #64748b;
}

.requirement-list li:before {
  content: "•";
  color: #1677ff;
  position: absolute;
  left: 0;
  font-weight: bold;
}

.parameter-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.scale-slider {
  flex: 1;
}

.scale-badge {
  background-color: #1677ff;
  color: white;
  border-radius: 16px;
  padding: 2px 10px;
  font-size: 12px;
  min-width: 40px;
  text-align: center;
}

.footer-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
}

.action-button {
  min-width: 120px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-button:deep(.ant-btn-primary) {
  color: white;
}

.generate-button {
  background: linear-gradient(135deg, #1677ff, #4096ff);
  border: none;
  color: white !important;
}

.apply-button {
  background: linear-gradient(135deg, #52c41a, #73d13d);
  border: none;
  color: white !important;
}

.result-container {
  min-height: 200px;
  position: relative;
  border: 2px dashed #e2e8f0;
}

.result-container.has-result {
  border: none;
}

.no-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.no-result :deep(svg) {
  font-size: 32px;
  margin-bottom: 8px;
}

.processing-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  backdrop-filter: blur(2px);
}

.processing-content {
  text-align: center;
}

.processing-content :deep(svg) {
  font-size: 24px;
  color: #1677ff;
  margin-bottom: 8px;
}
</style>
