<template>
  <div id="homePage" class="home-page" ref="homePageRef">
    <!-- 标题区域 - 科技感增强 -->
    <div class="hero-container">
      <div class="hero-backdrop"></div>
      <div class="hero-content">
        <div class="title-content">
          <img src="../assets/logo.png" alt="网站图标" class="title-icon" />
          <h1 class="artistic-title gradient-text">云笺阁</h1>
        </div>
        <div class="subtitle">企业级云图库平台</div>
        
        <!-- 改进的搜索框 -->
        <div class="search-bar-container glass-effect">
          <a-input-search
            placeholder="从海量图片中搜索"
            v-model:value="searchParams.searchText"
            enter-button
            size="large"
            @search="doSearch"
            class="search-input"
          >
            <template #enterButton>
              <a-button type="primary" size="large" class="search-button">
                <template #icon><SearchOutlined /></template>
                搜索
              </a-button>
            </template>
          </a-input-search>
        </div>
        
        <!-- 添加可视化统计或引导 -->
        <div class="hero-stats">
          <div class="stat-item">
            <div class="stat-number">10K+</div>
            <div class="stat-label">精选图片</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number">100+</div>
            <div class="stat-label">专业分类</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number">5K+</div>
            <div class="stat-label">活跃用户</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 内容区域 -->
    <div class="content-container" ref="contentRef">
      <!-- 筛选栏 - 改进样式 -->
      <div class="filter-container glass-effect">
        <a-tabs v-model:activeKey="selectedCategory" @change="doSearch" class="category-tabs">
          <a-tab-pane key="all" tab="全部" />
          <a-tab-pane v-for="category in categoryList" :key="category" :tab="category" />
        </a-tabs>
        
        <div class="tag-bar">
          <div class="tag-label">标签：</div>
          <a-space :size="[0, 8]" wrap>
            <a-checkable-tag
              v-for="(tag, index) in tagList"
              :key="tag"
              v-model:checked="selectedTagList[index]"
              @change="doSearch"
              class="custom-tag"
            >
              {{ tag }}
            </a-checkable-tag>
          </a-space>
        </div>
      </div>
      
      <!-- 瀑布流图片列表 -->
      <div class="pictures-container">
        <WaterfallPictureList :show-op="true" :dataList="dataList" :loading="loading" :onReload="fetchData" />
        
        <!-- 加载状态指示器 - 确保在瀑布流底部 -->
        <div class="load-more-container" v-if="dataList.length > 0">
          <div v-if="loading" class="loading-indicator">
            <a-spin />
            <span class="loading-text">加载中...</span>
          </div>
          <div v-else-if="noMoreData" class="no-more-indicator">
            没有更多内容了
          </div>
          <div v-else class="load-more-button-wrapper">
            <a-button @click="loadMore" type="primary" ghost class="load-more-button">加载更多</a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, onUnmounted, nextTick } from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import WaterfallPictureList from '@/components/WaterfallPictureList.vue'
import { SearchOutlined } from '@ant-design/icons-vue'

// 表格数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)
const noMoreData = ref(false)
const homePageRef = ref<HTMLElement | null>(null)
const contentRef = ref<HTMLElement | null>(null)

// 搜索条件和分页参数分离
const searchParams = reactive<Omit<API.PictureQueryRequest, 'current' | 'pageSize'>>({
  sortField: 'createTime',
  sortOrder: 'descend'
})

// 单独管理分页参数
const pageParams = reactive({
  current: 1,
  pageSize: 10  // 增加每次加载的数量
})

