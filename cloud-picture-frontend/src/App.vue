<template>
  <ConfigProvider :locale="defaultLocale">
    <div id="app">
      <BasicLayout/>
    </div>
  </ConfigProvider>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import { healthUsingGet } from '@/api/mainController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { ConfigProvider } from 'ant-design-vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'

const defaultLocale = zhCN;

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()

healthUsingGet().then((res)=>{
  console.log(res)
})

</script>

<style scoped>
#app {
  background-color: #f5f7fa;
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden; /* 防止水平滚动条 */
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background-color: #f5f7fa;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background-color: #909399;
}

::-webkit-scrollbar-corner {
  background-color: #f5f7fa;
}
</style>
