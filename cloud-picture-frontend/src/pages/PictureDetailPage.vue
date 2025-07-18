<template>
  <div id="pictureDetailPage" class="picture-detail-page">
    <div class="background-blur" :style="{ backgroundImage: `url(${picture.url})` }"></div>
    <div class="content-wrapper">
      <!-- 图片主体展示区 -->
      <div class="image-showcase">
        <div class="image-container">
          <img
            class="main-image"
            :src="picture.url"
            @click.stop="showPreview"
          />
          
          <!-- 图片信息层 -->
          <div class="image-meta">
            <h1 class="image-title">{{ picture.name || '未命名作品' }}</h1>
            <p class="image-introduction">{{ picture.introduction || '暂无简介' }}</p>
            
            <div class="author-info">
              <a-avatar :size="40" :src="picture.user?.userAvatar" />
              <div class="author-details">
                <div class="author-name">{{ picture.user?.userName || '未知作者' }}</div>
                <div class="creation-time">{{ formatDate(picture.createTime) }}</div>
              </div>
            </div>
          </div>
          
          <!-- 互动操作浮层 -->
          <div class="floating-actions">
            <div class="stat-item">
              <LikeOutlined class="stat-icon" :class="{ 'active': picture.isLiked }" />
              <span>{{ picture.likeCount || 0 }}</span>
            </div>
            <div class="stat-item" @click="doShare">
              <ShareAltOutlined class="stat-icon" />
              <span>{{ picture.shareCount || 0 }}</span>
            </div>
            <div class="stat-item" @click="doDownload">
              <DownloadOutlined class="stat-icon"  />
              <span>{{ picture.downloadCount || 0 }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 详细信息和操作区 -->
      <div class="details-section">
        <div class="details-container">
          <div class="tags-container">
            <a-tag class="category-tag">
              {{ picture.category || '默认' }}
            </a-tag>
            <a-tag v-for="tag in picture.tags" :key="tag" class="picture-tag">
              {{ tag }}
            </a-tag>
          </div>
          
          <a-divider class="styled-divider" />
          
          <div class="tech-specs">
            <h3 class="section-title">图片规格</h3>
            <div class="specs-grid">
              <div class="spec-item">
                <div class="spec-label">格式</div>
                <div class="spec-value">{{ picture.picFormat || '-' }}</div>
              </div>
              <div class="spec-item">
                <div class="spec-label">尺寸</div>
                <div class="spec-value">{{ picture.picWidth || 0 }} × {{ picture.picHeight || 0 }}</div>
              </div>
              <div class="spec-item">
                <div class="spec-label">比例</div>
                <div class="spec-value">{{ picture.picScale || '-' }}</div>
              </div>
              <div class="spec-item">
                <div class="spec-label">大小</div>
                <div class="spec-value">{{ formatSize(picture.picSize) }}</div>
              </div>
              <div class="spec-item">
                <div class="spec-label">主色调</div>
                <div class="spec-value color-display">
                  <span>{{ picture.picColor || '-' }}</span>
                  <div 
                    class="color-box"
                    v-if="picture.picColor"
                    :style="{ backgroundColor: toHexColor(picture.picColor) }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
          
          <a-divider class="styled-divider" />
          
          <!-- 操作按钮 -->
          <div class="actions-container">
            <a-button type="primary" class="action-button download-button" @click.stop="doDownload">
              <DownloadOutlined />
              下载图片
            </a-button>
            
            <div class="secondary-actions">
              <a-button v-if="canEdit" class="action-button edit-button" @click="doEdit">
                <EditOutlined />
                编辑
              </a-button>
              <a-button class="action-button share-button" @click="doShare">
                <ShareAltOutlined />
                分享
              </a-button>
              <a-button v-if="canDelete" danger class="action-button delete-button" @click="doDelete">
                <DeleteOutlined />
                删除
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <ShareModal ref="shareModalRef" title="分享图片" :link="shareLink" @close="handleShareModalClose" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import {
  deletePictureUsingPost, downloadPictureUsingPost,
  getPictureVoByIdUsingGet,
  sharePictureUsingPost
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { downloadImage, formatSize } from '@/utils'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useRouter } from 'vue-router'
import { 
  DeleteOutlined, EditOutlined, ShareAltOutlined, 
  DownloadOutlined, EyeOutlined, LikeOutlined
} from '@ant-design/icons-vue'
import { toHexColor } from '@/utils'
import ShareModal from '@/components/ShareModal.vue'
import { SPACE_PERMISSION_ENUM } from '@/constants/space.ts'

interface Props {
  id: string | number
}

const loginUserStore = useLoginUserStore()

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (picture.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canEdit = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDelete = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

const props = defineProps<Props>()

const picture = ref<API.PictureVO>({})

// 格式化日期
const formatDate = (date: Date | string | undefined) => {
  if (!date) return '未知时间';
  const d = new Date(date);
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: props.id
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('获取图片详情失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error('获取图片详情失败，' + error.message)
  }
}

const router = useRouter()

// 编辑
const doEdit = () => {
  router.push({
    path: '/add_picture',
    query: {
      id: picture.value.id,
      spaceId: picture.value.spaceId
    }
  })
}

// 删除
const doDelete = async () => {
  const id = picture.value.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    await router.push('/')
  } else {
    message.error('删除失败')
  }
}

onMounted(() => {
  fetchPictureDetail()
  
  // 页面加载动画效果
  document.querySelector('.picture-detail-page')?.classList.add('loaded')
})

// 处理下载
const doDownload = async (e) => {
  // 阻止事件冒泡
  if (e) {
    e.stopPropagation()
  }
  
  if (!picture.value.id) {
    message.error('图片ID不存在')
    return
  }

  try {
    // 先调用下载API更新下载次数
    const res = await downloadPictureUsingPost({
      pictureId: picture.value.id
    })

    if (res.data.code === 0) {
      // 执行实际下载
      downloadImage(picture.value.url)
      message.success('下载成功')

      // 更新下载计数
      if (res.data.data) {
        picture.value.downloadCount = res.data.data.downloadCount || 0
      }
    } else {
      message.error('下载失败，' + res.data.message)
    }
  } catch (error: any) {
    console.error('下载失败:', error)
    message.error('下载失败，' + error.message)
  }
}

//-----------------------分享操作---------------
// 分享弹窗引用
const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>('')

// 分享
const doShare = async (e) => {
  // 阻止事件冒泡
  if (e) {
    e.stopPropagation()
  }
  
  // 调用分享API
  try {
    const res = await sharePictureUsingPost({
      pictureId: picture.value.id
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value.shareCount = res.data.data.shareCount || 0
      // 更新分享链接
      shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.value.id}`
      // 打开分享弹窗
      if (shareModalRef.value) {
        shareModalRef.value.openModal()
      }
    } else {
      message.error('分享失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error('分享失败，' + error.message)
  }
}

// 处理分享弹窗关闭
const handleShareModalClose = () => {
  // 刷新页面数据
  fetchPictureDetail()
}

// 图片预览
const showPreview = (e) => {
  // 阻止事件冒泡和默认行为
  e.stopPropagation()
  e.preventDefault()
  
  // 使用内置的方法或库来预览图片，而不是下载
  const url = picture.value.url
  if (url) {
    // 创建一个模态框来显示图片，避免直接打开新窗口
    const modal = document.createElement('div')
    modal.style.position = 'fixed'
    modal.style.top = '0'
    modal.style.left = '0'
    modal.style.right = '0'
    modal.style.bottom = '0'
    modal.style.backgroundColor = 'rgba(0,0,0,0.9)'
    modal.style.display = 'flex'
    modal.style.justifyContent = 'center'
    modal.style.alignItems = 'center'
    modal.style.zIndex = '9999'
    modal.style.cursor = 'pointer'
    
    const img = document.createElement('img')
    img.src = url
    img.style.maxWidth = '90%'
    img.style.maxHeight = '90%'
    img.style.objectFit = 'contain'
    
    modal.appendChild(img)
    document.body.appendChild(modal)
    
    // 点击模态框时关闭
    modal.onclick = () => {
      document.body.removeChild(modal)
    }
  }
}
</script>

<style scoped>
.picture-detail-page {
  position: relative;
  min-height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.picture-detail-page.loaded {
  opacity: 1;
  transform: translateY(0);
}

.background-blur {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
  filter: blur(50px);
  opacity: 0.1;
  z-index: -1;
  transform: scale(1.2);
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 100%;
  margin: 0;
  padding: 0;
  width: 100%;
  box-sizing: border-box;
  animation: fadeIn 1s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 图片展示区 */
.image-showcase {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
  max-width: none;
}

.image-container {
  position: relative;
  max-width: 100%;
  width: 100%;
  box-shadow: none;
  border-radius: 0;
  overflow: hidden;
  background-color: #f5f5f5;
  transition: transform 0.3s ease;
}

.image-container:hover {
  transform: translateY(-5px);
}

.main-image {
  width: 100%;
  display: block;
  object-fit: contain;
  height: 75vh;
  background-color: #f5f5f5;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.main-image:hover {
  transform: scale(1.02);
}

/* 图片信息层 */
.image-meta {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), rgba(0, 0, 0, 0));
  color: white;
  z-index: 5;
  transition: opacity 0.3s ease;
}

.image-container:hover .image-meta {
  opacity: 1;
}

.image-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.5);
}

.image-introduction {
  font-size: 16px;
  margin-bottom: 20px;
  opacity: 0.9;
  max-width: 80%;
  line-height: 1.5;
}

.author-info {
  display: flex;
  align-items: center;
}

.author-details {
  margin-left: 12px;
}

.author-name {
  font-weight: 600;
  font-size: 16px;
}

.creation-time {
  font-size: 13px;
  opacity: 0.7;
}

/* 互动操作浮层 */
.floating-actions {
  position: absolute;
  right: 20px;
  top: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  z-index: 5;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 50%;
  width: 50px;
  height: 50px;
  justify-content: center;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
  cursor: pointer;
}

.stat-item:hover {
  transform: scale(1.1);
  background-color: rgba(255, 255, 255, 0.95);
}

.stat-icon {
  font-size: 20px;
  color: #555;
}

.stat-icon.active {
  color: #ff4d4f;
}

.stat-item span {
  font-size: 12px;
  color: #555;
  margin-top: 2px;
}

/* 详细信息区 */
.details-section {
  width: 100%;
  padding: 0 20px 20px;
}

.details-container {
  background-color: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  animation: slideUp 0.8s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.category-tag {
  background: linear-gradient(45deg, #1890ff, #096dd9);
  color: white;
  border: none;
  font-weight: 500;
}

.picture-tag {
  border-radius: 4px;
  padding: 0 12px;
  transition: all 0.2s;
}

.picture-tag:hover {
  transform: translateY(-3px);
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
}

.styled-divider {
  margin: 24px 0;
  background: linear-gradient(to right, rgba(24, 144, 255, 0.3), rgba(24, 144, 255, 0));
  height: 1px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 4px;
  background: linear-gradient(to bottom, #1890ff, #096dd9);
  border-radius: 2px;
}

.specs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.spec-item {
  border-radius: 8px;
  padding: 12px;
  background-color: #f8f8f8;
  transition: all 0.2s;
}

.spec-item:hover {
  background-color: #f0f0f0;
  transform: translateY(-3px);
}

.spec-label {
  color: #888;
  font-size: 13px;
  margin-bottom: 4px;
}

.spec-value {
  color: #333;
  font-weight: 500;
  font-size: 15px;
}

.color-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-box {
  width: 18px;
  height: 18px;
  border-radius: 4px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

/* 操作按钮区 */
.actions-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 20px;
}

.action-button {
  border-radius: 6px;
  height: 44px;
  font-weight: 500;
  transition: all 0.3s;
}

.download-button {
  width: 100%;
  font-size: 16px;
  background: linear-gradient(45deg, #1890ff, #096dd9);
  border: none;
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.download-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(24, 144, 255, 0.4);
}

.secondary-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

/* 预览蒙层样式 */
:deep(.preview-mask) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: white;
}

:deep(.preview-mask .anticon) {
  font-size: 24px;
  margin-bottom: 8px;
}

/* 响应式调整 */
@media (min-width: 768px) {
  .content-wrapper {
    padding: 30px;
  }
}

@media (max-width: 767px) {
  .image-meta {
    padding: 20px;
  }
  
  .image-title {
    font-size: 22px;
  }
  
  .details-container {
    padding: 20px;
  }
  
  .specs-grid {
    grid-template-columns: 1fr 1fr;
  }
  
  .secondary-actions {
    flex-wrap: wrap;
  }
  
  .floating-actions {
    flex-direction: row;
    top: auto;
    bottom: 70px;
    right: 10px;
  }
  
  .stat-item {
    width: 44px;
    height: 44px;
  }
}
</style>
