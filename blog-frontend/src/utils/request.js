// src/utils/request.js
import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
    baseURL: '', // Vite 代理 / nginx 代理统一处理
    timeout: 5000
})

// ===================== 新加的：请求拦截器（自动带token） =====================
request.interceptors.request.use(config => {
    // 从本地拿到token
    const token = localStorage.getItem('token')
    if (token) {
        // 放到请求头里
        config.headers['token'] = token
    }
    return config
})
// ==========================================================================

// 响应拦截器：自动适配 Result 格式
request.interceptors.response.use(
    res => {
        const result = res.data
        // 成功：直接返回 data 里的数据
        if (result.code === 200) {
            return result.data
        }
        // 失败：提示错误并抛出异常
        alert(result.msg || '请求失败')
        return Promise.reject(result)
    },
    err => {
        alert('网络异常，请检查后端是否启动')
        return Promise.reject(err)
    }
)

export default request
