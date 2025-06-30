<template>
  <div id="spaceDetailPage">
    <a-flex justify="space-between" align="bottom">
      <h2>{{ space.spaceName }}({{ SPACE_TYPE_MAP[space.spaceType] }})</h2>
      <a-space size="middle">
        <a-button v-if="canUploadPicture" type="primary" :href="`/add_picture?spaceId=${id}`" target="_blank">+ 创建图片</a-button>
        <a-button
          v-if="canManageSpaceUser"
          type="primary"
          ghost
          :icon="h(TeamOutlined)"
          :href="`/spaceUserManage/${id}`"
          target="_blank"
        >
          成员管理
        </a-button>

        <a-button
          v-if="canManageSpaceUser"
          style="background-color: #2a5704; color: #fff"
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
        >
          批量编辑
        </a-button>
        <a-tooltip :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`">
          <a-progress
            type="circle"
            :percent="((space.totalSize*100)/space.maxSize).toFixed(1)"
            :size="42"
          />
        </a-tooltip>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />
    <!-- 搜索表单 -->
    <PictureSearchForm :on-search="onSearch"/>
    <div style="margin-bottom: 16px"/>
    <!-- 按颜色搜索 -->
    <a-form-item label="按颜色搜索" style="margin-top: 16px">
      <color-picker format="hex" @pureColorChange="onColorChange" />
    </a-form-item>
    <!-- 图片列表 -->
    <PictureList
      class="card"
      :dataList="dataList"
      :loading="loading"
      :showOp="true"
      :on-reload="fetchData"
      :canEdit="canEditPicture"
      :canDelete="canDeletePicture"
    />
    <!-- 分页 -->
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
    />
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
import { EditOutlined, BarChartOutlined,TeamOutlined } from '@ant-design/icons-vue'
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


</style>
