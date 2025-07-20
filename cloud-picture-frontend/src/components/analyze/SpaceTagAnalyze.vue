<template>
  <div class="space-tag-analyze">
    <div class="component-header">
      <h3 class="component-title">
        <TagsOutlined class="title-icon" />
        图片标签分析
      </h3>
      <div class="reload-action" @click="fetchData" :class="{ 'is-loading': loading }">
        <ReloadOutlined />
      </div>
    </div>
    
    <div class="chart-container">
      <div v-if="!loading && dataList.length === 0" class="empty-data">
        <InboxOutlined class="empty-icon" />
        <p class="empty-text">暂无标签数据</p>
      </div>
      <v-chart 
        :option="options" 
        class="tag-chart"
        :loading="loading" 
        autoresize
      />
    </div>
    
    <div class="stats-footer">
      <div class="stat-item">
        <span class="stat-label">标签总数</span>
        <span class="stat-value">{{ tagCount }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">最热门标签</span>
        <span class="stat-value highlight">{{ topTag }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">标签分布</span>
        <span class="stat-value">{{ tagDistribution }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watchEffect } from 'vue'
import { getSpaceTagAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'
import VChart from 'vue-echarts'
import 'echarts'
import 'echarts-wordcloud'
import {
  TagsOutlined,
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
const dataList = ref<API.SpaceTagAnalyzeResponse[]>([])

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSpaceTagAnalyzeUsingPost({
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
    message.error('获取标签数据失败')
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

// 统计信息
const tagCount = computed(() => {
  return dataList.value.length;
})

const topTag = computed(() => {
  if (!dataList.value || dataList.value.length === 0) return '无';
  
  // 找出出现次数最多的标签
  const sortedData = [...dataList.value].sort((a, b) => (b.count || 0) - (a.count || 0));
  const tag = sortedData[0].tag;
  return !tag || tag === '' || tag === 'null' || tag === null ? '无标签' : tag;
})

const tagDistribution = computed(() => {
  if (!dataList.value || dataList.value.length === 0) return '无数据';
  
  // 计算有标签和无标签的图片数量
  const hasTag = dataList.value.filter(item => 
    item.tag && item.tag !== '' && item.tag !== 'null'
  );
  const hasTagCount = hasTag.reduce((sum, item) => sum + (item.count || 0), 0);
  
  const noTag = dataList.value.find(item => 
    !item.tag || item.tag === '' || item.tag === 'null'
  );
  const noTagCount = noTag ? noTag.count || 0 : 0;
  
  const total = hasTagCount + noTagCount;
  if (total === 0) return '无数据';
  
  const hasTagPercent = Math.round((hasTagCount / total) * 100);
  return `${hasTagPercent}% 已标记`;
})

// 图表选项
const options = computed(() => {
  const tagData = dataList.value.map((item) => ({
    name: !item.tag || item.tag === '' || item.tag === 'null' || item.tag === null ? '无标签' : item.tag,
    value: item.count || 0,
  }))

  // 自定义颜色数组，使用渐变色，比随机颜色更美观
  const colors = [
    '#1677ff', '#4096ff', '#0958d9',  // 蓝色系
    '#52c41a', '#95de64', '#389e0d',  // 绿色系
    '#722ed1', '#b37feb', '#531dab',  // 紫色系
    '#f5222d', '#ff7a45', '#fa541c',  // 红色系
    '#faad14', '#ffc53d', '#d48806',  // 黄色系
    '#13c2c2', '#5cdbd3', '#006d75',  // 青色系
  ];

  return {
    tooltip: {
      show: true,
      trigger: 'item',
      formatter: (params: any) => `${params.name}: ${params.value} 次`,
      backgroundColor: 'rgba(255, 255, 255, 0.9)',
      borderColor: '#e6f0ff',
      borderWidth: 1,
      padding: [6, 12],
      textStyle: {
        color: '#1e293b',
      },
      extraCssText: 'box-shadow: 0 3px 14px rgba(0,0,0,0.1); border-radius: 8px;'
    },
    series: [{
      type: 'wordCloud',
      gridSize: 6,
      sizeRange: [14, 60],
      rotationRange: [-45, 45],
      shape: 'circle',
      width: '100%',
      height: '100%',
      drawOutOfBound: false,
      textStyle: {
        fontFamily: '"Montserrat", "Helvetica", "Arial", sans-serif',
        fontWeight: 'normal',
        color: function(params: any) {
          // 使用固定的调色板
          return colors[params.dataIndex % colors.length];
        }
      },
      emphasis: {
        textStyle: {
          fontWeight: 'bold',
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
        }
      },
      data: tagData
    }]
  }
})

// 对外暴露 fetchData 方法
defineExpose({
  fetchData
})
</script>

<style scoped>
.space-tag-analyze {
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
  color: #722ed1;
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
  color: #722ed1;
  background-color: rgba(114, 46, 209, 0.1);
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

.tag-chart {
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
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  background-color: rgba(114, 46, 209, 0.05);
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
  font-size: 16px;
  font-weight: 500;
  color: #1e293b;
}

.highlight {
  color: #722ed1;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .stats-footer {
    flex-direction: column;
    gap: 12px;
  }
  
  .stat-item {
    flex-direction: row;
    justify-content: space-between;
  }
}
</style>
