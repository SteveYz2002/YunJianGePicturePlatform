<template>
  <div id="spaceUserManage" class="admin-page">
    <div class="page-header glass-effect">
      <div class="page-title">
        <h2 class="gradient-text">空间成员管理</h2>
        <div class="title-underline"></div>
      </div>
      <a-space class="action-buttons">
        <a-button type="primary" href="/add_space" target="_blank">
          <div style="margin-top: 8px;">
            <PlusOutlined />
            创建空间
          </div>
        </a-button>
        <a-button 
          type="primary" 
          href="/space_analyze?queryPublic=1" 
          target="_blank" 
          class="danger-button"
        >
        <div style="margin-top: 8px;">
          <LineChartOutlined />
          分析公共图库
        </div>
        </a-button>
        <a-button 
          type="primary" 
          href="/space_analyze?queryAll=1" 
          target="_blank" 
          class="success-button"
        >
        <div style="margin-top: 8px;">
          <BarChartOutlined />
          分析全部空间
        </div>
        </a-button>
      </a-space>
    </div>
    
    <!-- 添加成员表单 -->
    <div class="form-container glass-effect">
      <div class="form-title">添加空间成员</div>
      <a-form layout="inline" :model="formData" @finish="handleSubmit" class="add-user-form">
        <a-form-item label="用户 ID" name="userId">
          <a-input 
            v-model:value="formData.userId" 
            placeholder="请输入用户 ID" 
            allow-clear 
            class="custom-input"
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" class="add-button">
            <template #icon><UserAddOutlined /></template>
            添加用户
          </a-button>
        </a-form-item>
      </a-form>
    </div>

    <!-- 表格 -->
    <div class="table-container glass-effect">
      <a-table 
        :columns="columns" 
        :data-source="dataList"
        class="custom-table"
        :pagination="false"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'userInfo'">
            <div class="user-info">
              <a-avatar :src="record.user?.userAvatar" class="user-avatar" />
              <span class="user-name">{{ record.user?.userName || '未设置名称' }}</span>
            </div>
          </template>
          <template v-if="column.dataIndex === 'spaceRole'">
            <a-select
              v-model:value="record.spaceRole"
              :options="SPACE_ROLE_OPTIONS"
              @change="(value) => editSpaceRole(value, record)"
              class="role-select"
            />
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            <div class="time-display">
              {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
            </div>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="action-group">
              <a-button 
                type="primary" 
                danger 
                @click="handleDelete(record.id)"
                class="action-button delete-button"
              >
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </div>
          </template>
        </template>
      </a-table>
      
      <div class="empty-placeholder" v-if="!dataList || dataList.length === 0">
        <a-empty description="暂无空间成员" />
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>

import { onMounted, reactive, ref,defineProps } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { SPACE_ROLE_OPTIONS } from '@/constants/space.ts'
import {
  listSpaceUserUsingPost,
  deleteSpaceUserUsingPost,
  editSpaceUserUsingPost,
  addSpaceUserUsingPost
} from '@/api/spaceUserController.ts'
import { 
  PlusOutlined, 
  DeleteOutlined,
  LineChartOutlined,
  BarChartOutlined,
  UserAddOutlined
} from '@ant-design/icons-vue'

interface Props {
  id: string
}

const props = defineProps<Props>()

// 表格列
const columns = [
  {
    title: '用户',
    dataIndex: 'userInfo'
  },
  {
    title: '角色',
    dataIndex: 'spaceRole'
  },
  {
    title: '创建时间',
    dataIndex: 'createTime'
  },
  {
    title: '操作',
    key: 'action'
  }
]

// 表格数据
const dataList = ref<API.SpaceUserVO[]>([])

// 获取数据
const fetchData = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }
  const res = await listSpaceUserUsingPost({
    spaceId
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('获取数据失败' + res.data.message)
  }
}

// 编辑角色
const editSpaceRole = async (value: any, record: any) => {
  const res = await editSpaceUserUsingPost({
    id: record.id,
    spaceRole: value
  })
  if (res.data.code === 0) {
    message.success('修改成功')
  } else {
    message.error('修改失败，' + res.data.message)
  }
}


const handleDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteSpaceUserUsingPost({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    await fetchData()
  } else {
    message.error('删除失败')
  }
}


// 页面加载时获取数据
onMounted(() => {
  fetchData()
})

// 添加用户
const formData = reactive<API.SpaceUserAddRequest>({})

const handleSubmit = async () => {
  const spaceId = props.id
  if (!spaceId) {
    return
  }
  const res = await addSpaceUserUsingPost({
    spaceId,
    ...formData,
  })
  if (res.data.code === 0) {
    message.success('添加成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('添加失败，' + res.data.message)
  }
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

.form-container {
  padding: 20px;
  border-radius: var(--border-radius-lg);
  margin-bottom: 24px;
  box-shadow: var(--shadow-sm);
}

.form-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-color);
}

.add-user-form {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  width: 100%;
  align-items: center;
}

.custom-input {
  min-width: 240px;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  transition: all var(--transition-fast);
}

.custom-input:hover {
  border-color: var(--primary-light);
  box-shadow: 0 0 0 2px rgba(37, 100, 235, 0.1);
}

.add-button {
  height: 32px;
  border-radius: var(--border-radius-md);
  transition: all var(--transition-normal);
  background-color: var(--primary-color);
}

.add-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.table-container {
  border-radius: var(--border-radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  min-height: 200px;
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

/* 用户信息样式 */
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  border: 2px solid rgba(37, 100, 235, 0.2);
  transition: all var(--transition-normal);
  box-shadow: var(--shadow-sm);
}

.user-avatar:hover {
  transform: scale(1.1);
  box-shadow: var(--shadow-md);
}

.user-name {
  font-weight: 500;
  color: var(--text-primary);
}

/* 角色选择器 */
.role-select {
  min-width: 120px;
  border-radius: var(--border-radius-md);
}

:deep(.role-select .ant-select-selector) {
  border-radius: var(--border-radius-md) !important;
  transition: all var(--transition-fast);
}

:deep(.role-select:hover .ant-select-selector) {
  border-color: var(--primary-light) !important;
}

:deep(.role-select.ant-select-focused .ant-select-selector) {
  border-color: var(--primary-color) !important;
  box-shadow: 0 0 0 2px rgba(37, 100, 235, 0.1) !important;
}

.time-display {
  color: var(--text-secondary);
  font-size: 13px;
}

/* 操作按钮区域 */
.action-group {
  display: flex;
  justify-content: center;
  width: 100%;
}

.action-button {
  border-radius: var(--border-radius-md) !important;
  transition: all var(--transition-fast) !important;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px !important;
}

.action-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

/* 修复ant-design按钮内文字居中问题 */
:deep(.action-button .ant-btn-icon) {
  margin-right: 6px !important; 
}

:deep(.action-button .ant-btn-icon + span) {
  margin-left: 0 !important;
}

/* 确保删除按钮内容居中 */
.delete-button {
  background-color: var(--danger-color);
  border-color: var(--danger-color);
  min-width: 80px;
}

:deep(.delete-button span) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* 空状态样式 */
.empty-placeholder {
  padding: 40px 0;
}

:deep(.ant-empty) {
  margin: 32px 0;
}

:deep(.ant-empty-description) {
  color: var(--text-secondary);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .add-user-form {
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
}
</style>
