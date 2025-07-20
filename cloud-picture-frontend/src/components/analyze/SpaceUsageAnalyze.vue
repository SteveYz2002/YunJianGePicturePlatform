<template>
  <div class="space-usage-analyze">
    <div class="component-header">
      <h3 class="component-title">
        <DatabaseOutlined class="title-icon" />
        存储与容量分析
      </h3>
      <div class="reload-action" @click="fetchData" :class="{ 'is-loading': loading }">
        <ReloadOutlined />
      </div>
    </div>

    <div class="metrics-grid">
      <!-- 存储空间指标卡 -->
      <div class="metric-card storage-card">
        <div class="metric-header">
          <span class="metric-label">存储空间</span>
          <HddOutlined class="metric-icon" />
        </div>
        
        <div class="progress-container">
          <a-progress 
            type="dashboard" 
            :percent="data.sizeUsageRatio ?? 0" 
            :width="180"
            :strokeColor="spaceProgressColor"
            :format="() => ''"
          />
          <div class="progress-center">
            <span class="usage-percent">{{ data.sizeUsageRatio ? data.sizeUsageRatio.toFixed(1) : 0 }}%</span>
          </div>
        </div>
        
        <div class="metric-details">
          <div class="detail-item">
            <span class="detail-label">已用空间</span>
            <span class="detail-value">{{ formatSize(data.usedSize) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">总容量</span>
            <span class="detail-value">{{ data.maxSize ? formatSize(data.maxSize) : '无限制' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">剩余空间</span>
            <span class="detail-value">{{ formatSize(data.maxSize ? data.maxSize - data.usedSize : 0) }}</span>
          </div>
        </div>
      </div>

      <!-- 图片数量指标卡 -->
      <div class="metric-card count-card">
        <div class="metric-header">
          <span class="metric-label">图片数量</span>
          <PictureOutlined class="metric-icon" />
        </div>
        
        <div class="progress-container">
          <a-progress 
            type="dashboard" 
            :percent="data.countUsageRatio ?? 0" 
            :width="180"
            :strokeColor="countProgressColor"
            :format="() => ''"
          />
          <div class="progress-center">
            <span class="usage-percent">{{ data.countUsageRatio ? data.countUsageRatio.toFixed(1) : 0 }}%</span>
          </div>
        </div>
        
        <div class="metric-details">
          <div class="detail-item">
            <span class="detail-label">图片总数</span>
            <span class="detail-value">{{ data.usedCount ?? 0 }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">最大容量</span>
            <span class="detail-value">{{ data.maxCount ?? '无限制' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">剩余容量</span>
            <span class="detail-value">{{ data.maxCount ? data.maxCount - data.usedCount : '无限' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect, computed } from 'vue'
import { getSpaceUsageAnalyzeUsingPost } from '@/api/spaceAnalyzeController.ts'
import { message } from 'ant-design-vue'
import { formatSize } from '@/utils'
import { 
  DatabaseOutlined, 
  ReloadOutlined, 
  HddOutlined,
  PictureOutlined
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
const data = ref<API.SpaceUsageAnalyzeResponse>({})

// 进度条颜色
const spaceProgressColor = computed(() => {
  const percent = data.value.sizeUsageRatio || 0;
  if (percent < 50) return { '0%': '#52c41a', '100%': '#1677ff' };
  if (percent < 80) return { '0%': '#faad14', '100%': '#ff7a45' };
  return { '0%': '#ff4d4f', '100%': '#cf1322' };
});

const countProgressColor = computed(() => {
  const percent = data.value.countUsageRatio || 0;
  if (percent < 50) return { '0%': '#1677ff', '100%': '#52c41a' };
  if (percent < 80) return { '0%': '#ff7a45', '100%': '#faad14' };
  return { '0%': '#cf1322', '100%': '#ff4d4f' };
});

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  try {
    const res = await getSpaceUsageAnalyzeUsingPost({
      spaceId: props.spaceId,
      queryAll: props.queryAll,
      queryPublic: props.queryPublic
    })
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data
    } else {
      message.error('获取数据失败' + res.data.message)
    }
  } catch (error) {
    message.error('获取数据出错')
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

// 对外暴露 fetchData 方法
defineExpose({
  fetchData
})
</script>

<style scoped>
.space-usage-analyze {
  width: 100%;
  height: 100%;
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

.metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  height: calc(100% - 50px);
}

.metric-card {
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.metric-card::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  opacity: 0.8;
}

.storage-card::after {
  background: linear-gradient(90deg, #1677ff, #4096ff);
}

.count-card::after {
  background: linear-gradient(90deg, #52c41a, #95de64);
}

.metric-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.metric-label {
  font-size: 16px;
  font-weight: 500;
  color: #1e293b;
}

.metric-icon {
  font-size: 20px;
}

.storage-card .metric-icon {
  color: #1677ff;
}

.count-card .metric-icon {
  color: #52c41a;
}

.progress-container {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  margin: 8px 0 16px;
}

.progress-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.usage-percent {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
}

.metric-details {
  margin-top: auto;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  padding: 6px 0;
  border-top: 1px dashed rgba(0, 0, 0, 0.06);
}

.detail-item:first-child {
  border-top: none;
}

.detail-label {
  color: #64748b;
  font-size: 14px;
}

.detail-value {
  font-weight: 500;
  color: #1e293b;
  font-size: 14px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .metrics-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .metric-card {
    height: auto;
  }
}
</style>
