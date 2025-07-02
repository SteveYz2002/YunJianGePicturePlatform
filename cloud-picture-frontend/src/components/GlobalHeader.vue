<template>
  <div id="globalHeader" class="global-header">
    <a-row :wrap="false">
      <a-col flex="240px">
        <router-link to="/">
          <div class="title-bar">
            <img src="../assets/logo.png" alt="logo" class="logo" />
            <div class="title-container">
              <div class="title">云笺阁</div>
              <div class="subtitle">云图库平台</div>
            </div>
          </div>
        </router-link>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
          style="margin-top: 10px"
        />
      </a-col>
      <!--用户信息展示栏-->
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <!--鼠标悬浮展示下拉框-->
            <a-dropdown>
              <a-space>
                <a-avatar
                  :src="loginUserStore.loginUser.userAvatar"
                  :style="{ marginRight: '16px' }"
                />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <!-- 展示用户头像-->

              <template #overlay>
                <a-menu>
                  <a-menu-item>
                    <router-link to="/my_space">
                      <UserOutlined />
                      我的空间
                    </router-link>
                  </a-menu-item>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                  <a-menu-item>
                    <EditFilled />
                    <a href="/editPassword"> 修改密码</a>
                  </a-menu-item>
                  <a-menu-item>
                    <InfoCircleOutlined />
                    <a href="/personal_info"> 个人信息</a>
                  </a-menu-item>
                </a-menu>
                <!-- todo 个人信息-->
              </template>
            </a-dropdown>

          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import { HomeOutlined, LogoutOutlined, UserOutlined, EditFilled, InfoCircleOutlined } from '@ant-design/icons-vue'
import { type MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogoutUsingPost } from '@/api/userController.ts'

const loginUserStore = useLoginUserStore()

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
    if (menu?.key?.startsWith('/admin')) {
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
.global-header {
  background: linear-gradient(135deg, #87CEEB 0%, #64B5F6 100%);
  transition: all 0.3s ease;
  padding: 0 24px;
  color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.global-header :deep(.ant-menu-horizontal) {
  background-color: transparent;
  border-bottom: none;
  color: #fff;
  line-height: 64px;
}

.global-header :deep(.ant-menu-horizontal > .ant-menu-item),
.global-header :deep(.ant-menu-horizontal > .ant-menu-submenu) {
  padding: 0 12px;
  font-weight: 500;
}

.global-header :deep(.ant-menu) {
  line-height: 46px;
  height: 46px;
}

.global-header :deep(.ant-menu-horizontal) {
  height: 46px;
}

.global-header :deep(.ant-menu-horizontal > .ant-menu-item:hover),
.global-header :deep(.ant-menu-horizontal > .ant-menu-submenu:hover) {
  color: #fff;
  border-bottom-color: #fff;
  background: rgba(255, 255, 255, 0.1);
}

.global-header :deep(.ant-menu-horizontal > .ant-menu-item-selected) {
  color: #fff;
  border-bottom-color: #fff;
  background: rgba(255, 255, 255, 0.15);
}

#globalHeader .title-bar {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 8px;
}

.title-container {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-left: 12px;
  white-space: nowrap; /* 防止换行 */
}

.title {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.3px;
  line-height: 1.2;
}

.subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin-left: 8px;
  position: relative;
  padding-left: 8px;
}

.subtitle::before {
  content: '|';
  position: absolute;
  left: 0;
  color: rgba(255, 255, 255, 0.5);
}

.logo {
  height: 40px;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.05);
}

.user-login-status {
  display: flex;
  align-items: center;
  height: 64px;
}

.user-login-status :deep(.ant-avatar) {
  border: 2px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.user-login-status :deep(.ant-avatar:hover) {
  border-color: rgba(255, 255, 255, 0.4);
  transform: scale(1.05);
}

.user-login-status :deep(.ant-space) {
  color: #fff;
}

.user-login-status :deep(.ant-btn-primary) {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  font-weight: 500;
}

.user-login-status :deep(.ant-btn-primary:hover) {
  background: rgba(255, 255, 255, 0.3);
}
</style>
