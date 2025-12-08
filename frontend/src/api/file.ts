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
    
    if (response.data.code === 200) {
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

