<template>
  <div id="userManagePage" >
    <h2>用户管理</h2>
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch" class="card" style="margin-bottom: 10px">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" allow-clear/>
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" allow-clear/>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <!-- 表格 -->
    <a-table :columns="columns" :data-source="dataList" :pagination="pagination" @change="doTableChange" class="card">
      <template #bodyCell="{ column, record }">
        <template v-if="['userAccount', 'userName', 'userProfile'].includes(column.dataIndex)">
          <div>
            <a-input
              v-if="editableData[record.id]"
              v-model:value="editableData[record.id][column.dataIndex]"
              style="margin: -5px 0"
            />
            <template v-else>
              {{ record[column.dataIndex] }}
            </template>
          </div>
        </template>
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" :width="120" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole == 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'updateTime'">
          {{ dayjs(record.updateTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="editable-row-operations">
            <a-button danger @click="handleDelete(record.id)" style="margin-right: 10px">删除</a-button>
            <span v-if="editableData[record.id]">
              <a-typography-link @click="save(record.id)">保存</a-typography-link>
              <a-popconfirm title="确定取消?" @confirm="cancel(record.id)">
               <a>取消</a>
              </a-popconfirm>
            </span>
            <span v-else>
            <a-button type="primary" @click="edit(record.id)">编辑</a-button>
            </span>
          </div>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>

import { computed, onMounted, provide, reactive, ref, type UnwrapRef } from 'vue'
import { deleteUserUsingPost, listUserVoByPageUsingPost, updateUserUsingPost } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { cloneDeep } from 'lodash-es';


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
.editable-row-operations a {
  margin-right: 8px;
}

/* 设置表格背景色与卡片一致 */
:deep(.ant-table) {
  background-color: #f3f3f3;
}

:deep(.ant-table-thead > tr > th) {
  background-color: #f3f3f3;
}

:deep(.ant-table-tbody > tr > td) {
  background-color: #f3f3f3;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background-color: #e8e8e8 !important;
}
</style>
