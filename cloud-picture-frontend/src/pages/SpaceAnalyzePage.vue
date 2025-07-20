<template>
  <div id="spaceAnalyzePage" class="space-analyze-page">
    <div class="header-section tech-card">
      <div class="header-content">
        <div class="title-section">
          <h2 class="page-title">
            <AreaChartOutlined class="title-icon" />
            空间图库分析
            <span class="space-identifier">
              <span v-if="queryAll" class="space-badge all">全部空间</span>
              <span v-else-if="queryPublic" class="space-badge public">公共图库</span>
              <span v-else class="space-badge specific">
                <a :href="`/space/${spaceId}`" target="_blank" class="space-link">空间 ID: {{ spaceId }}</a>
              </span>
            </span>
          </h2>
          <p class="page-subtitle">深入了解图库使用情况、分类统计和用户行为</p>
        </div>
        <div class="action-buttons">
          <a-button type="primary" @click="refreshAllData" class="refresh-button">
            <template #icon><ReloadOutlined /></template>
            刷新数据
          </a-button>
        </div>
      </div>
    </div>

    <div class="analytics-grid">
      <!-- 空间使用分析 -->
      <div class="analytics-card usage-card">
        <div class="card-wrapper tech-card">
          <SpaceUsageAnalyze 
            :spaceId="spaceId" 
            :queryAll="queryAll" 
            :queryPublic="queryPublic"
            ref="usageAnalyzeRef" 
          />
        </div>
      </div>
      
      <!-- 空间分类分析 -->
      <div class="analytics-card category-card">
        <div class="card-wrapper tech-card">
          <SpaceCategoryAnalyze 
            :spaceId="spaceId" 
            :queryAll="queryAll" 
            :queryPublic="queryPublic" 
            ref="categoryAnalyzeRef"
          />
        </div>
      </div>
      
      <!-- 标签分析 -->
      <div class="analytics-card tag-card">
        <div class="card-wrapper tech-card">
          <SpaceTagAnalyze 
            :spaceId="spaceId" 
            :queryAll="queryAll" 
            :queryPublic="queryPublic" 
            ref="tagAnalyzeRef"
          />
        </div>
      </div>
      
      <!-- 图片大小分段分析 -->
      <div class="analytics-card size-card">
        <div class="card-wrapper tech-card">
          <SpaceSizeAnalyze 
            :spaceId="spaceId" 
            :queryAll="queryAll" 
            :queryPublic="queryPublic" 
            ref="sizeAnalyzeRef"
          />
        </div>
      </div>
      
      <!-- 用户上传行为分析 -->
      <div class="analytics-card user-card">
        <div class="card-wrapper tech-card">
          <SpaceUserAnalyze 
            :spaceId="spaceId" 
            :queryAll="queryAll" 
            :queryPublic="queryPublic" 
            ref="userAnalyzeRef"
          />
        </div>
      </div>
      
      <!-- 空间使用排行分析 -->
      <div v-if="isAdmin" class="analytics-card rank-card">
        <div class="card-wrapper tech-card">
          <SpaceRankAnalyze 
            :spaceId="spaceId" 
            :queryAll="queryAll" 
            :queryPublic="queryPublic" 
            ref="rankAnalyzeRef"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import SpaceUsageAnalyze from '@/components/analyze/SpaceUsageAnalyze.vue'
import SpaceCategoryAnalyze from '@/components/analyze/SpaceCategoryAnalyze.vue'
import SpaceTagAnalyze from '@/components/analyze/SpaceTagAnalyze.vue'
import SpaceSizeAnalyze from '@/components/analyze/SpaceSizeAnalyze.vue'
import SpaceUserAnalyze from '@/components/analyze/SpaceUserAnalyze.vue'
import SpaceRankAnalyze from '@/components/analyze/SpaceRankAnalyze.vue'
import { useRoute } from 'vue-router'
import { computed, ref } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import { AreaChartOutlined, ReloadOutlined } from '@ant-design/icons-vue'

const route = useRoute()

