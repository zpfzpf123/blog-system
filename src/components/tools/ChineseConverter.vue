<template>
  <div class="tool-content">
    <div class="input-group">
      <label>输入文本:</label>
      <textarea v-model="input" placeholder="输入简体或繁体中文..." rows="5"></textarea>
    </div>
    <div class="btn-group">
      <button @click="toTraditional" class="btn primary">简体 → 繁体</button>
      <button @click="toSimplified" class="btn primary">繁体 → 简体</button>
      <button @click="copy" class="btn">复制</button>
    </div>
    <div class="output-group">
      <label>转换结果:</label>
      <textarea v-model="output" readonly rows="5"></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const input = ref('')
const output = ref('')

// 简繁对照表 (常用字)
const s2t: Record<string, string> = {
  '国': '國', '学': '學', '发': '發', '会': '會', '为': '為', '这': '這', '来': '來', '时': '時',
  '个': '個', '们': '們', '说': '說', '对': '對', '过': '過', '动': '動', '开': '開', '关': '關',
  '长': '長', '问': '問', '头': '頭', '见': '見', '实': '實', '现': '現', '电': '電', '话': '話',
  '车': '車', '东': '東', '西': '西', '书': '書', '写': '寫', '读': '讀', '语': '語', '认': '認',
  '识': '識', '让': '讓', '请': '請', '进': '進', '还': '還', '没': '沒', '经': '經', '业': '業',
  '专': '專', '机': '機', '网': '網', '页': '頁', '图': '圖', '视': '視', '频': '頻', '听': '聽',
  '声': '聲', '乐': '樂', '画': '畫', '设': '設', '计': '計', '数': '數', '据': '據', '库': '庫',
  '统': '統', '系': '係', '软': '軟', '件': '件', '硬': '硬', '盘': '盤', '内': '內', '存': '存',
  '处': '處', '理': '理', '器': '器', '显': '顯', '示': '示', '输': '輸', '入': '入', '出': '出',
  '运': '運', '行': '行', '编': '編', '码': '碼', '程': '程', '序': '序', '算': '算', '法': '法',
  '变': '變', '量': '量', '类': '類', '型': '型', '函': '函', '数': '數', '组': '組', '织': '織',
  '结': '結', '构': '構', '接': '接', '口': '口', '调': '調', '用': '用', '返': '返', '回': '回',
  '错': '錯', '误': '誤', '异': '異', '常': '常', '测': '測', '试': '試', '调': '調', '试': '試',
  '发': '發', '布': '佈', '部': '部', '署': '署', '服': '服', '务': '務', '端': '端', '前': '前',
  '后': '後', '台': '臺', '管': '管', '理': '理', '员': '員', '用': '用', '户': '戶', '登': '登',
  '录': '錄', '注': '註', '册': '冊', '密': '密', '码': '碼', '验': '驗', '证': '證', '权': '權',
  '限': '限', '角': '角', '色': '色', '菜': '菜', '单': '單', '列': '列', '表': '表', '详': '詳',
  '情': '情', '添': '添', '加': '加', '删': '刪', '除': '除', '修': '修', '改': '改', '查': '查',
  '询': '詢', '搜': '搜', '索': '索', '排': '排', '序': '序', '筛': '篩', '选': '選', '导': '導',
  '航': '航', '链': '鏈', '接': '接', '跳': '跳', '转': '轉', '刷': '刷', '新': '新', '加': '加',
  '载': '載', '缓': '緩', '冲': '沖', '响': '響', '应': '應', '请': '請', '求': '求', '状': '狀',
  '态': '態', '成': '成', '功': '功', '失': '失', '败': '敗', '警': '警', '告': '告', '提': '提',
  '醒': '醒', '确': '確', '认': '認', '取': '取', '消': '消', '保': '保', '存': '存', '复': '復',
  '制': '製', '粘': '粘', '贴': '貼', '剪': '剪', '切': '切', '撤': '撤', '销': '銷', '恢': '恢',
}

// 生成繁简对照表
const t2s: Record<string, string> = {}
for (const [s, t] of Object.entries(s2t)) {
  t2s[t] = s
}

const toTraditional = () => {
  output.value = input.value.split('').map(c => s2t[c] || c).join('')
}

const toSimplified = () => {
  output.value = input.value.split('').map(c => t2s[c] || c).join('')
}

const copy = async () => {
  await navigator.clipboard.writeText(output.value)
}
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.input-group, .output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; font-size: 1rem; resize: vertical; }
textarea:focus { outline: none; border-color: #667eea; }
.btn-group { display: flex; gap: 0.5rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
</style>
