<template>
  <div class="upload-container">
    <el-card class="upload-card">
      <template #header>
        <div class="card-header">
          <h2>
            <el-icon><Upload /></el-icon>
            上传视频
          </h2>
        </div>
      </template>
      
      <el-form
        ref="uploadFormRef"
        :model="uploadForm"
        :rules="uploadRules"
        label-width="100px"
        class="upload-form"
      >
        <el-form-item label="视频文件" prop="file">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            :show-file-list="true"
            accept="video/*"
            drag
            class="upload-dragger"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将视频文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 MP4、AVI、MOV 等格式，文件大小不超过 100MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="视频标题" prop="title">
          <el-input
            v-model="uploadForm.title"
            placeholder="请输入视频标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="视频描述" prop="description">
          <el-input
            v-model="uploadForm.description"
            type="textarea"
            placeholder="请输入视频描述"
            :rows="4"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="uploading"
            @click="handleUpload"
            :disabled="!uploadForm.file"
          >
            {{ uploading ? '上传中...' : '上传视频' }}
          </el-button>
          <el-button @click="resetForm">
            重置
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 上传进度 -->
      <div v-if="uploading" class="upload-progress">
        <el-progress
          :percentage="uploadProgress"
          :status="uploadProgress === 100 ? 'success' : ''"
        />
        <p class="progress-text">{{ uploadStatus }}</p>
      </div>
    </el-card>
    
    <!-- 上传须知 -->
    <el-card class="tips-card">
      <template #header>
        <h3>
          <el-icon><InfoFilled /></el-icon>
          上传须知
        </h3>
      </template>
      <ul class="tips-list">
        <li>请确保视频内容健康向上，符合平台规范</li>
        <li>建议视频分辨率不低于 720p，时长不超过 30 分钟</li>
        <li>支持格式：MP4、AVI、MOV、WMV、FLV</li>
        <li>文件大小限制：100MB</li>
        <li>上传后请耐心等待处理完成</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { videoApi } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const uploadFormRef = ref()
const uploadRef = ref()
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadStatus = ref('')

const uploadForm = reactive({
  file: null,
  title: '',
  description: ''
})

const uploadRules = {
  file: [
    { required: true, message: '请选择要上传的视频文件', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入视频描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

// 文件选择处理
const handleFileChange = (file) => {
  uploadForm.file = file.raw
}

// 上传前检查
const beforeUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  const isLt100M = file.size / 1024 / 1024 < 100

  if (!isVideo) {
    ElMessage.error('只能上传视频文件!')
    return false
  }
  if (!isLt100M) {
    ElMessage.error('视频文件大小不能超过 100MB!')
    return false
  }
  return true
}

// 上传视频
const handleUpload = async () => {
  if (!uploadFormRef.value) return
  
  let progressInterval = null
  
  try {
    const valid = await uploadFormRef.value.validate()
    if (!valid) return
    
    if (!uploadForm.file) {
      ElMessage.error('请选择要上传的视频文件')
      return
    }
    
    console.log('开始上传视频:', {
      fileName: uploadForm.file.name,
      fileSize: uploadForm.file.size,
      fileType: uploadForm.file.type,
      title: uploadForm.title,
      description: uploadForm.description
    })
    
    uploading.value = true
    uploadProgress.value = 0
    uploadStatus.value = '准备上传...'
    
    // 模拟上传进度
    progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += Math.random() * 10
        uploadStatus.value = '上传中...'
      }
    }, 200)
    
    const formData = new FormData()
    formData.append('file', uploadForm.file)
    formData.append('title', uploadForm.title)
    formData.append('description', uploadForm.description)
    
    console.log('FormData已创建，准备发送请求')
    
    const response = await videoApi.uploadVideo(formData)
    
    console.log('上传响应:', response)
    
    clearInterval(progressInterval)
    progressInterval = null
    uploadProgress.value = 100
    uploadStatus.value = '上传完成'
    
    if (response.code === 200) {
      ElMessage.success('视频上传成功!')
      setTimeout(() => {
        router.push('/videos/my')
      }, 1000)
    } else {
      console.error('上传失败，响应码:', response.code, '消息:', response.message)
      ElMessage.error(response.message || '上传失败')
    }
  } catch (error) {
    console.error('上传异常:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      stack: error.stack
    })
    
    if (progressInterval) {
      clearInterval(progressInterval)
    }
    
    let errorMessage = '上传失败，请重试'
    if (error.response) {
      errorMessage = `上传失败: ${error.response.data?.message || error.response.statusText || '服务器错误'}`
    } else if (error.request) {
      errorMessage = '上传失败: 无法连接到服务器'
    }
    
    ElMessage.error(errorMessage)
  } finally {
    setTimeout(() => {
      uploading.value = false
      uploadProgress.value = 0
      uploadStatus.value = ''
    }, 2000)
  }
}

// 重置表单
const resetForm = () => {
  uploadForm.file = null
  uploadForm.title = ''
  uploadForm.description = ''
  uploadRef.value?.clearFiles()
  if (uploadFormRef.value) {
    uploadFormRef.value.clearValidate()
  }
}
</script>

<style scoped>
.upload-container {
  max-width: 800px;
  margin: 0 auto;
}

.upload-card {
  margin-bottom: 20px;
}

.card-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #333;
}

.upload-form {
  max-width: 600px;
}

.upload-dragger {
  width: 100%;
}

.upload-progress {
  margin-top: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}

.progress-text {
  text-align: center;
  margin-top: 10px;
  color: #666;
}

.tips-card h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #333;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: #666;
  line-height: 1.8;
}

.tips-list li {
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .upload-form {
    max-width: 100%;
  }
  
  .upload-dragger {
    margin-bottom: 20px;
  }
}
</style>
