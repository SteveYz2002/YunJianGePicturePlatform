<template>
  <div id="globalHeader" class="global-header">
    <div class="header-content">
      <!-- 品牌标识区域 -->
      <router-link to="/" class="brand-section">
        <div class="logo-container">
          <img src="../assets/logo.png" alt="logo" class="logo" />
        </div>
        <div class="brand-info">
          <h1 class="brand-name">云笺阁</h1>
          <div class="brand-slogan">企业级云图库平台</div>
        </div>
      </router-link>

      <!-- 导航菜单区域 -->
      <div class="nav-section">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
          class="main-menu"
          :overflowedIndicator="overflowedIndicator"
        />
      </div>

      <!-- 用户信息区域 -->
      <div class="user-section">
        <div v-if="loginUserStore.loginUser.id" class="user-logged-in">
          <a-dropdown placement="bottomRight">
            <div class="user-profile">
              <a-avatar
                :src="loginUserStore.loginUser.userAvatar"
                class="user-avatar"
              />
              <span class="user-name">{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
              <down-outlined class="dropdown-icon" />
            </div>

            <template #overlay>
              <a-menu class="user-menu">
                <a-menu-item class="menu-item">
                  <router-link to="/my_space" class="menu-link">
                    <UserOutlined />
                    <span> 我的空间</span>
                  </router-link>
                </a-menu-item>
                <a-menu-item class="menu-item">
                  <a class="menu-link" href="/personal_info">
                    <InfoCircleOutlined />
                    <span> 个人信息</span>
                  </a>
                </a-menu-item>
                <a-menu-item class="menu-item">
                  <a class="menu-link" href="/editPassword">
                    <EditFilled />
                    <span> 修改密码</span>
                  </a>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item class="menu-item logout" @click="doLogout">
                  <LogoutOutlined />
                  <span> 退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div v-else class="user-not-logged-in">
          <a-button type="primary" class="login-button" href="/user/login">登录</a-button>
          <a-button class="register-button" href="/user/register">注册</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import { 
  HomeOutlined, 
  LogoutOutlined, 
  UserOutlined, 
  EditFilled, 
  InfoCircleOutlined,
  DownOutlined,
  EllipsisOutlined
} from '@ant-design/icons-vue'
import { type MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingPost } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()
const overflowedIndicator = h(EllipsisOutlined)

// 未经滤的菜单项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页'
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片'
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理'
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理'
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理'
  },
  {
    key: 'others',
    label: h('a', { href: 'https://github.com/SteveYz2002', target: '_blank' }, '关于'),
    title: '关于'
  }
]

// 根据权限过滤菜单项
const filteredMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    // 管理员才能访问admin开头的菜单
    if (menu?.key && typeof menu.key === 'string' && menu.key.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示的菜单项
const items = computed(() => {
  return filteredMenus(originItems)
})

// 路由对象
const router = useRouter()

//当前要高亮的菜单项
const current = ref<string[]>(['home'])
//监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

//路由跳转事件
const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key
  })
}

//退出登录
const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录'
    })
    message.success('退出登录成功')
    await router.push({ path: '/user/login', replace: true })
  } else {
    message.error('退出登录失败,' + res.data.message)
  }
}
</script>

<style scoped>
/* 全局顶部栏 */
.global-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.85);
  box-shadow: none; /* 移除阴影效果 */
  transition: all 0.3s ease;
  width: 100%; /* 确保宽度是100% */
}

/* 整体布局调整 */
.header-content {
  width: 100%; /* 使用100%宽度 */
  margin: 0 auto;
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px; /* 左右内边距 */
}

/* 品牌部分 */
.brand-section {
  display: flex;
  align-items: center;
  text-decoration: none;
  transition: all 0.3s ease;
  height: 64px;
  width: 180px; /* 固定宽度 */
  flex: 0 0 auto; /* 不伸缩，保持固定大小 */
}

.brand-section:hover {
  transform: translateY(-2px);
}

.logo-container {
  position: relative;
  display: flex;
  align-items: center;
  flex-shrink: 0; /* 防止缩小 */
}

.logo {
  height: 38px;
  width: auto; /* 保持宽高比 */
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
  transition: transform 0.3s ease;
  flex-shrink: 0; /* 防止缩小 */
}

.brand-info {
  margin-left: 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
  flex-shrink: 0; /* 防止缩小 */
  white-space: nowrap; /* 防止文字换行 */
}

