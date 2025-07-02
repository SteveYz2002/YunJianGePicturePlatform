<template>
  <div class="space-rank-analyze">
    <a-card title="空间使用排行分析">
      <v-chart :option="options" style="height: 320px; max-width: 100%;" :loading="loading" @click="goTo"/>
    </a-card>
  </div>

</template>

<script setup lang="ts">
import { computed, ref, watchEffect } from 'vue'
import { getSpaceRankAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'
import  VChart from 'vue-echarts'
import  'echarts'
import { useRouter } from 'vue-router'

const router = useRouter()

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false
})

// 加载状态
const loading = ref(true)

// 图表数据
const dataList = ref<API.Space[]>([])

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const res = await getSpaceRankAnalyzeUsingPost({
    spaceId: props.spaceId,
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    topN: 10
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('获取数据失败' + res.data.message)
  }
  loading.value = false
}

/**
 * 监听数据，参数改变时触发加载
 */
watchEffect(() => {
  fetchData()
})

// 图表选项
const options = computed(() => {
  const spaceNames = dataList.value.map((item) => item.spaceName)
  const spaceId = dataList.value.map((item) => item.id)
  const usageData = dataList.value.map((item) => (item.totalSize / (1024 * 1024)).toFixed(2)) // 转为 MB

  return {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: spaceNames,
    },
    yAxis: {
      type: 'value',
      name: '空间使用量 (MB)',
    },
    series: [
      {
        name: '空间使用量 (MB)',
        type: 'bar',
        data: usageData,
        itemStyle: {
          color: '#5470C6', // 自定义柱状图颜色
        },
      },
    ],
    spaceId: spaceId
  }
})

// 处理点击事件，跳转到空间详情页
const goTo = (params: any) => {
  // 获取点击的数据索引
  const dataIndex = params.dataIndex
  const spaceIds = options.value.spaceId

  if (dataIndex !== undefined && spaceIds && spaceIds.length > dataIndex) {
    // 获取对应的空间ID
    const spaceId = spaceIds[dataIndex]
    // 跳转到空间详情页
    window.open(`/space/${spaceId}`)
  }
}

</script>
