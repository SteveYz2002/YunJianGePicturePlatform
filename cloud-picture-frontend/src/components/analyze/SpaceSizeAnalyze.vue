<template>
  <div class="space-size-analyze">
    <div class="component-header">
      <h3 class="component-title">
        <FileImageOutlined class="title-icon" />
        图片大小分布
      </h3>
      <div class="reload-action" @click="fetchData" :class="{ 'is-loading': loading }">
        <ReloadOutlined />
      </div>
    </div>
    
    <div class="chart-container">
      <div v-if="!loading && dataList.length === 0" class="empty-data">
        <InboxOutlined class="empty-icon" />
        <p class="empty-text">暂无图片大小数据</p>
      </div>
      <v-chart 
        :option="options" 
        class="size-chart"
        :loading="loading" 
        autoresize
      />
    </div>
    
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watchEffect } from 'vue'
import { getSpaceSizeAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'
import VChart from 'vue-echarts'
import 'echarts'
import {
  FileImageOutlined,
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
const dataList = ref<API.SpaceSizeAnalyzeResponse[]>([])


// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSpaceSizeAnalyzeUsingPost({
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
    message.error('获取图片大小数据失败')
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
  const pieData = dataList.value.map((item) => ({
    name: item.sizeRange,
    value: item.count || 0,
  }))

  // 自定义渐变色
  const colors = [
    ['#1677ff', '#4096ff'],
    ['#52c41a', '#95de64'],
    ['#722ed1', '#b37feb'],
    ['#fa8c16', '#ffd591'],
    ['#13c2c2', '#5cdbd3'],
    ['#eb2f96', '#ff85c0']
  ];

  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 张图片 ({d}%)',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e6f0ff',
      borderWidth: 1,
      padding: [8, 12],
      textStyle: {
        color: '#1e293b',
      },
      extraCssText: 'box-shadow: 0 3px 14px rgba(0,0,0,0.1); border-radius: 8px;'
    },
    legend: {
      type: 'scroll',
      orient: 'horizontal',
      bottom: 0,
      icon: 'circle',
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 15,
      textStyle: {
        color: '#64748b',
        fontSize: 12
      },
      pageTextStyle: {
        color: '#64748b'
      }
    },
    series: [
      {
        name: '图片大小',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '14',
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        },
        labelLine: {
          show: false
        },
        data: pieData,
        color: colors.map(color => {
          return {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: color[0] },
              { offset: 1, color: color[1] }
            ]
          }
        })
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
.space-size-analyze {
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
  color: #fa8c16;
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
  color: #fa8c16;
  background-color: rgba(250, 140, 22, 0.1);
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

.size-chart {
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
  justify-content: space-between;
  margin-top: 16px;
  background-color: rgba(250, 140, 22, 0.05);
  border-radius: 8px;
  padding: 12px 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  padding: 0 8px;
  border-right: 1px dashed rgba(0, 0, 0, 0.06);
}

.stat-item:last-child {
  border-right: none;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
}

.stat-value.highlight {
  color: #fa8c16;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .stats-footer {
    flex-direction: column;
    padding: 12px;
    gap: 12px;
  }
  
  .stat-item {
    flex-direction: row;
    justify-content: space-between;
    width: 100%;
    padding: 4px 0;
    border-right: none;
    border-bottom: 1px dashed rgba(0, 0, 0, 0.06);
  }
  
  .stat-item:last-child {
    border-bottom: none;
  }
  
  .stat-label {
    margin-top: 0;
  }
}
</style>
