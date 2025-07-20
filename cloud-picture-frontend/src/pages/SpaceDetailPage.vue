<template>
  <div id="spaceDetailPage" class="space-detail-page">
    <div class="page-header tech-card">
      <div class="header-content">
        <div class="space-info">
          <h2 class="space-title">
            <span class="title-text">{{ space.spaceName }}</span>
            <span class="space-type-badge">{{ SPACE_TYPE_MAP[space.spaceType] }}</span>
          </h2>
          <p class="space-description" v-if="space.description">{{ space.description }}</p>
        </div>
        <div class="space-actions">
          <a-space size="middle">
            <a-button 
              v-if="canUploadPicture" 
              type="primary" 
              :href="`/add_picture?spaceId=${id}`" 
              target="_blank"
              class="action-button create-button"
            >
              <template #icon><plus-outlined /></template>
              创建图片
            </a-button>
            <a-button
              v-if="canManageSpaceUser"
              type="primary"
              ghost
              :icon="h(TeamOutlined)"
              :href="`/spaceUserManage/${id}`"
              target="_blank"
              class="action-button member-button"
            >
              成员管理
            </a-button>

            <a-button
              v-if="canManageSpaceUser"
              class="action-button analyze-button"
              :icon="h(BarChartOutlined)"
              :href="`/space_analyze?spaceId=${id}`"
              target="_blank"
            >
              空间分析
            </a-button>
            <a-button
              :icon="h(EditOutlined)"
              @click="doBatchEdit"
              v-if="canEditPicture"
              class="action-button edit-button"
            >
              批量编辑
            </a-button>
            <div class="space-usage">
              <a-tooltip :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`">
                <a-progress
                  type="circle"
                  :percent="((space.totalSize*100)/space.maxSize).toFixed(1)"
                  :size="48"
                  :strokeColor="spaceUsageColor"
                  class="usage-progress"
                />
              </a-tooltip>
              <span class="usage-text">{{ ((space.totalSize*100)/space.maxSize).toFixed(1) }}%</span>
            </div>
          </a-space>
        </div>
      </div>
    </div>

    <div class="search-section tech-card">
      <!-- 搜索表单 -->
      <div class="search-form-wrapper">
        <h3 class="section-title">搜索筛选</h3>
        <PictureSearchForm :on-search="onSearch" class="search-form"/>
      </div>

      <!-- 按颜色搜索 -->
      <div class="color-search-wrapper">
        <h3 class="section-title">按颜色搜索</h3>
        <div class="color-picker-container">
          <color-picker format="hex" @pureColorChange="onColorChange" class="color-picker" />
          <span class="color-hint">选择颜色以查找相似图片</span>
        </div>
      </div>
    </div>

    <!-- 图片列表 -->
    <div class="pictures-section tech-card">
      <h3 class="section-title">图片列表</h3>
      <div class="picture-container">
        <PictureList
          class="picture-list"
          :dataList="dataList"
          :loading="loading"
          :showOp="true"
          :on-reload="fetchData"
          :canEdit="canEditPicture"
          :canDelete="canDeletePicture"
        />
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <a-pagination
          v-model:current="searchParams.current"
          v-model:pageSize="searchParams.pageSize"
          :total="total"
          @change="onPageChange"
          class="tech-pagination"
          show-size-changer
          :pageSizeOptions="['10', '20', '50', '100']"
        />
      </div>
    </div>

    <BatchEditPictureModal
      ref="batchEditPictureModalRef"
      :spaceId="id"
      :picture-list="dataList"
      :on-success="onBatchEditPictureSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, h, computed } from 'vue'
import {
  getSpaceVoByIdUsingGet
} from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { listPictureVoByPageUsingPost, searchPictureByColorUsingPost } from '@/api/pictureController.ts'
import { formatSize } from '@/utils'
import PictureList from '@/components/PictureList.vue'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
import { ColorPicker } from 'vue3-colorpicker'
import "vue3-colorpicker/style.css"
import BatchEditPictureModal from '@/components/BatchEditPictureModal.vue'
import { 
  EditOutlined, 
  BarChartOutlined, 
  TeamOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import { SPACE_PERMISSION_ENUM, SPACE_TYPE_MAP } from '@/constants/space.ts'
interface Props {
  id: string | number
}

const props = defineProps<Props>()

const space = ref<API.SpaceVO>({})

// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

// 空间使用进度条颜色
const spaceUsageColor = computed(() => {
  const percent = ((space.value.totalSize || 0) * 100) / (space.value.maxSize || 1);
  if (percent < 60) return { '0%': '#52c41a', '100%': '#1677ff' };
  if (percent < 80) return { '0%': '#faad14', '100%': '#ff7a45' };
  return { '0%': '#ff4d4f', '100%': '#cf1322' };
});

//----------------- 获取空间详情-------------------------
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('获取空间详情失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error('获取空间详情失败，' + error.message)
  }
}

const router = useRouter()

onMounted(() => {
  fetchSpaceDetail()
})

// -------------------------获取空间列表-----------------------

// 表格数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend'
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams.value,
    tags: [] as string[]
  }
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败' + res.data.message)
  }
  loading.value = false
}

onMounted(() => {
  fetchData()
})

const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

const onSearch = (newSearchParams : API.PictureQueryRequest) => {
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1
  }
  fetchData()
}

// 按颜色搜索
const onColorChange = async (color: string) => {
  loading.value=true
  const res = await searchPictureByColorUsingPost({
    picColor: color,
    spaceId: props.id
  })
  if(res.data.code === 0 && res.data.data){
    const data = res.data.data ?? []
    dataList.value = data
    total.value = data.length
  }else{
    message.error('获取数据失败'+ res.data.message)
  }
  loading.value=false
}

// -----------------------批量编辑图片-----------------------
const batchEditPictureModalRef = ref()

// 批量编辑图片成功
const onBatchEditPictureSuccess = () => {
  fetchData()
}

// 打开编辑弹窗
const doBatchEdit = () => {
  if(batchEditPictureModalRef.value){
    batchEditPictureModalRef.value.openModal()
  }
}

// 空间id改变时，重新获取数据
watch(
  () => props.id,
  (newSpaceId) => {
    fetchSpaceDetail()
    fetchData()
  },
)

</script>

<style scoped>
.space-detail-page {
  padding: 20px;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Tech-inspired cards */
.tech-card {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  padding: 24px;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(232, 232, 232, 0.8);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.tech-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #1677ff, #52c41a, #722ed1);
  opacity: 0.8;
}

.tech-card:hover {
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.12);
  transform: translateY(-3px);
}

/* Page header styles */
.page-header {
  position: relative;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 20px;
}

.space-info {
  flex: 1;
  min-width: 300px;
}

.space-title {
  display: flex;
  align-items: center;
  margin: 0 0 12px 0;
  font-size: 28px;
  font-weight: 600;
  background: linear-gradient(90deg, #1677ff, #52c41a);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

.space-type-badge {
  margin-left: 12px;
  font-size: 14px;
  background: linear-gradient(45deg, #1677ff, #52c41a);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: normal;
}

.space-description {
  color: #666;
  margin: 8px 0;
  font-size: 14px;
  max-width: 600px;
}

.space-actions {
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
}

/* Button styles */
.action-button {
  border-radius: 8px;
  height: 36px;
  padding: 0 16px;
  font-weight: 500;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.action-button:hover {
  transform: translateY(-2px);
}

.create-button {
  background: linear-gradient(45deg, #1677ff, #4096ff);
  border: none;
}

.create-button:hover {
  background: linear-gradient(45deg, #275fb9, #1677ff);
}

.member-button {
  border-color: #1677ff;
  color: white;
}

.analyze-button {
  background: linear-gradient(45deg, #389e0d, #52c41a);
  border: none;
  color: white;
}

.analyze-button:hover {
  background: linear-gradient(45deg, #237804, #389e0d);
}

.edit-button {
  background: white;
  border-color: #d9d9d9;
}

/* Space usage styles */
.space-usage {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-left: 10px;
}

.usage-progress {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.usage-text {
  margin-top: 4px;
  font-size: 12px;
  color: #666;
}

/* Search section styles */
.search-section {
  display: flex;
  flex-wrap: wrap;
  gap: 32px;
  align-items: flex-start;
}

.section-title {
  font-size: 18px;
  margin-top: 0;
  margin-bottom: 16px;
  color: #333;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 18px;
  width: 4px;
  background: linear-gradient(180deg, #1677ff, #52c41a);
  border-radius: 2px;
}

.search-form-wrapper {
  flex: 2;
  min-width: 300px;
}

.color-search-wrapper {
  flex: 1;
  min-width: 200px;
}

.color-picker-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.color-picker {
  width: 100%;
  max-width: 300px;
}

.color-hint {
  font-size: 12px;
  color: #888;
}

/* Pictures section */
.picture-container {
  margin: 16px 0;
  min-height: 300px;
}

.picture-list {
  border-radius: 8px;
  overflow: hidden;
}

/* Pagination styles */
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.tech-pagination {
  --ant-primary-color: #1677ff;
}

/* Responsive design */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
  }
  
  .space-actions {
    justify-content: flex-start;
    margin-top: 16px;
  }
  
  .search-section {
    flex-direction: column;
  }
  
  .tech-card {
    padding: 16px;
  }
}
</style>
