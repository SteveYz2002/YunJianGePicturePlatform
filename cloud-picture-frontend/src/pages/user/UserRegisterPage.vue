<template>
  <div class="login-container">
    <div id="userRegisterPage" class="login-box">
      <h2 class="title">云图库 - 用户注册</h2>
      <div class="desc">企业级智能协同云图库</div>
      <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
        <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
        </a-form-item>
        <a-form-item
          name="userPassword"
          :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码不能小于 8 位' },
        ]"
        >
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
        </a-form-item>
        <a-form-item
          name="checkPassword"
          :rules="[
          { required: true, message: '请确认密码' },
          { min: 8, message: '确认密码不能小于 8 位' },
        ]"
        >
          <a-input-password v-model:value="formState.checkPassword" placeholder="请确认密码" />
        </a-form-item>
        <div class="tips">
          已有账号？
          <RouterLink to="/user/login">去登录</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>

</template>


<script setup lang="ts">
import { reactive } from 'vue'
import { userRegisterUsingPost } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import router from '@/router'

// 用于接受表单输入的值
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
})

const loginUserStore = useLoginUserStore()

/**
 * 表单提交
 * @param values
 */
const handleSubmit = async (values: any) => {
  //校验两次输入的代码是否一致
  if(formState.userPassword !== formState.checkPassword){
    message.error('两次输入的密码不一致')
    return
  }
  const res = await userRegisterUsingPost(values)
  // 注册成功，跳转到登录页
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    await router.push({ path: '/user/login', replace: true })
  } else {
    message.error('注册失败,' + res.data.message)
  }
}

</script>

<style scoped>
#userRegisterPage {
  max-width: 360px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}

.login-container {
  height: 70vh;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fdfdfd;
}

.login-box {
  position: absolute;
  width: 350px;
  padding: 30px;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 0, 0, .1);
  background-color: rgba(255, 255, 255, .5);
}
</style>
