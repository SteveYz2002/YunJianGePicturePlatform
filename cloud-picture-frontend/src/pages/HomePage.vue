<template>
  <div id="homePage" >
    <!-- 标题 -->
    <div class="title-container">
      <div class="title-with-icon">
        <div class="title-content">
          <img src="../assets/logo.png" alt="网站图标" class="title-icon" />
          <h1 class="artistic-title">云笺阁</h1>
        </div>
        <div class="subtitle">企业级云图库平台</div>
      </div>
    </div>
    <!-- 搜索框 -->
    <div class="search-bar">
      <a-input-search
        placeholder="从海量图片中搜索"
        v-model:value="searchParams.searchText"
        enter-button="搜索"
        size="large"
        @search="doSearch"
      />
    </div>
    <div class="card" style="margin-top: 50px">
      <!-- 分类和标签筛选 -->
      <a-tabs v-model:activeKey="selectedCategory" @change="doSearch" >
        <a-tab-pane key="all" tab="全部" />
        <a-tab-pane v-for="category in categoryList" :key="category" :tab="category" />
      </a-tabs>
      <div class="tag-bar">
        <span style="margin-right: 8px">标签：</span>
        <a-space :size="[0, 8]" wrap>
          <a-checkable-tag
            v-for="(tag, index) in tagList"
            :key="tag"
            v-model:checked="selectedTagList[index]"
            @change="doSearch"
          >
            {{ tag }}
          </a-checkable-tag>
        </a-space>
      </div>
      <!-- 图片列表 -->
      <PictureList :show-op="true" :dataList="dataList" :loading="loading" />
      <!-- 分页 -->
      <a-pagination
        style="text-align: right"
        v-model:current="searchParams.current"
        v-model:pageSize="searchParams.pageSize"
        :total="total"
        @change="onPageChange"
      />
    </div>


  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  listPictureTagCategoryUsingGet,
  listPictureVoByPageUsingPost
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PictureList from '@/components/PictureList.vue'

// 表格数据
const dataList = ref<API.PictureVO[]>([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = reactive<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend'
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    ...searchParams,
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
  const res = await listPictureVoByPageUsingPost({
    ...params
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败' + res.data.message)
  }
  loading.value = false
}

onMounted(() => {
  fetchData()
})

const onPageChange = (page: number, pageSize: number) => {
  searchParams.current = page
  searchParams.pageSize = pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  // 重置分页参数
  searchParams.current = 1
  fetchData()
}

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
  } else {
    message.error('获取标签分类列表失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

const router = useRouter()

</script>

<style scoped>
#homePage {
  margin-bottom: 16px;
}

.title-container {
  text-align: center;
  padding: 32px 0;
  margin-bottom: 24px;
}

.title-with-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.title-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.subtitle {
  font-size: 16px;
  color: #666;
  font-family: "Microsoft YaHei", sans-serif;
  letter-spacing: 2px;
  animation: subtitleFade 3s ease-in-out infinite;
  opacity: 0.8;
  margin-top: 8px;
}

@keyframes subtitleFade {
  0%, 100% {
    opacity: 0.8;
    transform: translateY(0);
  }
  50% {
    opacity: 1;
    transform: translateY(-2px);
  }
}

.title-icon {
  width: 48px;
  height: 48px;
  object-fit: contain;
  animation: iconFloat 3s ease-in-out infinite;
}

.artistic-title {
  font-size: 48px;
  font-family: "华文行楷", "楷体", cursive;
  background: linear-gradient(45deg, #2196F3, #E91E63);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: 4px;
  animation: titleFloat 3s ease-in-out infinite;
  margin: 0;
  padding: 0;
}

@keyframes titleFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes iconFloat {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-8px) scale(1.05);
  }
}


#homePage .search-bar {
  max-width: 480px;
  margin: 0 auto 16px;
  padding: 10px;
}
#homePage .tag-bar {
  margin-bottom: 16px;
}

</style>
