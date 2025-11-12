<template>
  <div class="post-create">
    <el-card class="create-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>
            <el-icon><EditPen /></el-icon>
            发布帖子
          </h2>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        class="create-form"
      >
        <el-form-item label="帖子标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入帖子标题（5-200字）"
            maxlength="200"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item label="帖子内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            placeholder="分享你的想法、经验或故事..."
            :rows="12"
            maxlength="5000"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              <el-icon><Check /></el-icon>
              发布
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { postApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  title: '',
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 5, max: 200, message: '标题长度应在 5 到 200 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 10, max: 5000, message: '内容长度应在 10 到 5000 个字符', trigger: 'blur' }
  ]
}

// 提交表单
const handleSubmit = async () => {
  // 先进行表单验证
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请检查输入内容')
    return
  }

  submitting.value = true
  try {
    const response = await postApi.createPost({
      title: form.title,
      content: form.content
    })

    if (response.code === 200) {
      ElMessage.success('发布成功')
      router.push('/posts')
    } else {
      ElMessage.error(response.message || '发布失败')
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = async () => {
  if (form.title || form.content) {
    try {
      await ElMessageBox.confirm('确定要取消发布吗？未保存的内容将丢失', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '继续编辑',
        type: 'warning'
      })
      router.back()
    } catch {
      // 用户取消
    }
  } else {
    router.back()
  }
}
</script>

<style scoped>
.post-create {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.create-card {
  border-radius: 12px;
  overflow: hidden;
}

.card-header h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.create-form {
  margin-top: 20px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
}

:deep(.el-textarea__inner) {
  font-size: 15px;
  line-height: 1.6;
  font-family: inherit;
}

:deep(.el-input__inner) {
  font-size: 15px;
}

@media (max-width: 768px) {
  .post-create {
    padding: 15px;
  }

  :deep(.el-form-item__label) {
    width: 100% !important;
    text-align: left;
    margin-bottom: 8px;
  }

  :deep(.el-form-item__content) {
    margin-left: 0 !important;
  }
}
</style>

