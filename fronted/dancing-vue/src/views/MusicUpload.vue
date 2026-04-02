<template>
  <div class="upload-container">
    <el-card class="upload-card">
      <template #header>
        <h2>
          <el-icon><Upload /></el-icon>
          上传音乐
        </h2>
      </template>

      <el-form
        ref="uploadFormRef"
        :model="uploadForm"
        :rules="uploadRules"
        label-width="100px"
      >
        <el-form-item label="音乐文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            accept="audio/*,.mp3,.wav,.ogg,.m4a,.flac"
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">将音乐文件拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 MP3/WAV/OGG/M4A/FLAC，最大 50MB</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="音乐标题" prop="title">
          <el-input v-model="uploadForm.title" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="音乐描述" prop="description">
          <el-input
            v-model="uploadForm.description"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="uploading"
            :disabled="!uploadForm.file"
            @click="handleUpload"
          >
            {{ uploading ? '上传中...' : '上传音乐' }}
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { musicApi } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const uploadFormRef = ref()
const uploadRef = ref()
const uploading = ref(false)

const uploadForm = reactive({
  file: null,
  title: '',
  description: ''
})

const uploadRules = {
  file: [{ required: true, message: '请选择要上传的音乐文件', trigger: 'change' }],
  title: [
    { required: true, message: '请输入音乐标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入音乐描述', trigger: 'blur' },
    { min: 2, max: 500, message: '描述长度在 2 到 500 个字符', trigger: 'blur' }
  ]
}

const handleFileChange = (file) => {
  uploadForm.file = file.raw
}

const beforeUpload = (file) => {
  const isAudio = file.type.startsWith('audio/')
  const ext = (file.name || '').toLowerCase()
  const validExt = ['.mp3', '.wav', '.ogg', '.m4a', '.flac'].some((e) => ext.endsWith(e))
  const isLt50M = file.size / 1024 / 1024 < 50

  if (!isAudio && !validExt) {
    ElMessage.error('只能上传音频文件')
    return false
  }
  if (!isLt50M) {
    ElMessage.error('音乐文件大小不能超过 50MB')
    return false
  }
  return true
}

const handleUpload = async () => {
  if (!uploadFormRef.value) return

  try {
    const valid = await uploadFormRef.value.validate()
    if (!valid || !uploadForm.file) return

    uploading.value = true
    const formData = new FormData()
    formData.append('file', uploadForm.file)
    formData.append('title', uploadForm.title)
    formData.append('description', uploadForm.description)

    const response = await musicApi.uploadMusic(formData)
    if (response.code === 200) {
      ElMessage.success('音乐上传成功')
      router.push('/music')
    } else {
      ElMessage.error(response.message || '上传失败')
    }
  } catch (error) {
    ElMessage.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

const resetForm = () => {
  uploadForm.file = null
  uploadForm.title = ''
  uploadForm.description = ''
  uploadRef.value?.clearFiles()
  uploadFormRef.value?.clearValidate()
}
</script>

<style scoped>
.upload-container {
  max-width: 820px;
  margin: 0 auto;
}

.upload-card h2 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
