<template>
  <div id="pictureManagePage" class="admin-page">
    <div class="page-header glass-effect">
      <div class="page-title">
        <h2 class="gradient-text">图片管理</h2>
        <div class="title-underline"></div>
      </div>
      <a-space class="action-buttons">
        <a-button type="primary" href="/add_picture" target="_blank">
          <template #icon><PlusOutlined /></template>
          创建图片
        </a-button>
        <a-button type="primary" href="/add_picture/batch" target="_blank" class="secondary-button">
          <template #icon><FileAddOutlined /></template>
          批量创建图片
        </a-button>
      </a-space>
    </div>
    
    <!-- 搜索表单 -->
    <div class="search-panel glass-effect">
      <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
        <a-form-item label="关键词" name="searchText">
          <a-input
            v-model:value="searchParams.searchText"
            placeholder="从名称和简介搜索"
            allow-clear
            class="custom-input"
          />
        </a-form-item>
        <a-form-item label="类型" name="category">
          <a-input 
            v-model:value="searchParams.category" 
            placeholder="请输入类型" 
            allow-clear 
            class="custom-input"
          />
        </a-form-item>
        <a-form-item label="标签" name="tags">
          <a-select
            v-model:value="searchParams.tags"
            mode="tags"
            :options="tagOptions"
            placeholder="请输入标签"
            style="min-width: 180px"
            allow-clear
            class="custom-select"
          />
        </a-form-item>
        <a-form-item label="审核状态" name="reviewStatus">
          <a-select
            v-model:value="searchParams.reviewStatus"
            :options="PIC_REVIEW_STATUS_OPTIONS"
            placeholder="请选择审核状态"
            style="min-width: 180px"
            allow-clear
            class="custom-select"
          />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" class="search-button">
            <template #icon><SearchOutlined /></template>
            搜索
          </a-button>
        </a-form-item>
      </a-form>
    </div>
    
    <!-- 表格 -->
    <div class="table-container glass-effect">
      <a-table
        :columns="columns"
        :data-source="dataList"
        :pagination="pagination"
        @change="doTableChange"
        class="custom-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'id'" >
            <div @click="JumpTo(record.id)" class="id-link">
              {{ record.id }}
            </div>
          </template>
          <template v-if="column.dataIndex === 'url'">
            <div class="image-preview">
              <a-image :src="record.url" :width="100" class="preview-image"/>
            </div>
          </template>
          <template v-if="column.dataIndex === 'category'">
            <a-tag class="category-tag">{{ record.category }}</a-tag>
          </template>
          <template v-if="column.dataIndex === 'tags'">
            <a-space wrap>
              <a-tag 
                v-for="tag in JSON.parse(record.tags || '[]')" 
                :key="tag"
                class="custom-tag"
              >
                {{ tag }}
              </a-tag>
            </a-space>
          </template>
          <template v-if="column.dataIndex === 'picInfo'">
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">格式:</span>
                <span class="info-value">{{ record.picFormat }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">宽度:</span>
                <span class="info-value">{{ record.picWidth }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">高度:</span>
                <span class="info-value">{{ record.picHeight }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">宽高比:</span>
                <span class="info-value">{{ record.picScale }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">大小:</span>
                <span class="info-value">{{ (record.picSize / 1024).toFixed(2) }}KB</span>
              </div>
            </div>
          </template>
          <template v-if="column.dataIndex === 'reviewMessage'">
            <div class="review-status">
              <div class="status-badge" :class="'status-' + record.reviewStatus">
                {{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}
              </div>
              <div class="review-details">
                <div class="review-item">
                  <span class="review-label">审核信息:</span> 
                  <span>{{ record.reviewMessage || '无' }}</span>
                </div>
                <div class="review-item">
                  <span class="review-label">审核人:</span> 
                  <span>{{ record.reviewerId || '无' }}</span>
                </div>
                <div class="review-item" v-if="record.reviewTime">
                  <span class="review-label">审核时间:</span> 
                  <span>{{ dayjs(record.reviewTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
                </div>
              </div>
            </div>
          </template>
          <template v-if="column.dataIndex === 'createTime'">
            <div class="time-display">
              {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
            </div>
          </template>
          <template v-if="column.dataIndex === 'editTime'">
            <div class="time-display">
              {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
            </div>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="action-group">
              <a-space wrap>
                <a-popconfirm
                  title="确定要审核通过该图片吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleReview(record, PIC_REVIEW_STATUS_ENUM.PASS)"
                  @cancel="cancel"
                  class="popconfirm"
                >
                  <a-button
                    v-if="record.reviewStatus!=PIC_REVIEW_STATUS_ENUM.PASS"
                    type="primary"
                    size="middle"
                    class="action-button pass-button"
                  >
                    <template #icon><CheckOutlined /></template>
                    通过
                  </a-button>
                </a-popconfirm>
                <a-popconfirm
                  title="确定要审核拒绝该图片吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleReview(record, PIC_REVIEW_STATUS_ENUM.REJECT)"
                  @cancel="cancel"
                  class="popconfirm"
                >
                  <a-button
                    v-if="record.reviewStatus!=PIC_REVIEW_STATUS_ENUM.REJECT"
                    type="primary"
                    danger
                    size="middle"
                    class="action-button reject-button"
                  >
                    <template #icon><CloseOutlined /></template>
                    拒绝
                  </a-button>
                </a-popconfirm>

                <a-button 
                  type="primary" 
                  :href="`/add_picture?id=${record.id}`" 
                  target="_blank"
                  class="action-button edit-button"
                >
                  <template #icon><EditOutlined /></template>
                  编辑
                </a-button>
                <a-popconfirm
                  title="确定要删除该图片吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleDelete(record.id)"
                  @cancel="cancel"
                  class="popconfirm"
                >
                  <a-button danger class="action-button delete-button">
                    <template #icon><DeleteOutlined /></template>
                    删除
                  </a-button>
                </a-popconfirm>
              </a-space>
            </div>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>
<script lang="ts" setup>

import { computed, onMounted, provide, reactive, ref, type UnwrapRef } from 'vue'
import {
  deletePictureUsingPost, doPictureReviewUsingPost,
  listPictureByPageUsingPost, listPictureTagCategoryUsingGet,
  updatePictureUsingPost
} from '@/api/pictureController.ts'
import { message, Tooltip } from 'ant-design-vue'
import dayjs from 'dayjs'
import { PIC_REVIEW_STATUS_ENUM, PIC_REVIEW_STATUS_MAP, PIC_REVIEW_STATUS_OPTIONS } from '@/constants/picture.ts'
import { 
  SearchOutlined, 
  PlusOutlined, 
  FileAddOutlined, 
  EditOutlined,
  DeleteOutlined,
  CheckOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80
  },
  {
    title: '图片',
    dataIndex: 'url'
  },
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true, // 开启省略显示
  },
  {
    title: '简介',
    dataIndex: 'introduction',
    ellipsis: true
  },
  {
    title: '类型',
    dataIndex: 'category',
    width: 100
  },
  {
    title: '标签',
    dataIndex: 'tags'
  },
  {
    title: '图片信息',
    dataIndex: 'picInfo'
  },
  {
    title: '用户id',
    dataIndex: 'userId',
    width: 80
  },
  {
    title: '空间id',
    dataIndex: 'spaceId',
    width: 80
  },
  {
    title: '审核信息',
    dataIndex: 'reviewMessage'
  },
  {
    title: '创建时间',
    dataIndex: 'createTime'
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime'
  },
  {
    title: '操作',
    key: 'action'
  }
]

// 表格数据
const dataList = ref<API.Picture[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend'
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current,
    pageSize: searchParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number) => `共 ${total} 条`
  }
})

const JumpTo = (id: number) => {
  window.open(`/picture/${id}`, '_blank')
}

// 表格变化之后，重新获取数据
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 获取数据
const fetchData = async () => {
  const res = await listPictureByPageUsingPost({
    ...searchParams,
    nullSpaceId:true
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败' + res.data.message)
  }
}

// 搜索
const doSearch = () => {
  // 重置分页参数
  searchParams.current = 1
  fetchData()
}

const handleDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    await fetchData()
  } else {
    message.error('删除失败')
  }
}

const tagOptions = ref<string[]>([])

// 页面加载时获取数据
onMounted(() => {
  fetchData()
})

// 审核数据
const handleReview = async (record: API.Picture, reviewStatus: number) => {
  const reviewMessage = reviewStatus === PIC_REVIEW_STATUS_ENUM.PASS ? '管理员审核通过' : '管理员审核拒绝'
  const res = await doPictureReviewUsingPost({
    id: record.id,
    reviewStatus,
    reviewMessage,
  })
  if(res.data.code === 0){
    message.success('审核操作成功')
    // 重新获取数据
    await fetchData()
  }else{
    message.error('审核操作失败'+res.data.message)
  }
}

const cancel = () => {
  message.info('已取消')
}

</script>

<style scoped>
.admin-page {
  padding: 0 0 40px;
  animation: fadeIn 0.5s ease;
}

.page-header {
  padding: 20px 24px;
  border-radius: var(--border-radius-lg);
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-sm);
}

.page-title {
  position: relative;
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

.action-buttons {
  gap: 12px;
}

.action-buttons .ant-btn {
  border-radius: var(--border-radius-md);
  height: 40px;
  padding: 0 16px;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-buttons .ant-btn .anticon {
  margin-right: 8px;
}

.action-buttons .ant-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.secondary-button {
  background: var(--accent-gradient, linear-gradient(135deg, #06b6d4, #0891b2)) !important;
  border: none !important;
}

.search-panel {
  padding: 20px;
  border-radius: var(--border-radius-lg);
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  width: 100%;
}

.custom-input,
.custom-select {
  min-width: 180px;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  transition: all var(--transition-fast);
}

.custom-input:hover,
.custom-select:hover {
  border-color: var(--primary-light);
  box-shadow: 0 0 0 2px rgba(37, 100, 235, 0.1);
}

.search-button {
  height: 32px;
  border-radius: var(--border-radius-md);
  transition: all var(--transition-normal);
}

.search-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.table-container {
  border-radius: var(--border-radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
}

.custom-table {
  width: 100%;
  overflow: hidden;
}

/* 表格样式 */
:deep(.ant-table) {
  background-color: transparent !important;
  border-radius: var(--border-radius-lg);
}

:deep(.ant-table-thead > tr > th) {
  background-color: rgba(37, 100, 235, 0.03) !important;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
}

:deep(.ant-table-tbody > tr > td) {
  background-color: transparent;
  border-bottom: 1px solid var(--border-color);
  transition: background-color var(--transition-fast);
}

:deep(.ant-table-tbody > tr:hover > td) {
  background-color: rgba(37, 100, 235, 0.04) !important;
}

:deep(.ant-pagination) {
  margin-top: 16px;
}

:deep(.ant-pagination-item-active) {
  border-color: var(--primary-color);
  background: var(--primary-color);
}

:deep(.ant-pagination-item-active a) {
  color: white;
}

/* 特定元素样式 */
.id-link {
  color: var(--primary-color);
  cursor: pointer;
  text-decoration: underline;
  font-weight: 500;
  transition: color var(--transition-fast);
}

.id-link:hover {
  color: var(--primary-dark);
}

.image-preview {
  overflow: hidden;
  border-radius: var(--border-radius-md);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  display: inline-block;
}

.image-preview:hover {
  transform: scale(1.05);
  box-shadow: var(--shadow-md);
}

.preview-image {
  object-fit: cover;
  border-radius: var(--border-radius-md);
}

.category-tag {
  background-color: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
  color: var(--success-color);
  font-weight: 500;
  border-radius: var(--border-radius-md);
  padding: 2px 8px;
}

.custom-tag {
  background-color: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.2);
  color: var(--primary-color);
  border-radius: var(--border-radius-md);
  padding: 1px 8px;
  margin: 2px;
  transition: all var(--transition-fast);
}

.custom-tag:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-weight: 500;
  color: var(--text-secondary);
  margin-right: 4px;
  min-width: 60px;
}

.info-value {
  color: var(--text-primary);
}

.review-status {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 500;
  border-radius: var(--border-radius-full);
  text-align: center;
  width: fit-content;
}

.status-0 {
  background-color: rgba(245, 158, 11, 0.1);
  color: var(--warning-color);
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.status-1 {
  background-color: rgba(16, 185, 129, 0.1);
  color: var(--success-color);
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.status-2 {
  background-color: rgba(239, 68, 68, 0.1);
  color: var(--danger-color);
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.review-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.review-item {
  font-size: 13px;
}

.review-label {
  color: var(--text-secondary);
  margin-right: 4px;
}

.time-display {
  color: var(--text-secondary);
  font-size: 13px;
}

.action-group {
  display: flex;
  justify-content: flex-start;
}

.action-button {
  border-radius: var(--border-radius-md) !important;
  transition: all var(--transition-fast) !important;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.edit-button {
  background-color: var(--primary-color);
}

.pass-button {
  background-color: var(--success-color);
  border-color: var(--success-color);
}

.reject-button {
  background-color: var(--danger-color);
  border-color: var(--danger-color);
}

:deep(.popconfirm .ant-popover-content) {
  border-radius: var(--border-radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .search-form {
    gap: 8px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .action-group {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
