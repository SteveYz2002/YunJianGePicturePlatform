<template>
  <div id="editPasswordPage">
    <h2>修改密码</h2>
    <div style="width: 50%; margin: 50px auto" class="card">
      <div>
        <a-form :model="formState" name="basic" autocomplete="off" label-width="80px" style="padding: 20px; margin-top: 20px" >
          <a-form-item
            :rules="[{ required: true, message: '请输入原密码', trigger: 'blur' }]"
            label="原密码"
            name="userPassword"
          >
            <a-input-password v-model:value="formState.userPassword" autocomplete="off" placeholder="请输入原密码" />
          </a-form-item>
          <a-form-item
            :rules="[
          { required: true, message: '请输入新密码' ,trigger: 'blur' },
          { min: 8, message: '密码不能小于 8 位' },
        ]"
            label="新密码"
            name="newPassword"
          >
            <a-input-password v-model:value="formState.newPassword" autocomplete="off" placeholder="请输入新密码" />
          </a-form-item>
          <a-form-item
            :rules="[
          { required: true, message: '请输入确认密码' ,trigger: 'blur' },
          { min: 8, message: '密码不能小于 8 位' },
        ]"
            label="确认密码"
            name="checkPassword"
          >
            <a-input-password v-model:value="formState.checkPassword" autocomplete="off" placeholder="请确认密码" />
          </a-form-item>
          <div style="text-align: center">
            <a-button type="primary" @click="save">保 存</a-button>
          </div>
        </a-form>
      </div>
    </div>
  </div>

</template>


<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { updatePasswordUsingPost, userLogoutUsingPost } from '@/api/userController'
import { useRouter } from 'vue-router'

const router = useRouter()

const formState = reactive<API.UserUpdateRequest>({
  userPassword: '',
  newPassword: '',
  checkPassword: ''
})

const save = async () => {
  try {
    const res = await updatePasswordUsingPost({
      userPassword: formState.userPassword,
      newPassword: formState.newPassword,
      checkPassword: formState.checkPassword
    })

    if (res.data.code === 0) {
      message.success('修改密码成功')
      // 跳转到登录页
      window.location.replace('/user/login')
    } else {
      // 修改失败，显示错误信息
      message.error("修改密码失败，" + res.data.message)
    }
  } catch (error) {
    message.error('请求发生错误，请稍后重试')
    console.error(error)
  }
}

</script>

<style scoped>

</style>
