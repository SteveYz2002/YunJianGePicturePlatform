<template>
  <div class="url-picture-upload">
    <a-input-group compact style="margin-bottom: 16px">
      <a-input v-model:value="fileUrl" style="width: calc(100% - 120px)" placeholder="请输入图片 URL" />
      <a-button type="primary" :loading="loading" @click="handleUpload" style="width: 120px">提交</a-button>
    </a-input-group>
    <div class="imag-wrapper">
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
    </div>
  </div>

</template>
<script lang="ts" setup>
import { ref } from 'vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadProps } from 'ant-design-vue'
import { uploadPictureByUrlUsingPost, uploadPictureUsingPost } from '@/api/pictureController.ts'

interface Props {
  picture?: API.PictureVO;
  onSuccess?: (newPicture: API.PictureVO) => void;
}

const props = defineProps<Props>()

const fileUrl = ref<string>('')

/**
 * @description: 上传图片
 * @param file
 */
const handleUpload = async () => {
  loading.value = true
  try {
    const params : API.PictureUploadRequest= { fileUrl: fileUrl.value}
    if(props.picture){
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将图片信息传递给父组件
      props.onSuccess?.(res.data.data)
    } else {
      message.error('图片上传失败' + res.data.message)
    }
  } catch (error) {
    console.log('图片上传失败', error)
    message.error('图片上传失败' + error.message)
  }
  loading.value = false
}

const loading = ref<boolean>(false)
const imageUrl = ref<string>('')



</script>
<style scoped>
.url-picture_upload {
  margin-bottom: 16px;
  text-align: center;}

.url-picture_upload img {
  max-width: 100%;
  max-height: 480px;
}
.url-picture_upload .img-wrapper{
  text-align: center;
  margin-top: 16px;
}
</style>
