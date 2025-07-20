<template>
  <a-modal
    class="image-text-to-image"
    v-model:visible="visible"
    :footer="false"
    :width="1000"
    @cancel="closeModal"
    :mask-closable="false"
    :closable="true"
  >
    <template #title>
      <div class="modal-title">
        <span class="title-icon"><BulbOutlined /></span>
        <span>AI 文生图</span>
      </div>
    </template>

    <a-row class="content-container" :gutter="24">
      <a-col :span="12">
        <div class="section-card prompt-card">
          <h4 class="section-title">
            <FormOutlined /> 文本提示词
          </h4>

          <div class="prompt-section">
            <a-form layout="vertical">
              <a-form-item label="提示词">
                <a-textarea
                  v-model:value="prompt"
                  placeholder="描述您想要生成的图像，例如：'一只可爱的猫咪在阳光下玩耍'"
                  :rows="4"
                />
              </a-form-item>
              <a-form-item label="负面提示词">
                <a-textarea
                  v-model:value="negativePrompt"
                  placeholder="描述您不希望在图像中出现的内容，例如：'模糊、变形、低质量'"
                  :rows="2"
                />
              </a-form-item>
            </a-form>
          </div>

          <div class="parameters-section">
            <h5><SlidersFilled /> 生成参数：</h5>
            <a-form layout="vertical">
              <a-form-item label="图像尺寸">
                <a-select v-model:value="imageSize">
                  <a-select-option value="1024*1024">1024 × 1024 (正方形)</a-select-option>
                  <a-select-option value="720*1280">720 × 1280 (竖屏)</a-select-option>
                  <a-select-option value="1280*720">1280 × 720 (横屏)</a-select-option>
                  <a-select-option value="768*768">768 × 768 (小尺寸)</a-select-option>
                </a-select>
              </a-form-item>
              <a-form-item label="提示词引导强度">
                <div class="parameter-row">
                  <a-slider
                    v-model:value="guidanceScale"
                    :min="1"
                    :max="30"
                    :step="1"
                    class="scale-slider"
                  />
                  <div class="scale-badge">{{ guidanceScale }}</div>
                </div>
              </a-form-item>
              <a-form-item label="推理步数">
                <div class="parameter-row">
                  <a-slider
                    v-model:value="steps"
                    :min="10"
                    :max="50"
                    :step="5"
                    class="scale-slider"
                  />
                  <div class="scale-badge">{{ steps }}</div>
                </div>
              </a-form-item>
              <a-form-item label="保存到空间">
                <a-select v-model:value="saveToSpace" placeholder="选择保存空间" allowClear>
                  <a-select-option v-for="space in spaces" :key="space.id" :value="space.id">
                    {{ space.name }}
                  </a-select-option>
                </a-select>
                <div class="space-info" v-if="!spaces || spaces.length === 0">
                  <a-alert type="info" message="未找到可用空间，将保存到我的空间" banner />
                </div>
              </a-form-item>
            </a-form>
          </div>
        </div>
      </a-col>

      <a-col :span="12">
        <div class="section-card result-image">
          <h4 class="section-title">
            <RocketOutlined /> 生成结果
          </h4>
          <div class="image-container result-container" :class="{'has-result': resultImageUrl}">
            <img
              v-if="resultImageUrl"
              :src="resultImageUrl"
              :alt="'AI 生成图像'"
              class="preview-image"
            />
            <div v-else class="no-result">
              <InboxOutlined />
              <p>生成的图像将在这里显示</p>
            </div>
            <div v-if="taskId && !resultImageUrl" class="processing-overlay">
              <div class="processing-content">
                <LoadingOutlined spin />
                <p>正在生成图像...</p>
              </div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>

    <div class="footer-actions">
      <a-button
        :disabled="!isPromptValid"
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
        :loading="saveLoading"
        class="action-button apply-button"
        @click="handleSaveImage"
      >
        <CheckOutlined /> 保存图片
      </a-button>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, computed, onBeforeUnmount, onMounted } from 'vue'
