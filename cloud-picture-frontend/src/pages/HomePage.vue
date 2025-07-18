<template>
  <div id="homePage" class="home-page" ref="homePageRef">
    <!-- 标题区域 - 增强设计 -->
    <div class="hero-container">
      <div class="title-content">
        <img src="../assets/logo.png" alt="网站图标" class="title-icon" />
        <h1 class="artistic-title">云笺阁</h1>
      </div>
      <div class="subtitle">企业级云图库平台</div>
      
      <!-- 搜索框改进，增加视觉焦点 -->
      <div class="search-bar-container">
        <a-input-search
          placeholder="从海量图片中搜索"
          v-model:value="searchParams.searchText"
          enter-button
          size="large"
          @search="doSearch"
        >
          <template #enterButton>
            <a-button type="primary" size="large">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
          </template>
        </a-input-search>
      </div>
    </div>
    
    <!-- 内容区域 -->
    <div class="content-container" ref="contentRef">
      <!-- 筛选栏 - 改进样式 -->
      <div class="filter-container">
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
            <a-button @click="loadMore" type="primary" ghost>加载更多</a-button>
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
#homePage {
  min-height: 100%; /* 填充整个可用空间 */
  background-color: #f5f7fa;
  padding-bottom: 0;
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
}

/* 标题英雄区域样式 */
.hero-container {
  text-align: center;
  padding: 30px 0; /* 减少上下内边距 */
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
  border-radius: 0 0 30px 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  flex-shrink: 0; /* 防止压缩 */
}

.title-content {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.title-icon {
  width: 54px;
  height: 54px;
  object-fit: contain;
  filter: drop-shadow(0 0 8px rgba(33, 150, 243, 0.3));
  animation: float 6s ease-in-out infinite;
}

.artistic-title {
  font-size: 54px;
  font-family: "华文行楷", "楷体", cursive;
  background: linear-gradient(45deg, #2196F3, #673AB7);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 2px 2px 8px rgba(33, 150, 243, 0.3);
  letter-spacing: 5px;
  margin: 0 0 0 16px;
  padding: 0;
  animation: glow 6s ease-in-out infinite;
}

.subtitle {
  font-size: 18px;
  font-weight: 300;
  color: #5e6c84;
  font-family: "Microsoft YaHei", sans-serif;
  letter-spacing: 3px;
  margin-bottom: 32px;
  animation: fadeIn 1.5s ease-out;
}

/* 搜索框容器样式 */
.search-bar-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 0 20px;
}

.search-bar-container :deep(.ant-input) {
  height: 48px;
  border-radius: 24px 0 0 24px;
  padding-left: 20px;
  font-size: 16px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-bar-container :deep(.ant-input:focus) {
  box-shadow: 0 3px 12px rgba(33, 150, 243, 0.2);
}

.search-bar-container :deep(.ant-input-group-addon) {
  background: transparent;
  border: none;
}

.search-bar-container :deep(.ant-btn) {
  height: 48px;
  border-radius: 0 24px 24px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 内容区域样式 */
.content-container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 20px 0; /* 移除所有底部内边距 */
  flex: 1;
  display: flex;
  flex-direction: column;
  width: 100%;
}

/* 筛选栏样式 */
.filter-container {
  background-color: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  flex-shrink: 0; /* 防止压缩 */
}

/* 分类标签页样式 */
.category-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 16px;
}

.category-tabs :deep(.ant-tabs-tab) {
  padding: 8px 16px;
  font-size: 15px;
}

.category-tabs :deep(.ant-tabs-tab-active) {
  font-weight: 600;
}

.category-tabs :deep(.ant-tabs-ink-bar) {
  height: 3px;
  border-radius: 3px;
  background: linear-gradient(90deg, #2196F3, #673AB7);
}

/* 标签栏样式 */
.tag-bar {
  display: flex;
  align-items: flex-start;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.tag-label {
  font-weight: 500;
  margin-right: 12px;
  color: #5e6c84;
  margin-top: 4px;
}

.custom-tag {
  margin-right: 8px;
  margin-bottom: 8px;
  padding: 5px 10px;
  border-radius: 6px;
  transition: all 0.3s;
}

.custom-tag:hover {
  transform: translateY(-2px);
}

/* 图片区容器样式 */
.pictures-container {
  margin-bottom: 20px;
  padding: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative; /* 为内部绝对定位元素提供参考 */
}

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 0;
  margin: 0;
  flex-shrink: 0; /* 防止压缩 */
}

/* 加载状态样式 */
.load-more-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
  width: 100%;
  position: relative;
  clear: both;
  margin-top: 20px;
}

.loading-indicator, .no-more-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
  padding: 15px 0;
}

.loading-text {
  margin-left: 10px;
}

.no-more-indicator {
  padding: 10px 0;
  color: #999;
}

/* 新增加载更多按钮样式 */
.load-more-button-wrapper {
  margin: 10px 0;
  display: flex;
  justify-content: center;
}

/* 动画效果 */
@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0);
  }
  50% {
    transform: translateY(-12px) rotate(5deg);
  }
}

@keyframes glow {
  0%, 100% {
    text-shadow: 2px 2px 8px rgba(33, 150, 243, 0.3);
  }
  50% {
    text-shadow: 2px 2px 16px rgba(33, 150, 243, 0.6);
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .artistic-title {
    font-size: 42px;
  }
  
  .title-icon {
    width: 42px;
    height: 42px;
  }
  
  .subtitle {
    font-size: 16px;
    letter-spacing: 2px;
  }
  
  .hero-container {
    padding: 30px 0;
  }
  
  .search-bar-container :deep(.ant-input),
  .search-bar-container :deep(.ant-btn) {
    height: 42px;
  }
}

@media (max-width: 480px) {
  .artistic-title {
    font-size: 36px;
  }
  
  .title-icon {
    width: 36px;
    height: 36px;
  }
  
  .hero-container {
    padding: 24px 0;
  }
  
  .tag-bar {
    flex-direction: column;
  }
  
  .tag-label {
    margin-bottom: 8px;
  }
}
</style>
