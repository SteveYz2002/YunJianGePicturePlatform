
<template>
  <div id="addPicturePage" class="add-picture-page">
    <div class="page-header glass-effect">
      <div class="page-title">
        <h2 class="gradient-text">{{ route.query?.id ? '修改图片' : '创建图片' }}</h2>
        <div class="title-underline"></div>
      </div>
      <a-typography-paragraph v-if="spaceId" class="space-info">
        保存至空间：<a :href="`/space/${spaceId}`" target="_blank" class="space-link">{{ spaceId }}</a>
      </a-typography-paragraph>
    </div>
    
    <!-- 选择上传方式 -->
    <div class="upload-container glass-effect">
      <a-tabs v-model:activeKey="uploadType" class="upload-tabs">
        <a-tab-pane key="file" tab="文件上传">
          <!-- 图片上传组件 -->
          <PictureUpload :picture="picture" :spaceId="spaceId" :onSuccess="onSuccess" />
        </a-tab-pane>
        <a-tab-pane key="url" tab="URL 上传" force-render>
          <!-- URL 图片上传组件 -->
          <UrlPictureUpload :picture="picture" :spaceId="spaceId" :onSuccess="onSuccess" />
        </a-tab-pane>
      </a-tabs>
    </div>
    
    <!-- 图片编辑工具栏 -->
    <div v-if="picture" class="edit-tools glass-effect">
      <div class="tools-title">图片工具</div>
      <a-space size="middle" class="tools-buttons">
        <a-button 
          class="tool-button edit-button" 
          @click="doEditPicture"
        >
          <template #icon><EditOutlined /></template>
          编辑图片
        </a-button>
        <a-button 
          type="primary" 
          class="tool-button ai-button" 
          @click="doImagePainting"
        >
          <template #icon><FullscreenOutlined /></template>
          AI 扩图
        </a-button>
      </a-space>

      <ImageCropper
        ref="imageCropperRef"
        :imageUrl="picture?.url"
        :picture="picture"
        :spaceId="spaceId"
        :space="space"
        :onSuccess="onCropSuccess"
      />
      <ImageOutPainting
        ref="imageOutPaintingRef"
        :picture="picture"
        :spaceId="spaceId"
        :onSuccess="onImageOutPaintingSuccess"
      />
    </div>
    
    <!-- 图片信息表单 -->
    <a-form
      v-if="picture"
      name="pictureForm"
      layout="vertical"
      :model="pictureForm"
      @finish="handleSubmit"
      class="picture-form glass-effect"
    >
      <div class="form-title">图片信息</div>
      
      <a-form-item name="name" label="名称">
        <a-input 
          v-model:value="pictureForm.name" 
          placeholder="请输入名称" 
          allow-clear 
          class="custom-input"
        />
      </a-form-item>
      
      <a-form-item name="introduction" label="简介">
        <a-textarea
          v-model:value="pictureForm.introduction"
          placeholder="请输入简介"
          :auto-size="{ minRows: 3, maxRows: 5 }"
          allow-clear
          class="custom-textarea"
        />
      </a-form-item>
      
      <a-form-item name="category" label="分类">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="请输入分类"
          :options="categoryOptions"
          allow-clear
          class="custom-select"
        />
      </a-form-item>
      
      <a-form-item name="tags" label="标签">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          placeholder="请输入标签"
          :options="tagOptions"
          allow-clear
          class="custom-select"
        />
      </a-form-item>
      
      <a-form-item>
        <a-button type="primary" html-type="submit" class="submit-button">
          {{ route.query?.id ? '保存修改' : '创建图片' }}
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import { computed, h, onMounted, reactive, ref, watchEffect } from 'vue'
import { message } from 'ant-design-vue'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController.ts'
import { useRoute, useRouter } from 'vue-router'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'
import ImageCropper from '@/components/ImageCropper.vue'
import { EditOutlined, FullscreenOutlined } from '@ant-design/icons-vue'
import ImageOutPainting from '@/components/ImageOutPainting.vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController.ts'

const router = useRouter()
const route = useRoute()

const picture = ref<API.PictureVO>()
const pictureForm = reactive<API.PictureEditRequest>({})
const uploadType = ref<'file' | 'url'>('file')
// 空间 id
const spaceId = computed(() => {
  return route.query?.spaceId
})

