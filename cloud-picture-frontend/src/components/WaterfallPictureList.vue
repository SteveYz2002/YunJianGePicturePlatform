<template>
  <div class="waterfall-container">
    <div v-if="loading" class="loading-container">
      <a-spin />
    </div>
    <div ref="waterfallRef">
      <div v-for="(picture, index) in dataList" :key="picture.id" class="waterfall-item">
        <div class="image-container" @click="doClickPicture(picture)">
          <img
            :src="picture.thumbnailUrl ?? picture.url"
            :alt="picture.name"
            class="waterfall-image"
            @load="onImageLoaded($event, index)"
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
        <div class="picture-actions" v-if="showOp">
          <div class="action-item" @click.stop="doLike(picture, $event)">
            <span v-if="picture.isLiked" style="color: #f5222d;">
              <svg width="18" height="18" viewBox="0 0 1024 1024" fill="currentColor"><path d="M923 283.6c-23.8-56.2-77.5-92.6-137.2-92.6-49.8 0-95.2 25.6-121.8 67.5-26.6-41.9-72-67.5-121.8-67.5-59.7 0-113.4 36.4-137.2 92.6-23.8 56.2-10.2 120.2 34.2 163.7l193.2 192.7c6.2 6.2 16.4 6.2 22.6 0l193.2-192.7c44.4-43.5 58-107.5 34.2-163.7z"></path></svg>
            </span>
            <span v-else style="color: #aaa;">
              <svg width="18" height="18" viewBox="0 0 1024 1024" fill="currentColor"><path d="M923 283.6c-23.8-56.2-77.5-92.6-137.2-92.6-49.8 0-95.2 25.6-121.8 67.5-26.6-41.9-72-67.5-121.8-67.5-59.7 0-113.4 36.4-137.2 92.6-23.8 56.2-10.2 120.2 34.2 163.7l193.2 192.7c6.2 6.2 16.4 6.2 22.6 0l193.2-192.7c44.4-43.5 58-107.5 34.2-163.7z" fill="none" stroke="currentColor" stroke-width="60"/></svg>
            </span>
            <span style="margin-left: 4px;">{{ picture.likeCount || 0 }}</span>
          </div>
          <div class="action-item" @click.stop="doShare(picture, $event)">
            <ShareAltOutlined />
            <span style="margin-left: 4px;">{{ picture.shareCount || 0 }}</span>
          </div>
          <div class="action-item" @click.stop="doSearch(picture, $event)">
            <SearchOutlined />
          </div>
          <div class="action-item" v-if="canEdit" @click.stop="doEdit(picture, $event)">
            <EditOutlined />
          </div>
          <div class="action-item" v-if="canDelete" @click.stop="doDelete(picture, $event)">
            <DeleteOutlined />
          </div>
        </div>
      </div>
    </div>
    <ShareModal ref="shareModalRef" title="分享图片" :link="shareLink" @close="handleShareModalClose" />
  </div>
</template>

