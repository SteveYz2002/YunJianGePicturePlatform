<template>
  <div class="space-category-analyze">
    <div class="component-header">
      <h3 class="component-title">
        <AppstoreOutlined class="title-icon" />
        图片分类分析
      </h3>
      <div class="reload-action" @click="fetchData" :class="{ 'is-loading': loading }">
        <ReloadOutlined />
      </div>
    </div>
    
    <div class="chart-container">
      <div v-if="!loading && dataList.length === 0" class="empty-data">
        <InboxOutlined class="empty-icon" />
        <p class="empty-text">暂无分类数据</p>
      </div>
      <v-chart 
        :option="options" 
        class="category-chart"
        :loading="loading" 
        autoresize
      />
    </div>
    
    <div class="legend-footer">
      <div class="legend-item count">
        <div class="legend-color"></div>
        <span class="legend-text">图片数量</span>
      </div>
      <div class="legend-item size">
        <div class="legend-color"></div>
        <span class="legend-text">图片大小 (MB)</span>
      </div>
      <div class="stat-badge">
        <span>分类总数: {{ categoryCount }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watchEffect } from 'vue'
import { getSpaceCategoryAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'
import VChart from 'vue-echarts'
import 'echarts'
import {
  AppstoreOutlined,
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

// 加载状态
const loading = ref(true)

// 图表数据
const dataList = ref<API.SpaceCategoryAnalyzeResponse[]>([])

// 分类总数
const categoryCount = computed(() => {
  return dataList.value.filter(item => 
    item.category && item.category !== '' && item.category !== 'null'
  ).length || 0;
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSpaceCategoryAnalyzeUsingPost({
      spaceId: props.spaceId,
      queryAll: props.queryAll,
      queryPublic: props.queryPublic
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data ?? []
    } else {
      message.error('获取数据失败' + res.data.message)
    }
  } catch (error) {
    message.error('获取分类数据失败')
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
  const categories = dataList.value.map((item) =>
    !item.category || item.category === '' || item.category === 'null' || item.category === null ? '未分类' : item.category
  )
  const countData = dataList.value.map((item) => item.count || 0)
  const sizeData = dataList.value.map((item) => ((item.totalSize || 0) / (1024 * 1024)).toFixed(2)) // 转为 MB

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
      extraCssText: 'box-shadow: 0 3px 14px rgba(0,0,0,0.1); border-radius: 8px;'
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '10%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: categories,
      axisLabel: {
        rotate: categories.length > 5 ? 45 : 0,
        interval: 0,
        color: '#64748b',
        fontSize: 12
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
    yAxis: [
      {
        type: 'value',
        name: '图片数量',
        nameTextStyle: {
          color: '#64748b'
        },
        axisLabel: {
          color: '#64748b'
        },
        axisLine: { 
          show: true, 
          lineStyle: { color: '#4096ff' } 
        },
        splitLine: {
          lineStyle: {
            color: '#f1f5f9',
            type: 'dashed'
          }
        }
      },
      {
        type: 'value',
        name: '图片大小 (MB)',
        nameTextStyle: {
          color: '#64748b'
        },
        position: 'right',
        axisLabel: {
          color: '#64748b'
        },
        axisLine: { 
          show: true, 
          lineStyle: { color: '#52c41a' } 
        },
        splitLine: {
          show: false
        }
      },
    ],
    series: [
      { 
        name: '图片数量', 
        type: 'bar', 
        yAxisIndex: 0,
        data: countData, 
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#4096ff' },
              { offset: 1, color: '#1677ff' }
            ]
          },
          borderRadius: [4, 4, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: '#69b1ff' },
                { offset: 1, color: '#4096ff' }
              ]
            }
          }
        },
        barWidth: '20%',
        barGap: '10%'
      },
      { 
        name: '图片总大小', 
        type: 'bar', 
        yAxisIndex: 1,
        data: sizeData,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#95de64' },
              { offset: 1, color: '#52c41a' }
            ]
          },
          borderRadius: [4, 4, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: {
              type: 'linear',
              x: 0,
              y: 0,
              x2: 0,
              y2: 1,
              colorStops: [
                { offset: 0, color: '#b7eb8f' },
                { offset: 1, color: '#95de64' }
              ]
            }
          }
        },
        barWidth: '20%',
        barGap: '10%'
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
.space-category-analyze {
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
  color: #52c41a;
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
  color: #52c41a;
  background-color: rgba(82, 196, 26, 0.1);
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

.category-chart {
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

.legend-footer {
  display: flex;
  align-items: center;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed rgba(0, 0, 0, 0.06);
}

.legend-item {
  display: flex;
  align-items: center;
  margin-right: 16px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  margin-right: 6px;
}

.legend-item.count .legend-color {
  background: linear-gradient(180deg, #4096ff, #1677ff);
}

.legend-item.size .legend-color {
  background: linear-gradient(180deg, #95de64, #52c41a);
}

.legend-text {
  font-size: 12px;
  color: #64748b;
}

.stat-badge {
  margin-left: auto;
  background-color: rgba(82, 196, 26, 0.1);
  color: #52c41a;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 12px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .legend-footer {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .legend-item {
    margin-right: 12px;
  }
  
  .stat-badge {
    margin-left: 0;
    margin-top: 8px;
    width: 100%;
    text-align: center;
  }
}
</style>
