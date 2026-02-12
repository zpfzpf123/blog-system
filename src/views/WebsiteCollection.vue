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
  --bg-page: #f5f8ff;
  --bg-panel: rgba(255, 255, 255, 0.86);
  --bg-surface: #ffffff;
  --bg-soft: #f7faff;
  --text-title: #0f172a;
  --text-main: #2d3c52;
  --text-secondary: #64748b;
  --text-muted: #8da0b8;
  --line-soft: #dce7f6;
  --line-strong: #c9d9ef;
  --accent: #2563eb;
  --accent-strong: #1d4ed8;
  --accent-soft: #e8f0ff;
  --accent-warm: #f97316;
  --danger: #dc2626;
  --danger-soft: #fee2e2;
  --radius-xl: 30px;
  --radius-lg: 20px;
  --radius-md: 14px;
  --radius-sm: 10px;
  --shadow-soft: 0 14px 36px rgba(37, 99, 235, 0.1);
  --shadow-card: 0 22px 48px rgba(15, 23, 42, 0.1);
  --font-display: 'ZCOOL XiaoWei', 'STZhongsong', 'Noto Serif SC', serif;
  --font-body: 'Outfit', 'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif;

  position: relative;
  display: grid;
  gap: 20px;
  min-height: 100%;
  padding: 28px;
  color: var(--text-main);
  font-family: var(--font-body);
  background:
    radial-gradient(circle at 6% -10%, rgba(37, 99, 235, 0.18) 0%, rgba(37, 99, 235, 0) 36%),
    radial-gradient(circle at 94% 0%, rgba(249, 115, 22, 0.14) 0%, rgba(249, 115, 22, 0) 34%),
    linear-gradient(180deg, #f7faff 0%, #f4f8ff 100%);
}

.website-collection::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.08) 1px, transparent 1px);
  background-size: 36px 36px;
  mask-image: linear-gradient(to bottom, rgba(0, 0, 0, 0.35), transparent 80%);
}

.website-collection > * {
  position: relative;
  z-index: 1;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
  border-radius: var(--radius-xl);
  border: 1px solid rgba(201, 217, 239, 0.76);
  background:
    linear-gradient(130deg, rgba(255, 255, 255, 0.95) 0%, rgba(247, 251, 255, 0.95) 54%, rgba(255, 247, 238, 0.9) 100%);
  box-shadow: var(--shadow-soft);
  padding: clamp(20px, 3vw, 30px);
  backdrop-filter: blur(12px);
}

.header-content {
  display: grid;
  gap: 10px;
}

.page-title {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--text-title);
  font-family: var(--font-display);
  font-size: clamp(1.7rem, 2.8vw, 2.45rem);
  line-height: 1.1;
  letter-spacing: -0.01em;
}

