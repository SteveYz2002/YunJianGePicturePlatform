<template>
  <div id="globalSider" :class="{ 'sider-collapsed': collapsed }">
    <a-layout-sider
      v-if="loginUserStore.loginUser.id"
      class="sider"
      width="240"
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :collapsed-width="0"
    >
      <div class="sider-inner">
        <!-- 用户信息展示 -->
        <div class="user-profile">
          <a-avatar 
            :src="loginUserStore.loginUser.userAvatar" 
            :size="48"
            class="user-avatar"
          />
          <div class="user-info">
            <div class="user-name">{{ loginUserStore.loginUser.userName || '无名用户' }}</div>
            <div class="user-role">{{ getUserRole(loginUserStore.loginUser.userRole) }}</div>
          </div>
        </div>
        
        <!-- 分割线 -->
        <div class="divider"></div>
        
        <!-- 主要内容区域 -->
        <div class="sider-content">
          <!-- 导航菜单 -->
          <a-menu
            mode="inline"
            v-model:selectedKeys="current"
            :items="menuItems"
            @click="doMenuClick"
            class="side-menu"
          />
        </div>
      </div>
      
      <!-- 快捷操作区域 - 绝对定位在底部 -->
      <div class="quick-actions">
        <a-tooltip placement="right" title="创建图片">
          <a-button 
            type="primary" 
            shape="circle" 
            class="quick-action-btn create-btn"
            @click="router.push('/add_picture')"
          >
            <template #icon><PlusOutlined /></template>
          </a-button>
        </a-tooltip>
        
        <a-tooltip placement="right" title="个人设置">
          <a-button 
            shape="circle" 
            class="quick-action-btn settings-btn"
            @click="router.push('/personal_info')"
          >
            <template #icon><SettingOutlined /></template>
          </a-button>
        </a-tooltip>
      </div>
    </a-layout-sider>
    
    <!-- 折叠按钮 -->
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
import { 
  PictureOutlined, 
  UserOutlined, 
  TeamOutlined, 
  MenuFoldOutlined, 
  MenuUnfoldOutlined,
  HomeOutlined,
  PlusOutlined,
  SettingOutlined,
  StarOutlined,
  FolderOutlined,
  GlobalOutlined
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { SPACE_TYPE_ENUM } from '@/constants/space.ts'
import { listMyTeamSpaceUsingPost } from '@/api/spaceUserController.ts'
import { message } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()
const collapsed = ref(false)

// 定义事件
const emit = defineEmits(['collapse-change'])

const toggleCollapsed = () => {
  collapsed.value = !collapsed.value
  // 发射折叠状态变化事件
  emit('collapse-change', collapsed.value)
}

// 监听折叠状态变化
watchEffect(() => {
  emit('collapse-change', collapsed.value)
})

// 获取用户角色名称
const getUserRole = (role: string | undefined): string => {
  if (role === 'admin') return '管理员'
  if (role === 'user') return '普通用户'
  return '访客'
}

// 固定菜单列表
const fixedMenuItems = [
  {
    key: '/',
    label: '发现',
    icon: () => h(GlobalOutlined),
    title: '发现热门图片'
  },
  {
    key: '/my_space',
    label: '我的空间',
    icon: () => h(FolderOutlined),
    title: '管理我的个人空间'
  },
  {
    key: '/add_space?type=' + SPACE_TYPE_ENUM.TEAM,
    label: '创建团队',
    icon: () => h(TeamOutlined),
    title: '创建一个新的团队空间'
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
      icon: () => h('span', { class: 'team-space-icon' }, space?.spaceName?.[0] || 'T')
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

// 当前选中菜单
const current = ref<string[]>([])

// 手动设置当前选中菜单
const setSelectedMenu = (key: string) => {
  current.value = [key]
}

// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, failure) => {
  // 获取基本路径，去掉查询参数
  const path = to.path
  
  // 特殊处理：如果是从"我的空间"页面跳转来的，保持"我的空间"菜单选中状态
  if (from.path === '/my_space') {
    current.value = ['/my_space']
    return
  }
  
  // 对于菜单项带有查询参数的情况，需要特殊处理
  if (path === '/add_space' && to.fullPath.includes('?type=')) {
    current.value = [to.fullPath.split('?')[0] + '?type=' + SPACE_TYPE_ENUM.TEAM]
  } else {
    // 检查路径是否在固定菜单中
    const fixedMenuItem = fixedMenuItems.find(item => item.key === path)
    if (fixedMenuItem) {
      current.value = [fixedMenuItem.key]
    } else {
      // 检查是否在团队空间菜单中
      const isTeamSpace = path.startsWith('/space/')
      if (isTeamSpace) {
        current.value = [path]
      } else {
        // 其他情况使用完整路径
        current.value = [path]
      }
    }
  }
})

// 路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  // 先设置选中状态，再进行路由跳转
  // 这样即使页面重定向，选中状态也会保持
  setSelectedMenu(key)
  router.push(key)
}
</script>

