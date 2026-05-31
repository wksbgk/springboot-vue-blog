<template>
  <div class="page">
    <!-- 顶部栏 -->
    <header class="header">
      <div class="header-left">
        <h1 class="logo">Blog</h1>
        <nav>
          <router-link to="/" class="nav-link">用户管理</router-link>
          <router-link to="/articles" class="nav-link active">文章管理</router-link>
        </nav>
      </div>
      <div class="header-right">
        <span class="user-badge">
          <span class="avatar">{{ currentUser.username?.charAt(0)?.toUpperCase() }}</span>
          {{ currentUser.username }}
          <em class="role-tag">{{ currentUser.role === 'admin' ? '管理员' : '用户' }}</em>
        </span>
        <button class="btn btn-outline" @click="handleLogout">退出</button>
      </div>
    </header>

    <!-- 工具栏 -->
    <div class="toolbar">
      <span class="count">共 {{ total }} 篇文章</span>
      <button v-if="currentUser.role === 'admin'" class="btn btn-primary" @click="handleAdd">+ 写文章</button>
    </div>

    <!-- 加载 / 错误 -->
    <div v-if="loading" class="state">加载中...</div>
    <div v-else-if="error" class="state error">{{ error }}</div>

    <!-- 文章卡片列表 -->
    <div v-else class="card-list">
      <div v-for="a in list" :key="a.id" class="card" @click="showDetail(a)">
        <div class="card-body">
          <div class="card-header">
            <h3 class="article-title">{{ a.title }}</h3>
            <span v-if="a.status === 'draft'" class="badge badge-draft">草稿</span>
          </div>
          <p class="article-summary">{{ a.summary || (a.content || '').slice(0, 120) }}</p>
          <div class="article-meta">
            <span>👁 {{ a.viewCount || 0 }} 阅读</span>
            <span>{{ formatTime(a.createTime) }}</span>
          </div>
        </div>
        <div v-if="currentUser.role === 'admin'" class="card-actions" @click.stop>
          <button class="btn btn-sm" @click="handleEdit(a)">编辑</button>
          <button class="btn btn-sm btn-danger" @click="handleDelete(a.id)">删除</button>
        </div>
      </div>
      <div v-if="list.length === 0" class="state">还没有文章，管理员快来写第一篇吧 ✍️</div>
    </div>

    <!-- 分页 -->
    <div v-if="pages > 1" class="pagination">
      <button :disabled="current <= 1" @click="goPage(current - 1)">上一页</button>
      <span v-for="p in pages" :key="p">
        <button v-if="p === current" class="active">{{ p }}</button>
        <button v-else @click="goPage(p)">{{ p }}</button>
      </span>
      <button :disabled="current >= pages" @click="goPage(current + 1)">下一页</button>
    </div>

    <!-- 详情弹窗 -->
    <div v-show="detailVisible" class="modal" @click.self="closeDetail">
      <div class="modal-box detail-box">
        <h2>{{ detail.title }}</h2>
        <div class="detail-meta">
          <span>👁 {{ detail.viewCount || 0 }} 次阅读</span>
          <span>发布于 {{ formatTime(detail.createTime) }}</span>
          <span v-if="detail.status === 'draft'" class="badge badge-draft">草稿</span>
        </div>
        <div class="detail-content" v-html="renderedContent"></div>

        <!-- 评论区 -->
        <div class="comment-section">
          <h4>评论 ({{ comments.length }})</h4>
          <div v-if="commentsLoading">加载中...</div>
          <div v-else>
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <div class="comment-avatar">{{ c.userId }}</div>
              <div class="comment-body">
                <div class="comment-meta">
                  <span class="comment-author">{{ c.username }}</span>
                  <span>{{ formatTime(c.createTime) }}</span>
                </div>
                <p class="comment-content">{{ c.content }}</p>
              </div>
              <button v-if="currentUser.role === 'admin' || currentUser.id === c.userId" class="btn btn-sm btn-danger" @click="handleDelComment(c.id)" style="flex-shrink:0;">删除</button>
            </div>
            <div v-if="comments.length === 0" class="comment-empty">暂无评论，来说两句吧</div>
          </div>

          <!-- 发评论 -->
          <div v-if="currentUser.id" class="comment-input">
            <input v-model="commentText" placeholder="写下你的评论..." @keyup.enter.prevent="submitComment" />
            <button type="button" class="btn btn-primary btn-sm" @click="submitComment" :disabled="!commentText.trim()">发表</button>
          </div>
          <div v-else class="comment-login-hint">
            <router-link to="/login">登录</router-link> 后即可评论
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn" @click="closeDetail">关闭</button>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div v-show="dialogVisible" class="modal" @click.self="dialogVisible=false">
      <div class="modal-box edit-box">
        <h3>{{ isEdit ? '编辑文章' : '写文章' }}</h3>
        <div class="form-group">
          <label>标题</label>
          <input v-model="form.title" placeholder="请输入标题" />
        </div>
        <div class="form-group">
          <label>摘要</label>
          <input v-model="form.summary" placeholder="一句话概述文章内容" />
        </div>
        <div class="form-group">
          <label>内容</label>
          <textarea v-model="form.content" placeholder="想写点什么..." rows="10"></textarea>
        </div>
        <div class="form-group">
          <label>状态</label>
          <select v-model="form.status">
            <option value="published">发布</option>
            <option value="draft">草稿</option>
          </select>
        </div>
        <div class="modal-actions">
          <button class="btn" @click="dialogVisible=false">取消</button>
          <button class="btn btn-primary" @click="submitForm">{{ isEdit ? '保存' : '发布' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import { marked } from 'marked'

const router = useRouter()
const currentUser = ref(JSON.parse(localStorage.getItem('user') || '{}'))

const list = ref([])
const total = ref(0)
const current = ref(1)
const pages = ref(1)
const loading = ref(false)
const error = ref('')
const detailVisible = ref(false)
const detail = ref({})
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ id: '', title: '', summary: '', content: '', status: 'published' })

