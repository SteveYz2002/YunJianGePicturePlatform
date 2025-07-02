<template>
  <div id="globalSider">
    <a-layout-sider
      v-if="loginUserStore.loginUser.id"
      class="sider"
      width="200"
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :collapsed-width="0"
    >
      <a-menu
        mode="inline"
        v-model:selectedKeys="current"
        :items="menuItems"
        @click="doMenuClick"
      />
    </a-layout-sider>
    <a-button
      v-if="loginUserStore.loginUser.id"
      class="trigger-button"
      type="primary"
      @click="toggleCollapsed"
      :class="{ 'trigger-collapsed': collapsed }"
    >
      <MenuUnfoldOutlined v-if="collapsed" />
      <MenuFoldOutlined v-else />
    </a-button>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref, watchEffect } from 'vue'
import { PictureOutlined, UserOutlined, TeamOutlined, MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'
import { listMyTeamSpaceUsingPost } from '@/api/spaceUserController.ts'
import { message } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()
const collapsed = ref(false)

const toggleCollapsed = () => {
  collapsed.value = !collapsed.value
}

// 固定菜单列表
const fixedMenuItems = [
  {
    key: '/',
    label: '公共图库',
    icon: () => h(PictureOutlined)
  },
  {
    key: '/my_space',
    label: '我的空间',
    icon: () => h(UserOutlined)
  },
  {
    key: '/add_space?type=' + SPACE_TYPE_ENUM.TEAM,
    label: '创建团队',
    icon: () => h(TeamOutlined)
  }
]

const teamSpaceList = ref<API.SpaceUserVO[]>([])

const menuItems = computed(() => {
  // 如果没有团队空间，只展示固定菜单
  if(teamSpaceList.value.length < 1){
    return fixedMenuItems
  }
  // 如果有团队空间，则展示固定菜单和团队空间菜单
  // 展示团队空间分组
  const teamSpaceSubMenus = teamSpaceList.value.map((spaceUser) => {
    const space = spaceUser.space
    return {
      key: '/space/' + spaceUser.spaceId,
      label: space?.spaceName,
    }
  })
  const teamSpaceMenuGroup = {
    type: 'group',
    label: '我的团队',
    key: 'teamSpace',
    children: teamSpaceSubMenus,
  }
  return [...fixedMenuItems, teamSpaceMenuGroup]
})

// 加载团队空间列表
const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.data.code === 0 && res.data.data) {
    teamSpaceList.value = res.data.data
  } else {
    message.error('加载我的团队空间失败，' + res.data.message)
  }
}

/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  // 登录才加载
  if (loginUserStore.loginUser.id) {
    fetchTeamSpaceList()
  }
})

const router = useRouter()

// 当前选中菜单
const current = ref<string[]>([])
// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, failure) => {
  current.value = [to.path]
})

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}

</script>

<style scoped>
#globalSider {
  height: 100%;
}

#globalSider .sider {
  background: linear-gradient(180deg, #E3F2FD 0%, #BBDEFB 100%);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  border-right: 1px solid #e0e0e0;
  height: 100%;
  overflow-y: auto;
}


#globalSider :deep(.ant-menu) {
  background: transparent;
  border-right: none;
}

#globalSider :deep(.ant-menu-item) {
  margin: 8px 12px;
  border-radius: 6px;
  height: 40px;
  line-height: 40px;
  transition: all 0.3s ease;
}

#globalSider :deep(.ant-menu-item:hover) {
  background-color: rgba(100, 181, 246, 0.1);
}

#globalSider :deep(.ant-menu-item-selected) {
  background-color: #64B5F6 !important;
  color: white;
  font-weight: 500;
}

#globalSider :deep(.ant-menu-item-selected .anticon) {
  color: white;
}

#globalSider :deep(.ant-menu-item .anticon) {
  font-size: 16px;
}

#globalSider :deep(.ant-menu-item-group-title) {
  padding: 12px 16px 8px;
  color: #5c6b77;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

#globalSider :deep(.ant-menu-item-group-list .ant-menu-item) {
  padding-left: 32px !important;
  font-size: 14px;
}

#globalSider :deep(.ant-layout-sider-zero-width-trigger) {
  top: 12px;
  right: -32px;
  width: 32px;
  height: 32px;
  border-radius: 0 4px 4px 0;
  background: #64B5F6;
}

.trigger-button {
  position: absolute;
  bottom: 64px;
  left: 200px;
  width: 32px;
  height: 32px;
  font-size: 16px;
  text-align: center;
  border-radius: 0 4px 4px 0;
  transition: all 0.3s;
  z-index: 100;
}

.trigger-collapsed {
  left: 0;
}
</style>
