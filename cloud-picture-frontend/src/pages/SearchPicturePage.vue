<template>
  <div id="searchPicturePage" class="search-picture-page">
    <div class="header-section tech-card">
      <h2 class="page-title">以图搜图</h2>
      <p class="page-subtitle">通过图像找到相似内容</p>
    </div>

    <div class="original-image-section tech-card">
      <h3 class="section-title">原图</h3>
      <div class="image-preview">
        <a-card hoverable class="original-image-card">
          <template #cover>
            <div class="image-container">
              <img
                class="preview-image"
                :alt="picture.name"
                :src="picture.thumbnailUrl ?? picture.url"
              />
              <div class="image-info">
                <span class="image-name">{{ picture.name }}</span>
                <span v-if="picture.category" class="image-category">{{ picture.category }}</span>
              </div>
            </div>
          </template>
        </a-card>
      </div>
    </div>

    <div class="results-section tech-card">
      <div class="results-header">
        <h3 class="section-title">识图结果</h3>
        <div class="results-stats">
          <a-tag color="blue">{{ dataList.length }} 张相似图片</a-tag>
          <a-button 
            type="primary" 
            ghost 
            @click="fetchResultData" 
            :loading="loading"
            class="refresh-button"
          >
            <template #icon><ReloadOutlined /></template>
            刷新结果
          </a-button>
        </div>
      </div>

      <a-empty v-if="!loading && dataList.length === 0" description="暂无搜索结果" />
      
      <!--图片结果列表-->
      <a-list
        class="results-list"
        :grid="{ gutter: 24, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
        :data-source="dataList"
        :loading="loading"
      >
        <template #renderItem="{ item: picture }">
          <a-list-item>
            <a :href="picture.fromUrl" target="_blank" class="result-item-link">
              <a-card hoverable class="result-card">
                <template #cover>
                  <div class="image-container">
                    <img
                      class="result-image"
                      :alt="picture.name"
                      :src="picture.thumbUrl ?? picture.url"
                    />
                    <div class="similarity-badge" v-if="picture.score">
                      {{ (parseFloat(picture.score) * 100).toFixed(1) }}% 相似度
                    </div>
                  </div>
                </template>
                <template #body>
                  <div class="result-card-content">
                    <div class="result-title" :title="picture.name">{{ picture.name || '未知图片' }}</div>
                    <div class="result-source">{{ getSourceName(picture.fromUrl) }}</div>
                  </div>
                </template>
              </a-card>
            </a>
          </a-list-item>
        </template>
      </a-list>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import {
  getPictureVoByIdUsingGet, searchPictureByPictureUsingPost
} from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import { ReloadOutlined } from '@ant-design/icons-vue'

const picture = ref<API.PictureVO>({})

const route = useRoute()

const pictureId = computed(() => {
  return route.query?.pictureId
})

// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: pictureId.value
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('获取图片详情失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error('获取图片详情失败，' + error.message)
  }
}

// 以图搜图结果
const dataList = ref<API.ImageSearchResult[]>([])

const loading = ref<boolean>(true)

// 获取搜图结果
const fetchResultData = async () => {
  loading.value = true
  try {
    const res = await searchPictureByPictureUsingPost({
      pictureId: pictureId.value
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data ?? []
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (error: any) {
    message.error('获取图片数据失败，' + error.message)
  }
  loading.value = false
}

// 提取来源网站名称
const getSourceName = (url: string) => {
  if (!url) return '未知来源';
  try {
    const hostname = new URL(url).hostname;
    return hostname.replace(/^www\./, '');
  } catch (e) {
    return '外部链接';
  }
}

onMounted(() => {
  fetchPictureDetail()
  fetchResultData()
})
</script>

<style scoped>
.search-picture-page {
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
  text-align: center;
  padding: 30px 24px;
}

.page-title {
  font-size: 28px;
  margin-bottom: 8px;
  font-weight: 600;
  background: linear-gradient(90deg, #1677ff, #52c41a);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

.page-subtitle {
  color: #666;
  font-size: 16px;
  margin-bottom: 0;
}

/* Section styles */
.section-title {
  font-size: 18px;
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  height: 18px;
  width: 4px;
  background: linear-gradient(180deg, #1677ff, #52c41a);
  border-radius: 2px;
}

/* Original image section */
.original-image-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.image-preview {
  width: 100%;
  max-width: 300px;
  margin: 0 auto;
}

.original-image-card {
  width: 100%;
  overflow: hidden;
  transition: all 0.3s ease;
}

.original-image-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.image-container {
  position: relative;
  overflow: hidden;
}

.preview-image {
  height: 220px;
  width: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.original-image-card:hover .preview-image {
  transform: scale(1.05);
}

.image-info {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 8px 12px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.image-name {
  max-width: 70%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.image-category {
  font-style: italic;
}

/* Results section */
.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.results-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.refresh-button {
  transition: all 0.3s;
}

.refresh-button:hover {
  transform: translateY(-2px);
}

.results-list {
  margin-top: 16px;
}

.result-item-link {
  display: block;
  text-decoration: none;
  color: inherit;
}

.result-card {
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.result-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.result-image {
  height: 180px;
  object-fit: cover;
  width: 100%;
  transition: transform 0.5s ease;
}

.result-card:hover .result-image {
  transform: scale(1.05);
}

.similarity-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: linear-gradient(45deg, #1677ff, #4096ff);
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.result-card-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 12px;
}

.result-title {
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.result-source {
  color: #888;
  font-size: 12px;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .results-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .tech-card {
    padding: 16px;
  }
  
  .page-title {
    font-size: 24px;
  }
}
</style>
