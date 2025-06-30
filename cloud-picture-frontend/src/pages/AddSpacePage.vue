<template>
  <div id="addSpacePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改' : '创建' }}{{SPACE_TYPE_MAP[spaceType]}}
    </h2>
    <!--空间上传表单-->
    <a-form layout="vertical" :model="spaceForm" @finish="handleSubmit" class="card">
      <a-form-item label="空间名称" name="spaceName">
        <a-input v-model:value="spaceForm.spaceName" placeholder="请输入空间名称" allow-clear />
      </a-form-item>
      <a-form-item label="空间级别" name="spaceLevel">
        <a-select
          v-model:value="spaceForm.spaceLevel"
          :options="SPACE_LEVEL_OPTIONS"
          placeholder="请选择空间级别"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" style="width: 100%">提交</a-button>
      </a-form-item>
    </a-form>
    <!-- 空间级别介绍 -->
    <a-card title="空间级别介绍">
      <a-typography-paragraph>
        * 目前仅支持开通普通版，如需升级，请联系
        <a href="https://github.com/SteveYz2002" target="_blank">Steve.Yz</a>
      </a-typography-paragraph>
      <a-typography-paragraph v-for="spaceLevel in spaceLevelList">
        {{ spaceLevel.text }}：大小 {{ formatSize(spaceLevel.maxSize) }}，数量 {{ spaceLevel.maxCount }}
      </a-typography-paragraph>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  addSpaceUsingPost, updateSpaceUsingPost, getSpaceVoByIdUsingGet, listSpaceLevelUsingGet
} from '@/api/spaceController.ts'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { SPACE_LEVEL_OPTIONS, SPACE_TYPE_ENUM, SPACE_TYPE_MAP } from '@/constants/space.ts'
import { formatSize } from '@/utils'


const space = ref<API.SpaceVO>()

const spaceForm = reactive<API.SpaceAddRequest | API.SpaceEditRequest>({})

const spaceLevelList = ref<API.SpaceLevel[]>([])

const router = useRouter()

const loading = ref(false)

const route = useRoute()

// 空间类别，默认为私有空间
const spaceType = computed(() => {
  if(route.query?.type){
    return Number(route.query.type)
  }else {
    return SPACE_TYPE_ENUM.PRIVATE
  }
})

const fetchSpaceLevelList = async () => {
  const res = await listSpaceLevelUsingGet()
  if (res.data.code === 0 && res.data.data) {
    spaceLevelList.value = res.data.data
  } else {
    message.error('加载空间级别失败，' + res.data.message)
  }
}

onMounted(() => {
  fetchSpaceLevelList()
})

/**
 * 表单提交
 */
const handleSubmit = async (values: any) => {
  const spaceId = space.value?.id;
  let res
  if(spaceId){
    res = await updateSpaceUsingPost({
      id: spaceId,
      ...spaceForm
    })
  }else {
    loading.value = true
    res = await addSpaceUsingPost({
      spaceType : spaceType.value,
      ...spaceForm
    })
  }
  // 操作成功
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    // 跳转到空间详情页
    router.push({
      path: `/space/${res.data.data}`
    })
  } else {
    message.error('操作失败' + res.data.message)
  }
  loading.value = false
}

// 获取老数据
const getOldData = async () => {
  const id = route.query.id
  if (id) {
    const res = await getSpaceVoByIdUsingGet({
      id: id
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      space.value = data
      // 填充表单
      spaceForm.spaceName = data.spaceName
      spaceForm.spaceLevel = data.spaceLevel
    } else {
      message.error('加载空间失败，' + res.data.message)
    }
  }
}

onMounted(() => {
  getOldData()
})

</script>

<style scoped>
#addSpacePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