/**
 * 图片上传成功
 * @param newPicture
 */
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  console.log(values)
  const pictureId = picture.value.id
  if (!pictureId) {
    return
  }
  const res = await editPictureUsingPost({
    id: pictureId,
    spaceId: spaceId.value,
    ...values,
  })
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('创建成功')
    // 跳转到图片详情页
    router.push({
      path: `/picture/${pictureId}`,
    })
  } else {
    message.error('创建失败，' + res.data.message)
  }
}

const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])

/**
 * 获取标签和分类选项
 * @param values
 */
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

// 获取老数据
const getOldPicture = async () => {
  // 获取到 id
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoByIdUsingGet({
      id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.introduction = data.introduction
      pictureForm.category = data.category
      pictureForm.tags = data.tags
    }
  }
}

onMounted(() => {
  getOldPicture()
})

// ----- 图片编辑器引用 ------
const imageCropperRef = ref()

// 编辑图片
const doEditPicture = async () => {
  imageCropperRef.value?.openModal()
}

// 编辑成功事件
const onCropSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

// ----- AI 扩图引用 -----
const imageOutPaintingRef = ref()

// 打开 AI 扩图弹窗
const doImagePainting = async () => {
  imageOutPaintingRef.value?.openModal()
}

// AI 扩图保存事件
const onImageOutPaintingSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}
const space = ref<API.SpaceVO>()

// 获取空间信息
const fetchSpace = async () => {
  // 获取数据
  if (spaceId.value) {
    const res = await getSpaceVoByIdUsingGet({
      id: spaceId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    }
  }
}

watchEffect(() => {
  fetchSpace()
})



</script>

<style scoped>
.add-picture-page {
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
  margin-bottom: 8px;
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

.space-info {
  margin-top: 16px;
  margin-bottom: 0;
  color: var(--text-secondary);
}

.space-link {
  color: var(--primary-color);
  transition: color var(--transition-fast);
  font-weight: 500;
}

.space-link:hover {
  color: var(--primary-dark);
  text-decoration: underline;
}

.upload-container {
  padding: 20px;
  border-radius: var(--border-radius-lg);
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
}

.upload-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 20px;
}

.upload-tabs :deep(.ant-tabs-tab) {
  padding: 8px 16px;
  transition: all var(--transition-fast);
}

.upload-tabs :deep(.ant-tabs-tab-active) {
  font-weight: 500;
}

.upload-tabs :deep(.ant-tabs-ink-bar) {
  background: var(--primary-gradient);
  height: 3px;
  border-radius: 3px 3px 0 0;
}

.edit-tools {
  padding: 20px 24px;
  border-radius: var(--border-radius-lg);
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
}

.tools-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.tools-buttons {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 16px;
}

.tool-button {
  height: 40px;
  border-radius: var(--border-radius-md);
  padding: 0 20px;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
}

.tool-button .anticon {
  margin-right: 8px;
}

.tool-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.edit-button {
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.ai-button {
  background: var(--primary-gradient);
  border: none;
}

.picture-form {
  padding: 20px 24px;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
}

.form-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 24px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.custom-input,
.custom-textarea,
.custom-select {
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  transition: all var(--transition-fast);
}

.custom-input:hover,
.custom-textarea:hover,
.custom-select:hover {
  border-color: var(--primary-light);
  box-shadow: 0 0 0 2px rgba(37, 100, 235, 0.1);
}

.custom-input:focus,
.custom-textarea:focus,
.custom-select:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(37, 100, 235, 0.2);
}

.submit-button {
  width: 100%;
  height: 44px;
  border-radius: var(--border-radius-md);
  font-size: 16px;
  font-weight: 500;
  background: var(--primary-gradient);
  border: none;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.submit-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  opacity: 0.95;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .add-picture-page {
    padding: 0 16px 30px;
  }
  
  .page-header,
  .upload-container,
  .edit-tools,
  .picture-form {
    padding: 16px;
  }
  
  .tools-buttons {
    flex-direction: column;
    align-items: stretch;
  }
  
  .tool-button {
    width: 100%;
  }
}
</style>
