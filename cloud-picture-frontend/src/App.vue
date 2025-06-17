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

// const loginUserStore = useLoginUserStore()
// loginUserStore.fetchLoginUser()
//
// healthUsingGet().then((res)=>{
//   console.log(res)
// })

</script>

<style scoped>

</style>
