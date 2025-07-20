<template>
  <a-modal 
    v-model:visible="visible" 
    :title="title" 
    :footer="null"
    @cancel="closeModal"
    wrapClassName="share-modal-wrapper"
    :maskClosable="true"
  >
    <div class="share-content">
      <div class="share-section">
        <h4 class="section-title">
          <LinkOutlined class="section-icon" />
          分享链接
        </h4>
        <div class="link-container">
          <a-input 
            :value="link" 
            readonly 
            class="share-link-input" 
            :addonAfter="h(CopyOutlined, { onClick: copyLink, class: 'copy-icon' })"
          />
          <div class="link-tips">点击右侧图标复制链接</div>
        </div>
      </div>
      
      <a-divider class="share-divider" />
      
      <div class="share-section">
        <h4 class="section-title">
          <QrcodeOutlined class="section-icon" />
          扫码查看
        </h4>
        <div class="qrcode-container">
          <a-qrcode 
            :value="link" 
            icon="src/assets/logo.png" 
            :size="180"
            errorLevel="M"
            class="qrcode"
          />
          <div class="qrcode-tips">使用手机相机扫描二维码</div>
        </div>
      </div>
      
      <div class="share-actions">
        <a-button type="primary" @click="closeModal" class="close-button">
          完成
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { defineProps, ref, withDefaults, defineExpose, defineEmits, h } from 'vue'
import { 
  LinkOutlined, 
  QrcodeOutlined, 
  CopyOutlined 
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'

/**
 * 定义组件属性类型
 */
interface Props {
  title: string
  link: string
}

/**
 * 给组件指定初始值
 */
const props = withDefaults(defineProps<Props>(), {
  title: '分享图片',
  link:  'https://github.com/SteveYz2002'
})

// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
}

// 复制链接
const copyLink = () => {
  navigator.clipboard.writeText(props.link)
    .then(() => {
      message.success('链接已复制到剪贴板')
    })
    .catch(() => {
      message.error('复制失败，请手动复制')
    })
}

// 定义事件
const emit = defineEmits(['close'])

// 关闭弹窗
const closeModal = () => {
  visible.value = false
  // 触发close事件
  emit('close')
}

// 暴露函数给父组件使用
defineExpose({
  openModal
})

</script>

<style scoped>
/* 弹窗整体样式 */
:deep(.share-modal-wrapper .ant-modal-content) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

:deep(.share-modal-wrapper .ant-modal-header) {
  background: linear-gradient(90deg, #1677ff, #4096ff);
  padding: 16px 24px;
  border-bottom: none;
}

:deep(.share-modal-wrapper .ant-modal-title) {
  color: white;
  font-weight: 600;
  font-size: 18px;
}

:deep(.share-modal-wrapper .ant-modal-close) {
  color: white;
  opacity: 0.8;
  transition: all 0.2s;
}

:deep(.share-modal-wrapper .ant-modal-close:hover) {
  opacity: 1;
  transform: rotate(90deg);
}

:deep(.share-modal-wrapper .ant-modal-body) {
  padding: 24px;
}

/* 分享内容区域 */
.share-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.share-section {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 16px;
  color: #1e293b;
}

.section-icon {
  margin-right: 8px;
  color: #1677ff;
  font-size: 18px;
}

/* 链接部分 */
.link-container {
  margin-bottom: 8px;
}

.share-link-input {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.share-link-input .ant-input) {
  padding: 8px 12px;
  height: 40px;
  font-family: monospace;
  background-color: #f8fafc;
}

:deep(.share-link-input .ant-input-group-addon) {
  background-color: #f0f7ff;
  border-left-color: #e6f0ff;
  padding: 0 12px;
  cursor: pointer;
  transition: all 0.2s;
}

:deep(.share-link-input .ant-input-group-addon:hover) {
  background-color: #e6f4ff;
}

.copy-icon {
  color: #1677ff;
  font-size: 16px;
}

.link-tips {
  margin-top: 6px;
  color: #64748b;
  font-size: 12px;
}

/* 二维码部分 */
.qrcode-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qrcode {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  padding: 12px;
  background-color: white;
  transition: all 0.3s;
}

.qrcode:hover {
  transform: scale(1.02);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.qrcode-tips {
  margin-top: 12px;
  color: #64748b;
  font-size: 12px;
}

/* 分隔线 */
.share-divider {
  margin: 8px 0;
  opacity: 0.6;
}

/* 按钮区域 */
.share-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.close-button {
  border-radius: 8px;
  min-width: 80px;
  background: linear-gradient(to right, #1677ff, #4096ff);
  border: none;
  height: 36px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.2);
  transition: all 0.3s;
}

.close-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(22, 119, 255, 0.3);
}

/* 响应式调整 */
@media (max-width: 576px) {
  .qrcode {
    transform: scale(0.9);
  }
}
</style>
