<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header" v-if="!isLoginPage">
        <GlobalHeader />
      </a-layout-header>
      <a-layout has-sider>
        <GlobalSider class="sider"/>
        <a-layout>
          <a-layout-content class="content">
            <router-view />
          </a-layout-content>
        </a-layout>
      </a-layout>
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
import { computed } from 'vue'

const route = useRoute()
const isLoginPage = computed(() => route.path === '/user/login' || route.path === '/user/register')
</script>

<style scoped>
#basicLayout {
  height: 100vh;
  display: flex;
}

#basicLayout :deep(.ant-layout) {
  height: 100%;
}

#basicLayout .header {
  padding-inline: 0;
  background: transparent;
  color: unset;
  margin-bottom: 1px;
}

#basicLayout .sider {
  height: calc(100vh - 64px); /* 64px是header高度 */
  border-right: none;
}

#basicLayout .sider .ant-layout-sider-children {
  height: 100%;
  display: flex;
  flex-direction: column;
}

#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
}

#basicLayout .content {
  padding: 28px;
  background: linear-gradient(to right, #fefefe, #fff);
  overflow-y: auto;
  flex: 1;
}

#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  text-align: center;
}

#basicLayout :deep(.ant-layout) {
  display: flex;
  flex-direction: column;
}

#basicLayout :deep(.ant-layout-has-sider) {
  flex-direction: row;
  flex: 1;
  overflow: hidden;
}
</style>
