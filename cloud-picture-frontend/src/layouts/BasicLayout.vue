<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header" v-if="!isLoginPage">
        <GlobalHeader />
      </a-layout-header>
      
      <!-- 主体内容布局，将侧边栏移到里面 -->
      <a-layout class="main-layout">
        <!-- 内容区域，根据登录状态和页面类型决定是否留出侧边栏的位置 -->
        <a-layout-content 
          :class="['content', isLoginPage ? 'login-page-content' : '']" 
          :style="{ marginLeft: !isUserLoggedIn || isLoginPage ? '0' : siderWidth + 'px' }"
        >
          <router-view />
        </a-layout-content>
        
        <!-- 侧边栏在布局结构上独立，只在登录状态下显示 -->
        <GlobalSider v-if="isUserLoggedIn && !isLoginPage" class="sider" @collapse-change="handleSiderCollapse"/>
      </a-layout>
      
      <!-- 页脚，占据全宽，不受侧边栏影响 -->
      <a-layout-footer class="footer">
        <a href="https://github.com/SteveYz2002" target="_blank"> by Steve.Yz </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from '@/components/GlobalSider.vue'
import { useRoute } from 'vue-router'
import { computed, ref } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const route = useRoute()
const loginUserStore = useLoginUserStore()
const isLoginPage = computed(() => route.path === '/user/login' || route.path === '/user/register')
const isUserLoggedIn = computed(() => loginUserStore.loginUser && loginUserStore.loginUser.id)

// 侧边栏宽度状态
const siderWidth = ref(240)

// 处理侧边栏折叠状态变化
const handleSiderCollapse = (collapsed: boolean) => {
  siderWidth.value = collapsed ? 0 : 240
}
</script>

<style scoped>
#basicLayout {
  height: 100vh;
  width: 100%;
  display: flex;
  overflow: hidden;
}

#basicLayout :deep(.ant-layout) {
  height: 100%;
  width: 100%;
}

#basicLayout .header {
  padding-inline: 0;
  background: transparent;
  color: unset;
  margin-bottom: 1px;
  width: 100%;
  z-index: 1001; /* 确保顶部栏在最上层 */
}

.main-layout {
  position: relative;
  flex: 1; /* 使内容区域占满除header和footer外的空间 */
  overflow: hidden;
}

#basicLayout .sider {
  position: fixed;
  left: 0;
  top: 64px; /* header高度 */
  height: calc(100vh - 64px - 41px); /* 减去header和footer的高度 */
  border-right: none;
  transition: all 0.3s ease;
  z-index: 1000;
}

#basicLayout .content {
  padding: 20px; /* 统一内边距 */
  background: linear-gradient(to right, #fefefe, #fff);
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  width: 100%;
  height: 100%;
  transition: margin-left 0.3s ease;
}

/* 登录/注册页面特殊样式 */
.main-layout .content.login-page-content {
  display: flex;
  justify-content: center;
  padding: 0;
  background: transparent;
}

#basicLayout .footer {
  background: #efefef;
  padding: 12px; /* 减少页脚高度 */
  text-align: center;
  width: 100%;
  position: relative; /* 确保footer位于正确的位置 */
  z-index: 1001; /* 确保footer显示在侧边栏之上 */
}

#basicLayout :deep(.ant-layout) {
  display: flex;
  flex-direction: column;
}

#basicLayout :deep(.ant-layout-has-sider) {
  flex-direction: row;
  flex: 1;
  overflow: hidden;
  width: 100%;
}

/* 响应式调整 */
@media (max-width: 768px) {
  #basicLayout .content {
    padding: 15px;
  }
}

@media (max-width: 576px) {
  #basicLayout .content {
    padding: 10px;
  }
}
</style>
