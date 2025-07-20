<template>
  <div id="userManagePage" class="admin-page">
    <div class="page-header glass-effect">
      <div class="page-title">
        <h2 class="gradient-text">用户管理</h2>
        <div class="title-underline"></div>
      </div>
    </div>
    
    <!-- 搜索表单 -->
    <div class="search-panel glass-effect">
      <a-form layout="inline" :model="searchParams" @finish="doSearch" class="search-form">
        <a-form-item label="账号">
          <a-input 
            v-model:value="searchParams.userAccount" 
            placeholder="输入账号" 
            allow-clear
            class="custom-input"
          />
        </a-form-item>
        <a-form-item label="用户名">
          <a-input 
            v-model:value="searchParams.userName" 
            placeholder="输入用户名" 
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
          <template v-if="['userAccount', 'userName', 'userProfile'].includes(column.dataIndex)">
            <div class="editable-field">
              <a-input
                v-if="editableData[record.id]"
                v-model:value="editableData[record.id][column.dataIndex]"
                class="edit-input"
              />
              <template v-else>
                <span class="field-text">{{ record[column.dataIndex] }}</span>
              </template>
            </div>
          </template>
          <template v-if="column.dataIndex === 'userAvatar'">
            <div class="avatar-container">
              <a-avatar :src="record.userAvatar" :size="64" class="user-avatar" />
            </div>
          </template>
          <template v-else-if="column.dataIndex === 'userRole'">
            <div class="role-badge" :class="record.userRole === 'admin' ? 'admin-role' : 'user-role'">
              {{ record.userRole === 'admin' ? '管理员' : '普通用户' }}
            </div>
          </template>
          <template v-if="column.dataIndex === 'createTime'">
            <div class="time-display">
              {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
            </div>
          </template>
          <template v-if="column.dataIndex === 'updateTime'">
            <div class="time-display">
              {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
            </div>
          </template>
          <template v-else-if="column.key === 'action'">
            <div class="action-group">
              <a-button 
                danger 
                @click="handleDelete(record.id)" 
                class="action-button delete-button"
              >
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
              
              <template v-if="editableData[record.id]">
                <a-button 
                  type="primary" 
                  @click="save(record.id)" 
                  class="action-button save-button"
                >
                  <template #icon><SaveOutlined /></template>
                  保存
                </a-button>
                
                <a-popconfirm 
                  title="确定取消编辑吗?" 
                  @confirm="cancel(record.id)"
                  class="popconfirm"
                >
                  <a-button class="action-button cancel-button">
                    <template #icon><CloseOutlined /></template>
                    取消
                  </a-button>
                </a-popconfirm>
              </template>
              
              <template v-else>
                <a-button 
                  type="primary" 
                  @click="edit(record.id)" 
                  class="action-button edit-button"
                >
                  <template #icon><EditOutlined /></template>
                  编辑
                </a-button>
              </template>
            </div>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>
<script lang="ts" setup>

import { computed, onMounted, provide, reactive, ref, type UnwrapRef } from 'vue'
import { deleteUserUsingPost, listUserVoByPageUsingPost, updateUserUsingPost } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { cloneDeep } from 'lodash-es';
import { 
  SearchOutlined, 
  EditOutlined,
  DeleteOutlined,
  SaveOutlined,
  CloseOutlined
} from '@ant-design/icons-vue'


const columns = [
  {
    title: 'id',
    dataIndex: 'id'
  },
  {
    title: '账号',
    dataIndex: 'userAccount'
  },
  {
    title: '用户名',
    dataIndex: 'userName'
  },
  {
    title: '头像',
    dataIndex: 'userAvatar'
  },
  {
    title: '简介',
    dataIndex: 'userProfile'
  },
  {
    title: '用户角色',
    dataIndex: 'userRole'
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
//
// interface UserDataType  {
//   createTime: string
//   id: number
//   userAccount: string
//   userAvatar: string
//   userName: string
//   userProfile: string
//   userRole: string
// }

// 表格数据
const dataList = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'ascend',
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
  const res = await listUserVoByPageUsingPost({
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
  const res = await deleteUserUsingPost({id})
  if (res.data.code === 0 ) {
    message.success('删除成功')
    await fetchData()
  }else{
    message.error("删除失败")
  }
}

const editableData: UnwrapRef<Record<number, API.UserVO>> = reactive({});

const edit = (id: number) => {
  editableData[id] = cloneDeep(dataList.value.filter(item => id === item.id)[0]);
};
const save = async (id: number) => {
  try {
    // 获取正在编辑的数据
    const editingData = editableData[id];

    // 调用后端接口
    const res = await updateUserUsingPost({
      id: id,
      userAccount: editingData.userAccount,
      userName: editingData.userName,
      userProfile: editingData.userProfile
    });
    if (res.data.code === 0) {
      message.success('更新成功');
      await fetchData();
      delete editableData[id];
    }else {
      message.error("更新失败")
    }
  }catch (e) {
    message.error("更新失败")
  }
};

const cancel = (key: number) => {
  delete editableData[key];
};


// 页面加载时获取数据
onMounted(() => {
  fetchData()
})


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

.custom-input {
  min-width: 180px;
  border-radius: var(--border-radius-md);
  border: 1px solid var(--border-color);
  transition: all var(--transition-fast);
}

.custom-input:hover {
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

/* 可编辑字段 */
.editable-field {
  min-height: 32px;
  display: flex;
  align-items: center;
}

.field-text {
  transition: color var(--transition-fast);
  padding: 4px 0;
}

.edit-input {
  border-radius: var(--border-radius-md);
  transition: all var(--transition-fast);
}

.edit-input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(37, 100, 235, 0.1);
}

/* 头像样式 */
.avatar-container {
  display: flex;
  align-items: center;
  justify-content: center;
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

/* 角色徽章 */
.role-badge {
  display: inline-block;
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 600;
  border-radius: var(--border-radius-full);
  text-align: center;
  transition: all var(--transition-normal);
}

.role-badge:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.admin-role {
  background-color: rgba(16, 185, 129, 0.1);
  color: var(--success-color);
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.user-role {
  background-color: rgba(59, 130, 246, 0.1);
  color: var(--primary-color);
  border: 1px solid rgba(59, 130, 246, 0.2);
}

.time-display {
  color: var(--text-secondary);
  font-size: 13px;
}

/* 操作按钮区域 */
.action-group {
  display: flex;
  gap: 8px;
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

.save-button {
  background-color: var(--success-color);
  border-color: var(--success-color);
}

.cancel-button {
  background-color: #f4f4f5;
  color: var(--text-secondary);
  border-color: #e4e4e7;
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
  
  .action-group {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
