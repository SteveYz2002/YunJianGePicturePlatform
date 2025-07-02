<template>
  <a-modal
    class="image-out-painting"
    v-model:visible="visible"
    title="AI 扩图"
    :footer="false"
    :width="1000"
    @cancel="closeModal"
  >
    <a-row gutter="16">
      <a-col span="8">
        <h4>原始图片</h4>
        <img :src="picture?.url" :alt="picture?.name" style="max-width: 100%; margin-top: 20px" />
        <div style="text-align: left; margin-left: 50px;margin-top: 10px">
          <h4>图像大小：{{ formatSize(picture?.picSize) }}</h4>
          <h4>图片尺寸（高/宽）：{{ picture?.picHeight ?? '-' }}px / {{ picture?.picWidth ?? '-' }}px</h4>
          <h4>图片格式：{{ picture?.picFormat }}</h4>
        </div>
        <div style="margin-top: 10px">
          <a-alert
            v-if="(picture?.picFormat!= 'webp' && picture?.picFormat!= 'jpg' && picture?.picFormat!= 'jpeg' && picture?.picFormat!= 'png' ) "
            type="warning"
            message="图片格式不符，无法扩图"
            show-icon
          />
          <a-alert
            v-if="picture?.picSize > 10 * 1024 * 1024"
            type="warning"
            message="图片大小超过10MB，无法扩图"
            show-icon
          />
          <a-alert
            v-if="picture?.picWidth < 512 || picture?.picHeight < 512"
            type="warning"
            message="图片尺寸小于512x512，无法扩图"
            show-icon
          />
        </div>
      </a-col>
      <a-col span="8">
        <h4>扩图结果</h4>
        <img
          v-if="resultImageUrl"
          :src="resultImageUrl"
          :alt="picture?.name"
          style="max-width: 100%; margin-top: 20px"
        />
      </a-col>
      <a-col span="8">
        <h4>扩图要求</h4>
        <a-card class="requirement-card" size="small">
          <h5>图像限制：</h5>
          <ul class="requirement-list">
            <li>图像格式：JPG、JPEG、PNG、HEIF、WEBP</li>
            <li>图像大小：不超过10MB</li>
            <li>图像分辨率：不低于512×512像素且不超过4096×4096像素</li>
            <li>图像单边长度范围：[512, 4096]，单位像素</li>
          </ul>
          <h5>扩图参数：</h5>
          <a-form layout="vertical">
            <a-form-item label="横向扩展比例">
              <a-slider v-model:value="xScale" :min="1" :max="4" :step="0.5" />
              <div class="scale-value">当前值: {{ xScale }}x</div>
            </a-form-item>
            <a-form-item label="纵向扩展比例">
              <a-slider v-model:value="yScale" :min="1" :max="4" :step="0.5" />
              <div class="scale-value">当前值: {{ yScale }}x</div>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
    <div style="margin-bottom: 16px" />
    <a-flex justify="center" gap="16">
      <a-button
        :disabled="(picture?.picFormat!= 'webp' && picture?.picFormat!= 'jpg' && picture?.picFormat!= 'jpeg' && picture?.picFormat!= 'png' ) || (picture?.picSize > 10 * 1024 * 1024) || (picture?.picWidth < 512 || picture?.picHeight < 512) "
        type="primary"
        :loading="!!taskId"
        ghost
        @click="createTask"
      >
        生成图片
      </a-button>
      <a-button v-if="resultImageUrl" type="primary" :loading="uploadLoading" @click="handleUpload">
        应用结果
      </a-button>
    </a-flex>
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
  const res = await createPictureOutPaintingTaskUsingPost({
    pictureId: props.picture.id,
    // 使用用户设置的扩图参数
    parameters: {
      xScale: xScale.value,
      yScale: yScale.value
    }
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('创建任务成功，请耐心等待，不要退出界面')
    console.log(res.data.data.output?.taskId)
    taskId.value = res.data.data.output?.taskId
    // 开启轮询
    startPolling()
  } else {
    message.error('图片任务失败，' + res.data.message)
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
  }, 5000) // 每 3 秒轮询一次
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

<style>
.image-out-painting {
  text-align: center;
}

.requirement-card {
  margin-bottom: 16px;
  text-align: left;
}

.requirement-list {
  padding-left: 20px;
  margin-bottom: 16px;
}

.requirement-list li {
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

h5 {
  font-weight: 500;
  margin-bottom: 12px;
  color: #333;
  text-align: left;
}

.requirement-card :deep(.ant-form-item-label) {
  text-align: left;
}

.requirement-card :deep(.ant-form-item) {
  margin-bottom: 24px;
}

.requirement-card :deep(.ant-slider) {
  margin: 0 auto;
  width: 90%;
}

.requirement-card .scale-value {
  margin-top: 4px;
  color: #666;
  text-align: center;
}
</style>