<script setup lang="ts">
import { EditOutlined, DeleteOutlined, SearchOutlined, ShareAltOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { deletePictureUsingPost, sharePictureUsingPost, likePictureUsingPost, unlikePictureUsingPost } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import ShareModal from '@/components/ShareModal.vue'
import { ref, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'

interface Props {
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

// 瀑布流组件引用
const waterfallRef = ref<HTMLElement | null>(null)
// 已加载的图片数量
const loadedImagesCount = ref(0)

// 监听数据变化，重置加载计数器
watch(() => props.dataList, () => {
  loadedImagesCount.value = 0
  nextTick(() => {
    updateLayout()
  })
}, { deep: true })

// 图片加载完成时触发
const onImageLoaded = (e: Event, index: number) => {
  loadedImagesCount.value++
  
  // 设置最大高度，保持图片比例
  const img = e.target as HTMLImageElement
  const maxHeight = 360 // 设置最大高度
  
  if (img.naturalHeight > maxHeight) {
    img.style.height = `${maxHeight}px`
    img.style.objectFit = 'cover'
  }
  
  // 每加载一张图片就更新一次布局，确保高度实时调整
  updateLayout()
}

// 更新瀑布流布局
const updateLayout = () => {
  if (!waterfallRef.value) return
  
  const items = waterfallRef.value.querySelectorAll('.waterfall-item') as NodeListOf<HTMLElement>
  if (!items.length) return

  // 重置所有元素的定位，以便重新计算
  items.forEach((item) => {
    item.style.position = ''
    item.style.top = ''
    item.style.left = ''
  })

  // 根据不同屏幕宽度设置列数（减少列数以增大图片）
  const containerWidth = waterfallRef.value.offsetWidth
  let columns = 4 // 默认列数减少
  
  if (containerWidth < 576) columns = 1
  else if (containerWidth < 768) columns = 2
  else if (containerWidth < 992) columns = 3
  else if (containerWidth < 1200) columns = 3
  else columns = 4

  const columnWidth = containerWidth / columns
  const columnHeights = Array(columns).fill(0)
  const verticalGap = 10 // 垂直间距减少，使项目更紧凑
  
  // 计算每个元素的位置
  items.forEach((item) => {
    // 找到高度最小的列
    const minHeightIndex = columnHeights.indexOf(Math.min(...columnHeights))
    
    // 设置元素位置，减少水平间距以让图片更大
    item.style.width = `${columnWidth - 14}px`
    item.style.position = 'absolute'
    item.style.left = `${minHeightIndex * columnWidth + 7}px` // 居中于列中
    item.style.top = `${columnHeights[minHeightIndex]}px`
    
    // 更新该列的高度，确保没有空隙
    columnHeights[minHeightIndex] += item.offsetHeight + verticalGap
  })
  
  // 设置容器高度为最高的列，确保足够显示所有内容
  const maxColumnHeight = Math.max(...columnHeights)
  waterfallRef.value.style.height = maxColumnHeight > 0 
    ? `${maxColumnHeight}px` // 不减去间距，确保有足够空间
    : 'auto' // 使用auto而不是0px
}

// 窗口大小变化时重新计算布局
const handleResize = () => {
  updateLayout()
}

// 创建一个ResizeObserver来监视瀑布流容器大小变化，性能更好
const createResizeObserver = () => {
  if (!waterfallRef.value) return null
  
  const observer = new ResizeObserver(() => {
    updateLayout()
  })
  
  observer.observe(waterfallRef.value)
  
  return observer
}

let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  window.addEventListener('resize', handleResize)
  // 监听滚动事件，确保在滚动时布局正确
  window.addEventListener('scroll', () => {
    // 使用requestAnimationFrame优化性能
    window.requestAnimationFrame(() => {
      updateLayout()
    })
  })
  
  // 初始布局
  nextTick(() => {
    updateLayout()
    // 使用ResizeObserver替代MutationObserver
    resizeObserver = createResizeObserver()
    
    // 延迟再次更新布局，解决初始渲染可能不完整的问题
    setTimeout(() => {
      updateLayout()
    }, 500)
  })
  
  // 组件卸载时清理
  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    // 移除滚动监听，确保使用相同的回调函数
    window.removeEventListener('scroll', () => {
      window.requestAnimationFrame(() => {
        updateLayout()
      })
    })
    if (resizeObserver) {
      resizeObserver.disconnect()
    }
  })
})