import {
  createPictureTextToImageTaskUsingPost,
  getPictureTextToImageTaskUsingGet,
  saveGeneratedImageUsingPost
} from '@/api/pictureController.ts'
import { getSpaceListByUserIdUsingGet } from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import {
  BulbOutlined,
  FormOutlined,
  RocketOutlined,
  SettingOutlined,
  SlidersFilled,
  ThunderboltOutlined,
  CheckOutlined,
  LoadingOutlined,
  InboxOutlined
} from '@ant-design/icons-vue'

interface Props {
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()
const loginUserStore = useLoginUserStore()

const prompt = ref<string>('')
const negativePrompt = ref<string>('')
const resultImageUrl = ref<string>('')
const imageSize = ref<string>('1024*1024')
const guidanceScale = ref<number>(10)
const steps = ref<number>(30)
const saveToSpace = ref<number | undefined>(props.spaceId)

// 空间列表
const spaces = ref<{ id: number, name: string }[]>([])

// 加载用户空间列表
const loadUserSpaces = async () => {
  try {
    // 检查用户ID
    if (!loginUserStore.loginUser?.id) {
      return
    }

    // 查询用户的空间
    const res = await getSpaceListByUserIdUsingGet({
      userId: loginUserStore.loginUser.id
    })

    if (res.data.code === 0 && res.data.data) {
      // 处理空间列表数据
      spaces.value = [
        { id: null, name: '公共空间' }, // 添加默认的我的空间
        ...res.data.data.map(space => ({
          id: space.id as number,
          name: space.spaceName as string
        }))
      ]

      // 如果没有指定空间ID，默认使用第一个空间
      if (!saveToSpace.value && spaces.value.length > 0) {
        saveToSpace.value = spaces.value[0].id
      }
    }
  } catch (error) {
    console.error('加载空间列表失败', error)
  }
}

onMounted(() => {
  // 组件挂载时加载空间列表
  loadUserSpaces()
})

// 任务 id
const taskId = ref<string>('')

// 是否提示词有效
const isPromptValid = computed(() => {
  return prompt.value.trim().length >= 5
})

// 错误信息与中文说明的映射关系
const errorMessageMap: Record<string, string> = {
  'input json error': '输入 json 错误',
  'The prompt content does not comply with green network verification': '提示词内容不合规',
  'The image content does not comply with green network verification': '生成的图像内容不合规',
  'the parameters must conform to the specification: xxx': '输入参数值超出范围',
  'algorithm process error': '算法错误',
  'oss upload error': '文件上传失败',
}

// 方法：根据 message 获取对应说明
function getErrorDescription(message: string): string {
  return errorMessageMap[message] || '未知错误'
}

/**
 * 创建任务
 */
const createTask = async () => {
  if (!isPromptValid.value) {
    message.error('请输入有效的提示词')
    return
  }
  try {
    const res = await createPictureTextToImageTaskUsingPost({
      prompt: prompt.value,
      negativePrompt: negativePrompt.value,
      spaceId: saveToSpace.value,
      parameters: {
        size: imageSize.value,
        guidanceScale: guidanceScale.value,
        steps: steps.value,
        n: 1
      }
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('创建任务成功，请耐心等待')
      console.log(res.data.data.output?.taskId)
      taskId.value = res.data.data.output?.taskId
      // 开启轮询
      startPolling()
    } else {
      message.error('创建任务失败，' + res.data.message)
    }
  } catch (error) {
    message.error('创建文生图任务失败')
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
      const res = await getPictureTextToImageTaskUsingGet({
        taskId: taskId.value
      })
      if (res.data.code === 0 && res.data.data) {
        const taskResult = res.data.data.output
        if (taskResult?.taskStatus === 'SUCCEEDED') {
          message.success('生成图像任务执行成功')
          if (taskResult.results && taskResult.results.length > 0) {
            resultImageUrl.value = taskResult.results[0].url
          }
          // 清理轮询
          clearPolling()
        } else if (taskResult?.taskStatus === 'FAILED') {
          message.error('生成图像任务执行失败，' + getErrorDescription(taskResult?.message))

          // 清理轮询
          clearPolling()
        }
      }
    } catch (error) {
      console.error('文生图任务轮询失败', error)
      message.error('文生图任务轮询失败，' + error.message)
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
  }
}

// 组件销毁前清理轮询
onBeforeUnmount(() => {
  clearPolling()
})

// 是否正在保存
const saveLoading = ref(false)

/**
 * 保存生成的图片
 */
const handleSaveImage = async () => {
  saveLoading.value = true
  try {
    const params = {
      taskId: taskId.value || '',
      imageUrl: resultImageUrl.value || '',
      prompt: prompt.value,
      // 如果未选择空间，默认使用我的空间（ID为-1）
      spaceId: saveToSpace.value ?? null
    }
    const res = await saveGeneratedImageUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片保存成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      // 关闭弹窗
      closeModal()
    } else {
      message.error('图片保存失败，' + res.data.message)
    }
  } catch (error: any) {
    console.error('图片保存失败', error)
    message.error('图片保存失败，' + (error?.message || '未知错误'))
  }
  saveLoading.value = false
}

// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
  // 默认使用传入的空间ID
  saveToSpace.value = props.spaceId
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
  // 重置所有状态
  resultImageUrl.value = ''
  taskId.value = ''
  prompt.value = ''
  negativePrompt.value = ''
  imageSize.value = '1024*1024'
  guidanceScale.value = 10
  steps.value = 30
  saveToSpace.value = props.spaceId
  clearPolling()
}

