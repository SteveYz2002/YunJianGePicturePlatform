<template>
  <div class="space-rank-analyze">
    <div class="component-header">
      <h3 class="component-title">
        <TrophyOutlined class="title-icon" />
        空间使用排行
      </h3>
      <div class="reload-action" @click="fetchData" :class="{ 'is-loading': loading }">
        <ReloadOutlined />
      </div>
    </div>
    
    <div class="chart-container">
      <div v-if="!loading && (!dataList || dataList.length === 0)" class="empty-data">
        <InboxOutlined class="empty-icon" />
        <p class="empty-text">暂无排行数据</p>
      </div>
      <v-chart 
        :option="options" 
        class="rank-chart"
        :loading="loading" 
        @click="goTo"
        autoresize
      />
    </div>
    
    <div class="legend-footer">
      <div class="footer-info">
        <InfoCircleOutlined />
        <span>点击柱形可跳转至对应空间</span>
      </div>
      <div class="stats-badge">
        <span>Top {{ dataList.length }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watchEffect } from 'vue'
import { getSpaceRankAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'
import VChart from 'vue-echarts'
import 'echarts'
import { useRouter } from 'vue-router'
import {
  TrophyOutlined,
  ReloadOutlined,
  InboxOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue'

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
  try {
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
  } catch (error) {
    message.error('获取排行榜数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 监听数据，参数改变时触发加载
 */
watchEffect(() => {
  fetchData()
})

// 图表选项
const options = computed(() => {
  const spaceNames = dataList.value.map((item) => item.spaceName || '未命名空间')
  const spaceIds = dataList.value.map((item) => item.id)
  const usageData = dataList.value.map((item) => {
    const sizeInMB = ((item.totalSize || 0) / (1024 * 1024)).toFixed(2);
    return {
      value: sizeInMB,
      itemStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {offset: 0, color: '#eb2f96'},
            {offset: 1, color: '#722ed1'}
          ]
        }
      }
    };
  })

  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e6f0ff',
      borderWidth: 1,
      padding: [8, 12],
      textStyle: {
        color: '#1e293b',
      },
      formatter: function(params: any) {
        const data = params[0];
        return `<div style="font-weight: 600;">${data.name}</div>使用空间: ${data.value} MB`;
      },
      extraCssText: 'box-shadow: 0 3px 14px rgba(0,0,0,0.1); border-radius: 8px;'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '8%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: spaceNames,
      axisLabel: {
        interval: 0,
        rotate: spaceNames.length > 5 ? 30 : 0,
        color: '#64748b',
        fontSize: 12,
        formatter: function (value: string) {
          // 如果名称太长，则进行截断
          if (value.length > 8) {
            return value.substring(0, 8) + '...';
          }
          return value;
        }
      },
      axisTick: {
        alignWithLabel: true
      },
      axisLine: {
        lineStyle: {
          color: '#e2e8f0'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '使用量 (MB)',
      nameTextStyle: {
        color: '#64748b',
        align: 'right'
      },
      axisLabel: {
        color: '#64748b'
      },
      splitLine: {
        lineStyle: {
          color: '#f1f5f9',
          type: 'dashed'
        }
      }
    },
    series: [
      {
        name: '空间使用量',
        type: 'bar',
        data: usageData,
        barWidth: '40%',
        barGap: '30%',
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c} MB',
          fontSize: 12,
          color: '#64748b'
        }
      }
    ],
    spaceIds: spaceIds,
    animation: true
  }
})

// 处理点击事件，跳转到空间详情页
const goTo = (params: any) => {
  // 获取点击的数据索引
  const dataIndex = params.dataIndex
  const spaceIds = options.value.spaceIds

  if (dataIndex !== undefined && spaceIds && spaceIds.length > dataIndex) {
    // 获取对应的空间ID
    const spaceId = spaceIds[dataIndex]
    // 跳转到空间详情页
    message.info('正在跳转到空间详情页...')
    window.open(`/space/${spaceId}`)
  }
}

// 对外暴露 fetchData 方法
defineExpose({
  fetchData
})
</script>

<style scoped>
.space-rank-analyze {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.component-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.component-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
}

.title-icon {
  color: #eb2f96;
  margin-right: 8px;
  font-size: 20px;
}

.reload-action {
  cursor: pointer;
  color: #94a3b8;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.reload-action:hover {
  color: #eb2f96;
  background-color: rgba(235, 47, 150, 0.1);
}

.is-loading {
  animation: spin 1.2s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.chart-container {
  position: relative;
  flex: 1;
  width: 100%;
  min-height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rank-chart {
  width: 100%;
  height: 100%;
  min-height: 260px;
  cursor: pointer;
}

.empty-data {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.empty-icon {
  font-size: 36px;
  color: #d9e1ec;
  margin-bottom: 8px;
}

.empty-text {
  color: #94a3b8;
  font-size: 14px;
}

.legend-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed rgba(0, 0, 0, 0.06);
}

.footer-info {
  display: flex;
  align-items: center;
  color: #64748b;
  font-size: 12px;
}

.footer-info :deep(svg) {
  margin-right: 6px;
  color: #eb2f96;
}

.stats-badge {
  background-color: rgba(235, 47, 150, 0.1);
  color: #eb2f96;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 12px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .legend-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
