<template>
  <div id="pictureManagePage">
    <a-flex justify="space-between">
      <h2>图片管理</h2>
      <a-space>
        <a-button type="primary" href="/add_picture" target="_blank">+ 创建图片</a-button>
        <a-button type="primary" href="/add_picture/batch" target="_blank" ghost>+ 批量创建图片</a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px"/>
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch" class="card" style="margin-bottom: 5px">
      <a-form-item label="关键词" name="searchText">
        <a-input
          v-model:value="searchParams.searchText"
          placeholder="从名称和简介搜索"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="类型" name="category">
        <a-input v-model:value="searchParams.category" placeholder="请输入类型" allow-clear />
      </a-form-item>
      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="searchParams.tags"
          mode="tags"
          :options="tagOptions"
          placeholder="请输入标签"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="审核状态" name="reviewStatus">
        <a-select
          v-model:value="searchParams.reviewStatus"
          :options="PIC_REVIEW_STATUS_OPTIONS"
          placeholder="请选择审核状态"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
      class="card">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'id'" >
          <div @click="JumpTo(record.id)" style="color: blue; cursor: pointer; text-decoration: underline;">
            {{ record.id }}
          </div>
        </template>
        <template v-if="column.dataIndex === 'url'">
          <a-image :src="record.url" :width="100"/>
        </template>
        <template v-if="column.dataIndex === 'category'">
          <div>{{ record.category }}</div>
        </template>
        <template v-if="column.dataIndex === 'tags'">
          <a-space wrap>
            <a-tag v-for="tag in JSON.parse(record.tags || '[]')" :key="tag">
              {{ tag }}
            </a-tag>
          </a-space>
        </template>
        <template v-if="column.dataIndex === 'picInfo'">
          <div>格式：{{ record.picFormat }}</div>
          <div>宽度：{{ record.picWidth }}</div>
          <div>高度：{{ record.picHeight }}</div>
          <div>宽高比：{{ record.picScale }}</div>
          <div>大小：{{ (record.picSize / 1024).toFixed(2) }}KB</div>
        </template>
        <template v-if="column.dataIndex === 'reviewMessage'">
          <div>审核状态：{{ PIC_REVIEW_STATUS_MAP[record.reviewStatus] }}</div>
          <div>审核信息：{{ record.reviewMessage }}</div>
          <div>审核人：{{ record.reviewerId }}</div>
          <div v-if="record.reviewTime">审核时间：{{ dayjs(record.reviewTime).format('YYYY-MM-DD HH:mm:ss') }}</div>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-popconfirm
              title="确定要审核通过该图片吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleReview(record, PIC_REVIEW_STATUS_ENUM.PASS)"
              @cancel="cancel"
            >
              <a-button
                v-if="record.reviewStatus!=PIC_REVIEW_STATUS_ENUM.PASS"
                type="link"
                target="_blank"
              >
                通过
              </a-button>
            </a-popconfirm>
            <a-popconfirm
              title="确定要审核拒绝该图片吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleReview(record, PIC_REVIEW_STATUS_ENUM.REJECT)"
              @cancel="cancel"
            >
              <a-button
                v-if="record.reviewStatus!=PIC_REVIEW_STATUS_ENUM.REJECT"
                type="link"
                danger
              >
                拒绝
              </a-button>
            </a-popconfirm>


            <a-button type="link" :href="`/add_picture?id=${record.id}`" target="_blank">编辑</a-button>
            <a-popconfirm
              title="确定要删除该图片吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDelete(record.id)"
              @cancel="cancel"
            >
              <a-button danger >删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
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

.editable-row-operations a {
  margin-right: 8px;
}

</style>
