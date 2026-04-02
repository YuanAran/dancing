<template>
  <div class="live-create">
    <div class="page-header">
      <el-button text @click="$router.push('/live')">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
      <h1>创建直播间</h1>
    </div>

    <el-card class="form-card" shadow="hover">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        label-position="top"
        @submit.prevent
      >
        <el-form-item label="直播间标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="例如：晚间广场舞教学"
            maxlength="60"
            show-word-limit
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="submitting" @click="submit">
            <el-icon><VideoCamera /></el-icon>
            创建并进入直播
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useLiveRoomStore } from '@/stores/liveRoom'
import { liveApi } from '@/api'
import { ElMessage } from 'element-plus'
import { ArrowLeft, VideoCamera } from '@element-plus/icons-vue'

const router = useRouter()
const liveRoomStore = useLiveRoomStore()
const formRef = ref(null)
const submitting = ref(false)

function generateLiveRoomId() {
  const t = Date.now().toString(36)
  const r = Math.random().toString(36).slice(2, 10)
  return `live_${t}_${r}`
}

const form = reactive({
  title: ''
})

const rules = {
  title: [
    { required: true, message: '请输入直播间标题', trigger: 'blur' },
    { min: 2, max: 60, message: '标题长度为 2～60 个字符', trigger: 'blur' }
  ]
}

const submit = async () => {
  const f = formRef.value
  if (!f) return
  await f.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const roomId = generateLiveRoomId()
      const title = form.title.trim()
      const res = await liveApi.announce({ roomId, title })
      if (res.code !== 200) {
        ElMessage.error(res.message || '上报直播间失败')
        return
      }
      liveRoomStore.registerRoom({ roomId, title })
      ElMessage.success('直播间已创建')
      router.push({ name: 'liveBroadcast', params: { roomId } })
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped>
.live-create {
  max-width: 640px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 12px 0 0;
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.form-card {
  border-radius: 16px;
}
</style>
