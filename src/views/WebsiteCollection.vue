<template>
  <div class="website-collection">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon">
            <Link />
          </el-icon>
          网站合集
        </h1>
        <p class="page-description">收集和整理您喜欢的网站，按分类管理</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showAddWebsiteDialog">
          <el-icon>
            <Plus />
          </el-icon>
          添加网站
        </el-button>
        <el-button @click="showAddCategoryDialog">
          <el-icon>
            <FolderAdd />
          </el-icon>
          添加分类
        </el-button>
        <el-button type="success" @click="showAIGenerateCategoryDialog">
          <el-icon>
            <Star />
          </el-icon>
          AI一键生成分类
        </el-button>

        <el-button type="primary" @click="showAICreateWebsiteDialog">
          <el-icon>
            <Plus />
          </el-icon>
          AI一键新增网站
        </el-button>

        <el-button type="warning" @click="showBatchImportDialog">
          <el-icon>
            <Upload />
          </el-icon>
          批量导入
        </el-button>

        <el-button type="info" @click="showStatusMonitorDialog">
          <el-icon>
            <Monitor />
          </el-icon>
          状态监控
        </el-button>
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <div class="category-header">
        <div class="header-left">
          <div class="title-container">
            <el-icon class="title-icon">
              <Menu />
            </el-icon>
            <span class="category-title">分类筛选</span>
            <div class="title-decoration"></div>
          </div>
          <span class="category-subtitle">按分类浏览和管理您的网站收藏</span>
          <div class="category-tips">
            <el-icon class="tip-icon">
              <InfoFilled />
            </el-icon>
            <span class="tip-text">右键点击分类项可进行编辑或删除操作</span>
          </div>
        </div>
        <el-tooltip content="点击进入编辑模式，拖拽分类项目可调整顺序" placement="top" :show-after="300">
          <el-button size="small" type="primary" text @click="toggleEditMode" class="edit-mode-btn"
            :class="{ active: isEditMode }">
            <el-icon>
              <Edit />
            </el-icon>
            {{ isEditMode ? '完成排序' : '调整顺序' }}
          </el-button>
        </el-tooltip>
      </div>

      <div class="categories-container" :class="{ 'edit-mode': isEditMode }">
        <el-radio-group v-model="selectedCategory" @change="handleCategoryChange">
          <!-- 全部分类（不可拖拽） -->
          <div class="category-item all-category" :class="{ selected: selectedCategory === '' }">
            <el-tooltip content="显示所有分类的网站" placement="top" :show-after="300">
              <el-radio-button label="" class="all-category-btn">
                <div class="btn-content">
                  <el-icon class="category-icon">
                    <Grid />
                  </el-icon>
                  <span class="category-text">全部</span>
                  <el-tag size="small" type="info" class="count-tag" :class="{ pulse: selectedCategory === '' }">
                    {{ getTotalCount() }}
                  </el-tag>
                </div>
              </el-radio-button>
            </el-tooltip>
          </div>

          <!-- 可拖拽的分类 -->
          <div v-for="(category, index) in sortedCategories" :key="category.id" class="category-item" :class="{
            selected: selectedCategory === category.id,
            dragging: draggedIndex === index,
            'drag-over': draggedIndex !== -1 && draggedIndex !== index,
          }" draggable="true" @dragstart="handleDragStart(index)" @dragover.prevent @drop="handleDrop(index)"
            @dragenter.prevent @dragend="handleDragEnd" @contextmenu.prevent="showActionButtons(category, $event)"
            @mouseleave="hideActionButtons(category.id)">
            <el-tooltip :content="category.description || '暂无描述'" placement="top" :show-after="300"
              :disabled="!category.description">
              <el-radio-button :label="category.id" class="category-btn">
                <div class="btn-content">
                  <div class="category-color-dot" :style="{ backgroundColor: category.color }"></div>
                  <span class="category-text">{{ category.name }}</span>
                  <el-tag size="small" type="info" class="count-tag"
                    :class="{ pulse: selectedCategory === category.id }">
                    {{ category.websiteCount ?? 0 }}
                  </el-tag>
                </div>
              </el-radio-button>
            </el-tooltip>

            <!-- 修改按钮 -->
            <el-button v-if="showEditButtons[category.id]" size="small" type="primary" circle class="edit-category-btn"
              @click.stop="editCategory(category)" @mouseenter="showEditButtons[category.id] = true">
              <el-icon>
                <Edit />
              </el-icon>
            </el-button>

            <!-- 删除按钮 -->
            <el-button v-if="showDeleteButtons[category.id]" size="small" type="danger" circle
              class="delete-category-btn" @click.stop="deleteCategory(category)"
              @mouseenter="showDeleteButtons[category.id] = true">
              <el-icon>
                <Delete />
              </el-icon>
            </el-button>

            <!-- 拖拽手柄 -->
            <el-tooltip content="拖拽调整分类顺序" placement="top" :show-after="300">
              <div v-if="isEditMode" class="drag-handle" :class="{ visible: isEditMode }">
                <el-icon>
                  <Rank />
                </el-icon>
              </div>
            </el-tooltip>
          </div>
        </el-radio-group>
      </div>
    </div>

    <!-- 搜索框 -->
    <div class="search-section">
      <div class="search-container">
        <div class="search-header">
          <div class="search-title-container">
            <el-icon class="search-title-icon">
              <Search />
            </el-icon>
            <span class="search-title">智能搜索</span>
            <div class="search-title-decoration"></div>
          </div>
          <span class="search-subtitle">快速找到您需要的网站资源</span>
        </div>
        <div class="search-input-wrapper">
          <el-input v-model="searchKeyword" placeholder="搜索网站名称、描述或URL..." clearable @input="handleSearch"
            @focus="handleSearchFocus" @blur="handleSearchBlur" class="search-input"
            :class="{ focused: isSearchFocused }">
            <template #prefix>
              <el-icon class="search-icon">
                <Search />
              </el-icon>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <!-- 网站列表 -->
    <div class="websites-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="websites-grid">
          <div v-for="n in pageSize" :key="n" class="website-card skeleton">
            <el-skeleton :rows="4" animated />
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="websites.length === 0" class="empty-state">
        <el-empty description="暂无网站" :image-size="120">
          <el-button type="primary" @click="showAddWebsiteDialog"> 添加第一个网站 </el-button>
        </el-empty>
      </div>

      <!-- 网站列表 -->
      <div v-else class="websites-grid">
        <div v-for="website in websites" :key="website.id" class="website-card" @click="visitWebsite(website)">
          <div class="card-header">
            <div class="website-icon">
              <img v-if="website.icon" :src="website.icon" :alt="website.name" @error="handleIconError" />
              <el-icon v-else>
                <Link />
              </el-icon>
            </div>
            <div class="website-info">
              <h3 class="website-name">{{ website.name }}</h3>
            </div>
            <div class="card-actions">
              <el-button size="small" circle @click.stop="copyWebsiteUrl(website)" class="copy-btn">
                <el-icon>
                  <DocumentCopy />
                </el-icon>
              </el-button>
              <el-button size="small" circle @click.stop="editWebsite(website)">
                <el-icon>
                  <Edit />
                </el-icon>
              </el-button>
              <el-button size="small" type="danger" circle @click.stop="deleteWebsite(website)">
                <el-icon>
                  <Delete />
                </el-icon>
              </el-button>
            </div>
          </div>

          <div class="card-body">
            <div class="website-description expanded">
              {{ website.description }}
            </div>
          </div>

          <div class="card-footer">
            <div class="website-meta">
              <span class="created-time">
                {{ formatDate(website.createdAt) }}
              </span>
            </div>
            <div class="category-badge">
              <!-- 支持多分类显示 -->
              <template v-if="website.categoryIds && website.categoryIds.length > 0">
                <el-tag v-for="categoryId in website.categoryIds" :key="categoryId"
                  :color="getCategoryColor(categoryId)" effect="dark" size="small" class="category-tag">
                  {{ getCategoryName(categoryId) }}
                </el-tag>
              </template>
              <el-tag v-else color="#909399" effect="dark" size="small"> 未分类 </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <!-- 分页信息 -->
      <div class="pagination-info">
        <span class="pagination-text">
          <span v-if="total > 0">
            显示第 {{ (currentPage - 1) * pageSize + 1 }}-{{
              Math.min(currentPage * pageSize, total)
            }}
            条， 共 {{ total }} 条记录
          </span>
          <span v-else>暂无数据</span>
        </span>
        <span class="pagination-pages" v-if="total > 0">
          第 {{ currentPage }} 页，共 {{ totalPages }} 页
        </span>
        <span class="pagination-pages" v-else>共 0 页</span>
      </div>

      <!-- 分页控件 -->
      <div class="pagination-controls">
        <!-- 每页显示数量选择 -->
        <div class="page-size-selector">
          <span class="page-size-label">每页显示：</span>
          <el-select v-model="pageSize" @change="handleSizeChange" size="small">
            <el-option v-for="size in [12, 24, 48, 96]" :key="size" :label="`${size} 条`" :value="size" />
          </el-select>
        </div>

        <!-- 分页导航 -->
        <div class="pagination-navigation">
          <!-- 快速跳转 -->
          <div class="page-jumper">
            <span class="jumper-label">跳转到：</span>
            <el-input v-model="jumpPage" type="number" :min="1" :max="totalPages" size="small" class="jumper-input"
              @keyup.enter="handleJumpPage" @blur="handleJumpPage" />
            <span class="jumper-text">页</span>
          </div>

          <!-- 分页按钮 -->
          <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :background="true"
            layout="prev, pager, next" :total="total" :pager-count="7" @current-change="handlePageChange"
            class="main-pagination" />
        </div>
      </div>
    </div>

    <!-- 添加/编辑网站对话框 -->
    <el-dialog v-model="websiteDialogVisible" :title="editingWebsite ? '编辑网站' : '添加网站'" width="600px"
      :close-on-click-modal="false">
      <el-form ref="websiteFormRef" :model="websiteForm" :rules="websiteRules" label-width="80px">
        <el-form-item label="网站名称" prop="name">
          <el-input v-model="websiteForm.name" placeholder="请输入网站名称" />
        </el-form-item>

        <el-form-item label="网站URL" prop="url">
          <el-input v-model="websiteForm.url" placeholder="请输入网站URL" />
        </el-form-item>

        <el-form-item label="网站描述" prop="description">
          <el-input v-model="websiteForm.description" type="textarea" :rows="3" placeholder="请输入网站描述" />
        </el-form-item>

        <el-form-item label="所属分类" prop="categoryIds">
          <el-select v-model="websiteForm.categoryIds" placeholder="请选择分类" multiple collapse-tags collapse-tags-tooltip
            style="width: 100%">
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="网站图标" prop="icon">
          <el-input v-model="websiteForm.icon" placeholder="请输入图标URL（可选）" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="websiteDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveWebsite" :loading="saving"> 保存 </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加分类对话框 -->
    <el-dialog v-model="categoryDialogVisible" title="添加分类" width="500px" :close-on-click-modal="false">
      <el-form ref="categoryFormRef" :model="categoryForm" :rules="categoryRules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="分类描述" prop="description">
          <el-input v-model="categoryForm.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
        </el-form-item>

        <el-form-item label="分类颜色" prop="color">
          <el-color-picker v-model="categoryForm.color" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="categoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCategory" :loading="savingCategory">
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- AI生成分类对话框 -->
    <el-dialog v-model="aiGenerateCategoryDialogVisible" title="" width="600px" :close-on-click-modal="false"
      class="ai-category-dialog">
      <!-- 自定义标题栏 -->
      <template #header>
        <div class="ai-dialog-header">
          <div class="ai-header-content">
            <div class="ai-header-icon">
              <el-icon class="ai-icon">
                <Star />
              </el-icon>
              <div class="ai-icon-glow"></div>
            </div>
            <div class="ai-header-text">
              <h3 class="ai-title">AI一键生成分类</h3>
              <p class="ai-subtitle">让AI为您智能生成分类描述和颜色</p>
            </div>
          </div>
          <div class="ai-header-decoration">
            <div class="decoration-dot"></div>
            <div class="decoration-dot"></div>
            <div class="decoration-dot"></div>
          </div>
        </div>
      </template>

      <!-- 对话框内容 -->
      <div class="ai-dialog-content">
        <!-- AI生成状态指示器 -->
        <div class="ai-status-indicator" v-if="aiGenerating">
          <div class="ai-loading-animation">
            <div class="ai-loading-circle"></div>
            <div class="ai-loading-circle"></div>
            <div class="ai-loading-circle"></div>
          </div>
          <p class="ai-loading-text">AI正在思考中...</p>
        </div>

        <!-- 表单内容 -->
        <div class="ai-form-container" :class="{ 'ai-form-hidden': aiGenerating }">
          <el-form ref="aiCategoryFormRef" :model="aiCategoryForm" :rules="aiCategoryRules" label-width="100px"
            class="ai-category-form">
            <!-- 分类名称输入 -->
            <el-form-item label="分类名称" prop="name" class="ai-form-item">
              <div class="ai-input-wrapper">
                <el-input v-model="aiCategoryForm.name" placeholder="请输入分类名称" class="ai-input" :disabled="aiGenerating">
                  <template #prefix>
                    <el-icon class="input-icon">
                      <FolderAdd />
                    </el-icon>
                  </template>
                </el-input>
                <div class="ai-input-focus-border"></div>
              </div>
            </el-form-item>

            <!-- 分类描述 -->
            <el-form-item label="分类描述" prop="description" class="ai-form-item">
              <div class="ai-input-wrapper">
                <el-input v-model="aiCategoryForm.description" type="textarea" :rows="3" placeholder="请输入分类描述"
                  class="ai-textarea" v-if="aiGeneratedResult" :disabled="aiGenerating">
                  <template #prefix>
                    <el-icon class="input-icon">
                      <Document />
                    </el-icon>
                  </template>
                </el-input>
                <div v-else class="ai-placeholder-container">
                  <div class="ai-placeholder-content">
                    <el-icon class="placeholder-icon">
                      <MagicStick />
                    </el-icon>
                    <p class="placeholder-text">点击"AI生成"按钮后可以编辑描述</p>
                    <div class="placeholder-decoration"></div>
                  </div>
                </div>
                <div class="ai-input-focus-border"></div>
              </div>
            </el-form-item>

            <!-- 分类颜色 -->
            <el-form-item label="分类颜色" prop="color" class="ai-form-item">
              <div class="ai-input-wrapper">
                <el-color-picker v-model="aiCategoryForm.color" v-if="aiGeneratedResult" show-alpha
                  class="ai-color-picker" :disabled="aiGenerating" />
                <div v-else class="ai-placeholder-container">
                  <div class="ai-placeholder-content">
                    <el-icon class="placeholder-icon">
                      <Brush />
                    </el-icon>
                    <p class="placeholder-text">点击"AI生成"按钮后可以选择颜色</p>
                    <div class="placeholder-decoration"></div>
                  </div>
                </div>
                <div class="ai-input-focus-border"></div>
              </div>
            </el-form-item>
          </el-form>
        </div>

        <!-- AI生成结果展示 -->
        <div v-if="aiGeneratedResult" class="ai-result-showcase">
          <div class="ai-result-header">
            <el-icon class="result-icon">
              <Check />
            </el-icon>
            <span class="result-title">AI生成完成</span>
          </div>
          <div class="ai-result-content">
            <div class="result-item">
              <span class="result-label">描述：</span>
              <span class="result-value">{{ aiGeneratedResult.description }}</span>
            </div>
            <div class="result-item">
              <span class="result-label">颜色：</span>
              <div class="result-color" :style="{ backgroundColor: aiGeneratedResult.color }"></div>
              <span class="result-color-code">{{ aiGeneratedResult.color }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 自定义底部按钮 -->
      <template #footer>
        <div class="ai-dialog-footer">
          <el-button @click="aiGenerateCategoryDialogVisible = false" class="ai-cancel-btn" :disabled="aiGenerating">
            <el-icon>
              <Close />
            </el-icon>
            取消
          </el-button>
          <el-button type="primary" @click="generateCategoryWithAI" :loading="aiGenerating" v-if="!aiGeneratedResult"
            class="ai-generate-btn">
            <el-icon>
              <Star />
            </el-icon>
            <span v-if="!aiGenerating">AI生成</span>
            <span v-else>生成中...</span>
          </el-button>
          <el-button type="success" @click="saveAIGeneratedCategory" :loading="savingAICategory"
            v-if="aiGeneratedResult" class="ai-save-btn">
            <el-icon>
              <Check />
            </el-icon>
            保存分类
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- AI新增网站对话框 -->
    <el-dialog v-model="aiCreateWebsiteDialogVisible" title="AI一键新增网站" width="700px" :close-on-click-modal="false">
      <!-- 第一步：输入网站地址 -->
      <div v-if="!aiWebsiteResult" class="ai-step">
        <div class="step-header">
          <el-icon class="step-icon">
            <Link />
          </el-icon>
          <span class="step-title">第一步：输入网站地址</span>
        </div>
        <el-form ref="aiWebsiteFormRef" :model="aiWebsiteForm" :rules="aiWebsiteRules">
          <el-form-item label="网站地址" prop="url">
            <el-input v-model="aiWebsiteForm.url" placeholder="请输入网站地址（如：https://github.com）" size="large" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 第二步：AI生成结果和手动编辑 -->
      <div v-if="aiWebsiteResult" class="ai-step">
        <div class="step-header">
          <el-icon class="step-icon">
            <Edit />
          </el-icon>
          <span class="step-title">第二步：编辑网站信息</span>
          <el-tag type="success" size="small">AI已生成，可手动修改</el-tag>
        </div>

        <el-form ref="aiWebsiteEditFormRef" :model="aiWebsiteEditForm" :rules="aiWebsiteEditRules" label-width="100px">
          <el-form-item label="网站名称" prop="name">
            <el-input v-model="aiWebsiteEditForm.name" placeholder="请输入网站名称" show-word-limit maxlength="50" />
          </el-form-item>

          <el-form-item label="网站描述" prop="description">
            <el-input v-model="aiWebsiteEditForm.description" type="textarea" :rows="3" placeholder="请输入网站描述"
              show-word-limit maxlength="200" />
          </el-form-item>

          <el-form-item label="所属分类" prop="categoryIds">
            <el-select v-model="aiWebsiteEditForm.categoryIds" multiple collapse-tags collapse-tags-tooltip
              placeholder="请选择分类" style="width: 100%">
              <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id">
                <div class="category-option">
                  <div class="category-color-dot" :style="{ backgroundColor: category.color }"></div>
                  <span>{{ category.name }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="网站图标">
            <div class="icon-preview">
              <img v-if="lastScrapedInfo?.favicon" :src="lastScrapedInfo.favicon" :alt="aiWebsiteEditForm.name"
                class="favicon-preview" @error="handleFaviconError" />
              <el-icon v-else class="favicon-placeholder">
                <Link />
              </el-icon>
              <span class="icon-info">{{ lastScrapedInfo?.favicon || '未抓取到图标' }}</span>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="aiCreateWebsiteDialogVisible = false">取消</el-button>

          <!-- 第一步按钮 -->
          <template v-if="!aiWebsiteResult">
            <el-button type="primary" :loading="aiGeneratingWebsite" :disabled="aiGeneratingWebsite"
              @click="generateWebsiteWithAI">
              <el-icon>
                <Star />
              </el-icon>
              AI生成
            </el-button>
          </template>

          <!-- 第二步按钮 -->
          <template v-if="aiWebsiteResult">
            <el-button @click="regenerateWebsiteWithAI" :loading="aiGeneratingWebsite">
              <el-icon>
                <Refresh />
              </el-icon>
              重新生成
            </el-button>
            <el-button type="success" :loading="savingAIWebsite" @click="saveAIGeneratedWebsite">
              <el-icon>
                <Check />
              </el-icon>
              保存网站
            </el-button>
          </template>
        </div>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="400px" :close-on-click-modal="false">
      <div class="delete-confirm">
        <el-icon class="warning-icon" color="#E6A23C">
          <Warning />
        </el-icon>
        <p>确定要删除网站 "{{ deletingWebsite?.name }}" 吗？</p>
        <p class="delete-tip">此操作不可恢复</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete" :loading="deleting"> 确定删除 </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改分类对话框 -->
    <el-dialog v-model="editCategoryDialogVisible" title="修改分类" width="500px" :close-on-click-modal="false">
      <el-form ref="editCategoryFormRef" :model="editCategoryForm" :rules="categoryRules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="editCategoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="分类描述" prop="description">
          <el-input v-model="editCategoryForm.description" type="textarea" :rows="3" placeholder="请输入分类描述" />
        </el-form-item>

        <el-form-item label="分类颜色" prop="color">
          <el-color-picker v-model="editCategoryForm.color" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editCategoryDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveEditCategory" :loading="savingEditCategory">
            保存修改
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 删除分类确认对话框 -->
    <el-dialog v-model="deleteCategoryDialogVisible" title="确认删除分类" width="400px" :close-on-click-modal="false">
      <div class="delete-confirm">
        <el-icon class="warning-icon" color="#E6A23C">
          <Warning />
        </el-icon>
        <p>确定要删除分类 "{{ deletingCategoryData?.name }}" 吗？</p>
        <p class="delete-tip">此操作不可恢复，该分类下的网站将变为未分类状态</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteCategoryDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDeleteCategory" :loading="deletingCategory">
            确定删除
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 网站状态监控对话框 -->
    <el-dialog v-model="statusMonitorDialogVisible" title="网站状态监控" width="900px" :close-on-click-modal="false">
      <div class="status-monitor-content">
        <!-- 监控控制面板 -->
        <div class="monitor-control-panel">
          <div class="control-header">
            <h3 class="control-title">
              <el-icon class="control-icon">
                <Monitor />
              </el-icon>
              监控控制
            </h3>
            <div class="control-actions">
              <el-button type="primary" @click="startBatchStatusCheck" :loading="checkingStatus"
                :disabled="checkingStatus">
                <el-icon>
                  <Refresh />
                </el-icon>
                开始批量检查
              </el-button>
              <el-button type="success" @click="toggleAutoMonitoring" :class="{ 'monitoring-active': autoMonitoring }">
                <el-icon>
                  <VideoPlay />
                </el-icon>
                {{ autoMonitoring ? '停止自动监控' : '启动自动监控' }}
              </el-button>
            </div>
          </div>

          <div class="monitor-settings">
            <div class="settings-grid">
              <div class="setting-item">
                <label class="setting-label">检查间隔</label>
                <div class="setting-control">
                  <el-select v-model="monitorSettings.checkInterval" :disabled="autoMonitoring" placeholder="选择检查间隔"
                    class="setting-select">
                    <el-option label="5分钟" value="5" />
                    <el-option label="15分钟" value="15" />
                    <el-option label="30分钟" value="30" />
                    <el-option label="1小时" value="60" />
                    <el-option label="6小时" value="360" />
                    <el-option label="12小时" value="720" />
                    <el-option label="24小时" value="1440" />
                  </el-select>
                  <span class="setting-unit">分钟</span>
                </div>
              </div>

              <div class="setting-item">
                <label class="setting-label">超时时间</label>
                <div class="setting-control">
                  <el-input-number v-model="monitorSettings.timeout" :min="5" :max="60" :disabled="autoMonitoring"
                    placeholder="超时时间" class="setting-input" />
                  <span class="setting-unit">秒</span>
                </div>
              </div>

              <div class="setting-item">
                <label class="setting-label">重试次数</label>
                <div class="setting-control">
                  <el-input-number v-model="monitorSettings.retryCount" :min="1" :max="5" :disabled="autoMonitoring"
                    placeholder="重试次数" class="setting-input" />
                  <span class="setting-unit">次</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 状态统计 -->
        <div class="status-statistics">
          <div class="statistics-header">
            <h3 class="statistics-title">
              <el-icon class="statistics-icon">
                <DataAnalysis />
              </el-icon>
              状态统计
            </h3>
            <el-button size="small" @click="refreshStatistics">
              <el-icon>
                <Refresh />
              </el-icon>
              刷新统计
            </el-button>
          </div>

          <div class="statistics-cards">
            <div class="stat-card total">
              <div class="stat-icon">
                <el-icon>
                  <Link />
                </el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statusStatistics.total }}</div>
                <div class="stat-label">总网站数</div>
              </div>
            </div>

            <div class="stat-card active">
              <div class="stat-icon">
                <el-icon>
                  <Check />
                </el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statusStatistics.active }}</div>
                <div class="stat-label">正常</div>
              </div>
            </div>

            <div class="stat-card inactive">
              <div class="stat-icon">
                <el-icon>
                  <Warning />
                </el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statusStatistics.inactive }}</div>
                <div class="stat-label">异常</div>
              </div>
            </div>

            <div class="stat-card broken">
              <div class="stat-icon">
                <el-icon>
                  <Close />
                </el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statusStatistics.broken }}</div>
                <div class="stat-label">失效</div>
              </div>
            </div>

            <div class="stat-card unknown">
              <div class="stat-icon">
                <el-icon>
                  <QuestionFilled />
                </el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ statusStatistics.unknown }}</div>
                <div class="stat-label">未知</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 状态列表 -->
        <div class="status-list">
          <div class="list-header">
            <h3 class="list-title">
              <el-icon class="list-icon">
                <List />
              </el-icon>
              网站状态列表
            </h3>
            <div class="list-filters">
              <el-select v-model="statusFilter" placeholder="状态筛选" size="small">
                <el-option label="全部状态" value="" />
                <el-option label="正常" value="active" />
                <el-option label="异常" value="inactive" />
                <el-option label="失效" value="broken" />
                <el-option label="未知" value="unknown" />
              </el-select>
              <el-input v-model="statusSearchKeyword" placeholder="搜索网站名称或URL" size="small" clearable
                style="width: 200px">
                <template #prefix>
                  <el-icon>
                    <Search />
                  </el-icon>
                </template>
              </el-input>
            </div>
          </div>

          <div class="status-table">
            <el-table :data="filteredStatusList" style="width: 100%" :row-class-name="getStatusRowClass"
              v-loading="loadingStatusList">
              <el-table-column prop="name" label="网站名称" min-width="150">
                <template #default="{ row }">
                  <div class="website-name-cell">
                    <span class="website-name">{{ row.name }}</span>
                    <el-tag :type="getStatusTagType(row.status)" size="small" class="status-tag">
                      {{ getStatusText(row.status) }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="url" label="URL" min-width="200">
                <template #default="{ row }">
                  <div class="url-cell">
                    <span class="url-text">{{ row.url }}</span>
                    <el-button size="small" type="primary" text @click="testWebsite(row)" :loading="row.testing">
                      <el-icon>
                        <Link />
                      </el-icon>
                      测试
                    </el-button>
                  </div>
                </template>
              </el-table-column>

              <el-table-column prop="lastCheckTime" label="最后检查" width="150">
                <template #default="{ row }">
                  <span class="check-time">
                    {{ row.lastCheckTime ? formatDateTime(row.lastCheckTime) : '从未检查' }}
                  </span>
                </template>
              </el-table-column>

              <el-table-column prop="responseTime" label="响应时间" width="100">
                <template #default="{ row }">
                  <span v-if="row.responseTime" class="response-time">
                    {{ row.responseTime }}ms
                  </span>
                  <span v-else class="no-response">-</span>
                </template>
              </el-table-column>

              <el-table-column prop="status" label="操作" width="120">
                <template #default="{ row }">
                  <el-button size="small" type="primary" @click="checkSingleWebsite(row)" :loading="row.checking">
                    检查
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="statusMonitorDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="exportStatusReport" :disabled="!statusList.length">
            导出状态报告
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog v-model="batchImportDialogVisible" title="批量导入网站" width="700px" :close-on-click-modal="false">
      <div class="batch-import-content">
        <!-- 导入方式选择 -->
        <div class="import-methods">
          <div class="method-section">
            <h3 class="method-title">
              <el-icon class="method-icon">
                <Upload />
              </el-icon>
              选择导入方式
            </h3>
            <div class="method-options">
              <el-radio-group v-model="importMethod" @change="handleImportMethodChange">
                <el-radio-button label="bookmarks">浏览器书签</el-radio-button>
                <el-radio-button label="file">文件上传</el-radio-button>
                <el-radio-button label="manual">手动输入</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <!-- 浏览器书签导入 -->
          <div v-if="importMethod === 'bookmarks'" class="import-section">
            <div class="section-header">
              <el-icon class="section-icon">
                <Link />
              </el-icon>
              <span class="section-title">从浏览器书签导入</span>
            </div>
            <div class="section-content">
              <p class="import-tip">请按照以下步骤操作：</p>
              <ol class="import-steps">
                <li>
                  在浏览器中按 <kbd>Ctrl+Shift+O</kbd>（Windows）或
                  <kbd>Cmd+Shift+O</kbd>（Mac）打开书签管理器
                </li>
                <li>选择要导出的书签文件夹</li>
                <li>右键选择"导出书签"或"导出到HTML文件"</li>
                <li>将导出的HTML文件拖拽到下方区域，或点击选择文件</li>
              </ol>
              <div class="file-upload-area">
                <el-upload ref="bookmarkUploadRef" :auto-upload="false" :show-file-list="false" accept=".html,.htm"
                  :on-change="handleBookmarkFileChange" drag class="bookmark-upload">
                  <el-icon class="upload-icon">
                    <Upload />
                  </el-icon>
                  <div class="upload-text">
                    <span>将书签HTML文件拖拽到此处，或<em>点击上传</em></span>
                  </div>
                  <template #tip>
                    <div class="upload-tip">支持 .html 和 .htm 格式的书签文件</div>
                  </template>
                </el-upload>
              </div>
            </div>
          </div>

          <!-- 文件上传导入 -->
          <div v-if="importMethod === 'file'" class="import-section">
            <div class="section-header">
              <el-icon class="section-icon">
                <Document />
              </el-icon>
              <span class="section-title">从文件导入</span>
            </div>
            <div class="section-content">
              <p class="import-tip">支持以下格式：</p>
              <ul class="format-list">
                <li><strong>CSV文件</strong>：包含网站名称、URL、描述、分类等列</li>
                <li>
                  <strong>JSON文件</strong>：包含网站数组，每个网站有name、url、description等字段
                </li>
                <li><strong>HTML文件</strong>：浏览器导出的书签文件</li>
              </ul>
              <div class="file-upload-area">
                <el-upload ref="fileUploadRef" :auto-upload="false" :show-file-list="false"
                  accept=".csv,.json,.html,.htm" :on-change="handleFileUpload" drag class="file-upload">
                  <el-icon class="upload-icon">
                    <Upload />
                  </el-icon>
                  <div class="upload-text">
                    <span>将文件拖拽到此处，或<em>点击上传</em></span>
                  </div>
                  <template #tip>
                    <div class="upload-tip">支持 .csv、.json、.html、.htm 格式</div>
                  </template>
                </el-upload>
              </div>
            </div>
          </div>

          <!-- 手动输入导入 -->
          <div v-if="importMethod === 'manual'" class="import-section">
            <div class="section-header">
              <el-icon class="section-icon">
                <Edit />
              </el-icon>
              <span class="section-title">手动输入网站信息</span>
            </div>
            <div class="section-content">
              <p class="import-tip">每行输入一个网站，格式：网站名称 | URL | 描述 | 分类（可选）</p>
              <el-input v-model="manualInputText" type="textarea" :rows="8"
                placeholder="示例：&#10;GitHub | https://github.com | 代码托管平台 | 开发工具&#10;Stack Overflow | https://stackoverflow.com | 程序员问答社区 | 学习资源"
                class="manual-input" />
              <div class="manual-input-actions">
                <el-button type="primary" @click="parseManualInput" :disabled="!manualInputText.trim()">
                  解析输入
                </el-button>
                <el-button @click="manualInputText = ''"> 清空输入 </el-button>
              </div>
            </div>
          </div>

          <!-- 导入预览 -->
          <div v-if="importPreview.length > 0" class="import-preview">
            <div class="preview-header">
              <h4>导入预览（{{ importPreview.length }}个网站）</h4>
              <el-button size="small" @click="clearImportPreview">清空预览</el-button>
            </div>
            <div class="preview-list">
              <div v-for="(item, index) in importPreview" :key="index" class="preview-item"
                :class="{ 'preview-item-error': item.error }">
                <div class="preview-item-header">
                  <span class="preview-index">{{ index + 1 }}</span>
                  <span class="preview-name">{{ item.name || '未命名' }}</span>
                  <span class="preview-url">{{ item.url || '无URL' }}</span>
                  <el-tag v-if="item.error" type="danger" size="small">{{ item.error }}</el-tag>
                </div>
                <div v-if="item.description" class="preview-description">
                  {{ item.description }}
                </div>
                <div v-if="item.categoryIds && item.categoryIds.length > 0" class="preview-categories">
                  <el-tag v-for="categoryId in item.categoryIds" :key="categoryId" :color="getCategoryColor(categoryId)"
                    effect="dark" size="small">
                    {{ getCategoryName(categoryId) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchImportDialogVisible = false">取消</el-button>
          <el-button v-if="importPreview.length > 0" type="primary" @click="startImport" :loading="importing"
            :disabled="!canImport">
            开始导入
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import {
  Link,
  Plus,
  FolderAdd,
  Search,
  Warning,
  Edit,
  Delete,
  Rank,
  Star,
  Menu,
  Grid,
  Refresh,
  Check,
  InfoFilled,
  Upload,
  Document,
  Monitor,
  VideoPlay,
  DataAnalysis,
  List,
  QuestionFilled,
  Close,
  MagicStick,
  Brush,
  DocumentCopy,
} from '@element-plus/icons-vue'
import {
  websiteApi,
  categoryApi,
  type Website,
  type Category,
  type WebsiteCreateRequest,
  type CategoryCreateRequest,
} from '../utils/websiteApi'
import { generateCategoryWithOllama, generateWebsiteInfoWithOllama } from '../utils/aiService'

// 响应式数据
const websites = ref<Website[]>([])
const categories = ref<Category[]>([])

const selectedCategory = ref<number | ''>('')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)
const totalAllWebsites = ref(0) // 存储所有网站的总数量，不受分类筛选影响，用于"全部"分类显示
const loading = ref(false)
const jumpPage = ref<number>(1)
const isSearchFocused = ref(false) // 搜索框焦点状态

// 拖拽排序相关状态
const isEditMode = ref(false)
const draggedIndex = ref<number>(-1)
const originalCategories = ref<Category[]>([])

// 对话框状态
const websiteDialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const deleteCategoryDialogVisible = ref(false)
const aiGenerateCategoryDialogVisible = ref(false)
const editingWebsite = ref<Website | null>(null)
const deletingWebsite = ref<Website | null>(null)
const saving = ref(false)
const deletingCategory = ref(false)
const deletingCategoryData = ref<Category | null>(null)
const showDeleteButtons = ref<{ [key: number]: boolean }>({})
const showEditButtons = ref<{ [key: number]: boolean }>({})
const editCategoryDialogVisible = ref(false)
const savingEditCategory = ref(false)
const editingCategoryData = ref<Category | null>(null)
const editCategoryForm = ref({
  name: '',
  description: '',
  color: '#409EFF',
})

// 批量导入相关状态
const batchImportDialogVisible = ref(false)
const importMethod = ref('bookmarks')
const importPreview = ref<
  Array<{
    name: string
    url: string
    description: string
    categoryIds: number[]
    error?: string
  }>
>([])
const manualInputText = ref('')
const importing = ref(false)
const canImport = ref(false)

// 网站状态监控相关状态
const statusMonitorDialogVisible = ref(false)
const autoMonitoring = ref(false)
const checkingStatus = ref(false)
const loadingStatusList = ref(false)
const statusFilter = ref('')
const statusSearchKeyword = ref('')

const monitorSettings = ref({
  checkInterval: '30',
  timeout: 10,
  retryCount: 2,
})

const statusStatistics = ref({
  total: 0,
  active: 0,
  inactive: 0,
  broken: 0,
  unknown: 0,
})

const statusList = ref<
  Array<{
    id: number
    name: string
    url: string
    status: string
    lastCheckTime?: string
    responseTime?: number
    checking?: boolean
    testing?: boolean
  }>
>([])

let monitoringTimer: NodeJS.Timeout | null = null

// 占位图（当网站图标加载失败时使用）
const FALLBACK_ICON_DATA_URL =
  'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 64 64"><defs><linearGradient id="g" x1="0" x2="1" y1="0" y2="1"><stop stop-color="%23667eea" offset="0%"/><stop stop-color="%23764ba2" offset="100%"/></linearGradient></defs><rect rx="12" ry="12" width="64" height="64" fill="url(%23g)"/><g fill="white" opacity="0.95"><circle cx="32" cy="32" r="16" fill="none" stroke="white" stroke-width="3"/><path d="M16 32h32M32 16v32" stroke="white" stroke-width="3" stroke-linecap="round"/></g></svg>'
const savingCategory = ref(false)
const deleting = ref(false)
const aiGenerating = ref(false)
const savingAICategory = ref(false)

// AI新增网站相关状态
const aiCreateWebsiteDialogVisible = ref(false)
const aiGeneratingWebsite = ref(false)
const savingAIWebsite = ref(false)
const aiWebsiteForm = ref({
  url: '',
})
const aiWebsiteResult = ref<{
  name: string
  description: string
  categoryId: number
  icon: string
} | null>(null)

// AI网站编辑表单
const aiWebsiteEditForm = ref({
  name: '',
  description: '',
  categoryIds: [] as number[],
})

// AI网站编辑表单引用
const aiWebsiteEditFormRef = ref<FormInstance>()

// 保存最近一次从后端抓取的网站信息（包含 favicon）
const lastScrapedInfo = ref<{
  title?: string
  description?: string
  keywords?: string
  favicon?: string
} | null>(null)

// 表单引用
const websiteFormRef = ref<FormInstance>()
const categoryFormRef = ref<FormInstance>()
const editCategoryFormRef = ref<FormInstance>()
const aiCategoryFormRef = ref<FormInstance>()
const aiWebsiteFormRef = ref<FormInstance>()

// 表单数据
const websiteForm = ref<WebsiteCreateRequest>({
  name: '',
  url: '',
  description: '',
  categoryIds: [],
  icon: '',
})

const categoryForm = ref<CategoryCreateRequest>({
  name: '',
  description: '',
  color: '#409EFF',
  sortOrder: 0,
})

const aiCategoryForm = ref({
  name: '',
  description: '',
  color: '#409EFF',
  sortOrder: 0,
})

const aiGeneratedResult = ref<{
  description: string
  color: string
} | null>(null)

// 表单验证规则
const websiteRules = {
  name: [{ required: true, message: '请输入网站名称', trigger: 'blur' }],
  url: [
    { required: false, message: '请输入网站URL', trigger: 'blur' },
    {
      validator: (rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (value && value.trim() !== '') {
          // 如果URL不为空，则验证格式
          const urlPattern = /^https?:\/\/\S+$/i
          if (!urlPattern.test(value)) {
            callback(new Error('请输入有效的 http/https URL'))
          } else {
            callback()
          }
        } else {
          // 如果URL为空，则通过验证
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  description: [{ required: true, message: '请输入网站描述', trigger: 'blur' }],
  categoryIds: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

const aiWebsiteRules = {
  url: [{ required: true, message: '请输入网站地址', trigger: 'blur' }],
}

const aiWebsiteEditRules = {
  name: [{ required: true, message: '请输入网站名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入网站描述', trigger: 'blur' }],
  categoryIds: [{ required: true, message: '请选择分类', trigger: 'change' }],
}

const categoryRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入分类描述', trigger: 'blur' }],
  color: [{ required: true, message: '请选择分类颜色', trigger: 'change' }],
}

const aiCategoryRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入分类描述', trigger: 'blur' }],
  color: [{ required: true, message: '请选择分类颜色', trigger: 'change' }],
}

// 计算属性
const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 排序后的分类列表
const sortedCategories = computed(() => {
  return [...categories.value].sort((a, b) => a.sortOrder - b.sortOrder)
})

// 加载网站列表
const loadWebsites = async () => {
  loading.value = true
  try {
    const requestParams = {
      page: currentPage.value,
      size: pageSize.value,
      categoryIds: selectedCategory.value === '' ? undefined : [selectedCategory.value],
      keyword: searchKeyword.value || undefined,
    }
    console.log('loadWebsites 请求参数:', requestParams)
    console.log('categoryIds 值:', requestParams.categoryIds)
    console.log('categoryIds 类型:', typeof requestParams.categoryIds)

    const response = await websiteApi.getWebsites(requestParams)
    websites.value = response.websites || []
    total.value = response.totalCount || 0

    // 只在第一次加载时更新所有网站的总数量
    if (totalAllWebsites.value === 0) {
      totalAllWebsites.value = response.totalCount || 0
    }
  } catch (error) {
    ElMessage.error('加载网站列表失败')
    console.error('加载网站失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await categoryApi.getAllCategories()
    categories.value = response || []
  } catch (error) {
    ElMessage.error('加载分类列表失败')
    console.error('加载分类失败:', error)
  }
}

// 加载所有网站的总数量（不受分类筛选影响）
const loadTotalAllWebsites = async () => {
  try {
    // 调用API获取所有网站的总数量，不传任何筛选条件
    const response = await websiteApi.getWebsites({
      page: 1,
      size: 1, // 只需要获取总数，不需要实际数据
    })
    totalAllWebsites.value = response.totalCount || 0
  } catch (error) {
    console.error('加载所有网站总数失败:', error)
    // 如果失败，不影响主要功能，使用默认值
    totalAllWebsites.value = 0
  }
}

// 获取所有分类的总网站数量（来自后端返回的 websiteCount）
const getTotalCount = () => {
  // 始终返回所有网站的总数量，不受分类筛选影响
  const count = totalAllWebsites.value
  console.log('getTotalCount 返回:', count, '(不受分类筛选影响)')
  return count
}

// 拖拽排序相关方法
const toggleEditMode = () => {
  isEditMode.value = !isEditMode.value
  if (isEditMode.value) {
    // 进入编辑模式时，保存原始顺序
    originalCategories.value = [...categories.value]
    ElMessage.info('拖拽分类项目可调整顺序，完成后点击"完成排序"保存')
  } else {
    // 退出编辑模式时，保存排序结果
    saveCategoryOrder()
  }
}

const handleDragStart = (index: number) => {
  if (!isEditMode.value) return
  draggedIndex.value = index
}

const handleDrop = (targetIndex: number) => {
  if (!isEditMode.value || draggedIndex.value === -1) return

  const sourceIndex = draggedIndex.value
  if (sourceIndex === targetIndex) return

  // 重新排序分类
  const newCategories = [...categories.value]
  const [movedCategory] = newCategories.splice(sourceIndex, 1)
  newCategories.splice(targetIndex, 0, movedCategory)

  // 更新排序字段
  newCategories.forEach((category, index) => {
    category.sortOrder = index
  })

  categories.value = newCategories
  draggedIndex.value = -1
}

const handleDragEnd = () => {
  draggedIndex.value = -1
}

const saveCategoryOrder = async () => {
  try {
    // 调用后端API保存分类顺序
    const orderData = categories.value.map((category, index) => ({
      id: category.id,
      sortOrder: index,
    }))

    // 调用后端API保存分类顺序
    await categoryApi.updateCategoryOrder(orderData)

    // 重新加载分类列表以确保数据同步
    await loadCategories()

    ElMessage.success('分类顺序保存成功')
  } catch (error) {
    ElMessage.error('保存分类顺序失败')
    console.error('保存分类顺序失败:', error)

    // 恢复原始顺序
    categories.value = [...originalCategories.value]
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 事件处理方法
const handleCategoryChange = async () => {
  console.log('handleCategoryChange 被调用，selectedCategory:', selectedCategory.value)
  console.log('selectedCategory 类型:', typeof selectedCategory.value)

  currentPage.value = 1
  jumpPage.value = 1
  await loadWebsites()
}

// AI新增网站相关方法
const showAICreateWebsiteDialog = () => {
  aiCreateWebsiteDialogVisible.value = true
  aiWebsiteForm.value.url = ''
  aiWebsiteResult.value = null
}

const generateWebsiteWithAI = async () => {
  if (!aiWebsiteFormRef.value) return

  // 并发防护：避免重复触发
  if (aiGeneratingWebsite.value) return

  try {
    await aiWebsiteFormRef.value.validate()
    aiGeneratingWebsite.value = true

    // 抓取网站信息（API内部已经处理了重试逻辑）
    let scrapedInfo: {
      title?: string
      description?: string
      keywords?: string
      favicon?: string
    } | null = null

    try {
      scrapedInfo = await websiteApi.scrapeWebsiteInfo(aiWebsiteForm.value.url)
      console.log('网站信息抓取成功:', scrapedInfo)
    } catch (error) {
      console.error('网站信息抓取失败:', error)
      ElMessage.error('网站信息抓取失败，请检查网址后重试')
      throw error
    }

    lastScrapedInfo.value = scrapedInfo

    // 调用AI生成网站信息，传递完整的抓取信息
    const result = await generateWebsiteInfoWithOllama(
      aiWebsiteForm.value.url,
      scrapedInfo || {},
      categories.value,
    )

    aiWebsiteResult.value = result

    // 将AI生成的结果填充到编辑表单中
    aiWebsiteEditForm.value = {
      name: result.name,
      description: result.description,
      categoryIds: [result.categoryId],
    }

    ElMessage.success('AI生成完成，请检查并编辑信息')
  } catch (error) {
    ElMessage.error('AI生成失败，请重试')
    console.error('AI生成失败:', error)
  } finally {
    aiGeneratingWebsite.value = false
  }
}

const regenerateWebsiteWithAI = async () => {
  await generateWebsiteWithAI()
}

const handleFaviconError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

const saveAIGeneratedWebsite = async () => {
  if (!aiWebsiteEditFormRef.value) return

  try {
    await aiWebsiteEditFormRef.value.validate()
    savingAIWebsite.value = true

    // 准备网站创建请求，使用编辑表单的数据
    const websiteRequest: WebsiteCreateRequest = {
      name: aiWebsiteEditForm.value.name,
      url: aiWebsiteForm.value.url,
      description: aiWebsiteEditForm.value.description,
      categoryIds: aiWebsiteEditForm.value.categoryIds,
      // 仅使用后端抓取到的 favicon（网络链接），不使用 AI 返回的占位图标名
      icon: lastScrapedInfo.value?.favicon || '',
      favicon: lastScrapedInfo.value?.favicon,
    }

    // 创建网站
    await websiteApi.createWebsite(websiteRequest)

    ElMessage.success('网站创建成功')
    aiCreateWebsiteDialogVisible.value = false

    // 重新加载数据
    await loadWebsites()
    await loadCategories()
  } catch (error) {
    ElMessage.error('保存网站失败')
    console.error('保存网站失败:', error)
  } finally {
    savingAIWebsite.value = false
  }
}

const getCategoryName = (categoryId: number) => {
  const category = categories.value.find((c) => c.id === categoryId)
  return category ? category.name : '未知分类'
}

const getCategoryColor = (categoryId: number) => {
  const category = categories.value.find((c) => c.id === categoryId)
  return category ? category.color : '#909399'
}

const handleSearch = async () => {
  currentPage.value = 1
  jumpPage.value = 1
  await loadWebsites()
}

// 搜索框焦点处理方法
const handleSearchFocus = () => {
  isSearchFocused.value = true
}

const handleSearchBlur = () => {
  isSearchFocused.value = false
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  jumpPage.value = page
  loadWebsites()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  jumpPage.value = 1
  loadWebsites()
}

const handleJumpPage = () => {
  const page = parseInt(jumpPage.value.toString())
  if (page && page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    loadWebsites()
  } else {
    jumpPage.value = currentPage.value
    ElMessage.warning('请输入有效的页码')
  }
}

// 网站管理方法
const showAddWebsiteDialog = () => {
  editingWebsite.value = null
  resetWebsiteForm()
  websiteDialogVisible.value = true
}

const editWebsite = (website: Website) => {
  editingWebsite.value = { ...website }
  websiteForm.value = {
    name: website.name,
    url: website.url,
    description: website.description,
    categoryIds: website.categoryIds || [], // 使用现有的categoryIds
    icon: website.icon || '',
  }
  websiteDialogVisible.value = true
}

const resetWebsiteForm = () => {
  websiteForm.value = {
    name: '',
    url: '',
    description: '',
    categoryIds: [],
    icon: '',
  }
}

const saveWebsite = async () => {
  if (!websiteFormRef.value) return

  try {
    await websiteFormRef.value.validate()
    saving.value = true

    if (editingWebsite.value) {
      // 编辑现有网站
      await websiteApi.updateWebsite(editingWebsite.value.id, websiteForm.value)
      ElMessage.success('网站更新成功')
    } else {
      // 添加新网站
      await websiteApi.createWebsite(websiteForm.value)
      ElMessage.success('网站添加成功')
    }

    websiteDialogVisible.value = false
    editingWebsite.value = null
    resetWebsiteForm()
    loadWebsites() // 重新加载数据
  } catch (error) {
    ElMessage.error('保存失败，请重试')
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

const deleteWebsite = (website: Website) => {
  deletingWebsite.value = website
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  if (!deletingWebsite.value) return

  deleting.value = true
  try {
    await websiteApi.deleteWebsite(deletingWebsite.value.id)

    ElMessage.success('网站删除成功')
    deleteDialogVisible.value = false
    deletingWebsite.value = null
    loadWebsites() // 重新加载数据
  } catch (error) {
    ElMessage.error('删除失败，请重试')
    console.error('删除失败:', error)
  } finally {
    deleting.value = false
  }
}

const visitWebsite = async (website: Website) => {
  try {
    // 在新标签页打开网站
    window.open(website.url, '_blank')
  } catch (error) {
    console.error('打开网站失败:', error)
  }
}

const copyWebsiteUrl = async (website: Website) => {
  try {
    // 使用现代浏览器的 Clipboard API
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(website.url)
      ElMessage.success('网站地址已复制到剪贴板')
    } else {
      // 降级方案：使用传统的 document.execCommand
      const textArea = document.createElement('textarea')
      textArea.value = website.url
      textArea.style.position = 'fixed'
      textArea.style.left = '-999999px'
      textArea.style.top = '-999999px'
      document.body.appendChild(textArea)
      textArea.focus()
      textArea.select()

      try {
        document.execCommand('copy')
        ElMessage.success('网站地址已复制到剪贴板')
      } catch (err) {
        ElMessage.error('复制失败，请手动复制')
      } finally {
        document.body.removeChild(textArea)
      }
    }
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}

// 分类管理方法
const showAddCategoryDialog = () => {
  categoryForm.value = {
    name: '',
    description: '',
    color: '#409EFF',
    sortOrder: 0,
  }
  categoryDialogVisible.value = true
}

const showAIGenerateCategoryDialog = () => {
  aiCategoryForm.value = {
    name: '',
    description: '',
    color: '#409EFF',
    sortOrder: 0,
  }
  aiGeneratedResult.value = null
  aiGenerateCategoryDialogVisible.value = true
}

const saveCategory = async () => {
  if (!categoryFormRef.value) return

  try {
    await categoryFormRef.value.validate()
    savingCategory.value = true

    await categoryApi.createCategory(categoryForm.value)

    ElMessage.success('分类添加成功')
    categoryDialogVisible.value = false
    loadCategories() // 重新加载分类
  } catch (error) {
    ElMessage.error('保存失败，请重试')
    console.error('保存失败:', error)
  } finally {
    savingCategory.value = false
  }
}

const generateCategoryWithAI = async () => {
  if (!aiCategoryFormRef.value) return

  try {
    // 只验证分类名称字段，其他字段在AI生成后填充
    await aiCategoryFormRef.value.validateField('name')
    aiGenerating.value = true

    // 调用AI服务生成分类描述和颜色
    console.log('开始调用AI服务，分类名称:', aiCategoryForm.value.name)
    const result = await generateCategoryWithOllama(aiCategoryForm.value.name)
    console.log('AI服务返回结果:', result)

    // 将AI生成的结果填充到表单中，用户可以进一步编辑
    aiCategoryForm.value.description = result.description
    aiCategoryForm.value.color = result.color
    console.log('表单字段已填充:', {
      description: aiCategoryForm.value.description,
      color: aiCategoryForm.value.color,
    })

    // 标记AI生成完成，显示编辑字段
    aiGeneratedResult.value = {
      description: result.description,
      color: result.color,
    }

    ElMessage.success('AI生成完成，您可以进一步编辑内容')
  } catch (error) {
    ElMessage.error('AI生成失败，请重试')
    console.error('AI生成失败:', error)
  } finally {
    aiGenerating.value = false
  }
}

const saveAIGeneratedCategory = async () => {
  if (!aiGeneratedResult.value) return

  // 验证表单
  if (!aiCategoryFormRef.value) return

  try {
    await aiCategoryFormRef.value.validate()
    savingAICategory.value = true

    const categoryData = {
      name: aiCategoryForm.value.name,
      description: aiCategoryForm.value.description,
      color: aiCategoryForm.value.color,
      sortOrder: aiCategoryForm.value.sortOrder,
    }

    await categoryApi.createCategory(categoryData)

    ElMessage.success('AI生成分类保存成功')
    aiGenerateCategoryDialogVisible.value = false
    loadCategories() // 重新加载分类
  } catch (error) {
    ElMessage.error('保存失败，请重试')
    console.error('保存失败:', error)
  } finally {
    savingAICategory.value = false
  }
}

const handleIconError = (event: Event) => {
  const img = event.target as HTMLImageElement
  // 防止循环触发
  img.onerror = null
  // 使用内置的 SVG 占位图
  img.src = FALLBACK_ICON_DATA_URL
  img.style.display = 'block'
  img.classList?.add('placeholder-icon')
}

// 生命周期
onMounted(async () => {
  // 并行加载分类列表和所有网站总数
  await Promise.all([loadCategories(), loadTotalAllWebsites()])
  // 然后加载网站列表
  await loadWebsites()
})

// 监听器
watch([selectedCategory, searchKeyword], () => {
  currentPage.value = 1
})

// 监听手动输入文本变化，延迟解析
let manualInputTimer: NodeJS.Timeout | null = null
watch(manualInputText, (newValue) => {
  if (manualInputTimer) {
    clearTimeout(manualInputTimer)
  }

  if (newValue.trim() && importMethod.value === 'manual') {
    manualInputTimer = setTimeout(() => {
      parseManualInput()
    }, 1000) // 1秒后自动解析
  }
})

const showActionButtons = (category: Category, event: MouseEvent) => {
  event.preventDefault()
  showEditButtons.value[category.id] = true
  showDeleteButtons.value[category.id] = true
}

const hideActionButtons = (categoryId: number) => {
  showEditButtons.value[categoryId] = false
  showDeleteButtons.value[categoryId] = false
}

const editCategory = (category: Category) => {
  editingCategoryData.value = category
  editCategoryForm.value = {
    name: category.name,
    description: category.description,
    color: category.color,
  }
  editCategoryDialogVisible.value = true
}

const saveEditCategory = async () => {
  if (!editCategoryFormRef.value || !editingCategoryData.value) return

  try {
    await editCategoryFormRef.value.validate()
    savingEditCategory.value = true

    await categoryApi.updateCategory(editingCategoryData.value.id, editCategoryForm.value)

    ElMessage.success('分类修改成功')
    editCategoryDialogVisible.value = false
    editingCategoryData.value = null
    loadCategories() // 重新加载分类
  } catch (error) {
    ElMessage.error('修改分类失败，请重试')
    console.error('修改分类失败:', error)
  } finally {
    savingEditCategory.value = false
  }
}

const deleteCategory = (category: Category) => {
  deletingCategoryData.value = category
  deleteCategoryDialogVisible.value = true
}

const confirmDeleteCategory = async () => {
  if (!deletingCategoryData.value) return

  deletingCategory.value = true
  try {
    await categoryApi.deleteCategory(deletingCategoryData.value.id)

    ElMessage.success('分类删除成功')
    deleteCategoryDialogVisible.value = false
    deletingCategoryData.value = null
    loadCategories() // 重新加载分类
  } catch (error) {
    ElMessage.error('删除分类失败，请重试')
    console.error('删除分类失败:', error)
  } finally {
    deletingCategory.value = false
  }
}

// 批量导入相关方法
const showBatchImportDialog = () => {
  batchImportDialogVisible.value = true
  importMethod.value = 'bookmarks'
  importPreview.value = []
  manualInputText.value = ''
  canImport.value = false
}

const handleImportMethodChange = () => {
  importPreview.value = []
  manualInputText.value = ''
  canImport.value = false
}

const handleBookmarkFileChange = (file: File) => {
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    const content = e.target?.result as string
    parseBookmarkFile(content)
  }
  reader.readAsText(file)
}

const parseBookmarkFile = (htmlContent: string) => {
  try {
    const parser = new DOMParser()
    const doc = parser.parseFromString(htmlContent, 'text/html')
    const links = doc.querySelectorAll('a')

    const websites: Array<{
      name: string
      url: string
      description: string
      categoryIds: number[]
    }> = []

    links.forEach((link) => {
      const name = link.textContent?.trim() || '未命名'
      const url = link.getAttribute('href') || ''
      const description = link.getAttribute('title') || name

      if (url && url.startsWith('http')) {
        websites.push({
          name,
          url,
          description,
          categoryIds: [],
        })
      }
    })

    if (websites.length === 0) {
      ElMessage.warning('未找到有效的书签链接')
      return
    }

    importPreview.value = websites
    canImport.value = true
    ElMessage.success(`成功解析 ${websites.length} 个书签`)
  } catch (error) {
    ElMessage.error('解析书签文件失败')
    console.error('解析书签文件失败:', error)
  }
}

const handleFileUpload = (file: File) => {
  if (!file) return

  const reader = new FileReader()
  reader.onload = (e) => {
    const content = e.target?.result as string
    const fileExtension = file.name.split('.').pop()?.toLowerCase()

    try {
      switch (fileExtension) {
        case 'json':
          parseJsonFile(content)
          break
        case 'csv':
          parseCsvFile(content)
          break
        case 'html':
        case 'htm':
          parseBookmarkFile(content)
          break
        default:
          ElMessage.error('不支持的文件格式')
      }
    } catch (error) {
      ElMessage.error('解析文件失败')
      console.error('解析文件失败:', error)
    }
  }
  reader.readAsText(file)
}

const parseJsonFile = (content: string) => {
  try {
    const data = JSON.parse(content)
    let websites: any[] = []

    if (Array.isArray(data)) {
      websites = data
    } else if (data.websites && Array.isArray(data.websites)) {
      websites = data.websites
    } else {
      throw new Error('无效的JSON格式')
    }

    const parsedWebsites = websites
      .map((item: any) => ({
        name: item.name || item.title || '未命名',
        url: item.url || item.link || '',
        description: item.description || item.desc || item.name || '未命名',
        categoryIds: item.categoryIds || item.categories || [],
      }))
      .filter((item: any) => item.url && item.url.startsWith('http'))

    if (parsedWebsites.length === 0) {
      ElMessage.warning('未找到有效的网站数据')
      return
    }

    importPreview.value = parsedWebsites
    canImport.value = true
    ElMessage.success(`成功解析 ${parsedWebsites.length} 个网站`)
  } catch (error) {
    ElMessage.error('解析JSON文件失败')
    console.error('解析JSON文件失败:', error)
  }
}

const parseCsvFile = (content: string) => {
  try {
    const lines = content.split('\n').filter((line) => line.trim())
    if (lines.length < 2) {
      throw new Error('CSV文件格式不正确')
    }

    const headers = lines[0].split(',').map((h) => h.trim().toLowerCase())
    const websites: any[] = []

    for (let i = 1; i < lines.length; i++) {
      const values = lines[i].split(',').map((v) => v.trim())
      if (values.length < headers.length) continue

      const website: any = {}
      headers.forEach((header, index) => {
        website[header] = values[index] || ''
      })

      if (website.url || website.link) {
        websites.push({
          name: website.name || website.title || '未命名',
          url: website.url || website.link,
          description: website.description || website.desc || website.name || '未命名',
          categoryIds: website.categoryids
            ? website.categoryids
              .split(',')
              .map((id: string) => parseInt(id.trim()))
              .filter((id: number) => !isNaN(id))
            : [],
        })
      }
    }

    if (websites.length === 0) {
      ElMessage.warning('未找到有效的网站数据')
      return
    }

    importPreview.value = websites
    canImport.value = true
    ElMessage.success(`成功解析 ${websites.length} 个网站`)
  } catch (error) {
    ElMessage.error('解析CSV文件失败')
    console.error('解析CSV文件失败:', error)
  }
}

const parseManualInput = () => {
  if (!manualInputText.value.trim()) {
    ElMessage.warning('请输入网站信息')
    return
  }

  try {
    const lines = manualInputText.value.split('\n').filter((line) => line.trim())
    const websites: any[] = []

    lines.forEach((line, index) => {
      const parts = line.split('|').map((part) => part.trim())
      if (parts.length < 2) {
        websites.push({
          name: '未命名',
          url: '',
          description: '格式错误',
          categoryIds: [],
          error: `第${index + 1}行格式错误，需要至少包含名称和URL`,
        })
        return
      }

      const name = parts[0] || '未命名'
      const url = parts[1] || ''
      const description = parts[2] || name
      const categoryNames = parts[3] ? parts[3].split(',').map((c) => c.trim()) : []

      // 根据分类名称查找分类ID
      const categoryIds = categoryNames
        .map((categoryName) => {
          const category = categories.value.find(
            (c) => c.name.toLowerCase() === categoryName.toLowerCase(),
          )
          return category ? category.id : null
        })
        .filter((id) => id !== null) as number[]

      if (!url || !url.startsWith('http')) {
        websites.push({
          name,
          url,
          description,
          categoryIds,
          error: `第${index + 1}行URL格式错误`,
        })
        return
      }

      websites.push({
        name,
        url,
        description,
        categoryIds,
      })
    })

    if (websites.length === 0) {
      ElMessage.warning('未找到有效的网站数据')
      return
    }

    importPreview.value = websites
    canImport.value = websites.some((w) => !w.error)
    ElMessage.success(`成功解析 ${websites.length} 个网站`)
  } catch (error) {
    ElMessage.error('解析手动输入失败')
    console.error('解析手动输入失败:', error)
  }
}

const clearImportPreview = () => {
  importPreview.value = []
  canImport.value = false
}

const startImport = async () => {
  if (!canImport.value || importPreview.value.length === 0) return

  const validWebsites = importPreview.value.filter((w) => !w.error)
  if (validWebsites.length === 0) {
    ElMessage.warning('没有有效的网站数据可以导入')
    return
  }

  importing.value = true
  try {
    // 准备导入数据
    const importData = validWebsites.map((website) => ({
      name: website.name,
      url: website.url,
      description: website.description,
      categoryIds: website.categoryIds,
    }))

    // 调用批量导入API
    const result = await websiteApi.importWebsites(importData)

    ElMessage.success(`导入完成！成功：${result.successCount}，失败：${result.failedCount}`)

    if (result.failedCount > 0 && result.errors.length > 0) {
      console.error('导入错误详情:', result.errors)
    }

    // 关闭对话框并重新加载数据
    batchImportDialogVisible.value = false
    await loadWebsites()
    await loadCategories()

    // 清空预览
    clearImportPreview()
  } catch (error) {
    ElMessage.error('批量导入失败')
    console.error('批量导入失败:', error)
  } finally {
    importing.value = false
  }
}

// 网站状态监控相关方法
const showStatusMonitorDialog = async () => {
  statusMonitorDialogVisible.value = true
  await loadStatusList()
  await refreshStatistics()
}

const loadStatusList = async () => {
  loadingStatusList.value = true
  try {
    // 使用新的API端点获取所有网站
    const allWebsites = await websiteApi.getAllWebsites()

    statusList.value = allWebsites.map((website) => ({
      id: website.id,
      name: website.name,
      url: website.url,
      status: website.status || 'unknown',
      lastCheckTime: website.lastCheckTime,
      responseTime: website.responseTime,
      checking: false,
      testing: false,
    }))
  } catch (error) {
    ElMessage.error('加载状态列表失败')
    console.error('加载状态列表失败:', error)
  } finally {
    loadingStatusList.value = false
  }
}

const refreshStatistics = () => {
  const total = statusList.value.length
  const active = statusList.value.filter((w) => w.status === 'active').length
  const inactive = statusList.value.filter((w) => w.status === 'inactive').length
  const broken = statusList.value.filter((w) => w.status === 'broken').length
  const unknown = statusList.value.filter((w) => w.status === 'unknown' || !w.status).length

  statusStatistics.value = { total, active, inactive, broken, unknown }
}

const startBatchStatusCheck = async () => {
  if (checkingStatus.value) return

  checkingStatus.value = true
  try {
    // 调用后端批量检查接口
    await websiteApi.batchCheckWebsiteStatus()

    // 重新加载状态列表
    await loadStatusList()
    await refreshStatistics()

    ElMessage.success('批量状态检查完成')
  } catch (error) {
    ElMessage.error('批量状态检查失败')
    console.error('批量状态检查失败:', error)
  } finally {
    checkingStatus.value = false
  }
}

const checkSingleWebsite = async (website: any) => {
  if (website.checking) return

  website.checking = true
  try {
    // 调用后端单个检查接口
    await websiteApi.checkWebsiteStatus(website.id)

    // 重新加载状态列表
    await loadStatusList()
    await refreshStatistics()

    ElMessage.success(`网站 "${website.name}" 状态检查完成`)
  } catch (error) {
    ElMessage.error(`网站 "${website.name}" 状态检查失败`)
    console.error('单个网站状态检查失败:', error)
  } finally {
    website.checking = false
  }
}

const testWebsite = async (website: any) => {
  if (website.testing) return

  website.testing = true
  try {
    const startTime = Date.now()

    // 创建一个图片请求来测试网站可用性
    const img = new Image()
    img.onload = () => {
      const responseTime = Date.now() - startTime
      website.responseTime = responseTime
      website.status = 'active'
      website.lastCheckTime = new Date().toISOString()
      website.testing = false
      refreshStatistics()
      ElMessage.success(`网站 "${website.name}" 测试成功，响应时间: ${responseTime}ms`)
    }

    img.onerror = () => {
      website.status = 'broken'
      website.lastCheckTime = new Date().toISOString()
      website.testing = false
      refreshStatistics()
      ElMessage.warning(`网站 "${website.name}" 测试失败，可能已失效`)
    }

    // 设置超时
    setTimeout(() => {
      if (website.testing) {
        website.testing = false
        ElMessage.error(`网站 "${website.name}" 测试超时`)
      }
    }, monitorSettings.value.timeout * 1000)

    // 开始测试
    img.src = `${website.url}/favicon.ico?t=${Date.now()}`
  } catch (error) {
    website.testing = false
    ElMessage.error(`网站 "${website.name}" 测试失败`)
    console.error('网站测试失败:', error)
  }
}

const toggleAutoMonitoring = () => {
  if (autoMonitoring.value) {
    // 停止自动监控
    if (monitoringTimer) {
      clearInterval(monitoringTimer)
      monitoringTimer = null
    }
    autoMonitoring.value = false
    ElMessage.info('自动监控已停止')
  } else {
    // 启动自动监控
    const intervalMinutes = parseInt(monitorSettings.value.checkInterval)
    const intervalMs = intervalMinutes * 60 * 1000

    monitoringTimer = setInterval(async () => {
      if (statusMonitorDialogVisible.value) {
        await startBatchStatusCheck()
      }
    }, intervalMs)

    autoMonitoring.value = true
    ElMessage.success(`自动监控已启动，每${intervalMinutes}分钟检查一次`)
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'active':
      return '正常'
    case 'inactive':
      return '异常'
    case 'broken':
      return '失效'
    default:
      return '未知'
  }
}

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'active':
      return 'success'
    case 'inactive':
      return 'warning'
    case 'broken':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusRowClass = ({ row }: { row: any }) => {
  switch (row.status) {
    case 'active':
      return 'status-row-active'
    case 'inactive':
      return 'status-row-inactive'
    case 'broken':
      return 'status-row-broken'
    default:
      return 'status-row-unknown'
  }
}

const filteredStatusList = computed(() => {
  let filtered = statusList.value

  // 状态筛选
  if (statusFilter.value) {
    filtered = filtered.filter((w) => w.status === statusFilter.value)
  }

  // 关键词搜索
  if (statusSearchKeyword.value) {
    const keyword = statusSearchKeyword.value.toLowerCase()
    filtered = filtered.filter(
      (w) => w.name.toLowerCase().includes(keyword) || w.url.toLowerCase().includes(keyword),
    )
  }

  return filtered
})

const formatDateTime = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const exportStatusReport = () => {
  try {
    const reportData = statusList.value.map((website) => ({
      网站名称: website.name,
      URL: website.url,
      状态: getStatusText(website.status),
      最后检查时间: website.lastCheckTime ? formatDateTime(website.lastCheckTime) : '从未检查',
      响应时间: website.responseTime ? `${website.responseTime}ms` : '-',
    }))

    // 创建CSV内容
    const headers = Object.keys(reportData[0])
    const csvContent = [
      headers.join(','),
      ...reportData.map((row) => headers.map((header) => `"${row[header]}"`).join(',')),
    ].join('\n')

    // 下载文件
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `网站状态报告_${new Date().toISOString().split('T')[0]}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    ElMessage.success('状态报告导出成功')
  } catch (error) {
    ElMessage.error('状态报告导出失败')
    console.error('导出状态报告失败:', error)
  }
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (monitoringTimer) {
    clearInterval(monitoringTimer)
    monitoringTimer = null
  }
})
</script>

<style scoped>
.website-collection {
  width: 98vw;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.header-content {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.title-icon {
  margin-right: 12px;
  font-size: 32px;
}

.page-description {
  margin: 0;
  opacity: 0.9;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.category-filter {
  margin-bottom: 24px;
  padding: 24px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 16px;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.category-filter:hover {
  transform: translateY(-2px);
  box-shadow:
    0 8px 30px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.15);
}

.category-filter::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb);
  background-size: 200% 100%;
  animation: gradient-shift 3s ease-in-out infinite;
}

@keyframes gradient-shift {

  0%,
  100% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }
}

.category-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left {
  flex: 1;
}

.title-container {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  position: relative;
}

.title-icon {
  font-size: 20px;
  color: #667eea;
  margin-right: 12px;
  animation: icon-bounce 2s ease-in-out infinite;
}

@keyframes icon-bounce {

  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }

  40% {
    transform: translateY(-8px);
  }

  60% {
    transform: translateY(-4px);
  }
}

.category-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a202c;
  margin-right: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-decoration {
  width: 40px;
  height: 3px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 2px;
  animation: decoration-grow 0.6s ease-out;
}

@keyframes decoration-grow {
  from {
    width: 0;
    opacity: 0;
  }

  to {
    width: 40px;
    opacity: 1;
  }
}

.category-subtitle {
  font-size: 14px;
  color: #718096;
  font-weight: 400;
  opacity: 0;
  animation: fade-in-up 0.6s ease-out 0.2s forwards;
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.edit-mode-btn {
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.edit-mode-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s;
}

.edit-mode-btn:hover::before {
  left: 100%;
}

.edit-mode-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.category-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(103, 194, 58, 0.1));
  border-radius: 8px;
  border-left: 3px solid #409eff;
  animation: tip-fade-in 0.8s ease-out 0.5s both;
}

@keyframes tip-fade-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tip-icon {
  font-size: 16px;
  color: #409eff;
  animation: tip-bounce 2s ease-in-out infinite;
}

@keyframes tip-bounce {

  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }

  40% {
    transform: translateY(-4px);
  }

  60% {
    transform: translateY(-2px);
  }
}

.tip-text {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.categories-container {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
  animation: container-fade-in 0.8s ease-out;
}

@keyframes container-fade-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.categories-container.edit-mode {
  gap: 12px;
}

.category-item {
  position: relative;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: item-slide-in 0.6s ease-out;
  animation-fill-mode: both;
}

.category-item:nth-child(1) {
  animation-delay: 0.1s;
}

.category-item:nth-child(2) {
  animation-delay: 0.2s;
}

.category-item:nth-child(3) {
  animation-delay: 0.3s;
}

.category-item:nth-child(4) {
  animation-delay: 0.4s;
}

.category-item:nth-child(5) {
  animation-delay: 0.5s;
}

.category-item:nth-child(6) {
  animation-delay: 0.6s;
}

.category-item:nth-child(7) {
  animation-delay: 0.7s;
}

.category-item:nth-child(8) {
  animation-delay: 0.8s;
}

.category-item:nth-child(9) {
  animation-delay: 0.9s;
}

.category-item:nth-child(10) {
  animation-delay: 1s;
}

.category-item:nth-child(11) {
  animation-delay: 1.1s;
}

.category-item:nth-child(12) {
  animation-delay: 1.2s;
}

@keyframes item-slide-in {
  from {
    opacity: 0;
    transform: translateX(-30px) scale(0.8);
  }

  to {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

.category-item:hover {
  transform: translateY(-4px) scale(1.02);
  z-index: 10;
}

.category-item.selected .el-radio-button__inner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: white;
  transform: scale(1.05);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
  animation: selected-bounce 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55);
}

@keyframes selected-bounce {
  0% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.1);
  }

  100% {
    transform: scale(1.05);
  }
}

/* 移除空规则集，整合到具体子元素样式中 */

.category-item.all-category .el-radio-button__inner {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-color: #f093fb;
  color: white;
}

.category-item.all-category.selected .el-radio-button__inner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  transform: scale(1.05);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
}

.category-icon {
  font-size: 16px;
  color: currentColor;
  transition: all 0.3s ease;
}

.category-text {
  font-weight: 500;
  transition: all 0.3s ease;
}

.category-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.category-item:hover .category-color-dot {
  transform: scale(1.2);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.count-tag {
  margin-left: 4px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.count-tag.pulse {
  animation: count-pulse 0.6s ease-in-out;
}

@keyframes count-pulse {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.1);
  }
}

.count-tag::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s;
}

.count-tag:hover::before {
  left: 100%;
}

.drag-handle {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: grab;
  font-size: 12px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  transform: scale(0.8);
}

.drag-handle.visible {
  opacity: 1;
  transform: scale(1);
}

.drag-handle:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.drag-handle:active {
  cursor: grabbing;
  transform: scale(0.95);
}

.category-item.dragging {
  opacity: 0.6;
  transform: scale(0.95) rotate(3deg);
  z-index: 1000;
  cursor: grabbing;
  filter: drop-shadow(0 8px 25px rgba(0, 0, 0, 0.2));
}

.category-item.drag-over {
  border: 2px dashed #667eea;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
}

.category-item.dragging .drag-handle {
  cursor: grabbing;
  transform: scale(1.2);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.8);
}

/* AI生成分类对话框样式 */
.ai-result {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-top: 8px;
}

.result-item {
  margin-bottom: 16px;
}

.result-item:last-child {
  margin-bottom: 0;
}

.result-item label {
  display: block;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.result-item .description {
  margin: 0;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  color: #606266;
  line-height: 1.6;
}

.color-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-value {
  font-family: monospace;
  font-size: 14px;
  color: #606266;
  background: white;
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.count-tag {
  margin-left: 8px;
}

.search-section {
  margin-bottom: 24px;
}

.search-container {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 16px;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.08),
    0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 24px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-container:hover {
  transform: translateY(-2px);
  box-shadow:
    0 8px 30px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.15);
}

.search-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #409eff, #67c23a, #e6a23c, #f56c6c);
  animation: border-flow 3s ease-in-out infinite;
}

@keyframes border-flow {

  0%,
  100% {
    transform: translateX(-100%);
  }

  50% {
    transform: translateX(100%);
  }
}

.search-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 20px;
}

.search-title-container {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  position: relative;
}

.search-title-icon {
  font-size: 20px;
  color: #409eff;
  margin-right: 12px;
  animation: icon-bounce 2s ease-in-out infinite;
}

@keyframes icon-bounce {

  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }

  40% {
    transform: translateY(-6px);
  }

  60% {
    transform: translateY(-3px);
  }
}

.search-title {
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-right: 12px;
}

.search-title-decoration {
  width: 20px;
  height: 3px;
  background: linear-gradient(90deg, #409eff, #67c23a);
  border-radius: 2px;
  animation: decoration-grow 2s ease-in-out infinite;
}

@keyframes decoration-grow {

  0%,
  100% {
    width: 20px;
  }

  50% {
    width: 40px;
  }
}

.search-subtitle {
  font-size: 14px;
  color: #606266;
  opacity: 0;
  animation: fade-in-up 0.6s ease-out 0.3s forwards;
}

.search-status {
  margin-top: 8px;
  animation: fade-in-up 0.6s ease-out 0.4s forwards;
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
}

.search-input {
  flex: 1;
  max-width: 500px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid #e4e7ed;
  background: #ffffff;
  transition: all 0.3s cubic-bezier(0.4, 0.2, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.search-input.focused :deep(.el-input__wrapper) {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
  transform: scale(1.02);
}

.search-input :deep(.el-input__inner) {
  font-size: 16px;
  padding: 12px 16px;
  color: #303133;
}

.search-input :deep(.el-input__prefix) {
  margin-right: 8px;
}

.search-icon {
  font-size: 18px;
  color: #909399;
  transition: all 0.3s ease;
}

.search-input.focused .search-icon {
  color: #409eff;
  transform: scale(1.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-container {
    padding: 20px;
    border-radius: 12px;
  }

  .search-header {
    margin-bottom: 16px;
  }

  .search-title {
    font-size: 18px;
  }

  .search-subtitle {
    font-size: 13px;
  }

  .search-input-wrapper {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .search-input {
    max-width: none;
  }
}

.websites-container {
  margin-bottom: 30px;
}

.loading-state {
  padding: 20px 0;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.websites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.website-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 20px;
  box-shadow:
    0 4px 20px rgba(0, 0, 0, 0.08),
    0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 24px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.8);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
  animation: card-fade-in 0.6s ease-out;
  animation-fill-mode: both;
}

.website-card:nth-child(1) {
  animation-delay: 0.1s;
}

.website-card:nth-child(2) {
  animation-delay: 0.2s;
}

.website-card:nth-child(3) {
  animation-delay: 0.3s;
}

.website-card:nth-child(4) {
  animation-delay: 0.4s;
}

.website-card:nth-child(5) {
  animation-delay: 0.5s;
}

.website-card:nth-child(6) {
  animation-delay: 0.6s;
}

@keyframes card-fade-in {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.website-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb, #f5576c);
  background-size: 200% 100%;
  animation: gradient-shift 3s ease-in-out infinite;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.website-card:hover::before {
  opacity: 1;
}

.website-card::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.03) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.website-card:hover::after {
  opacity: 1;
}

.website-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow:
    0 16px 40px rgba(0, 0, 0, 0.12),
    0 8px 20px rgba(102, 126, 234, 0.15);
  border-color: rgba(102, 126, 234, 0.3);
}

.website-card.skeleton {
  cursor: default;
}

.website-card.skeleton:hover {
  transform: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-color: #f0f0f0;
}

/* 添加浮动装饰元素 */
.website-card::after {
  content: '';
  position: absolute;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border-radius: 50%;
  opacity: 0;
  transition: all 0.3s ease;
  z-index: 0;
}

.website-card:hover::after {
  opacity: 1;
  transform: scale(1.5) rotate(180deg);
}

/* 添加点击波纹效果 */
.website-card {
  position: relative;
  overflow: hidden;
}

.website-card .ripple {
  position: absolute;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.3);
  transform: scale(0);
  animation: ripple 0.6s linear;
  pointer-events: none;
}

@keyframes ripple {
  to {
    transform: scale(4);
    opacity: 0;
  }
}

/* 添加卡片进入动画的延迟 */
.websites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
  animation: grid-fade-in 0.8s ease-out;
}

@keyframes grid-fade-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 添加渐变背景动画 */
@keyframes gradient-shift {

  0%,
  100% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }
}

/* 添加卡片悬停时的光效 */
.website-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb, #f5576c);
  background-size: 200% 100%;
  animation: gradient-shift 3s ease-in-out infinite;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.website-card:hover::before {
  opacity: 1;
}

/* 添加卡片内容的层级 */
.website-card>* {
  position: relative;
  z-index: 1;
}

/* 添加卡片阴影动画 */
.website-card {
  animation: card-shadow-pulse 4s ease-in-out infinite;
}

@keyframes card-shadow-pulse {

  0%,
  100% {
    box-shadow:
      0 4px 20px rgba(0, 0, 0, 0.08),
      0 2px 8px rgba(0, 0, 0, 0.06);
  }

  50% {
    box-shadow:
      0 6px 25px rgba(0, 0, 0, 0.1),
      0 3px 12px rgba(0, 0, 0, 0.08);
  }
}

/* 添加图标旋转动画 */
.website-icon {
  animation: icon-float 3s ease-in-out infinite;
}

@keyframes icon-float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-2px);
  }
}

.website-card:hover .website-icon {
  animation: icon-bounce 0.6s ease-out;
}

@keyframes icon-bounce {
  0% {
    transform: scale(1) rotate(0deg);
  }

  50% {
    transform: scale(1.15) rotate(5deg);
  }

  100% {
    transform: scale(1.1) rotate(5deg);
  }
}

.card-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
}

.website-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 2px solid rgba(255, 255, 255, 0.8);
}

.website-card:hover .website-icon {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
  border-color: rgba(102, 126, 234, 0.3);
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.05) 100%);
}

.website-icon::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(135deg, #667eea, #764ba2, #f093fb);
  border-radius: 18px;
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.website-card:hover .website-icon::before {
  opacity: 1;
}

.website-icon img {
  width: 36px;
  height: 36px;
  object-fit: contain;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.website-card:hover .website-icon img {
  transform: scale(1.05);
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.15));
}

.website-icon img.placeholder-icon {
  width: 36px;
  height: 36px;
  object-fit: contain;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.website-icon .el-icon {
  font-size: 28px;
  color: #667eea;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(102, 126, 234, 0.2));
}

.website-card:hover .website-icon .el-icon {
  color: #764ba2;
  transform: scale(1.1);
  filter: drop-shadow(0 4px 8px rgba(118, 75, 162, 0.3));
}

.website-info {
  flex: 1;
  min-width: 0;
}

.website-name {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #1a202c 0%, #374151 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.3;
  transition: all 0.3s ease;
  position: relative;
}

.website-card:hover .website-name {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transform: translateX(4px);
}

.website-name::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: width 0.3s ease;
}

.website-card:hover .website-name::after {
  width: 100%;
}

.card-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateX(20px);
}

.website-card:hover .card-actions {
  opacity: 1;
  transform: translateX(0);
}

.card-actions .el-button {
  border-radius: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-actions .el-button:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

/* 复制按钮特殊样式 */
.copy-btn {
  background: linear-gradient(135deg, #10b981, #059669) !important;
  border-color: #10b981 !important;
  color: white !important;
}

.copy-btn:hover {
  background: linear-gradient(135deg, #059669, #047857) !important;
  border-color: #059669 !important;
  transform: translateY(-2px) scale(1.05) rotate(5deg) !important;
}

.copy-btn .el-icon {
  animation: copyIconPulse 2s ease-in-out infinite;
}

@keyframes copyIconPulse {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.1);
  }
}

.card-body {
  margin-bottom: 20px;
  position: relative;
}

.website-description {
  margin: 0 0 16px 0;
  font-size: 15px;
  color: #64748b;
  line-height: 1.7;
  padding: 12px 16px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  transition: all 0.3s ease;
  position: relative;
  word-break: break-word;
  white-space: pre-wrap;
  display: block;
  max-height: none;
  overflow: visible;
}

.website-card:hover .website-description {
  background: rgba(102, 126, 234, 0.05);
  border-color: rgba(102, 126, 234, 0.2);
  color: #374151;
  transform: translateX(4px);
}

.website-description::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.website-card:hover .website-description::before {
  opacity: 1;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 2px solid rgba(226, 232, 240, 0.6);
  position: relative;
  transition: all 0.3s ease;
}

.website-card:hover .card-footer {
  border-top-color: rgba(102, 126, 234, 0.3);
}

.website-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.created-time {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 8px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  transition: all 0.3s ease;
}

.website-card:hover .created-time {
  background: rgba(102, 126, 234, 0.05);
  border-color: rgba(102, 126, 234, 0.2);
  color: #667eea;
  transform: translateX(4px);
}

.category-badge {
  flex-shrink: 0;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.category-tag {
  margin-right: 0;
  border-radius: 12px;
  font-weight: 600;
  padding: 6px 12px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.category-tag::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.category-tag:hover::before {
  left: 100%;
}

.category-tag:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.pagination-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 20px;
  padding: 0 20px;
}

.pagination-text {
  font-size: 14px;
  color: #606266;
}

.pagination-pages {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  gap: 20px;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-selector .el-select {
  width: 80px;
}

.page-size-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.pagination-navigation {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-jumper {
  display: flex;
  align-items: center;
  gap: 8px;
}

.jumper-label {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.jumper-input {
  width: 60px;
}

.jumper-text {
  font-size: 14px;
  color: #606266;
}

.main-pagination {
  margin: 0;
}

.delete-confirm {
  text-align: center;
  padding: 20px 0;
}

.warning-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.delete-tip {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

/* AI生成分类弹框样式 */
.ai-category-dialog {
  border-radius: 20px;
  overflow: hidden;
}

.ai-category-dialog :deep(.el-dialog) {
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  border: none;
  overflow: hidden;
}

.ai-category-dialog :deep(.el-dialog__header) {
  padding: 0;
  border-bottom: none;
}

.ai-category-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.ai-category-dialog :deep(.el-dialog__footer) {
  padding: 0;
  border-top: none;
}

/* 自定义标题栏 */
.ai-dialog-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px 30px;
  position: relative;
  overflow: hidden;
}

.ai-dialog-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="25" cy="25" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="75" cy="75" r="1" fill="rgba(255,255,255,0.1)"/><circle cx="50" cy="10" r="0.5" fill="rgba(255,255,255,0.1)"/><circle cx="10" cy="60" r="0.5" fill="rgba(255,255,255,0.1)"/><circle cx="90" cy="40" r="0.5" fill="rgba(255,255,255,0.1)"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>');
  opacity: 0.3;
}

.ai-header-content {
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.ai-header-icon {
  position: relative;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.ai-icon {
  font-size: 28px;
  color: white;
  animation: ai-icon-pulse 2s ease-in-out infinite;
}

.ai-icon-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
  border-radius: 16px;
  animation: ai-glow-pulse 3s ease-in-out infinite;
}

@keyframes ai-icon-pulse {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.1);
  }
}

@keyframes ai-glow-pulse {

  0%,
  100% {
    opacity: 0.3;
  }

  50% {
    opacity: 0.6;
  }
}

.ai-header-text {
  flex: 1;
}

.ai-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.ai-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 400;
}

.ai-header-decoration {
  position: absolute;
  top: 20px;
  right: 30px;
  display: flex;
  gap: 8px;
}

.decoration-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  animation: decoration-bounce 2s ease-in-out infinite;
}

.decoration-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.decoration-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes decoration-bounce {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-6px);
  }
}

/* 对话框内容 */
.ai-dialog-content {
  padding: 30px;
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  min-height: 300px;
  position: relative;
}

/* AI生成状态指示器 */
.ai-status-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 10;
}

.ai-loading-animation {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
}

.ai-loading-circle {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  animation: ai-loading-bounce 1.4s ease-in-out infinite both;
}

.ai-loading-circle:nth-child(1) {
  animation-delay: -0.32s;
}

.ai-loading-circle:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes ai-loading-bounce {

  0%,
  80%,
  100% {
    transform: scale(0);
  }

  40% {
    transform: scale(1);
  }
}

.ai-loading-text {
  font-size: 16px;
  color: #667eea;
  font-weight: 600;
  margin: 0;
  animation: ai-text-pulse 2s ease-in-out infinite;
}

@keyframes ai-text-pulse {

  0%,
  100% {
    opacity: 0.7;
  }

  50% {
    opacity: 1;
  }
}

/* 表单容器 */
.ai-form-container {
  transition: all 0.3s ease;
}

.ai-form-container.ai-form-hidden {
  opacity: 0;
  transform: translateY(20px);
  pointer-events: none;
}

.ai-category-form {
  animation: form-fade-in 0.6s ease-out;
}

@keyframes form-fade-in {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-form-item {
  margin-bottom: 24px;
  animation: item-slide-in 0.6s ease-out;
  animation-fill-mode: both;
}

.ai-form-item:nth-child(1) {
  animation-delay: 0.1s;
}

.ai-form-item:nth-child(2) {
  animation-delay: 0.2s;
}

.ai-form-item:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes item-slide-in {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.ai-input-wrapper {
  position: relative;
}

.ai-input,
.ai-textarea {
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  background: white;
}

.ai-input:focus,
.ai-textarea:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  transform: translateY(-2px);
}

.input-icon {
  color: #667eea;
  font-size: 16px;
}

.ai-input-focus-border {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 12px;
  border: 2px solid transparent;
  pointer-events: none;
  transition: all 0.3s ease;
}

.ai-input:focus+.ai-input-focus-border,
.ai-textarea:focus+.ai-input-focus-border {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 占位符容器 */
.ai-placeholder-container {
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  transition: all 0.3s ease;
  cursor: pointer;
}

.ai-placeholder-container:hover {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.03) 100%);
  transform: translateY(-2px);
}

.ai-placeholder-content {
  position: relative;
}

.placeholder-icon {
  font-size: 32px;
  color: #667eea;
  margin-bottom: 12px;
  animation: placeholder-icon-float 3s ease-in-out infinite;
}

@keyframes placeholder-icon-float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-4px);
  }
}

.placeholder-text {
  margin: 0;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
}

.placeholder-decoration {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  opacity: 0.3;
  animation: decoration-rotate 4s linear infinite;
}

@keyframes decoration-rotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

/* AI生成结果展示 */
.ai-result-showcase {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.03) 100%);
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 16px;
  padding: 20px;
  margin-top: 20px;
  animation: result-showcase-slide-in 0.6s ease-out;
}

@keyframes result-showcase-slide-in {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.ai-result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.result-icon {
  font-size: 20px;
  color: #67c23a;
  animation: result-icon-bounce 0.6s ease-out;
}

@keyframes result-icon-bounce {
  0% {
    transform: scale(0);
  }

  50% {
    transform: scale(1.2);
  }

  100% {
    transform: scale(1);
  }
}

.result-title {
  font-size: 16px;
  font-weight: 600;
  color: #67c23a;
}

.ai-result-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.result-label {
  font-weight: 600;
  color: #374151;
  min-width: 60px;
}

.result-value {
  color: #64748b;
  flex: 1;
}

.result-color {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.result-color-code {
  font-family: monospace;
  color: #64748b;
  font-size: 14px;
}

/* 底部按钮 */
.ai-dialog-footer {
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
  padding: 20px 30px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.ai-cancel-btn {
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.ai-cancel-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.ai-generate-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  color: white;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.ai-generate-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.ai-generate-btn:active {
  transform: translateY(-1px);
}

.ai-save-btn {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  color: white;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.3);
}

.ai-save-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(103, 194, 58, 0.4);
}

/* 颜色选择器样式 */
.ai-color-picker {
  border-radius: 12px;
  overflow: hidden;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-category-dialog {
    width: 95vw !important;
  }

  .ai-dialog-header {
    padding: 20px;
  }

  .ai-header-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .ai-header-icon {
    width: 50px;
    height: 50px;
  }

  .ai-icon {
    font-size: 24px;
  }

  .ai-title {
    font-size: 20px;
  }

  .ai-dialog-content {
    padding: 20px;
  }

  .ai-dialog-footer {
    padding: 16px 20px;
    flex-direction: column;
  }

  .ai-generate-btn,
  .ai-save-btn {
    width: 100%;
  }
}

/* 原有的AI生成相关样式 */
.ai-placeholder {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  text-align: center;
}

.ai-result {
  margin-top: 16px;
  padding: 16px;
  background-color: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 6px;
}

.result-item {
  margin-bottom: 12px;
}

.result-item:last-child {
  margin-bottom: 0;
}

.result-item label {
  font-weight: 600;
  color: #374151;
  margin-right: 8px;
}

.description {
  margin: 8px 0;
  padding: 8px;
  background-color: white;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
}

.color-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-value {
  font-family: monospace;
  color: #6b7280;
}

.dialog-footer {
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }

  .header-actions {
    justify-content: center;
  }

  .websites-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .website-card {
    padding: 20px;
    border-radius: 16px;
  }

  .website-card:hover {
    transform: translateY(-4px) scale(1.01);
  }

  .website-icon {
    width: 48px;
    height: 48px;
    margin-right: 12px;
  }

  .website-icon img {
    width: 32px;
    height: 32px;
  }

  .website-name {
    font-size: 18px;
  }

  .website-description {
    font-size: 14px;
    padding: 10px 12px;
  }

  .card-actions {
    opacity: 1;
    transform: translateX(0);
  }

  .category-tag {
    padding: 4px 8px;
    font-size: 11px;
  }

  .category-filter {
    overflow-x: auto;
    padding: 20px;
    border-radius: 12px;
  }

  .category-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .title-container {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .categories-container {
    gap: 12px;
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 8px;
  }

  .category-item {
    flex-shrink: 0;
  }

  .category-item:nth-child(n) {
    animation-delay: 0.1s;
  }

  .website-card {
    padding: 16px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .website-icon {
    margin-right: 0;
    margin-bottom: 8px;
  }

  .card-actions {
    opacity: 1;
    position: absolute;
    top: 16px;
    right: 16px;
  }

  .pagination-container {
    padding: 16px;
  }

  .pagination-info {
    flex-direction: column;
    gap: 12px;
    text-align: center;
    padding: 0;
  }

  .pagination-controls {
    flex-direction: column;
    gap: 16px;
    align-items: center;
  }

  .pagination-navigation {
    flex-direction: column;
    gap: 16px;
    align-items: center;
  }

  .page-jumper {
    order: 1;
  }

  .main-pagination {
    order: 2;
  }
}

/* AI新增网站对话框样式 */
.ai-step {
  margin-bottom: 24px;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05), rgba(118, 75, 162, 0.05));
  border-radius: 12px;
  border-left: 4px solid #667eea;
}

.step-icon {
  font-size: 20px;
  color: #667eea;
}

.step-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.category-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.icon-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.favicon-preview {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  object-fit: contain;
}

.favicon-placeholder {
  width: 24px;
  height: 24px;
  color: #6c757d;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-info {
  font-size: 14px;
  color: #6c757d;
  word-break: break-all;
}

/* 对话框底部按钮样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #e9ecef;
}

.dialog-footer .el-button {
  min-width: 100px;
}

.edit-category-btn {
  position: absolute;
  top: -8px;
  right: 32px;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;
}

.edit-category-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.6);
}

.edit-category-btn:active {
  transform: scale(0.95);
}

.delete-category-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 12px;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;
}

.delete-category-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(245, 108, 108, 0.6);
}

.delete-category-btn:active {
  transform: scale(0.95);
}

/* 批量导入相关样式 */
.batch-import-content {
  max-height: 70vh;
  overflow-y: auto;
}

.import-methods {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.method-section {
  border-bottom: 1px solid #e9ecef;
  padding-bottom: 20px;
}

.method-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 16px 0;
}

.method-icon {
  font-size: 20px;
  color: #667eea;
}

.method-options {
  display: flex;
  gap: 12px;
}

.import-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e9ecef;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.section-icon {
  font-size: 18px;
  color: #667eea;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.section-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.import-tip {
  margin: 0;
  color: #6c757d;
  font-size: 14px;
  line-height: 1.6;
}

.import-steps {
  margin: 0;
  padding-left: 20px;
  color: #495057;
  font-size: 14px;
  line-height: 1.8;
}

.import-steps li {
  margin-bottom: 8px;
}

.import-steps kbd {
  background: #e9ecef;
  border: 1px solid #ced4da;
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 12px;
  font-family: monospace;
}

.format-list {
  margin: 0;
  padding-left: 20px;
  color: #495057;
  font-size: 14px;
  line-height: 1.8;
}

.format-list li {
  margin-bottom: 8px;
}

.file-upload-area {
  margin-top: 16px;
}

.bookmark-upload,
.file-upload {
  width: 100%;
}

.upload-icon {
  font-size: 48px;
  color: #667eea;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #495057;
  margin-bottom: 8px;
}

.upload-text em {
  color: #667eea;
  font-style: normal;
  font-weight: 600;
}

.upload-tip {
  font-size: 14px;
  color: #6c757d;
  margin-top: 8px;
}

.manual-input {
  margin-bottom: 16px;
}

.manual-input-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.import-preview {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #e9ecef;
  margin-top: 24px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #dee2e6;
}

.preview-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.preview-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.preview-item {
  background: white;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.preview-item:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
}

.preview-item-error {
  border-color: #dc3545;
  background: #fff5f5;
}

.preview-item-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.preview-index {
  background: #667eea;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.preview-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
  min-width: 120px;
}

.preview-url {
  color: #6c757d;
  font-size: 13px;
  font-family: monospace;
  word-break: break-all;
  flex: 1;
  min-width: 200px;
}

.preview-description {
  color: #495057;
  font-size: 13px;
  line-height: 1.5;
  margin-bottom: 8px;
}

.preview-categories {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .batch-import-content {
    max-height: 60vh;
  }

  .import-methods {
    gap: 16px;
  }

  .import-section {
    padding: 16px;
  }

  .method-options {
    flex-direction: column;
    gap: 8px;
  }

  .preview-item-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .preview-name,
  .preview-url {
    min-width: auto;
    width: 100%;
  }

  .manual-input-actions {
    flex-direction: column;
    align-items: stretch;
  }
}

/* 网站状态监控相关样式 */
.status-monitor-content {
  max-height: 80vh;
  overflow-y: auto;
  padding: 0 4px;
}

.monitor-control-panel {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 20px;
  padding: 28px;
  margin-bottom: 28px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.08),
    0 2px 8px rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.monitor-control-panel:hover {
  transform: translateY(-4px);
  box-shadow:
    0 16px 48px rgba(0, 0, 0, 0.12),
    0 4px 16px rgba(0, 0, 0, 0.08);
}

.monitor-control-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2, #f093fb);
  background-size: 200% 100%;
  animation: gradient-shift 3s ease-in-out infinite;
}

.control-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.control-title {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1a202c;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.control-icon {
  font-size: 24px;
  color: #667eea;
  animation: icon-pulse 2s ease-in-out infinite;
}

@keyframes icon-pulse {

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.1);
  }
}

.control-actions {
  display: flex;
  gap: 16px;
}

.control-actions .el-button {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.control-actions .el-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.control-actions .el-button:hover::before {
  left: 100%;
}

.monitoring-active {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
  border-color: #67c23a;
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.3);
}

.monitor-settings {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 16px;
  padding: 28px;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  align-items: start;
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.setting-label {
  font-size: 15px;
  font-weight: 700;
  color: #374151;
  margin: 0;
  padding: 0;
  line-height: 1.4;
  display: flex;
  align-items: center;
  gap: 8px;
}

.setting-label::before {
  content: '';
  width: 4px;
  height: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.setting-control {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.setting-select,
.setting-input {
  flex: 1;
  min-width: 0;
}

.setting-select .el-input__wrapper,
.setting-input .el-input__wrapper {
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  background: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.setting-select .el-input__wrapper:hover,
.setting-input .el-input__wrapper:hover {
  border-color: #667eea;
  box-shadow:
    0 0 0 3px rgba(102, 126, 234, 0.1),
    0 4px 8px rgba(0, 0, 0, 0.04);
}

.setting-select .el-input__wrapper.is-focus,
.setting-input .el-input__wrapper.is-focus {
  border-color: #667eea;
  box-shadow:
    0 0 0 3px rgba(102, 126, 234, 0.2),
    0 4px 12px rgba(102, 126, 234, 0.1);
}

.setting-select .el-input__inner,
.setting-input .el-input__inner {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  padding: 12px 16px;
}

.setting-select .el-input__inner::placeholder,
.setting-input .el-input__inner::placeholder {
  color: #9ca3af;
  font-weight: 400;
}

.setting-unit {
  color: #64748b;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 12px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-radius: 8px;
  border: 1px solid rgba(102, 126, 234, 0.2);
  white-space: nowrap;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.setting-unit:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
}

/* 禁用状态样式 */
.setting-select.is-disabled .el-input__wrapper,
.setting-input.is-disabled .el-input__wrapper {
  background: #f8fafc;
  border-color: #e2e8f0;
  opacity: 0.6;
}

.setting-select.is-disabled .el-input__wrapper:hover,
.setting-input.is-disabled .el-input__wrapper:hover {
  border-color: #e2e8f0;
  box-shadow: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .setting-control {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .setting-unit {
    align-self: flex-start;
    margin-left: 0;
  }
}

.setting-unit {
  margin-left: 12px;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  padding: 4px 8px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 6px;
}

.status-statistics {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 20px;
  padding: 28px;
  margin-bottom: 28px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.08),
    0 2px 8px rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
}

.status-statistics::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #f093fb, #667eea, #764ba2);
  background-size: 200% 100%;
  animation: gradient-shift 3s ease-in-out infinite reverse;
}

.statistics-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(226, 232, 240, 0.8);
}

.statistics-title {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1a202c;
  background: linear-gradient(135deg, #f093fb 0%, #667eea 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.statistics-icon {
  font-size: 24px;
  color: #f093fb;
  animation: icon-rotate 3s linear infinite;
}

@keyframes icon-rotate {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}

.statistics-header .el-button {
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  transition: all 0.3s ease;
}

.statistics-header .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.statistics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  border-radius: 16px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid rgba(226, 232, 240, 0.8);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-6px) scale(1.02);
  box-shadow:
    0 16px 40px rgba(0, 0, 0, 0.12),
    0 4px 16px rgba(0, 0, 0, 0.08);
}

.stat-card.total::before {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.active::before {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.stat-card.inactive::before {
  background: linear-gradient(135deg, #e6a23c 0%, #f0a23c 100%);
}

.stat-card.broken::before {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.stat-card.unknown::before {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
}

.stat-card:hover::before {
  width: 8px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
}

.stat-card.total .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.stat-card.active .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.3);
}

.stat-card.inactive .stat-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #f0a23c 100%);
  box-shadow: 0 4px 16px rgba(230, 162, 60, 0.3);
}

.stat-card.broken .stat-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.3);
}

.stat-card.unknown .stat-icon {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
  box-shadow: 0 4px 16px rgba(144, 147, 153, 0.3);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: 800;
  color: #1a202c;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #1a202c 0%, #374151 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-number {
  transform: scale(1.05);
}

.stat-label {
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stat-label {
  font-size: 14px;
  color: #6c757d;
  font-weight: 500;
}

.status-list {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 20px;
  padding: 28px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.08),
    0 2px 8px rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
}

.status-list::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #764ba2, #f093fb, #667eea);
  background-size: 200% 100%;
  animation: gradient-shift 3s ease-in-out infinite;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(226, 232, 240, 0.8);
}

.list-title {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #1a202c;
  background: linear-gradient(135deg, #764ba2 0%, #f093fb 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.list-icon {
  font-size: 24px;
  color: #764ba2;
  animation: icon-bounce 2s ease-in-out infinite;
}

@keyframes icon-bounce {

  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }

  40% {
    transform: translateY(-6px);
  }

  60% {
    transform: translateY(-3px);
  }
}

.list-filters {
  display: flex;
  gap: 16px;
  align-items: center;
}

.list-filters .el-select,
.list-filters .el-input {
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.list-filters .el-select:hover,
.list-filters .el-input:hover {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.list-filters .el-select:focus-within,
.list-filters .el-input:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.status-table {
  margin-top: 20px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.status-table .el-table {
  border-radius: 16px;
}

.status-table .el-table__header {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.status-table .el-table__header th {
  background: transparent;
  border-bottom: 2px solid rgba(226, 232, 240, 0.8);
  font-weight: 700;
  color: #374151;
  padding: 16px 12px;
}

.status-table .el-table__body td {
  padding: 16px 12px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
}

.website-name-cell {
  display: flex;
  align-items: center;
  gap: 16px;
}

.website-name {
  font-weight: 600;
  color: #1a202c;
  font-size: 15px;
}

.status-tag {
  flex-shrink: 0;
  border-radius: 8px;
  font-weight: 600;
  padding: 4px 12px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.url-cell {
  display: flex;
  align-items: center;
  gap: 16px;
}

.url-text {
  color: #64748b;
  font-family:
    'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
  font-size: 13px;
  word-break: break-all;
  flex: 1;
  background: rgba(102, 126, 234, 0.05);
  padding: 6px 10px;
  border-radius: 6px;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.check-time {
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
  background: rgba(144, 147, 153, 0.05);
  padding: 4px 8px;
  border-radius: 4px;
}

.response-time {
  color: #67c23a;
  font-weight: 700;
  font-size: 13px;
  background: rgba(103, 194, 58, 0.1);
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid rgba(103, 194, 58, 0.2);
}

.no-response {
  color: #909399;
  font-size: 13px;
  font-weight: 500;
  background: rgba(144, 147, 153, 0.05);
  padding: 4px 8px;
  border-radius: 4px;
}

/* 状态行样式 */
.status-row-active {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.08) 0%, rgba(103, 194, 58, 0.03) 100%);
  border-left: 4px solid #67c23a;
  transition: all 0.3s ease;
}

.status-row-inactive {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.08) 0%, rgba(230, 162, 60, 0.03) 100%);
  border-left: 4px solid #e6a23c;
  transition: all 0.3s ease;
}

.status-row-broken {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.08) 0%, rgba(245, 108, 108, 0.03) 100%);
  border-left: 4px solid #f56c6c;
  transition: all 0.3s ease;
}

.status-row-unknown {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.08) 0%, rgba(144, 147, 153, 0.03) 100%);
  border-left: 4px solid #909399;
  transition: all 0.3s ease;
}

.status-row-active:hover {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.15) 0%, rgba(103, 194, 58, 0.08) 100%);
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.15);
}

.status-row-inactive:hover {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.15) 0%, rgba(230, 162, 60, 0.08) 100%);
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(230, 162, 60, 0.15);
}

.status-row-broken:hover {
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.15) 0%, rgba(245, 108, 108, 0.08) 100%);
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.15);
}

.status-row-unknown:hover {
  background: linear-gradient(135deg, rgba(144, 147, 153, 0.15) 0%, rgba(144, 147, 153, 0.08) 100%);
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(144, 147, 153, 0.15);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .status-monitor-content {
    max-height: 70vh;
  }

  .monitor-control-panel {
    padding: 16px;
  }

  .control-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .control-actions {
    width: 100%;
    justify-content: space-between;
  }

  .statistics-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .stat-card {
    padding: 16px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }

  .stat-number {
    font-size: 20px;
  }

  .list-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .list-filters {
    width: 100%;
    flex-direction: column;
    gap: 8px;
  }

  .website-name-cell {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .url-cell {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
