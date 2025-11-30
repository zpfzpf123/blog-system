/*
 * @Author: 18582297328 2622013323@qq.com
 * @Date: 2025-08-05 13:36:17
 * @LastEditors: 18582297328 2622013323@qq.com
 * @LastEditTime: 2025-11-30 21:01:10
 * @FilePath: \blog\src\utils\axios.ts
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  // 使用环境变量配置API基础URL
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:4567',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 在发送请求之前做些什么
    console.log('Request:', config.method?.toUpperCase(), config.url)
    
    // 为Git相关操作设置更长的超时时间（5分钟）
    if (config.url?.includes('/git-')) {
      config.timeout = 300000 // Git操作需要更长时间（5分钟）
      console.log('🔧 Git操作检测到，超时时间设置为5分钟')
    }
    
    return config
  },
  (error) => {
    // 对请求错误做些什么
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    // 对响应数据做点什么
    console.log('Response:', response.status, response.config.url)
    return response
  },
  (error) => {
    // 对响应错误做点什么
    console.error('Response Error:', error.response?.status, error.response?.data)
    
    // 统一错误处理
    if (error.response?.status === 401) {
      // 未授权，跳转到登录页或显示错误信息
      console.error('Unauthorized access')
    } else if (error.response?.status === 500) {
      // 服务器错误
      console.error('Server error occurred')
    }
    
    return Promise.reject(error)
  }
)

export default api