<style scoped>
#globalSider {
  height: 100%;
  position: relative;
}

/* 侧边栏主容器 */
#globalSider .sider {
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  height: 100%;
  overflow: hidden;
  transition: all 0.3s ease;
  position: relative; /* 为绝对定位的子元素提供参考 */
}

/* 内部容器 */
.sider-inner {
  height: calc(100% - 80px); /* 减去底部快捷操作区的高度 */
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 用户资料区域 */
.user-profile {
  padding: 24px 16px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.user-avatar {
  border: 2px solid rgba(24, 144, 255, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-info {
  margin-left: 12px;
  overflow: hidden;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}

.user-role {
  font-size: 12px;
  color: #999;
}

.divider {
  height: 1px;
  margin: 0;
  background-color: rgba(0, 0, 0, 0.05);
}

/* 主要内容区域 - 可滚动 */
.sider-content {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding-bottom: 16px; /* 为底部按钮留出额外空间 */
}

/* 菜单区域 */
.side-menu {
  border-right: none !important;
  padding: 16px 0;
}

/* 团队空间图标 */
:deep(.team-space-icon) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  color: white;
  font-size: 12px;
  font-weight: 500;
  margin-right: 10px;
}

/* 菜单样式 */
#globalSider :deep(.ant-menu) {
  background: transparent;
  border-right: none;
}

#globalSider :deep(.ant-menu-item) {
  margin: 4px 16px;
  border-radius: 8px;
  height: 46px;
  line-height: 46px;
  padding-left: 16px !important;
  transition: all 0.3s ease;
  box-shadow: none !important; /* 确保所有菜单项没有阴影 */
}

#globalSider :deep(.ant-menu-item:hover) {
  background-color: rgba(24, 144, 255, 0.06);
  color: #1890ff;
  box-shadow: none !important; /* 确保悬停时没有阴影 */
}

/* 选中菜单项样式 - 简化效果 */
#globalSider :deep(.ant-menu-inline .ant-menu-item-selected),
#globalSider :deep(.ant-menu-item.ant-menu-item-selected) {
  background: linear-gradient(90deg, rgba(24, 144, 255, 0.1), rgba(24, 144, 255, 0.2)) !important;
  color: #1890ff !important;
  font-weight: 500;
  position: relative;
  box-shadow: none !important; /* 强制移除所有阴影 */
}

/* 选中项左侧指示条 */
#globalSider :deep(.ant-menu-item-selected::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 4px;
  background: #1890ff;
  border-radius: 0 4px 4px 0;
}

#globalSider :deep(.ant-menu-item .anticon) {
  font-size: 18px;
  margin-right: 12px;
}

#globalSider :deep(.ant-menu-item-selected .anticon) {
  color: #1890ff;
}

#globalSider :deep(.ant-menu-item-group-title) {
  padding: 16px 24px 8px;
  color: #8c8c8c;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

#globalSider :deep(.ant-menu-item-group-list .ant-menu-item) {
  padding-left: 24px !important;
  font-size: 14px;
}

/* 快捷操作区 - 绝对定位在底部 */
.quick-actions {
  padding: 16px;
  display: flex;
  justify-content: space-around;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  background-color: #ffffff;
  position: absolute;
  bottom: 80px; /* 向上移动，避免被底部元素遮挡 */
  left: 0;
  right: 0;
  z-index: 10;
  bottom: 10px;
  box-shadow: none; /* 移除底部区域的阴影 */
}

.quick-action-btn {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.quick-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.create-btn {
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  border: none;
}

.settings-btn {
  background: #f0f0f0;
  border: none;
  color: #666;
}

/* 折叠按钮 */
.trigger-button {
  position: absolute;
  bottom: 70px;
  left: 240px;
  width: 32px;
  height: 32px;
  font-size: 16px;
  text-align: center;
  border-radius: 0 4px 4px 0;
  box-shadow: 4px 0 8px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #1890ff, #36cfc9);
  border: none;
  transition: all 0.3s;
  z-index: 100;
}

.trigger-collapsed {
  left: 0;
}
</style>