.title-icon {
  display: inline-grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  color: #1f4cc2;
  background: linear-gradient(145deg, #ebf2ff 0%, #ffffff 100%);
  box-shadow: inset 0 0 0 1px #d7e3f9;
}

.page-description {
  margin: 0;
  max-width: 680px;
  color: var(--text-secondary);
  line-height: 1.75;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  max-width: 640px;
}

.header-actions :deep(.el-button) {
  height: 42px;
  border-radius: 12px;
  padding: 0 16px;
  font-weight: 600;
  border-width: 1px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.header-actions :deep(.el-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.16);
}

.header-actions :deep(.el-button--primary) {
  border-color: transparent;
  background: linear-gradient(120deg, #2563eb 0%, #3b82f6 100%);
}

.header-actions :deep(.el-button--success) {
  border-color: transparent;
  background: linear-gradient(120deg, #0ea5a3 0%, #22c55e 100%);
}

.header-actions :deep(.el-button--warning) {
  border-color: transparent;
  background: linear-gradient(120deg, #f59e0b 0%, #f97316 100%);
  color: #fff;
}

.header-actions :deep(.el-button--info) {
  border-color: transparent;
  background: linear-gradient(120deg, #64748b 0%, #475569 100%);
}

.category-filter {
  display: grid;
  gap: 16px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--line-soft);
  background: var(--bg-panel);
  box-shadow: var(--shadow-soft);
  padding: clamp(16px, 2.4vw, 24px);
}

.category-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.header-left {
  display: grid;
  gap: 8px;
}

.title-container {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.category-title {
  color: var(--text-title);
  font-family: var(--font-display);
  font-size: clamp(1.2rem, 2.1vw, 1.45rem);
  letter-spacing: 0.01em;
}

.title-decoration {
  width: 70px;
  height: 8px;
  border-radius: 999px;
  background: linear-gradient(90deg, #2563eb 0%, rgba(37, 99, 235, 0) 100%);
}

.category-subtitle {
  color: var(--text-secondary);
  line-height: 1.65;
  font-size: 14px;
}

.category-tips {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  border-radius: 999px;
  border: 1px solid #d8e6fc;
  background: #f3f8ff;
  color: #4b6da8;
  padding: 6px 12px;
  font-size: 12px;
}

.tip-icon {
  color: var(--accent-strong);
}

.edit-mode-btn {
  align-self: flex-start;
  border-radius: 999px;
  border: 1px solid #c5d6f2;
  background: #f7faff;
  color: #456495;
  font-weight: 600;
}

.edit-mode-btn.active {
  border-color: transparent;
  background: linear-gradient(120deg, #2563eb 0%, #1d4ed8 100%);
  color: #fff;
}

.categories-container {
  border-radius: 16px;
  border: 1px dashed #d0def4;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.84) 0%, rgba(246, 250, 255, 0.84) 100%);
  padding: 14px;
}

.categories-container :deep(.el-radio-group) {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-item {
  position: relative;
}

.category-item :deep(.el-radio-button__inner) {
  border: 1px solid #d6e3f8 !important;
  border-radius: 12px !important;
  background: #ffffff !important;
  color: #334963 !important;
  min-height: 40px;
  padding: 9px 14px;
  line-height: 1;
  box-shadow: none !important;
  transition: all 0.2s ease;
}

.category-item :deep(.el-radio-button__inner:hover) {
  border-color: #8fb2ec !important;
  color: #1f4fc3 !important;
  background: #f5f9ff !important;
}

.category-item.selected :deep(.el-radio-button__inner),
.category-item :deep(.el-radio-button.is-active .el-radio-button__inner) {
  border-color: transparent !important;
  color: #ffffff !important;
  background: linear-gradient(125deg, #2563eb 0%, #4f46e5 100%) !important;
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.28) !important;
}

.category-item.dragging {
  opacity: 0.45;
}

.category-item.drag-over {
  transform: translateY(-2px);
}

.btn-content {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.category-text {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-color-dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.7);
}

.count-tag {
  border: none;
  border-radius: 999px;
  font-weight: 700;
  padding: 0 8px;
  color: #45618f;
  background: rgba(225, 236, 255, 0.92);
}

.category-item.selected .count-tag {
  color: #1f4fc3;
  background: rgba(255, 255, 255, 0.92);
}

.count-tag.pulse {
  animation: countPulse 1.35s ease-in-out infinite;
}

.edit-category-btn,
.delete-category-btn {
  position: absolute;
  top: -8px;
  width: 24px;
  height: 24px;
  border: none;
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.15);
}

.edit-category-btn {
  right: 34px;
}

.delete-category-btn {
  right: 4px;
}

.drag-handle {
  position: absolute;
  right: 8px;
  bottom: -7px;
  width: 22px;
  height: 22px;
  border-radius: 7px;
  display: grid;
  place-items: center;
  color: #4f6ea0;
  background: #eef4ff;
  border: 1px solid #d4e2f8;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.drag-handle.visible {
  opacity: 1;
}

.search-section {
  border-radius: var(--radius-lg);
  border: 1px solid var(--line-soft);
  background: var(--bg-panel);
  box-shadow: var(--shadow-soft);
  padding: clamp(16px, 2.4vw, 24px);
}

.search-container {
  display: grid;
  gap: 12px;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.search-title-container {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.search-title {
  color: var(--text-title);
  font-family: var(--font-display);
  font-size: clamp(1.16rem, 2vw, 1.36rem);
}

.search-title-decoration {
  width: 72px;
  height: 8px;
  border-radius: 999px;
  background: linear-gradient(90deg, #f97316 0%, rgba(249, 115, 22, 0) 100%);
}

.search-subtitle {
  color: var(--text-secondary);
  font-size: 13px;
}

.search-input-wrapper {
  position: relative;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 14px;
  border: 1px solid #d3e1f7;
  background: #ffffff;
  box-shadow: 0 1px 0 rgba(148, 163, 184, 0.08);
  transition: all 0.2s ease;
  min-height: 46px;
}

.search-input.focused :deep(.el-input__wrapper),
.search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #74a0ea;
  box-shadow:
    0 0 0 4px rgba(37, 99, 235, 0.12),
    0 10px 22px rgba(37, 99, 235, 0.12);
}

.search-input :deep(.el-input__inner) {
  color: #314864;
  font-size: 14px;
}

.search-input :deep(.el-input__inner::placeholder) {
  color: #91a4bc;
}

.search-icon {
  color: #7690b8;
}

.websites-container {
  min-height: 340px;
}

.loading-state,
.empty-state {
  border-radius: var(--radius-lg);
  border: 1px dashed var(--line-strong);
  background: rgba(255, 255, 255, 0.8);
  padding: clamp(18px, 3vw, 30px);
}

.websites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.website-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 205px;
  border-radius: 20px;
  border: 1px solid var(--line-soft);
  background:
    linear-gradient(180deg, #ffffff 0%, #f9fbff 100%);
  box-shadow: var(--shadow-card);
  padding: 16px;
  cursor: pointer;
  overflow: hidden;
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.website-card::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    radial-gradient(circle at 85% 10%, rgba(37, 99, 235, 0.12) 0%, rgba(37, 99, 235, 0) 42%);
}

.website-card > * {
  position: relative;
  z-index: 1;
}

.website-card:hover {
  transform: translateY(-4px);
  border-color: #9cb9e9;
  box-shadow: 0 24px 48px rgba(37, 99, 235, 0.18);
}

.website-card.skeleton {
  cursor: default;
}

.card-header {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 12px;
}

.website-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  border: 1px solid #d8e4f9;
  background: #f2f7ff;
  display: grid;
  place-items: center;
  color: #3b6dd6;
  flex-shrink: 0;
  overflow: hidden;
}

.website-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.website-info {
  min-width: 0;
}

.website-name {
  margin: 0;
  color: var(--text-title);
  font-size: 17px;
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-family: var(--font-display);
}

.card-actions {
  display: inline-flex;
  gap: 6px;
  opacity: 0.82;
  transition: opacity 0.2s ease;
}

.website-card:hover .card-actions {
  opacity: 1;
}

.card-actions :deep(.el-button) {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  border: 1px solid #d5e3f8;
  background: #f7faff;
  color: #60789d;
  margin: 0;
  transition: all 0.2s ease;
}

.card-actions :deep(.el-button:hover) {
  color: var(--accent-strong);
  border-color: #9db9e8;
  background: #eaf2ff;
}

.card-actions :deep(.el-button--danger) {
  border-color: #f7c7c7;
  background: #fff4f4;
  color: #d14343;
}

.card-actions :deep(.el-button--danger:hover) {
  border-color: #f19a9a;
  background: #ffe8e8;
  color: #b91c1c;
}

.card-body {
  flex: 1;
}

.website-description {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 13px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.website-description.expanded {
  -webkit-line-clamp: 4;
}

.card-footer {
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px dashed #d7e3f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.website-meta {
  color: var(--text-muted);
  font-size: 12px;
  white-space: nowrap;
}

.category-badge {
  display: inline-flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.category-tag,
.category-badge :deep(.el-tag) {
  border-radius: 999px;
  border: none;
  font-weight: 600;
}

.pagination-container {
  display: grid;
  gap: 10px;
  border-radius: 18px;
  border: 1px solid var(--line-soft);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: var(--shadow-soft);
  padding: 14px 16px;
}

.pagination-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.pagination-text,
.pagination-pages {
  color: #59708f;
  font-size: 13px;
}

.pagination-controls {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 12px 18px;
}

.page-size-selector {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.pagination-navigation {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.page-jumper {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
  white-space: nowrap;
}

.page-size-label,
.jumper-label,
.jumper-text {
  color: #5f7698;
  font-size: 12px;
}

.page-size-selector :deep(.el-select),
.jumper-input :deep(.el-input__wrapper) {
  min-width: 90px;
}

.jumper-input {
  width: 90px;
  flex: 0 0 90px;
}

.jumper-input :deep(.el-input) {
  width: 100%;
}

.page-size-selector :deep(.el-input__wrapper),
.jumper-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 1px solid #d6e3f8;
  background: #fff;
}

.main-pagination :deep(.el-pager li),
.main-pagination :deep(.btn-prev),
.main-pagination :deep(.btn-next) {
  border: 1px solid #d6e3f8;
  background: #ffffff;
  border-radius: 9px;
  font-weight: 600;
}

.main-pagination :deep(.el-pager li.is-active) {
  border-color: transparent;
  background: linear-gradient(120deg, #2563eb 0%, #3b82f6 100%);
  color: #fff;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.dialog-footer :deep(.el-button) {
  border-radius: 10px;
  min-width: 92px;
}

:deep(.el-dialog) {
  border-radius: 18px;
  border: 1px solid #d4e2f8;
  overflow: hidden;
  box-shadow: 0 26px 56px rgba(15, 23, 42, 0.24);
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #e2eaf7;
  margin-right: 0;
  padding: 16px 20px;
  background: linear-gradient(120deg, #f4f8ff 0%, #fff8f1 100%);
}

:deep(.el-dialog__body) {
  padding: 18px 20px;
  background: #fbfdff;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #e2eaf7;
  padding: 14px 20px;
  background: #ffffff;
}

:deep(.el-form-item__label) {
  color: #4a6288;
  font-weight: 600;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-select__wrapper) {
  border-radius: 10px;
  border-color: #d6e3f8;
  background: #fff;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus),
:deep(.el-select__wrapper.is-focused) {
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.12);
}

.ai-category-dialog :deep(.el-dialog) {
  background:
    radial-gradient(circle at 90% 0%, rgba(251, 146, 60, 0.14) 0%, rgba(251, 146, 60, 0) 42%),
    radial-gradient(circle at 0% 100%, rgba(37, 99, 235, 0.12) 0%, rgba(37, 99, 235, 0) 42%),
    #ffffff;
}

.ai-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.ai-header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-header-icon {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(125deg, #2563eb 0%, #7c3aed 100%);
  box-shadow: 0 10px 22px rgba(79, 70, 229, 0.26);
}

.ai-icon-glow {
  position: absolute;
  inset: -8px;
  border-radius: inherit;
  background: radial-gradient(circle, rgba(124, 58, 237, 0.36) 0%, rgba(124, 58, 237, 0) 72%);
  z-index: -1;
}

.ai-header-text {
  display: grid;
  gap: 2px;
}

.ai-title {
  margin: 0;
  color: #18305d;
  font-size: 18px;
}

.ai-subtitle {
  margin: 0;
  color: #5f7698;
  font-size: 13px;
}

.ai-header-decoration {
  display: inline-flex;
  gap: 6px;
}

.decoration-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #8fb2ef;
}

.ai-dialog-content {
  display: grid;
  gap: 14px;
}

.ai-status-indicator {
  border-radius: 14px;
  border: 1px solid #d6e3fa;
  background: #f7faff;
  text-align: center;
  padding: 16px;
}

.ai-loading-animation {
  display: inline-flex;
  gap: 7px;
}

.ai-loading-circle {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #4376dd;
  animation: aiDot 1s ease-in-out infinite;
}

.ai-loading-circle:nth-child(2) {
  animation-delay: 0.14s;
}

.ai-loading-circle:nth-child(3) {
  animation-delay: 0.28s;
}

.ai-loading-text {
  margin: 8px 0 0;
  color: #5f7698;
  font-size: 13px;
}

.ai-form-container {
  transition: opacity 0.2s ease;
}

.ai-form-hidden {
  opacity: 0.32;
  pointer-events: none;
}

.ai-input-wrapper {
  position: relative;
}

.ai-input-focus-border {
  position: absolute;
  inset: -3px;
  border-radius: 12px;
  border: 2px solid rgba(37, 99, 235, 0);
  pointer-events: none;
  transition: border-color 0.2s ease;
}

.ai-input-wrapper:focus-within .ai-input-focus-border {
  border-color: rgba(37, 99, 235, 0.24);
}

.ai-placeholder-container {
  border-radius: 10px;
  border: 1px dashed #cfdff7;
  background: #f7faff;
  min-height: 88px;
  display: grid;
  place-items: center;
  text-align: center;
  padding: 14px;
}

.ai-placeholder-content {
  display: grid;
  gap: 6px;
}

.placeholder-icon {
  color: #4e73b2;
  font-size: 18px;
}

.placeholder-text {
  margin: 0;
  color: #5f7698;
  font-size: 13px;
}

.placeholder-decoration {
  width: 56px;
  height: 6px;
  margin: 0 auto;
  border-radius: 999px;
  background: linear-gradient(90deg, #8fb2ef 0%, rgba(143, 178, 239, 0) 100%);
}

.ai-result-showcase {
  border-radius: 12px;
  border: 1px solid #cae2d0;
  background: #f2fdf5;
  padding: 12px;
}

.ai-result-header {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #2f7f56;
  font-weight: 700;
}

.ai-result-content {
  margin-top: 8px;
  display: grid;
  gap: 7px;
}

.result-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #315a45;
  font-size: 13px;
}

.result-label {
  color: #47735b;
  font-weight: 700;
}

.result-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1px solid rgba(0, 0, 0, 0.12);
}

.result-color-code {
  color: #2f7f56;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
}

.ai-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.ai-dialog-footer :deep(.el-button) {
  border-radius: 10px;
  min-width: 96px;
}

.ai-generate-btn {
  --el-button-bg-color: #1d4ed8;
  --el-button-border-color: transparent;
}

.ai-save-btn {
  --el-button-bg-color: #16a34a;
  --el-button-border-color: transparent;
}

.ai-step {
  border-radius: 14px;
  border: 1px solid #d7e4f8;
  background: #fff;
  padding: 14px;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.step-icon {
  color: #2f63d5;
}

.step-title {
  color: #274069;
  font-weight: 700;
}

.category-option {
  display: inline-flex;
  align-items: center;
  gap: 7px;
}

.icon-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  border-radius: 10px;
  border: 1px solid #d9e6fa;
  background: #f7faff;
  padding: 10px;
}

.favicon-preview,
.favicon-placeholder {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  border: 1px solid #d5e3f8;
  background: #fff;
  display: grid;
  place-items: center;
  object-fit: cover;
}

.icon-info {
  color: #627b9d;
  font-size: 12px;
  word-break: break-all;
}

.delete-confirm {
  display: grid;
  justify-items: center;
  gap: 8px;
  text-align: center;
  padding: 6px 2px;
}

.warning-icon {
  font-size: 42px;
}

.delete-confirm p {
  margin: 0;
  color: #3a4e69;
}

.delete-tip {
  color: #7a8fae;
  font-size: 13px;
}

.batch-import-content {
  max-height: 70vh;
  overflow: auto;
  padding-right: 2px;
}

.import-methods {
  display: grid;
  gap: 14px;
}

.method-section,
.import-section,
.import-preview {
  border-radius: 14px;
  border: 1px solid #d8e4f8;
  background: #fff;
  padding: 14px;
}

.method-title,
.section-title {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #28436b;
  font-size: 15px;
}

.method-icon,
.section-icon {
  color: #2e63d6;
}

.method-options {
  margin-top: 10px;
}

.method-options :deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.method-options :deep(.el-radio-button__inner) {
  border-radius: 10px !important;
  border: 1px solid #d6e3f8 !important;
  background: #f7faff !important;
  color: #4d6891 !important;
}

.method-options :deep(.el-radio-button.is-active .el-radio-button__inner) {
  border-color: transparent !important;
  color: #fff !important;
  background: linear-gradient(120deg, #2563eb 0%, #4f46e5 100%) !important;
}

.section-content {
  margin-top: 10px;
  display: grid;
  gap: 10px;
}

.import-tip,
.import-steps,
.format-list,
.upload-tip,
.preview-url,
.preview-description {
  margin: 0;
  color: #5e7596;
  line-height: 1.7;
  font-size: 13px;
}

.import-steps,
.format-list {
  padding-left: 18px;
}

.import-steps kbd {
  border-radius: 6px;
  border: 1px solid #d2def3;
  background: #f4f8ff;
  color: #416190;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  font-size: 12px;
  padding: 1px 6px;
}

.file-upload-area {
  margin-top: 2px;
}

.bookmark-upload,
.file-upload {
  width: 100%;
}

.bookmark-upload :deep(.el-upload-dragger),
.file-upload :deep(.el-upload-dragger) {
  border-radius: 12px;
  border: 1px dashed #b8ceef;
  background: #f8fbff;
  padding: 20px 10px;
  transition: all 0.2s ease;
}

.bookmark-upload :deep(.el-upload-dragger:hover),
.file-upload :deep(.el-upload-dragger:hover) {
  border-color: #7ea8e8;
  background: #f2f7ff;
}

.upload-icon {
  font-size: 34px;
  color: #2f63d5;
}

.upload-text {
  color: #4c668f;
}

.upload-text em {
  font-style: normal;
  color: #1f4fc3;
  font-weight: 700;
}

.manual-input :deep(.el-textarea__inner) {
  min-height: 180px;
}

.manual-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.import-preview {
  background: linear-gradient(180deg, #ffffff 0%, #f9fbff 100%);
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.preview-header h4 {
  margin: 0;
  color: #28436b;
  font-size: 15px;
}

.preview-list {
  max-height: 260px;
  overflow-y: auto;
  display: grid;
  gap: 10px;
}

.preview-item {
  border-radius: 10px;
  border: 1px solid #d6e3f8;
  background: #fff;
  padding: 10px;
}

.preview-item-error {
  border-color: #f4b2b2;
  background: #fff6f6;
}

.preview-item-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.preview-index {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #e8f0ff;
  color: #2f63d5;
  font-size: 12px;
  font-weight: 700;
  display: grid;
  place-items: center;
}

.preview-name {
  color: #28436b;
  font-weight: 700;
}

.preview-url {
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  word-break: break-all;
}

.preview-categories {
  margin-top: 6px;
  display: inline-flex;
  flex-wrap: wrap;
  gap: 6px;
}

.preview-categories :deep(.el-tag) {
  border: none;
  border-radius: 999px;
}

.status-monitor-content {
  max-height: 72vh;
  overflow: auto;
  display: grid;
  gap: 12px;
  padding-right: 4px;
}

.monitor-control-panel,
.status-statistics,
.status-list {
  border-radius: 14px;
  border: 1px solid #d8e4f8;
  background: #fff;
  padding: 14px;
}

.control-header,
.statistics-header,
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.control-title,
.statistics-title,
.list-title {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #274069;
  font-size: 17px;
}

.control-actions,
.list-filters {
  display: inline-flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.monitor-settings {
  border-radius: 12px;
  border: 1px dashed #d4e2f8;
  background: #f8fbff;
  padding: 10px;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
  gap: 12px;
}

.setting-item {
  display: grid;
  gap: 6px;
}

.setting-label {
  color: #4c668f;
  font-weight: 700;
  font-size: 13px;
}

.setting-control {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.setting-select,
.setting-input {
  flex: 1;
}

.setting-select :deep(.el-input__wrapper),
.setting-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  border: 1px solid #d6e3f8;
}

.setting-unit {
  color: #60789d;
  font-size: 12px;
}

.statistics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 10px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #d9e5f8;
  background: #f9fbff;
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: #fff;
  background: linear-gradient(120deg, #2563eb 0%, #3b82f6 100%);
}

.stat-number {
  color: #1f3b66;
  font-size: 22px;
  font-weight: 800;
  line-height: 1;
}

.stat-label {
  color: #5f7698;
  font-size: 12px;
}

.status-table {
  border-radius: 12px;
  border: 1px solid #d9e5f8;
  overflow: hidden;
}

.website-name-cell,
.url-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.website-name {
  font-weight: 700;
}

.url-text {
  color: #60789d;
  font-family: 'JetBrains Mono', 'Consolas', monospace;
  font-size: 12px;
}

.check-time,
.response-time,
.no-response {
  color: #5f7698;
  font-size: 12px;
}

.status-row-active {
  background: #f3fbf5;
}

.status-row-inactive {
  background: #fff9f1;
}

.status-row-broken {
  background: #fff5f5;
}

.status-row-unknown {
  background: #f7f8fb;
}

@keyframes countPulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.08);
  }
}

@keyframes aiDot {
  0%,
  100% {
    transform: translateY(0);
    opacity: 0.45;
  }
  50% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

@media (max-width: 1200px) {
  .website-collection {
    padding: 20px;
  }

  .page-header {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
    max-width: none;
  }
}

@media (max-width: 900px) {
  .category-header,
  .search-header,
  .control-header,
  .statistics-header,
  .list-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .pagination-controls {
    display: grid;
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .pagination-navigation {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .categories-container :deep(.el-radio-group) {
    gap: 8px;
  }

  .websites-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .settings-grid {
    grid-template-columns: 1fr;
  }

  .statistics-cards {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .website-collection {
    padding: 12px;
    gap: 14px;
  }

  .page-header,
  .category-filter,
  .search-section,
  .pagination-container,
  .monitor-control-panel,
  .status-statistics,
  .status-list {
    border-radius: 14px;
    padding: 12px;
  }

  .page-title {
    font-size: 1.45rem;
  }

  .header-actions :deep(.el-button) {
    flex: 1 1 calc(50% - 6px);
  }

  .websites-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .website-card {
    min-height: 190px;
  }

  .pagination-navigation {
    width: 100%;
    justify-content: flex-start;
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .main-pagination {
    align-self: center;
  }

  .page-jumper {
    width: auto;
    align-self: flex-end;
  }

  .method-options :deep(.el-radio-group) {
    width: 100%;
  }

  .method-options :deep(.el-radio-button) {
    flex: 1;
  }

  .manual-input-actions {
    width: 100%;
    justify-content: stretch;
  }

  .manual-input-actions :deep(.el-button) {
    flex: 1;
  }

  .statistics-cards {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