// 空间 id
const spaceId = computed(() => {
  return route.query?.spaceId as string
})

// 是否查询所有空间
const queryAll = computed(() => {
  return !!route.query?.queryAll
})

// 是否查询公共空间
const queryPublic = computed(() => {
  return !!route.query?.queryPublic
})

// 是否是管理员
const loginUserStore = useLoginUserStore()
const loginUser = loginUserStore.loginUser
const isAdmin = computed(() => {
  return loginUser?.userRole === 'admin'
})

// 子组件引用
const usageAnalyzeRef = ref()
const categoryAnalyzeRef = ref()
const tagAnalyzeRef = ref()
const sizeAnalyzeRef = ref()
const userAnalyzeRef = ref()
const rankAnalyzeRef = ref()

// 刷新所有数据
const refreshAllData = async () => {
  message.loading({ content: '正在刷新数据...', key: 'refreshing' })
  
  // 调用所有子组件的 fetchData 方法（如果存在）
  const components = [
    usageAnalyzeRef.value, 
    categoryAnalyzeRef.value, 
    tagAnalyzeRef.value, 
    sizeAnalyzeRef.value, 
    userAnalyzeRef.value, 
    rankAnalyzeRef.value
  ]
  
  // 并行刷新所有数据
  await Promise.all(
    components
      .filter(component => component && typeof component.fetchData === 'function')
      .map(component => component.fetchData())
  )
  
  message.success({ content: '数据刷新成功', key: 'refreshing' })
}
</script>

<style scoped>
.space-analyze-page {
  padding: 20px;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Tech-inspired cards */
.tech-card {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  padding: 24px;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(232, 232, 232, 0.8);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.tech-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #1677ff, #52c41a, #722ed1);
  opacity: 0.8;
}

/* Header section */
.header-section {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.title-section {
  flex: 1;
}

.page-title {
  font-size: 28px;
  margin-bottom: 8px;
  font-weight: 600;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.title-icon {
  margin-right: 12px;
  color: #1677ff;
  font-size: 24px;
}

.space-identifier {
  margin-left: 16px;
  display: inline-flex;
  align-items: center;
}

.space-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: normal;
}

.space-badge.all {
  background: linear-gradient(45deg, #1677ff, #4096ff);
  color: white;
}

.space-badge.public {
  background: linear-gradient(45deg, #52c41a, #95de64);
  color: white;
}

.space-badge.specific {
  background: linear-gradient(45deg, #722ed1, #b37feb);
  color: white;
}

.space-link {
  color: white;
  text-decoration: none;
  transition: all 0.2s;
}

.space-link:hover {
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.6);
}

.page-subtitle {
  color: #666;
  font-size: 16px;
  margin-bottom: 0;
}

.action-buttons {
  margin-left: 20px;
}

.refresh-button {
  border-radius: 8px;
  transition: all 0.2s;
}

.refresh-button:hover {
  transform: translateY(-2px);
}

/* Analytics Grid Layout */
.analytics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 24px;
}

.analytics-card {
  height: 100%;
  animation: cardAppear 0.5s ease-out forwards;
  animation-fill-mode: both;
  opacity: 0;
}

@keyframes cardAppear {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-wrapper {
  height: 100%;
  margin-bottom: 0;
}

/* Apply staggered animation delay to cards */
.usage-card { animation-delay: 0.1s; }
.category-card { animation-delay: 0.2s; }
.tag-card { animation-delay: 0.3s; }
.size-card { animation-delay: 0.4s; }
.user-card { animation-delay: 0.5s; }
.rank-card { animation-delay: 0.6s; }

/* Responsive adjustments */
@media (max-width: 1200px) {
  .analytics-grid {
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  }
}

@media (max-width: 768px) {
  .analytics-grid {
    grid-template-columns: 1fr;
  }
  
  .header-content {
    flex-direction: column;
  }
  
  .action-buttons {
    margin-left: 0;
    margin-top: 12px;
  }
  
  .space-identifier {
    margin-left: 0;
    margin-top: 8px;
  }
  
  .page-title {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
