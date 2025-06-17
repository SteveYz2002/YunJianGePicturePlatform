import router from '@/router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'

// 定义一个变量来跟踪是否已经获取过登录用户信息
let fistFetchLoginUser = true

/**
 * 路由守卫
 */
router.beforeEach(async (to, from, next) =>{
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser
  // 确保页面首次加载时已经获取到了登录用户信息
  if(fistFetchLoginUser){
    await loginUserStore.fetchLoginUser();
    loginUser = loginUserStore.loginUser
    fistFetchLoginUser = false
  }
  const toUrl = to.fullPath
  // 可以自己定义权限校验逻辑
  if(toUrl.startsWith('/admin')){
    if(!loginUser || loginUser.userRole !== 'admin'){
      message.error('无权限');
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
  }
  next()
})
