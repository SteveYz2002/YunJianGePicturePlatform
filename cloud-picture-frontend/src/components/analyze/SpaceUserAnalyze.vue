<template>
  <div class="space-user-analyze">
    <div class="component-header">
      <h3 class="component-title">
        <UserOutlined class="title-icon" />
        用户上传趋势
      </h3>
      <div class="header-actions">
        <div class="reload-action" @click="fetchData" :class="{ 'is-loading': loading }">
          <ReloadOutlined />
        </div>
      </div>
    </div>
    
    <div class="filter-section">
      <a-segmented 
        v-model:value="timeDimension" 
        :options="timeDimensionOptions" 
        class="time-filter"
      />
      <a-input-search 
        placeholder="请输入用户 ID" 
        enter-button="搜索" 
        @search="doSearch"
        class="user-search"
        allowClear
      />
    </div>
    
    <div class="chart-container">
      <div v-if="!loading && (!dataList || dataList.length === 0)" class="empty-data">
        <InboxOutlined class="empty-icon" />
        <p class="empty-text">暂无用户上传数据</p>
      </div>
      <v-chart 
        :option="options" 
        class="user-chart"
        :loading="loading" 
        autoresize
      />
    </div>
    
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watchEffect } from 'vue'
import { getSpaceUserAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'
import VChart from 'vue-echarts'
import 'echarts'
import {
  UserOutlined,
  ReloadOutlined,
  InboxOutlined
} from '@ant-design/icons-vue'

interface Props {
  queryAll?: boolean
  queryPublic?: boolean
  spaceId?: number
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false
})

const userId = ref<string>()
const doSearch = (value: string) => {
  userId.value = value
  fetchData()
}

const timeDimension = ref<string>('day')
const timeDimensionOptions = [
  {
    label: '按日',
    value: 'day',
  },
  {
    label: '按周',
    value: 'week',
  },
  {
    label: '按月',
    value: 'month',
  },
]

// 时间维度标签
const timeDimensionLabel = computed(() => {
  switch (timeDimension.value) {
    case 'day': return '日';
    case 'week': return '周';
    case 'month': return '月';
    default: return '日';
  }
});

// 加载状态
const loading = ref(true)

// 图表数据
const dataList = ref<API.SpaceUserAnalyzeResponse[]>([])

// 统计信息
const totalUploads = computed(() => {
  if (!dataList.value || dataList.value.length === 0) return 0;
  return dataList.value.reduce((sum, item) => sum + (item.count || 0), 0);
});

const averageUploads = computed(() => {
  if (!dataList.value || dataList.value.length === 0) return 0;
  return Math.round(totalUploads.value / dataList.value.length);
});

const maxUploads = computed(() => {
  if (!dataList.value || dataList.value.length === 0) return 0;
  return Math.max(...dataList.value.map(item => item.count || 0));
});

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSpaceUserAnalyzeUsingPost({
      spaceId: props.spaceId,
      queryAll: props.queryAll,
      queryPublic: props.queryPublic,
      timeDimension: timeDimension.value,
      userId: userId.value
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data ?? []
    } else {
      message.error('获取数据失败' + res.data.message)
    }
  } catch (error) {
    message.error('获取用户上传数据失败')
  } finally {
    loading.value = false
  }
}

/**
 * 监听数据，参数改变时触发加载
 */
watchEffect(() => {
  if (timeDimension.value) {
    fetchData()
  }
})

// 图表选项
const options = computed(() => {
  const periods = dataList.value.map((item) => item.period) // 时间区间
  const counts = dataList.value.map((item) => item.count || 0) // 上传数量

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
        return `${data.name}<br/>${data.seriesName}: ${data.value} 张`;
      },
      extraCssText: 'box-shadow: 0 3px 14px rgba(0,0,0,0.1); border-radius: 8px;'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: periods,
      axisLabel: {
        color: '#64748b',
        fontSize: 12,
        rotate: periods.length > 10 ? 45 : 0
      },
      axisLine: {
        lineStyle: {
          color: '#e2e8f0'
        }
      },
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value',
      name: '上传数量',
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
        name: '上传数量',
        type: 'line',
        data: counts,
        smooth: true,
        symbol: 'emptyCircle',
        symbolSize: 8,
        itemStyle: {
          color: '#1677ff'
        },
        lineStyle: {
          width: 3,
          shadowColor: 'rgba(22, 119, 255, 0.3)',
          shadowBlur: 10
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(22, 119, 255, 0.3)' },
              { offset: 1, color: 'rgba(22, 119, 255, 0.05)' }
            ]
          }
        },
        emphasis: {
          focus: 'series',
          itemStyle: {
            color: '#1677ff',
            borderWidth: 2,
            borderColor: '#fff',
            shadowColor: 'rgba(22, 119, 255, 0.5)',
            shadowBlur: 10
          }
        }
      }
    ],
    animation: true
  }
})

// 对外暴露 fetchData 方法
defineExpose({
  fetchData
})
</script>

<style scoped>
.space-user-analyze {
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
  color: #1677ff;
  margin-right: 8px;
  font-size: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
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
  color: #1677ff;
  background-color: rgba(22, 119, 255, 0.1);
}

.is-loading {
  animation: spin 1.2s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.filter-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  gap: 16px;
  flex-wrap: wrap;
}

.time-filter {
  min-width: 200px;
}

.user-search {
  min-width: 220px;
  max-width: 300px;
}

:deep(.user-search .ant-input) {
  border-radius: 8px 0 0 8px;
}

:deep(.user-search .ant-input-search-button) {
  border-radius: 0 8px 8px 0;
  height: 32px;
  background-color: #1677ff;
  border-color: #1677ff;
}

:deep(.user-search .ant-input-search-button:hover) {
  background-color: #4096ff;
  border-color: #4096ff;
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

.user-chart {
  width: 100%;
  height: 100%;
  min-height: 260px;
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

.stats-footer {
  display: flex;
  justify-content: space-around;
  margin-top: 16px;
  background-color: rgba(22, 119, 255, 0.05);
  border-radius: 8px;
  padding: 12px 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #1677ff;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    gap: 12px;
  }
  
  .time-filter,
  .user-search {
    width: 100%;
    max-width: none;
  }
  
  .stats-footer {
    flex-direction: column;
    gap: 12px;
    padding: 16px;
  }
  
  .stat-item {
    flex-direction: row;
    justify-content: space-between;
    width: 100%;
  }
}
</style>
