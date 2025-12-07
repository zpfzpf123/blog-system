<template>
  <div class="tool-content">
    <div class="settings">
      <div class="setting-item">
        <label>类型:</label>
        <select v-model="type">
          <option value="paragraphs">段落</option>
          <option value="sentences">句子</option>
          <option value="words">单词</option>
        </select>
      </div>
      <div class="setting-item">
        <label>数量:</label>
        <input v-model.number="count" type="number" min="1" max="100" />
      </div>
      <div class="setting-item">
        <label>语言:</label>
        <select v-model="lang">
          <option value="latin">拉丁文</option>
          <option value="chinese">中文</option>
        </select>
      </div>
    </div>
    <div class="btn-group">
      <button @click="generate" class="btn primary">生成</button>
      <button @click="copy" class="btn">复制</button>
    </div>
    <div class="output-group">
      <label>生成结果:</label>
      <textarea v-model="output" readonly rows="10"></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const type = ref('paragraphs')
const count = ref(3)
const lang = ref('latin')
const output = ref('')

const latinWords = 'lorem ipsum dolor sit amet consectetur adipiscing elit sed do eiusmod tempor incididunt ut labore et dolore magna aliqua enim ad minim veniam quis nostrud exercitation ullamco laboris nisi aliquip ex ea commodo consequat duis aute irure in reprehenderit voluptate velit esse cillum fugiat nulla pariatur excepteur sint occaecat cupidatat non proident sunt culpa qui officia deserunt mollit anim id est laborum'.split(' ')

const chineseWords = '的一是不了在人有我他这个们中来上大为和国地到以说时要就出会可也你对生能而子那得于着下自之年过发后作里用道行所然家种事成方多经么去法学如都同现当没动面起看定天分还进好小部其些主样理心她本前开但因只从想实日军者意无力它与长把机十民第公此已工使情明性知全三又关点正业外将两高间由问很最重并物手应战向头文体政美相见被利什二等产或新己制身果加西斯月话合回特代内信表化老给世位次度门任常先海通教儿原东声提立及比员解水名真论处走义各入几口认条平系气题活尔更别打女变四神总何电数安少报才结反受目太量再感建务做接必场件计管期市直德资命山金指克许统区保至队形社便空决治展马科司五基眼书非则听白却界达光放强即像难且权思王象完设式色路记南品住告类求据程北边死张该交规万取拉格望觉术领共确传师观清今切院让识候带导争运笑飞风步改收根干造言联持组每济车亲极林服快办议往元英士证近失转夫令准布始怎呢存未远叫台单影具罗字爱击流备兵连调深商算质团集百需价花党华城石级整府离况亚请技际约示复病息究线似官火断精满支视消越器容照须九增研写称企八功吗包片史委乎查轻易早曾除农找装广显吧阿李标谈吃图念六引历首医局突专费号尽另周较注语仅考落青随选列武红响虽推势参希古众构房半节土投某案黑维革划敌致陈律足态护七兴派孩验责营星够章音跟志底站严巴例防族供效续施留讲型料终答紧黄绝奇察母京段依批群项故按河米围江织害斗双境客纪采举杀攻父苏密低朝友诉止细愿千值仍男钱破网热助倒育属坐帐错'.split('')

const generate = () => {
  const words = lang.value === 'latin' ? latinWords : chineseWords
  
  const getWord = () => words[Math.floor(Math.random() * words.length)]
  
  const getSentence = () => {
    const len = Math.floor(Math.random() * 10) + 5
    const sentence = Array.from({ length: len }, getWord).join(lang.value === 'latin' ? ' ' : '')
    return lang.value === 'latin' 
      ? sentence.charAt(0).toUpperCase() + sentence.slice(1) + '.'
      : sentence + '。'
  }
  
  const getParagraph = () => {
    const len = Math.floor(Math.random() * 4) + 3
    return Array.from({ length: len }, getSentence).join(lang.value === 'latin' ? ' ' : '')
  }
  
  switch (type.value) {
    case 'words':
      output.value = Array.from({ length: count.value }, getWord).join(lang.value === 'latin' ? ' ' : '')
      break
    case 'sentences':
      output.value = Array.from({ length: count.value }, getSentence).join(lang.value === 'latin' ? ' ' : '')
      break
    case 'paragraphs':
      output.value = Array.from({ length: count.value }, getParagraph).join('\n\n')
      break
  }
}

const copy = async () => {
  await navigator.clipboard.writeText(output.value)
}

onMounted(generate)
</script>

<style scoped>
.tool-content { display: flex; flex-direction: column; gap: 1rem; }
.settings { display: flex; flex-wrap: wrap; gap: 1rem; }
.setting-item { display: flex; flex-direction: column; gap: 0.25rem; }
.setting-item label { font-size: 0.85rem; color: #666; }
.setting-item input, .setting-item select { padding: 0.5rem; border: 1px solid #ddd; border-radius: 6px; min-width: 120px; }
.btn-group { display: flex; gap: 0.5rem; }
.btn { padding: 0.5rem 1rem; border: 1px solid #ddd; border-radius: 6px; cursor: pointer; background: white; }
.btn:hover { border-color: #667eea; color: #667eea; }
.btn.primary { background: #667eea; color: white; border-color: #667eea; }
.output-group { display: flex; flex-direction: column; gap: 0.5rem; }
label { font-weight: 600; color: #333; }
textarea { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 8px; resize: vertical; }
</style>