const formatTime = (t) => {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

const renderedContent = computed(() => {
  if (!detail.value.content) return ''
  return marked(detail.value.content)
})

const handleLogout = () => {
  if (!confirm('确定要退出登录吗？')) return
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

const getList = async (pageNum = 1) => {
  loading.value = true
  error.value = ''
  try {
    const res = await request.get('/api/article/list', { params: { pageNum, pageSize: 5 } })
    list.value = res.records
    total.value = res.total
    current.value = res.current
    pages.value = res.pages
  } catch (e) {
    error.value = '加载失败，请检查网络'
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goPage = (p) => {
  if (p < 1 || p > pages.value) return
  getList(p)
}

// 评论
const comments = ref([])
const commentsLoading = ref(false)
const commentText = ref('')

const showDetail = async (article) => {
  detailVisible.value = true
  document.body.style.overflow = 'hidden'
  try {
    const res = await request.get(`/api/article/${article.id}`)
    detail.value = res
  } catch (e) {
    detail.value = article
  }
  fetchComments(article.id)
}

const closeDetail = () => {
  detailVisible.value = false
  document.body.style.overflow = ''
  getList()
}

const fetchComments = async (articleId) => {
  commentsLoading.value = true
  try {
    comments.value = await request.get(`/api/comment/article/${articleId}`)
  } catch (e) { comments.value = [] }
  finally { commentsLoading.value = false }
}

const submitComment = async () => {
  if (!commentText.value.trim()) return
  try {
    const res = await request.post('/api/comment', { articleId: detail.value.id, content: commentText.value.trim() })
    comments.value.push(res)
    commentText.value = ''
  } catch (e) { alert('评论失败，请先登录'); console.error(e) }
}

const handleDelComment = async (id) => {
  if (!confirm('确定删除这条评论吗？')) return
  try { await request.delete(`/api/comment/${id}`); fetchComments(detail.value.id) }
  catch (e) { alert('删除失败'); console.error(e) }
}
const handleAdd = () => {
  isEdit.value = false
  form.value = { id: '', title: '', summary: '', content: '', status: 'published' }
  dialogVisible.value = true
}
const handleEdit = (row) => {
  isEdit.value = true
  form.value = { id: row.id, title: row.title, summary: row.summary || '', content: row.content, status: row.status || 'published' }
  dialogVisible.value = true
}
const submitForm = async () => {
  if (!form.value.title || !form.value.content) { alert('请填写标题和内容'); return }
  try {
    if (isEdit.value) await request.put('/api/article', form.value)
    else await request.post('/api/article', form.value)
    dialogVisible.value = false
    getList()
  } catch (e) { alert('提交失败'); console.error(e) }
}
const handleDelete = async (id) => {
  if (!confirm('确定删除这篇文章吗？')) return
  try { await request.delete(`/api/article/${id}`); getList() }
  catch (e) { alert('删除失败'); console.error(e) }
}

onMounted(() => getList())
</script>

<style scoped>
* { box-sizing: border-box; margin: 0; padding: 0; }
.page { min-height: 100vh; background: #f5f7fa; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; color: #2c3e50; }
.header { display: flex; justify-content: space-between; align-items: center; background: #fff; padding: 0 32px; height: 60px; box-shadow: 0 1px 3px rgba(0,0,0,.06); }
.header-left { display: flex; align-items: center; gap: 32px; }
.logo { font-size: 20px; font-weight: 700; color: #42b983; letter-spacing: -0.5px; }
nav { display: flex; gap: 4px; }
.nav-link { padding: 8px 16px; border-radius: 6px; text-decoration: none; color: #606266; font-size: 14px; transition: .2s; }
.nav-link:hover { background: #f0f9f4; color: #42b983; }
.nav-link.active { background: #e8f5e9; color: #42b983; font-weight: 600; }
.header-right { display: flex; align-items: center; gap: 16px; }
.user-badge { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #606266; }
.avatar { width: 32px; height: 32px; border-radius: 50%; background: linear-gradient(135deg, #42b983, #3aa373); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; }
.role-tag { font-style: normal; font-size: 12px; padding: 2px 8px; border-radius: 10px; background: #f0f9f4; color: #42b983; }

.toolbar { margin: 24px 0 0; padding: 0 32px; display: flex; justify-content: space-between; align-items: center; }
.count { font-size: 14px; color: #909399; }

.card-list { margin: 16px 0 40px; padding: 0 32px; display: flex; flex-direction: column; gap: 14px; }
.card { background: #fff; border-radius: 14px; overflow: hidden; display: flex; box-shadow: 0 1px 4px rgba(0,0,0,.04); cursor: pointer; transition: .2s; }
.card:hover { box-shadow: 0 8px 24px rgba(0,0,0,.1); transform: translateY(-2px); }
.card-body { flex: 1; min-width: 0; padding: 20px 24px; }
.card-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.article-title { font-size: 17px; font-weight: 600; color: #2c3e50; }
.article-summary { font-size: 14px; color: #909399; line-height: 1.6; margin-bottom: 10px; }
.article-meta { display: flex; gap: 20px; font-size: 13px; color: #c0c4cc; }
.card-actions { display: flex; gap: 8px; align-items: center; padding: 0 24px 0 0; flex-shrink: 0; }

.badge { display: inline-block; font-size: 11px; padding: 2px 10px; border-radius: 10px; font-weight: 500; }
.badge-draft { background: #fff7e6; color: #fa8c16; }

.state { text-align: center; padding: 60px 0; color: #909399; font-size: 15px; }
.state.error { color: #f56c6c; }

/* 弹窗 */
.modal { position: fixed; inset: 0; background: rgba(0,0,0,.35); backdrop-filter: blur(4px); display: flex; align-items: flex-start; justify-content: center; z-index: 100; overflow-y: auto; padding: 60px 0; }
.modal-box { background: #fff; border-radius: 16px; padding: 28px; width: 520px; box-shadow: 0 20px 60px rgba(0,0,0,.15); }
.edit-box { width: 620px; max-height: 85vh; overflow-y: auto; }
.modal-box h3 { font-size: 18px; margin-bottom: 20px; }
.detail-box { width: 700px; max-height: 85vh; overflow-y: auto; }
.detail-box h2 { font-size: 22px; margin: 16px 0 12px; text-align: center; }
.detail-meta { display: flex; gap: 16px; align-items: center; font-size: 13px; color: #c0c4cc; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid #f0f0f0; }
.detail-content { font-size: 15px; line-height: 2; color: #333; }
.detail-content :deep(h2) { font-size: 18px; margin: 20px 0 10px; padding-bottom: 6px; border-bottom: 1px solid #eee; }
.detail-content :deep(h3) { font-size: 16px; margin: 16px 0 8px; }
.detail-content :deep(p) { margin: 8px 0; text-indent: 2em; }
.detail-content :deep(ul), .detail-content :deep(ol) { padding-left: 2em; margin: 8px 0; }
.detail-content :deep(code) { background: #f5f7fa; padding: 2px 6px; border-radius: 4px; font-size: 13px; }
.detail-content :deep(pre) { background: #f5f7fa; padding: 12px 16px; border-radius: 8px; overflow-x: auto; margin: 12px 0; }
.detail-content :deep(strong) { font-weight: 600; }

/* 评论区 */
.comment-section { margin-top: 28px; padding-top: 20px; border-top: 1px solid #f0f0f0; }
.comment-section h4 { font-size: 16px; margin-bottom: 16px; }
.comment-item { display: flex; gap: 12px; padding: 12px 0; border-bottom: 1px solid #f8f8f8; align-items: flex-start; }
.comment-avatar { width: 34px; height: 34px; border-radius: 50%; background: linear-gradient(135deg, #e0e7ff, #c7d2fe); color: #6366f1; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; flex-shrink: 0; }
.comment-body { flex: 1; min-width: 0; }
.comment-meta { display: flex; gap: 12px; font-size: 12px; color: #c0c4cc; margin-bottom: 4px; }
.comment-author { color: #606266; font-weight: 500; }
.comment-content { font-size: 14px; color: #4a4a4a; line-height: 1.6; }
.comment-empty { text-align: center; color: #c0c4cc; padding: 20px 0; font-size: 14px; }
.comment-input { display: flex; gap: 10px; margin-top: 16px; }
.comment-input input { flex: 1; padding: 10px 14px; border: 1px solid #dcdfe6; border-radius: 8px; font-size: 14px; outline: none; }
.comment-input input:focus { border-color: #42b983; }
.comment-login-hint { text-align: center; color: #909399; font-size: 13px; margin-top: 16px; }
.comment-login-hint a { color: #42b983; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #606266; margin-bottom: 6px; font-weight: 500; }
.form-group input, .form-group textarea, .form-group select { width: 100%; padding: 10px 12px; border: 1px solid #dcdfe6; border-radius: 8px; font-size: 14px; transition: .2s; outline: none; font-family: inherit; resize: vertical; background: #fff; }
.form-group input:focus, .form-group textarea:focus, .form-group select:focus { border-color: #42b983; box-shadow: 0 0 0 3px rgba(66,185,131,.12); }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }

.btn { padding: 8px 18px; border-radius: 8px; border: 1px solid #dcdfe6; background: #fff; color: #606266; font-size: 13px; cursor: pointer; transition: .2s; }
.btn:hover { border-color: #42b983; color: #42b983; }
.btn-primary { background: #42b983; color: #fff; border-color: #42b983; }
.btn-primary:hover { background: #3aa373; }
.btn-danger { color: #f56c6c; border-color: #fbc4c4; }
.btn-danger:hover { background: #fef0f0; }
.btn-outline { background: transparent; border: 1px solid #dcdfe6; }
.btn-sm { padding: 5px 12px; font-size: 12px; }

.pagination { display: flex; justify-content: center; align-items: center; gap: 6px; padding: 0 32px 40px; }
.pagination button { min-width: 36px; height: 36px; border-radius: 8px; border: 1px solid #dcdfe6; background: #fff; color: #606266; font-size: 13px; cursor: pointer; transition: .2s; }
.pagination button:hover { border-color: #42b983; color: #42b983; }
.pagination button.active { background: #42b983; color: #fff; border-color: #42b983; }
.pagination button:disabled { opacity: .4; cursor: not-allowed; }
</style>