// 跳转至图片详情
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`
  })
}

const doSearch = (picture: any, e: any) => {
  // 阻止事件冒泡
  e.stopPropagation()
  // 打开新的页面
  window.open(`/search_picture?pictureId=${picture.id}`)
}

// 编辑
const doEdit = (picture: any, e: any) => {
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
const doDelete = async (picture: any, e: any) => {
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
const shareLink = ref<string>('')

  // 分享
const doShare = async (picture: API.PictureVO, e: Event) => {
  // 阻止事件冒泡
  e.stopPropagation()
  if (picture.id) {
    shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
  }

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

const loginUserStore = useLoginUserStore()

const doLike = async (picture: API.PictureVO, e: Event) => {
  e.stopPropagation()
  if (!loginUserStore.loginUser || !loginUserStore.loginUser.id) {
    message.warning('请先登录后再点赞')
    return
  }
  try {
    if (picture.isLiked) {
      // 取消点赞
      const res = await unlikePictureUsingPost({
        pictureId: picture.id
      })
      if (res.data.code === 0) {
        picture.isLiked = false
        picture.likeCount = (picture.likeCount || 1) - 1
      } else {
        message.error(res.data.message || '取消点赞失败')
      }
    } else {
      // 点赞
      const res = await likePictureUsingPost({
        pictureId: picture.id
      })
      if (res.data.code === 0) {
        picture.isLiked = true
        picture.likeCount = (picture.likeCount || 0) + 1
      } else {
        message.error(res.data.message || '点赞失败')
      }
    }
  } catch (error) {
    message.error('操作失败')
  }
}
</script>

<style scoped>
.waterfall-container {
  position: relative;
  width: 100%;
  margin-bottom: 0; /* 确保没有底部间距 */
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 200px;
  animation: fadeIn 0.5s ease;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
  background: linear-gradient(135deg, rgba(37, 100, 235, 0.02), rgba(6, 182, 212, 0.03));
  border-radius: var(--border-radius-lg);
  min-height: 200px;
}

.waterfall-item {
  box-sizing: border-box;
  padding: 8px;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
  will-change: transform;
}

.waterfall-item:hover {
  transform: translateY(-10px);
  z-index: 5;
}

.image-container {
  position: relative;
  overflow: hidden;
  border-radius: var(--border-radius-lg);
  box-shadow: var(--shadow-sm);
  background-color: var(--bg-light);
  cursor: pointer;
  transition: box-shadow var(--transition-normal), transform var(--transition-normal);
  /* 保证图片底部没有空隙 */
  line-height: 0;
}

.waterfall-item:hover .image-container {
  box-shadow: var(--shadow-lg);
  transform: scale(1.01);
}

.waterfall-image {
  display: block;
  width: 100%;
  height: auto;
  max-height: 360px; /* 与脚本中保持一致 */
  transition: transform 0.7s var(--transition-normal);
  object-fit: cover;
}

.image-container:hover .waterfall-image {
  transform: scale(1.05);
}

.image-details {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  background: linear-gradient(to bottom, 
    rgba(0, 0, 0, 0.1), 
    rgba(0, 0, 0, 0.8)
  );
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity var(--transition-normal);
  padding: 15px;
  backdrop-filter: blur(3px);
  -webkit-backdrop-filter: blur(3px);
}

.image-container:hover .image-details {
  opacity: 1;
  animation: slideUp 0.3s ease forwards;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ellipsis-text {
  white-space: nowrap;
  text-overflow: ellipsis;
  max-width: 100%;
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
}

.picture-actions {
  display: flex;
  justify-content: space-around;
  padding: 12px;
  background: var(--bg-light);
  border-radius: 0 0 var(--border-radius-lg) var(--border-radius-lg);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  transform: translateY(0);
  transition: transform var(--transition-normal), background var(--transition-normal);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
}

.waterfall-item:hover .picture-actions {
  background: linear-gradient(to right, rgba(255, 255, 255, 0.97), rgba(240, 242, 245, 0.97));
}

.action-item {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: var(--border-radius-md);
  transition: all var(--transition-fast);
  color: var(--text-secondary);
}

.action-item:hover {
  background-color: rgba(37, 100, 235, 0.1);
  transform: translateY(-2px) scale(1.05);
  color: var(--primary-color);
}

.load-more-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 40px;
  padding: 20px 0;
  position: relative;
}

.load-more-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 1px;
  background: linear-gradient(90deg, 
      transparent, 
      var(--border-color), 
      transparent);
}

.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.loading-text {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 8px;
}

.no-more-indicator {
  font-size: 14px;
  color: var(--text-secondary);
  padding: 10px 20px;
  background: rgba(240, 242, 245, 0.6);
  border-radius: var(--border-radius-full);
}

.load-more-button-wrapper {
  transform: scale(1);
  transition: transform var(--transition-normal);
}

.load-more-button-wrapper:hover {
  transform: scale(1.05);
}
</style> 