<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from '@/utils/axios'

interface GitUser {
  id?: number
  name: string
  username: string
  password: string
  email?: string
  description?: string
  isDefault?: boolean
  createdAt?: string
  updatedAt?: string
}

const gitUsers = ref<GitUser[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增 Git 账号')
const isEdit = ref(false)
const showPassword = ref<Record<string, boolean>>({})

const formData = ref<GitUser>({
  name: '',
  username: '',
  password: '',
  email: '',
  description: '',
  isDefault: false
})

const defaultUser = computed(() => gitUsers.value.find((user) => user.isDefault))
const userCount = computed(() => gitUsers.value.length)
const defaultCount = computed(() => (defaultUser.value ? 1 : 0))

const resetForm = () => {
  formData.value = {
    name: '',
    username: '',
    password: '',
    email: '',
    description: '',
    isDefault: false
  }
}

const fetchGitUsers = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/git-users')
    gitUsers.value = response.data
  } catch (error: any) {
    ElMessage.error(`获取 Git 账号列表失败：${error.response?.data?.message || error.message}`)
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  dialogTitle.value = '新增 Git 账号'
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (user: GitUser) => {
  dialogTitle.value = '编辑 Git 账号'
  isEdit.value = true
  formData.value = { ...user }
  dialogVisible.value = true
}

const saveGitUser = async () => {
  if (!formData.value.name || !formData.value.username || !formData.value.password) {
    ElMessage.warning('请填写账号名称、用户名和 Token')
    return
  }

  loading.value = true
  try {
    if (isEdit.value && formData.value.id) {
      await axios.put(`/api/git-users/${formData.value.id}`, formData.value)
      ElMessage.success('更新成功')
    } else {
      await axios.post('/api/git-users', formData.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await fetchGitUsers()
  } catch (error: any) {
    ElMessage.error(`保存失败：${error.response?.data?.message || error.message}`)
  } finally {
    loading.value = false
  }
}

const deleteGitUser = async (user: GitUser) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 Git 账号“${user.name}”吗？`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }
    )

    loading.value = true
    await axios.delete(`/api/git-users/${user.id}`)
    ElMessage.success('删除成功')
    await fetchGitUsers()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`删除失败：${error.response?.data?.message || error.message}`)
    }
  } finally {
    loading.value = false
  }
}

const setAsDefault = async (user: GitUser) => {
  loading.value = true
  try {
    await axios.post(`/api/git-users/${user.id}/set-default`)
    ElMessage.success('已设置为默认账号')
    await fetchGitUsers()
  } catch (error: any) {
    ElMessage.error(`设置失败：${error.response?.data?.message || error.message}`)
  } finally {
    loading.value = false
  }
}

const togglePassword = (id?: number) => {
  const key = String(id)
  showPassword.value[key] = !showPassword.value[key]
}

const copyToken = async (token: string) => {
  try {
    await navigator.clipboard.writeText(token)
    ElMessage.success('Token 已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

const getUserInitial = (name: string) => {
  const trimmed = name?.trim()
  if (!trimmed) return '?'
  return trimmed.charAt(0).toUpperCase()
}

onMounted(() => {
  fetchGitUsers()
})
</script>

<template>
  <div class="git-manager">
    <span class="bg-orb orb-a"></span>
    <span class="bg-orb orb-b"></span>
    <span class="bg-grid"></span>

    <div class="layout-shell">
      <aside class="control-dock">
        <div class="dock-brand">
          <div class="logo-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22" />
            </svg>
          </div>
          <p class="dock-kicker">Credential Ops</p>
          <h1 class="dock-title">Git 凭据中心</h1>
          <p class="dock-desc">统一管理 GitHub / GitLab 账号与 Token，确保日常提交流程安全、清晰、可追踪。</p>
        </div>

        <div class="metric-grid">
          <article class="metric-card">
            <span class="metric-label">账号总数</span>
            <strong class="metric-value">{{ userCount }}</strong>
            <span class="metric-note">当前已录入的凭据数量</span>
          </article>
          <article class="metric-card highlight">
            <span class="metric-label">默认账号</span>
            <strong class="metric-value">{{ defaultCount }}</strong>
            <span class="metric-note">执行 Git 操作时优先使用</span>
          </article>
        </div>

        <button class="btn-create" @click="openCreateDialog">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 5v14M5 12h14" />
          </svg>
          新增 Git 账号
        </button>

        <div class="dock-tip">
          <h3>安全建议</h3>
          <ul>
            <li>Token 建议使用最小权限策略。</li>
            <li>定期轮换长期未更新的凭据。</li>
            <li>不同项目可使用独立账号隔离风险。</li>
          </ul>
        </div>
      </aside>

      <main class="workspace" v-loading="loading">
        <header class="workspace-header">
          <div>
            <p class="workspace-kicker">Account Library</p>
            <h2>Git 账号列表</h2>
          </div>
          <span v-if="defaultUser" class="default-pill">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
            </svg>
            默认账号 · {{ defaultUser.name }}
          </span>
          <span v-else class="default-pill muted">尚未设置默认账号</span>
        </header>

        <div v-if="gitUsers.length > 0" class="cards-grid">
          <article
            v-for="(user, index) in gitUsers"
            :key="user.id"
            class="user-card"
            :class="{ 'is-default': user.isDefault }"
            :style="{ '--delay': `${index * 70}ms` }"
          >
            <header class="card-head">
              <div class="user-avatar" :class="{ 'avatar-default': user.isDefault }">
                {{ getUserInitial(user.name) }}
              </div>
              <div class="identity-block">
                <p class="user-name">
                  {{ user.name }}
                  <span v-if="user.isDefault" class="tag-default">默认</span>
                </p>
                <p class="user-username">@{{ user.username }}</p>
              </div>
              <button v-if="!user.isDefault" class="set-default-chip" @click="setAsDefault(user)">
                设为默认
              </button>
            </header>

            <dl class="meta-list">
              <div class="meta-row">
                <dt>邮箱</dt>
                <dd>{{ user.email || '未填写' }}</dd>
              </div>

              <div class="meta-row token-row">
                <dt>Token</dt>
                <dd class="token-box">
                  <code>
                    {{ showPassword[String(user.id)] ? user.password : '**************' }}
                  </code>
                  <div class="token-actions">
                    <button
                      class="mini-btn"
                      @click.stop="togglePassword(user.id)"
                      :title="showPassword[String(user.id)] ? '隐藏' : '显示'"
                    >
                      <svg
                        v-if="!showPassword[String(user.id)]"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                      >
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                        <circle cx="12" cy="12" r="3" />
                      </svg>
                      <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                        <line x1="1" y1="1" x2="23" y2="23" />
                      </svg>
                    </button>
                    <button class="mini-btn" @click.stop="copyToken(user.password)" title="复制">
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                        <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                      </svg>
                    </button>
                  </div>
                </dd>
              </div>

              <div class="meta-row description-row">
                <dt>备注</dt>
                <dd class="description">{{ user.description || '暂无备注' }}</dd>
              </div>
            </dl>

            <footer class="card-actions">
              <button class="action-btn edit" @click="openEditDialog(user)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
                编辑
              </button>

              <button class="action-btn delete" @click="deleteGitUser(user)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                </svg>
                删除
              </button>
            </footer>
          </article>
        </div>

        <section v-else-if="!loading" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
              <circle cx="8.5" cy="7" r="4" />
              <path d="M20 8v6M23 11h-6" />
            </svg>
          </div>
          <h3>还没有 Git 账号</h3>
          <p>点击左侧「新增 Git 账号」，为项目准备可用凭据。</p>
          <button class="empty-action" @click="openCreateDialog">立即添加账号</button>
        </section>
      </main>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      class="git-dialog"
      destroy-on-close
    >
      <el-form :model="formData" label-position="top">
        <el-form-item label="账号名称" required>
          <el-input v-model="formData.name" placeholder="例如：我的 GitHub 主账号" />
        </el-form-item>

        <el-form-item label="Git 用户名" required>
          <el-input v-model="formData.username" placeholder="请输入 GitHub/GitLab 用户名" />
        </el-form-item>

        <el-form-item label="Personal Access Token" required>
          <el-input
            v-model="formData.password"
            type="password"
            show-password
            placeholder="ghp_xxxxxxxxxxxx"
          />
          <div class="form-hint">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <path d="M12 16v-4M12 8h.01" />
            </svg>
            在 GitHub Settings -> Developer settings -> Personal access tokens 中创建。
          </div>
        </el-form-item>

        <el-form-item label="邮箱（可选）">
          <el-input v-model="formData.email" placeholder="your@email.com" />
        </el-form-item>

        <el-form-item label="备注（可选）">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="补充说明信息..." />
        </el-form-item>

        <el-form-item>
          <div class="switch-row">
            <span class="switch-label">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
              </svg>
              设为默认账号
            </span>
            <el-switch v-model="formData.isDefault" />
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <button class="dialog-btn cancel" @click="dialogVisible = false">取消</button>
        <button class="dialog-btn confirm" :disabled="loading" @click="saveGitUser">
          {{ loading ? '保存中...' : '保存' }}
        </button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=Space+Grotesk:wght@500;600;700&display=swap');

.git-manager {
  --bg-main: #fefbf4;
  --bg-frost: #f8fbff;
  --surface: rgba(255, 255, 255, 0.84);
  --surface-strong: rgba(255, 255, 255, 0.94);
  --line: rgba(30, 41, 59, 0.11);
  --text-main: #10213b;
  --text-sub: #56647a;
  --primary: #1f6feb;
  --primary-soft: #e8f1ff;
  --accent: #f59e0b;
  --accent-soft: #fff5da;
  --danger: #d63a34;
  --shadow-lg: 0 24px 46px rgba(15, 23, 42, 0.12);
  --shadow-md: 0 14px 28px rgba(15, 23, 42, 0.09);
  --shadow-sm: 0 8px 16px rgba(15, 23, 42, 0.08);

  position: relative;
  height: calc(100vh - 64px);
  overflow: hidden;
  padding: 24px 28px;
  background:
    radial-gradient(70vw 38vh at -10% -20%, #ffe4bf 0%, transparent 60%),
    radial-gradient(60vw 35vh at 112% -6%, #dbeafe 0%, transparent 58%),
    linear-gradient(160deg, var(--bg-main) 0%, #fbfdff 55%, var(--bg-frost) 100%);
  font-family: 'Plus Jakarta Sans', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.bg-orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(0.5px);
  pointer-events: none;
}

.orb-a {
  top: 6%;
  left: 45%;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle at 40% 40%, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0) 68%);
}

.orb-b {
  bottom: -70px;
  right: 8%;
  width: 320px;
  height: 320px;
  background: radial-gradient(circle at 40% 30%, rgba(253, 230, 138, 0.42) 0%, rgba(253, 230, 138, 0) 70%);
}

.bg-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.25;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.12) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.12) 1px, transparent 1px);
  background-size: 42px 42px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.26) 0%, rgba(0, 0, 0, 0) 80%);
}

.layout-shell {
  position: relative;
  z-index: 1;
  height: 100%;
  max-width: 1480px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 340px minmax(0, 1fr);
  gap: 24px;
}

.control-dock {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 26px;
  border-radius: 24px;
  background: var(--surface);
  border: 1px solid rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(16px);
  box-shadow: var(--shadow-lg);
  overflow: auto;
}

.dock-brand {
  padding-bottom: 6px;
}

.logo-icon {
  width: 58px;
  height: 58px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
  color: #fff;
  background: linear-gradient(140deg, #0f62d6 0%, #3c8dff 48%, #76b4ff 100%);
  box-shadow: 0 16px 26px rgba(31, 111, 235, 0.34);
}

.logo-icon svg {
  width: 28px;
  height: 28px;
}

.dock-kicker,
.workspace-kicker {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.13em;
  text-transform: uppercase;
  color: #6b7a8f;
  font-weight: 700;
}

.dock-title {
  margin: 8px 0;
  font-family: 'Space Grotesk', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 30px;
  line-height: 1.18;
  color: var(--text-main);
}

.dock-desc {
  margin: 0;
  color: var(--text-sub);
  font-size: 14px;
  line-height: 1.72;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.metric-card {
  padding: 14px 12px;
  border-radius: 14px;
  border: 1px solid rgba(31, 111, 235, 0.14);
  background: rgba(232, 241, 255, 0.65);
}

.metric-card.highlight {
  border-color: rgba(245, 158, 11, 0.25);
  background: rgba(255, 245, 218, 0.76);
}

.metric-label {
  display: block;
  font-size: 12px;
  color: #58708f;
  margin-bottom: 8px;
}

.metric-value {
  font-family: 'Space Grotesk', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  display: block;
  font-size: 30px;
  line-height: 1;
  color: #093b82;
}

.metric-card.highlight .metric-value {
  color: #92400e;
}

.metric-note {
  display: block;
  margin-top: 8px;
  font-size: 11px;
  line-height: 1.45;
  color: #6e7e94;
}

.btn-create {
  width: 100%;
  border: 0;
  border-radius: 14px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(140deg, #1559c7 0%, #1f6feb 50%, #4693ff 100%);
  box-shadow: 0 18px 30px rgba(21, 89, 199, 0.3);
  transition: transform 0.24s ease, box-shadow 0.24s ease;
}

.btn-create:hover {
  transform: translateY(-2px);
  box-shadow: 0 22px 34px rgba(21, 89, 199, 0.36);
}

.btn-create svg {
  width: 16px;
  height: 16px;
}

.dock-tip {
  margin-top: auto;
  border-radius: 14px;
  padding: 16px 16px 6px;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.7) 0%, rgba(240, 247, 255, 0.85) 100%);
  border: 1px dashed rgba(31, 111, 235, 0.26);
}

.dock-tip h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #0f3a76;
}

.dock-tip ul {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #50637f;
  font-size: 12px;
  line-height: 1.72;
}

.workspace {
  min-width: 0;
  border-radius: 28px;
  padding: 28px;
  background: var(--surface-strong);
  border: 1px solid rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-lg);
  overflow: auto;
}

.workspace-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.3);
}

.workspace-header h2 {
  margin: 8px 0 0;
  font-family: 'Space Grotesk', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 28px;
  line-height: 1.14;
  color: var(--text-main);
}

.default-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: #9a3412;
  background: linear-gradient(145deg, #fff1c7 0%, #ffe4a8 100%);
  border: 1px solid rgba(245, 158, 11, 0.22);
  white-space: nowrap;
}

.default-pill svg {
  width: 14px;
  height: 14px;
}

.default-pill.muted {
  color: #64748b;
  background: #f2f6fb;
  border-color: #d8e2ee;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.user-card {
  position: relative;
  display: flex;
  flex-direction: column;
  border-radius: 18px;
  border: 1px solid var(--line);
  background: #fff;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  animation: card-raise 0.45s cubic-bezier(0.21, 0.93, 0.4, 1) backwards;
  animation-delay: var(--delay, 0ms);
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.user-card::after {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(168deg, rgba(255, 255, 255, 0.66) 0%, rgba(255, 255, 255, 0) 38%);
}

.user-card:hover {
  transform: translateY(-3px);
  border-color: rgba(31, 111, 235, 0.3);
  box-shadow: var(--shadow-md);
}

.user-card.is-default {
  border-color: rgba(245, 158, 11, 0.32);
  box-shadow: 0 14px 30px rgba(245, 158, 11, 0.14);
}

@keyframes card-raise {
  from {
    transform: translateY(16px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.card-head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(180deg, rgba(248, 251, 255, 0.95) 0%, rgba(255, 255, 255, 0.95) 100%);
  border-bottom: 1px solid rgba(148, 163, 184, 0.22);
}

.user-avatar {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-family: 'Space Grotesk', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(145deg, #145bcf 0%, #3d8fff 100%);
  box-shadow: 0 10px 18px rgba(31, 111, 235, 0.28);
}

.user-avatar.avatar-default {
  background: linear-gradient(145deg, #dc8b00 0%, #f59e0b 100%);
  box-shadow: 0 10px 18px rgba(245, 158, 11, 0.3);
}

.identity-block {
  min-width: 0;
  flex: 1;
}

.user-name {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-main);
}

.user-username {
  margin: 4px 0 0;
  font-size: 12px;
  color: #6b7a8f;
}

.tag-default {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 999px;
  background: var(--accent-soft);
  color: #9a3412;
  font-size: 10px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.set-default-chip {
  border: 1px solid rgba(31, 111, 235, 0.23);
  background: #fff;
  color: #145bcf;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
  cursor: pointer;
  transition: all 0.2s ease;
}

.set-default-chip:hover {
  background: var(--primary-soft);
  border-color: rgba(31, 111, 235, 0.4);
}

.meta-list {
  position: relative;
  z-index: 1;
  margin: 0;
  padding: 16px 16px 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.meta-row {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  font-size: 12px;
}

.meta-row dt {
  margin: 0;
  color: #72839a;
  font-weight: 700;
}

.meta-row dd {
  margin: 0;
  color: #30445f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.token-row {
  align-items: stretch;
}

.token-box {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  background: linear-gradient(160deg, #f8fbff 0%, #f2f7ff 100%);
}

.token-box code {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  color: #1f2d47;
}

.token-actions {
  display: flex;
  align-items: center;
  gap: 5px;
}

.mini-btn {
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  color: #62748e;
  cursor: pointer;
  transition: all 0.2s ease;
}

.mini-btn:hover {
  color: #1559c7;
  background: var(--primary-soft);
}

.mini-btn svg {
  width: 14px;
  height: 14px;
}

.description-row {
  align-items: flex-start;
}

.meta-row .description {
  white-space: normal;
  line-height: 1.58;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-actions {
  position: relative;
  z-index: 1;
  margin-top: auto;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  padding: 0 16px 16px;
}

.action-btn {
  border: 0;
  border-radius: 10px;
  padding: 10px 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background-color 0.18s ease;
}

.action-btn svg {
  width: 14px;
  height: 14px;
}

.action-btn.edit {
  color: #145bcf;
  background: var(--primary-soft);
}

.action-btn.edit:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(31, 111, 235, 0.2);
}

.action-btn.delete {
  color: var(--danger);
  background: #ffecec;
}

.action-btn.delete:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 16px rgba(220, 38, 38, 0.2);
}

.empty-state {
  min-height: 58vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--text-sub);
}

.empty-icon {
  width: 76px;
  height: 76px;
  border-radius: 24px;
  background: linear-gradient(150deg, #eaf2ff 0%, #f6f9ff 100%);
  color: #5d80b0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  box-shadow: 0 12px 24px rgba(67, 109, 166, 0.18);
}

.empty-icon svg {
  width: 36px;
  height: 36px;
}

.empty-state h3 {
  margin: 0;
  font-size: 22px;
  font-family: 'Space Grotesk', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: var(--text-main);
}

.empty-state p {
  margin: 8px 0 16px;
  max-width: 460px;
  line-height: 1.7;
  font-size: 14px;
}

.empty-action {
  border: 0;
  border-radius: 999px;
  padding: 10px 18px;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  cursor: pointer;
  background: linear-gradient(145deg, #145bcf 0%, #1f6feb 100%);
  box-shadow: 0 14px 24px rgba(21, 89, 199, 0.28);
}

:deep(.git-dialog) {
  border-radius: 20px;
  overflow: hidden;
}

:deep(.git-dialog .el-dialog__header) {
  margin: 0;
  padding: 18px 22px;
  border-bottom: 1px solid #e8eef8;
  background: linear-gradient(180deg, #f7faff 0%, #fefefe 100%);
}

:deep(.git-dialog .el-dialog__title) {
  font-family: 'Space Grotesk', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 18px;
  color: var(--text-main);
}

:deep(.git-dialog .el-dialog__body) {
  padding: 22px;
}

:deep(.git-dialog .el-dialog__footer) {
  padding: 14px 22px 20px;
  border-top: 1px solid #e8eef8;
}

:deep(.git-dialog .el-form-item__label) {
  font-size: 13px;
  font-weight: 700;
  color: #344968;
  margin-bottom: 6px;
}

:deep(.git-dialog .el-input__wrapper),
:deep(.git-dialog .el-textarea__inner) {
  border-radius: 10px;
  box-shadow: none;
  border: 1px solid #d9e4f2;
}

:deep(.git-dialog .el-input__wrapper:hover),
:deep(.git-dialog .el-textarea__inner:hover) {
  border-color: #b9c9dd;
}

:deep(.git-dialog .el-input__wrapper.is-focus),
:deep(.git-dialog .el-textarea__inner:focus) {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(31, 111, 235, 0.14);
}

.form-hint {
  margin-top: 8px;
  padding: 10px 12px;
  border-radius: 10px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 12px;
  line-height: 1.5;
  color: #2f5594;
  background: #edf4ff;
}

.form-hint svg {
  width: 14px;
  height: 14px;
  margin-top: 1px;
  flex-shrink: 0;
}

.switch-row {
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid #e2e8f3;
  background: #f8fbff;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.switch-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #3f556f;
}

.switch-label svg {
  width: 16px;
  height: 16px;
  color: #d89218;
}

.dialog-btn {
  border: 0;
  border-radius: 10px;
  padding: 10px 20px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.dialog-btn.cancel {
  margin-right: 10px;
  background: #f4f7fb;
  color: #5b6d84;
  border: 1px solid #d8e1ed;
}

.dialog-btn.confirm {
  color: #fff;
  background: linear-gradient(145deg, #145bcf 0%, #1f6feb 100%);
  box-shadow: 0 12px 20px rgba(21, 89, 199, 0.24);
}

.dialog-btn.confirm:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  box-shadow: none;
}

@media (max-width: 1180px) {
  .git-manager {
    height: auto;
    min-height: calc(100vh - 64px);
    overflow: auto;
  }

  .layout-shell {
    grid-template-columns: 1fr;
    height: auto;
  }

  .control-dock {
    position: relative;
    overflow: visible;
  }

  .workspace {
    min-height: 58vh;
  }
}

@media (max-width: 760px) {
  .git-manager {
    padding: 14px;
  }

  .control-dock,
  .workspace {
    border-radius: 20px;
    padding: 18px;
  }

  .dock-title {
    font-size: 26px;
  }

  .workspace-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .workspace-header h2 {
    font-size: 24px;
  }

  .cards-grid {
    grid-template-columns: 1fr;
  }

  .card-actions {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