// 获取数据
const fetchData = async (loadMore = false) => {
  if (loading.value && loadMore) return // 防止重复加载
  
  loading.value = true
  
  // 如果不是加载更多，则重置数据
  if (!loadMore) {
    dataList.value = []
    pageParams.current = 1
    noMoreData.value = false
  }
  
  // 转换搜索参数
  const params = {
    ...searchParams,
    current: pageParams.current,
    pageSize: pageParams.pageSize,
    tags: [] as string[],
  }
  if (selectedCategory.value !== 'all') {
    params.category = selectedCategory.value
  }
  selectedTagList.value.forEach((checked, index) => {
    if (checked) {
      params.tags.push(tagList.value[index])
    }
  })
  
  try {
    console.log('正在请求页码:', pageParams.current, '每页大小:', pageParams.pageSize)
    const res = await listPictureVoByPageUsingPost(params)
    
    if (res.data.code === 0 && res.data.data) {
      const newRecords = res.data.data.records ?? []
      total.value = res.data.data.total ?? 0
      
      console.log('获取到数据:', newRecords.length, '条, 总数:', total.value)
      
      // 判断是否还有更多数据
      if (newRecords.length === 0 || (pageParams.current * pageParams.pageSize) >= total.value) {
        console.log('没有更多数据')
        noMoreData.value = true
      } else {
        noMoreData.value = false
      }
      
      // 如果是加载更多，则追加数据，否则替换数据
      if (loadMore) {
        dataList.value = [...dataList.value, ...newRecords]
      } else {
        dataList.value = newRecords
      }
      
      // 增加页码，为下次加载做准备
      pageParams.current++
    } else {
      message.error('获取数据失败' + res.data.message)
    }
  } catch (error) {
    console.error('获取数据失败:', error)
    message.error('获取数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 搜索
const doSearch = () => {
  fetchData(false) // 重置数据并搜索
}

// 滚动加载更多
const handleScroll = () => {
  if (loading.value || noMoreData.value) return
  
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  const windowHeight = window.innerHeight
  const documentHeight = Math.max(
    document.body.scrollHeight,
    document.documentElement.scrollHeight
  )
  
  // 调试信息
  console.log('滚动位置:', scrollTop, '窗口高度:', windowHeight, '文档高度:', documentHeight, '距底部:', documentHeight - (scrollTop + windowHeight))
  
  // 当距离底部100px时，加载更多（之前是200px，可能太早触发）
  if (documentHeight - (scrollTop + windowHeight) < 100) {
    console.log('触发加载更多')
    fetchData(true)
  }
}

// 添加防抖函数
function debounce(func: Function, wait: number) {
  let timeout: number | null = null
  return function(this: any, ...args: any[]) {
    if (timeout) clearTimeout(timeout)
    timeout = window.setTimeout(() => {
      func.apply(this, args)
      timeout = null
    }, wait)
  }
}

// 使用防抖函数包装滚动处理
const debouncedHandleScroll = debounce(handleScroll, 200)

// 手动加载更多函数
const loadMore = () => {
  if (!loading.value && !noMoreData.value) {
    console.log('手动点击加载更多')
    fetchData(true)
  }
}

// 监听滚动事件
onMounted(() => {
  fetchData()
  getTagCategoryOptions()
  // 使用防抖函数处理滚动
  window.addEventListener('scroll', debouncedHandleScroll)
  
  // 初始检查，防止首屏数据不足无法滚动的情况
  nextTick(() => {
    // 首次加载后检查是否需要加载更多
    setTimeout(() => {
      handleScroll()
    }, 500)
  })
})

onUnmounted(() => {
  window.removeEventListener('scroll', debouncedHandleScroll)
})

const categoryList = ref<string[]>([])
const selectedCategory = ref<string>('all')
const tagList = ref<string[]>([])
const selectedTagList = ref<boolean[]>([])

// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接受的格式
    categoryList.value = res.data.data.categoryList ?? []
    tagList.value = res.data.data.tagList ?? []
    // 初始化所有标签为未选中
    selectedTagList.value = tagList.value.map(() => false)
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

const router = useRouter()
</script>

<style scoped>
.home-page {
  width: 100%;
  min-height: 100vh;
  background-color: var(--bg-light);
}

/* 英雄区域样式 */
.hero-container {
  position: relative;
  width: 100%;
  padding: 80px 20px 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  overflow: hidden;
  background-color: var(--bg-light);
}

.hero-backdrop {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(37, 100, 235, 0.05), rgba(6, 182, 212, 0.08));
  z-index: 0;
  overflow: hidden;
}

.hero-backdrop::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 25% 25%, rgba(37, 100, 235, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(6, 182, 212, 0.15) 0%, transparent 50%);
  z-index: -1;
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 800px;
  width: 100%;
  animation: fadeIn 0.8s ease-out;
}

.title-content {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.title-icon {
  height: 60px;
  width: auto;
  margin-right: 12px;
  filter: drop-shadow(0 4px 6px rgba(0, 0, 0, 0.1));
  animation: pulse 2s infinite;
}

.artistic-title {
  font-size: 48px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 1px;
  background-size: 200% auto;
  animation: gradientShift 3s linear infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.subtitle {
  font-size: 18px;
  color: var(--text-secondary);
  margin-bottom: 40px;
  font-weight: 400;
}

/* 搜索栏样式 */
.search-bar-container {
  width: 100%;
  max-width: 600px;
  margin: 0 auto 40px;
  border-radius: var(--border-radius-full);
  padding: 6px;
  box-shadow: var(--shadow-md);
}

.search-input {
  border-radius: var(--border-radius-full);
  overflow: hidden;
}

.search-input :deep(.ant-input) {
  height: 48px;
  font-size: 16px;
  padding-left: 20px;
  border: none;
  background: transparent;
}

.search-input :deep(.ant-input-search-button) {
  height: 48px;
  font-size: 16px;
  border-radius: 0 var(--border-radius-full) var(--border-radius-full) 0 !important;
  width: 110px;
}

.search-button {
  font-weight: 600;
  font-size: 16px;
}

/* 统计数据展示 */
.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: var(--primary-color);
  margin-bottom: 4px;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: linear-gradient(to bottom, transparent, var(--border-color), transparent);
}

/* 内容区域样式 */
.content-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

/* 筛选栏样式 */
.filter-container {
  margin-bottom: 30px;
  border-radius: var(--border-radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-sm);
}

.category-tabs {
  margin-bottom: 20px;
}

.category-tabs :deep(.ant-tabs-nav::before) {
  border-bottom-color: var(--border-color);
}

.category-tabs :deep(.ant-tabs-tab) {
  padding: 10px 16px;
  font-size: 15px;
  transition: all var(--transition-fast);
}

.category-tabs :deep(.ant-tabs-tab:hover) {
  color: var(--primary-color);
}

.category-tabs :deep(.ant-tabs-tab-active) {
  font-weight: 600;
}

.category-tabs :deep(.ant-tabs-ink-bar) {
  background: var(--primary-gradient);
  height: 3px;
  border-radius: 3px 3px 0 0;
}

.tag-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.tag-label {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
  margin-right: 10px;
  margin-bottom: 8px;
}

.custom-tag {
  margin: 0 8px 8px 0;
  padding: 4px 10px;
  border-radius: var(--border-radius-md);
  transition: all var(--transition-fast);
  border: 1px solid var(--border-color);
}

.custom-tag:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.custom-tag:deep(.ant-tag-checkable-checked) {
  background-color: var(--primary-color);
  color: white;
  border-color: var(--primary-color);
}

/* 图片容器样式 */
.pictures-container {
  position: relative;
  width: 100%;
}

/* 加载更多区域 */
.load-more-container {
  margin-top: 40px;
  text-align: center;
  padding: 20px 0;
  position: relative;
}

.load-more-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 1px;
  background: linear-gradient(90deg, 
      transparent, 
      var(--border-color), 
      transparent);
}

.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.loading-text {
  font-size: 14px;
  color: var(--text-secondary);
  margin-top: 8px;
}

.no-more-indicator {
  font-size: 14px;
  color: var(--text-secondary);
  padding: 10px 20px;
  background: rgba(240, 242, 245, 0.6);
  border-radius: var(--border-radius-full);
}

.load-more-button {
  padding: 0 28px;
  height: 40px;
  font-size: 15px;
  border-radius: var(--border-radius-md);
  transition: all var(--transition-normal);
}

.load-more-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .hero-container {
    padding: 60px 16px 40px;
  }
  
  .artistic-title {
    font-size: 38px;
  }
  
  .subtitle {
    font-size: 16px;
    margin-bottom: 30px;
  }
  
  .hero-stats {
    gap: 10px;
  }
  
  .stat-item {
    padding: 0 10px;
  }
  
  .stat-number {
    font-size: 26px;
  }
  
  .stat-label {
    font-size: 12px;
  }
  
  .content-container {
    padding: 0 12px 40px;
  }
  
  .filter-container {
    padding: 15px;
    margin-bottom: 20px;
  }
}

@media (max-width: 576px) {
  .hero-container {
    padding: 40px 12px 30px;
  }
  
  .title-icon {
    height: 40px;
  }
  
  .artistic-title {
    font-size: 32px;
  }
  
  .subtitle {
    font-size: 14px;
    margin-bottom: 24px;
  }
  
  .search-input :deep(.ant-input) {
    height: 42px;
    font-size: 14px;
  }
  
  .search-input :deep(.ant-input-search-button) {
    height: 42px;
    width: 90px;
  }
  
  .hero-stats {
    flex-direction: column;
    gap: 16px;
  }
  
  .stat-divider {
    display: none;
  }
  
  .tag-label {
    margin-right: 0;
    width: 100%;
    margin-bottom: 10px;
  }
}
</style>
