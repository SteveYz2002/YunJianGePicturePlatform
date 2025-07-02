<template>
  <div class="picture-list">
    <!-- 图片列表 -->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item : picture }">
        <a-list-item style="padding: 0">
          <!-- 单张图片 -->
          <a-card hoverable @click="doClickPicture(picture)">
            <template #cover>
              <div class="image-container">
                <img
                  style="height: 220px; object-fit: cover; width: 100%;"
                  :alt="picture.name"
                  :src="picture.thumbnailUrl ?? picture.url"
                />
                <div class="image-details">
                  <h4 class="ellipsis-text">{{ picture.name }}</h4>
                  <a-flex>
                    <a-tag color="green" bordered>
                      {{ picture.category ?? '默认' }}
                    </a-tag>
                    <a-tag v-for="tag in picture.tags" :key="tag" style="background-color: white">
                      {{ tag }}
                    </a-tag>
                  </a-flex>
                </div>
              </div>
            </template>
            <template #actions v-if="showOp">
                <div @click="e => doShare(picture, e)" style="display: flex; align-items: center; justify-content: center; cursor: pointer;">
                  <ShareAltOutlined />
                  <span style="margin-left: 4px;">{{ picture.shareCount || 0 }}</span>
                </div>
              <SearchOutlined @click="e => doSearch(picture, e)"/>
              <EditOutlined v-if="canEdit" @click="e => doEdit(picture, e)"/>
              <DeleteOutlined v-if="canDelete" @click="e => doDelete(picture, e)"/>
            </template>

          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <ShareModal ref="shareModalRef" :link="shareLink" @close="handleShareModalClose" />
  </div>
</template>

<script setup lang="ts">
import {EditOutlined, DeleteOutlined, SearchOutlined, ShareAltOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { deletePictureUsingPost, sharePictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import ShareModal from '@/components/ShareModal.vue'
import { ref } from 'vue'

interface Props{
  dataList?: API.PictureVO[]
  loading?: boolean
  showOp?: boolean
  onReload?: () => void
  canEdit?: boolean
  canDelete?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
  canEdit: false,
  canDelete: false,
})

const router = useRouter()
// 跳转至图片详情
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`
  })
}

const doSearch = (picture : any, e : any) => {
  // 阻止事件冒泡
  e.stopPropagation()
  // 打开新的页面
  window.open(`/search_picture?pictureId=${picture.id}`)
}

// 编辑
const doEdit = (picture : any, e : any) => {
  // 阻止事件冒泡
  e.stopPropagation()
  // 跳转时要携带spaceId
  router.push({
    path: '/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

// 删除
const doDelete = async (picture : any, e : any) => {
  // 阻止事件冒泡
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    props.onReload?.()
    // 让外层刷新
  } else {
    message.error('删除失败')
  }
}

//-----------------------分享操作---------------
// 分享弹窗引用
const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>()

// 分享
const doShare = async (picture: API.PictureVO, e: Event) => {
  // 阻止事件冒泡s
  e.stopPropagation()
  shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`

  // 调用分享API更新分享次数
  try {
    const res = await sharePictureUsingPost({ pictureId: picture.id })
    if (res.data.code === 0) {
      // 更新前端显示的分享次数
      picture.shareCount = res.data.data?.shareCount
    } else {
      message.error('分享次数更新失败')
    }
  } catch (error) {
    console.error('分享API调用失败:', error)
  }

  // 打开分享弹窗
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}

// 处理分享弹窗关闭事件
const handleShareModalClose = () => {
  // 刷新页面数据
  props.onReload?.()
}


</script>

<style scoped>
.ellipsis-text {
  /* 不换行 */
  white-space: nowrap;
  /* 溢出隐藏 */
  overflow: hidden;
  /* 溢出显示省略号 */
  text-overflow: ellipsis;
  /* 确保有宽度限制 */
  max-width: 100%;
  margin-bottom: 10px;
  font-size: 16px;
}

.picture-card :deep(.ant-card-body) {
  padding: 0;
}

.image-container {
  position: relative;
  overflow: hidden;
}

.image-details {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  padding: 10px;
}

.image-container:hover .image-details {
  opacity: 1;
}

.image-details h4 {
  margin-bottom: 10px;
  font-size: 16px;
}

.image-details .ant-tag {
  margin: 2px;
}
</style>