// 暴露函数给父组件
defineExpose({
  openModal
})
</script>

<style scoped>
.image-text-to-image {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.image-text-to-image :deep(.ant-modal-content) {
  border-radius: 12px;
  overflow: hidden;
}

.image-text-to-image :deep(.ant-modal-header) {
  padding: 16px 24px;
  background: linear-gradient(135deg, #722ed1, #b37feb);
  border-bottom: none;
}

.image-text-to-image :deep(.ant-modal-body) {
  padding: 24px;
}

.image-text-to-image :deep(.ant-modal-close) {
  color: white;
}

.image-text-to-image :deep(.ant-modal-close:hover) {
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
  background-color: #fafafa;
  border-radius: 8px;
  padding: 16px;
  height: 100%;
  min-height: 400px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.result-image {
  display: flex;
  flex-direction: column;
}

.section-title {
  margin-top: 0;
  margin-bottom: 16px;
  color: #333;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.prompt-section, .parameters-section {
  margin-bottom: 20px;
}

.parameters-section h5 {
  margin-top: 10px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #555;
  display: flex;
  align-items: center;
  gap: 6px;
}

.parameter-row {
  display: flex;
  align-items: center;
}

.scale-slider {
  flex: 1;
}

.scale-badge {
  min-width: 40px;
  padding: 2px 8px;
  margin-left: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  text-align: center;
  font-size: 12px;
}

.image-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  min-height: 250px;
}

.result-container {
  border: 2px dashed #d9d9d9;
}

.result-container.has-result {
  border: none;
}

.preview-image {
  max-width: 100%;
  max-height: 300px;
  object-fit: contain;
}

.no-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}

.no-result .anticon {
  font-size: 48px;
  margin-bottom: 10px;
}

.processing-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.processing-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.processing-content .anticon {
  font-size: 36px;
  margin-bottom: 10px;
}

.footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

.action-button {
  min-width: 120px;
}

.generate-button {
  background-color: #722ed1;
}

.apply-button {
  background-color: #52c41a;
}

.space-info {
  margin-top: 8px;
}
</style>