.brand-name {
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(45deg, #2196F3, #673AB7);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin: 0 0 4px 0;
  padding: 0;
  letter-spacing: 0.5px;
  line-height: 1;
  flex-shrink: 0; /* 防止缩小 */
}

.brand-slogan {
  font-size: 12px;
  color: #666;
  margin: 0;
  line-height: 1;
  flex-shrink: 0; /* 防止缩小 */
}

/* 导航部分 */
.nav-section {
  flex: 1; /* 使用1而不是3，让其自然伸缩 */
  display: flex;
  justify-content: center;
  overflow: visible;
  padding: 0; /* 移除内边距 */
  margin: 0 20px; /* 使用外边距代替内边距 */
}

.main-menu {
  background: transparent;
  border-bottom: none;
  line-height: 64px;
  width: 100%; /* 使用100%宽度 */
  min-width: auto; /* 移除最小宽度限制 */
}

/* 确保菜单项不会过早折叠 */
.global-header :deep(.ant-menu) {
  flex-wrap: wrap; /* 允许菜单项换行 */
  width: 100%;
  display: flex;
  justify-content: center;
}

.global-header :deep(.ant-menu-overflow) {
  justify-content: center; /* 居中显示菜单项 */
  width: 100%;
}

.global-header :deep(.ant-menu-overflow-item) {
  flex: none !important; /* 不允许菜单项伸缩，保持原始大小 */
  text-align: center; /* 文本居中 */
  padding: 0 !important;
  margin: 0 10px !important; /* 恢复菜单项间距 */
}

/* 美化折叠子菜单样式，确保它可以正确打开 */
.global-header :deep(.ant-menu-submenu-popup) {
  z-index: 1050 !important; /* 提高层级，确保菜单能显示在其他元素之上 */
}

.global-header :deep(.ant-menu-submenu-popup .ant-menu) {
  background-color: white;
  border-radius: 8px;
  padding: 4px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  min-width: 120px;
}

.global-header :deep(.ant-menu-submenu-popup .ant-menu-item) {
  padding: 10px 16px;
  height: auto;
  line-height: 1.5;
  margin: 4px 0;
  border-radius: 4px;
  color: #333;
}

.global-header :deep(.ant-menu-submenu-popup .ant-menu-item:hover) {
  background-color: rgba(24, 144, 255, 0.06);
  color: #1890ff;
}

/* 保持其他样式不变 */

/* 自适应菜单样式 */
.global-header :deep(.ant-menu-horizontal) {
  border-bottom: none;
}

/* 美化菜单折叠按钮样式 */
.global-header :deep(.ant-menu-horizontal-overflow-item-rest .anticon) {
  font-size: 18px;
  color: #1890ff;
  transition: transform 0.3s;
}

.global-header :deep(.ant-menu-horizontal-overflow-item-rest:hover .anticon) {
  transform: rotate(90deg);
  color: #0c6bc9;
}

/* 美化折叠按钮的背景和样式 */
.global-header :deep(.ant-menu-horizontal-overflow-item-rest) {
  padding: 0 10px;
  margin-left: 5px;
}

/* 美化下拉菜单样式 */
.global-header :deep(.ant-menu-horizontal-overflow-popup .ant-menu) {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
  border-radius: 8px;
  padding: 5px;
}

/* 美化折叠子菜单样式 */
.global-header :deep(.ant-menu-submenu-popup) {
  min-width: 120px;
}

.global-header :deep(.ant-menu-submenu-popup .ant-menu-item) {
  padding: 10px 16px;
  margin: 2px 0;
  border-radius: 4px;
}

.global-header :deep(.ant-menu-submenu-popup .ant-menu-vertical) {
  border-radius: 8px;
  padding: 4px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.global-header :deep(.ant-menu-submenu-popup .ant-menu-item:hover) {
  background-color: rgba(24, 144, 255, 0.06);
  color: #1890ff;
}

.global-header :deep(.ant-menu-submenu-popup .ant-menu-item-selected) {
  background-color: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

/* 用户部分 */
.user-section {
  width: auto;
  min-width: 110px; /* 最小宽度 */
  flex: 0 0 auto; /* 不伸缩，保持固定大小 */
  display: flex;
  justify-content: flex-end;
}

.user-logged-in {
  cursor: pointer;
}

.user-profile {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 40px;
  transition: all 0.3s ease;
  background-color: rgba(240, 242, 245, 0.5);
}

.user-profile:hover {
  background-color: rgba(240, 242, 245, 0.9);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border: 2px solid rgba(24, 144, 255, 0.2);
}

.user-name {
  margin: 0 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  font-size: 12px;
  color: #999;
}

/* 用户菜单 */
.global-header :deep(.ant-dropdown-menu) {
  border-radius: 8px;
  padding: 4px;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.global-header :deep(.ant-dropdown-menu-item) {
  padding: 8px 12px;
  border-radius: 4px;
  margin: 2px 0;
}

.global-header :deep(.menu-link) {
  display: flex;
  align-items: center;
  color: #333;
  text-decoration: none;
}

.global-header :deep(.menu-link span) {
  margin-left: 8px;
}

.global-header :deep(.ant-dropdown-menu-item-divider) {
  margin: 4px 0;
}

.global-header :deep(.logout) {
  color: #ff4d4f;
}

.global-header :deep(.ant-dropdown-menu-item:hover) {
  background-color: rgba(24, 144, 255, 0.06);
}

.global-header :deep(.logout:hover) {
  background-color: rgba(255, 77, 79, 0.06);
}

/* 未登录用户 */
.user-not-logged-in {
  display: flex;
  gap: 10px;
}

.login-button {
  background: linear-gradient(45deg, #1890ff, #36cfc9);
  border: none;
  font-weight: 500;
}

.register-button {
  color: #1890ff;
}

/* 菜单项样式 */
.global-header :deep(.ant-menu-horizontal > .ant-menu-item),
.global-header :deep(.ant-menu-horizontal > .ant-menu-submenu) {
  padding: 0 15px; /* 恢复内边距 */
  margin: 0 5px; /* 添加适当的外边距 */
  font-weight: 500;
  font-size: 15px;
  color: #333;
  border-bottom: 2px solid transparent;
  flex: none; /* 不伸缩，保持原始大小 */
  text-align: center; /* 文本居中 */
}

/* 未选中菜单项的悬停效果 */
.global-header :deep(.ant-menu-horizontal > .ant-menu-item:not(.ant-menu-item-selected):hover),
.global-header :deep(.ant-menu-horizontal > .ant-menu-submenu:not(.ant-menu-submenu-selected):hover) {
  color: #1890ff;
}

/* 已选中菜单项的悬停效果 - 不改变底部边框 */

.global-header :deep(.ant-menu-horizontal > .ant-menu-item-selected) {
  color: #1890ff;
  font-weight: 600;
}

/* 响应式调整 */
@media (min-width: 1200px) {
  .header-content {
    padding: 0 40px; /* 在大屏幕上增加内边距 */
  }
  
  .nav-section {
    margin: 0 40px; /* 在大屏幕上增加外边距 */
  }
  
  .global-header :deep(.ant-menu-horizontal > .ant-menu-item),
  .global-header :deep(.ant-menu-horizontal > .ant-menu-submenu) {
    padding: 0 15px;
    font-size: 15px;
  }
}

@media (max-width: 992px) {
  .brand-section {
    width: 150px; /* 减小品牌区域宽度 */
  }
  
  .nav-section {
    margin: 0 10px; /* 减小导航区域外边距 */
  }
  
  .global-header :deep(.ant-menu-horizontal > .ant-menu-item),
  .global-header :deep(.ant-menu-horizontal > .ant-menu-submenu) {
    padding: 0 10px; /* 减小内边距 */
    margin: 0 3px; /* 减小外边距 */
  }
}

@media (max-width: 768px) {
  .brand-slogan {
    display: none; /* 隐藏品牌标语 */
  }
  
  .brand-section {
    width: auto; /* 自动宽度 */
    min-width: 120px; /* 最小宽度 */
  }
  
  .nav-section {
    margin: 0 5px; /* 进一步减小导航区域外边距 */
  }
  
  .global-header :deep(.ant-menu-horizontal > .ant-menu-item),
  .global-header :deep(.ant-menu-horizontal > .ant-menu-submenu) {
    padding: 0 8px;
    margin: 0 2px;
    font-size: 14px;
  }
  
  .global-header :deep(.ant-menu-overflow-item) {
    margin: 0 5px !important; /* 减小菜单项间距 */
  }
}

@media (max-width: 576px) {
  .user-name {
    display: none;
  }
  
  .dropdown-icon {
    display: none;
  }
  
  .user-profile {
    padding: 2px;
    background-color: transparent;
  }
  
  .login-button,
  .register-button {
    font-size: 12px;
    padding: 0 10px;
  }
}
</style>
