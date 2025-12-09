import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 上传头像
 */
export async function uploadAvatar(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)

  const token = localStorage.getItem('token')
  
  try {
    const response = await axios.post('/api/user/file/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': token ? `Bearer ${token}` : ''
      }
    })
    
    const code = response.data.code
    if (code === 200 || code === '200' || code === 0 || code === '0' || code === undefined || code === null) {
      return response.data.data
    } else {
      ElMessage.error(response.data.message || '上传失败')
      throw new Error(response.data.message)
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '上传失败')
    throw error
  }
}

/**
 * 上传课程封面
 */
export async function uploadCourseCover(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)
  const token = localStorage.getItem('token')
  try {
    const response = await axios.post('/api/course/file/cover', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': token ? `Bearer ${token}` : ''
      }
    })
    const code = response.data.code
    if (code === 200 || code === '200' || code === 0 || code === '0' || code === undefined || code === null) {
      return response.data.data
    } else {
      ElMessage.error(response.data.message || '上传失败')
      throw new Error(response.data.message)
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '上传失败')
    throw error
  }
}

/**
 * 上传课程视频（本地存储）
 */
export async function uploadVideo(file: File): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)

  const token = localStorage.getItem('token')

  try {
    const response = await axios.post('/api/course/file/video', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        'Authorization': token ? `Bearer ${token}` : ''
      }
    })

    const code = response.data.code
    if (code === 200 || code === '200' || code === 0 || code === '0' || code === undefined || code === null) {
      return response.data.data
    } else {
      ElMessage.error(response.data.message || '上传失败')
      throw new Error(response.data.message)
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '上传失败')
    throw error
  }
}

