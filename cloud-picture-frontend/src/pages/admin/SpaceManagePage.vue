<template>
  <div id="spaceManagePage" class="admin-page">
    <div class="page-header glass-effect">
      <div class="page-title">
        <h2 class="gradient-text">空间管理</h2>
        <div class="title-underline"></div>
      </div>
      <a-space class="action-buttons">
        <a-button type="primary" href="/add_space" target="_blank">
          <template #icon><PlusOutlined /></template>
          创建空间
        </a-button>
        <a-button 
          type="primary" 
          href="/space_analyze?queryPublic=1" 
          target="_blank" 
          class="danger-button"
        >
          <template #icon><LineChartOutlined /></template>
          分析公共图库
        </a-button>
        <a-button 
          type="primary" 
          href="/space_analyze?queryAll=1" 
          target="_blank" 
          class="success-button"
        >
          <template #icon><BarChartOutlined /></template>
          分析全部空间
        </a-button>
      </a-space>
    </div>
    
    <!-- 搜索表单 -->
    <div class="search-panel glass-effect">
      <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
        <a-form-item label="空间名称" name="searchText">
          <a-input
            v-model:value="searchParams.spaceName"
            placeholder="请输入空间名称"
            allow-clear
            class="custom-input"
          />
        </a-form-item>
        <a-form-item label="空间级别" name="spaceLevel">
          <a-select
            v-model:value="searchParams.spaceLevel"
            :options="SPACE_LEVEL_OPTIONS"
            placeholder="请选择空间级别"
            style="min-width: 180px"
            allow-clear
            class="custom-select"
          />
        </a-form-item>
        <a-form-item label="空间类别" name="spaceType">
          <a-select
            v-model:value="searchParams.spaceType"
            :options="SPACE_TYPE_OPTIONS"
            placeholder="请选择空间类别"
            style="min-width: 180px"
            allow-clear
            class="custom-select"
          />
        </a-form-item>
        <a-form-item label="用户 id" name="userId">
          <a-input 
            v-model:value="searchParams.userId" 
            placeholder="请输入用户 id" 
            allow-clear 
            class="custom-input"
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
          <template v-if="column.dataIndex === 'id'">
            <span class="space-id">{{ record.id }}</span>
          </template>
          <template v-if="column.dataIndex === 'spaceName'">
            <div class="space-name">{{ record.spaceName }}</div>
          </template>
          <template v-if="column.dataIndex === 'spaceLevel'">
            <div class="level-badge" :class="'level-' + record.spaceLevel">
              {{ SPACE_LEVEL_MAP[record.spaceLevel] }}
            </div>
          </template>
          <template v-if="column.dataIndex === 'spaceType'">
            <div class="type-badge" :class="'type-' + record.spaceType">
              {{ SPACE_TYPE_MAP[record.spaceType] }}
            </div>
          </template>
          <template v-if="column.dataIndex === 'spaceUseInfo'">
            <div class="usage-container">
              <div class="usage-info">
                <span class="usage-label">存储空间:</span>
                <div class="usage-bar-container">
                  <div 
                    class="usage-bar" 
                    :style="{
                      width: `${Math.min(100, (record.totalSize / record.maxSize) * 100)}%`,
                      backgroundColor: getSpaceUsageColor(record.totalSize / record.maxSize)
                    }"
                  ></div>
                </div>
                <span class="usage-text">{{ formatSize(record.totalSize) }} / {{ formatSize(record.maxSize) }}</span>
              </div>
              <div class="usage-info">
                <span class="usage-label">文件数量:</span>
                <div class="usage-bar-container">
                  <div 
                    class="usage-bar" 
                    :style="{
                      width: `${Math.min(100, (record.totalCount / record.maxCount) * 100)}%`,
                      backgroundColor: getItemUsageColor(record.totalCount / record.maxCount)
                    }"
                  ></div>
                </div>
                <span class="usage-text">{{ record.totalCount }} / {{ record.maxCount }}</span>
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
                <a-button 
                  type="primary" 
                  :href="`/space_analyze?spaceId=${record.id}`" 
                  target="_blank"
                  class="action-button analyze-button"
                >
                  <template #icon><AreaChartOutlined /></template>
                  分析
                </a-button>
                <a-button 
                  type="primary" 
                  :href="`/add_space?id=${record.id}`" 
                  target="_blank"
                  class="action-button edit-button"
                >
                  <template #icon><EditOutlined /></template>
                  编辑
                </a-button>
                <a-popconfirm
                  title="确定要删除该空间吗？"
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
  deleteSpaceUsingPost,
  listSpaceByPageUsingPost,
  updateSpaceUsingPost
} from '@/api/spaceController.ts'
import { message, Tooltip } from 'ant-design-vue'
import dayjs from 'dayjs'
import { SPACE_LEVEL_MAP, SPACE_LEVEL_OPTIONS, SPACE_TYPE_MAP, SPACE_TYPE_OPTIONS } from '@/constants/space.ts'
import { formatSize } from '@/utils'
import { 
  SearchOutlined, 
  PlusOutlined, 
  EditOutlined,
  DeleteOutlined,
  LineChartOutlined,
  BarChartOutlined,
  AreaChartOutlined,
} from '@ant-design/icons-vue'

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName'
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel'
  },
  {
    title: '空间类别',
    dataIndex: 'spaceType'
  },
  {
    title: '使用情况',
    dataIndex: 'spaceUseInfo'
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80
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
const dataList = ref<API.Space[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.SpaceQueryRequest>({
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

// 表格变化之后，重新获取数据
const doTableChange = (page: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 获取数据
const fetchData = async () => {
  const res = await listSpaceByPageUsingPost({
    ...searchParams
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
  const res = await deleteSpaceUsingPost({ id })
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

const cancel = () => {
  message.info('已取消')
}

// 计算存储空间使用率的颜色
const getSpaceUsageColor = (ratio: number) => {
  if (ratio < 0.6) return 'var(--success-color)';
  if (ratio < 0.8) return 'var(--warning-color)';
  return 'var(--danger-color)';
};

// 计算项目数量使用率的颜色
const getItemUsageColor = (ratio: number) => {
  if (ratio < 0.6) return 'var(--success-color)';
  if (ratio < 0.8) return 'var(--warning-color)';
  return 'var(--danger-color)';
};

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

.danger-button {
  background: linear-gradient(135deg, #ef4444, #b91c1c) !important;
  border: none !important;
}

.success-button {
  background: linear-gradient(135deg, #10b981, #059669) !important;
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
.space-id {
  font-family: 'SF Mono', monospace;
  font-weight: 500;
  color: var(--text-secondary);
}

.space-name {
  font-weight: 600;
  color: var(--primary-color);
  display: inline-block;
  padding: 4px 0;
  transition: color var(--transition-fast);
}

/* 级别徽章 */
.level-badge {
  display: inline-block;
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 600;
  border-radius: var(--border-radius-full);
  text-align: center;
  transition: all var(--transition-normal);
}

.level-badge:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.level-1 {
  background-color: rgba(16, 185, 129, 0.1);
  color: #10b981;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.level-2 {
  background-color: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.level-3 {
  background-color: rgba(139, 92, 246, 0.1);
  color: #8b5cf6;
  border: 1px solid rgba(139, 92, 246, 0.2);
}

/* 类型徽章 */
.type-badge {
  display: inline-block;
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 600;
  border-radius: var(--border-radius-full);
  text-align: center;
  transition: all var(--transition-normal);
}

.type-badge:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.type-1 {
  background-color: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.type-2 {
  background-color: rgba(236, 72, 153, 0.1);
  color: #ec4899;
  border: 1px solid rgba(236, 72, 153, 0.2);
}

.type-3 {
  background-color: rgba(6, 182, 212, 0.1);
  color: #06b6d4;
  border: 1px solid rgba(6, 182, 212, 0.2);
}

/* 空间使用情况 */
.usage-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.usage-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.usage-label {
  font-weight: 500;
  font-size: 12px;
  color: var(--text-secondary);
}

.usage-bar-container {
  height: 6px;
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: var(--border-radius-full);
  overflow: hidden;
  width: 100%;
  position: relative;
}

.usage-bar {
  height: 100%;
  border-radius: var(--border-radius-full);
  transition: width 0.5s ease-out, background-color 0.5s ease;
}

.usage-text {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
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

.analyze-button {
  background-color: var(--accent-color);
  border-color: var(--accent-color);
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
  
  .action-buttons {
    flex-wrap: wrap;
  }
  
  .action-group {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
