-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 清空现有数据
DELETE FROM css_animations;
DELETE FROM animation_categories;

-- 重置自增ID
ALTER TABLE css_animations AUTO_INCREMENT = 1;
ALTER TABLE animation_categories AUTO_INCREMENT = 1;

-- 插入分类 (12个分类)
INSERT INTO animation_categories (name) VALUES 
('悬停效果'),
('加载动画'),
('按钮特效'),
('文字动画'),
('图形变换'),
('入场动画'),
('退出动画'),
('强调效果'),
('背景特效'),
('边框动画'),
('阴影效果'),
('3D特效');

-- ==================== 悬停效果 (10个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('放大悬停', '悬停效果', '鼠标悬停时平滑放大',
'.hover-scale {
  transition: transform 0.3s ease;
}
.hover-scale:hover {
  transform: scale(1.1);
}'),

('缩小悬停', '悬停效果', '悬停时轻微缩小',
'.hover-shrink {
  transition: transform 0.3s ease;
}
.hover-shrink:hover {
  transform: scale(0.95);
}'),

('发光悬停', '悬停效果', '悬停时产生柔和光晕',
'.hover-glow {
  transition: box-shadow 0.3s ease;
}
.hover-glow:hover {
  box-shadow: 0 0 25px rgba(139, 92, 246, 0.6), 0 0 50px rgba(139, 92, 246, 0.3);
}'),

('3D倾斜', '悬停效果', '悬停时产生3D透视倾斜',
'.hover-tilt {
  transition: transform 0.4s ease;
}
.hover-tilt:hover {
  transform: perspective(500px) rotateX(10deg) rotateY(-10deg);
}'),

('上浮阴影', '悬停效果', '悬停时元素上浮并加深阴影',
'.hover-lift {
  transition: transform 0.3s, box-shadow 0.3s;
}
.hover-lift:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}'),

('旋转悬停', '悬停效果', '悬停时轻微旋转',
'.hover-rotate {
  transition: transform 0.4s ease;
}
.hover-rotate:hover {
  transform: rotate(5deg);
}'),

('倾斜悬停', '悬停效果', '悬停时X轴倾斜',
'.hover-skew {
  transition: transform 0.3s ease;
}
.hover-skew:hover {
  transform: skewX(-5deg);
}'),

('模糊悬停', '悬停效果', '悬停时背景模糊',
'.hover-blur {
  transition: filter 0.3s ease;
}
.hover-blur:hover {
  filter: blur(2px);
}'),

('亮度悬停', '悬停效果', '悬停时增加亮度',
'.hover-bright {
  transition: filter 0.3s ease;
}
.hover-bright:hover {
  filter: brightness(1.2);
}'),

('饱和度悬停', '悬停效果', '悬停时增加饱和度',
'.hover-saturate {
  transition: filter 0.3s ease;
}
.hover-saturate:hover {
  filter: saturate(1.5);
}');

-- ==================== 加载动画 (12个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('旋转圆环', '加载动画', '经典的旋转加载圈',
'.loader-spin {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(139, 92, 246, 0.2);
  border-top-color: #8b5cf6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}'),

('脉冲圆点', '加载动画', '三个圆点依次脉冲',
'.loader-dots {
  display: flex;
  gap: 8px;
}
.loader-dots span {
  width: 12px;
  height: 12px;
  background: #8b5cf6;
  border-radius: 50%;
  animation: dot-pulse 1.4s ease-in-out infinite;
}
.loader-dots span:nth-child(2) { animation-delay: 0.2s; }
.loader-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes dot-pulse {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}'),

('弹跳方块', '加载动画', '方块上下弹跳',
'.loader-bounce {
  width: 20px;
  height: 20px;
  background: linear-gradient(135deg, #8b5cf6, #f72585);
  border-radius: 4px;
  animation: bounce 0.6s ease-in-out infinite alternate;
}
@keyframes bounce {
  from { transform: translateY(0) scale(1, 1); }
  to { transform: translateY(-25px) scale(0.9, 1.1); }
}'),

('波浪条', '加载动画', '五条竖线波浪起伏',
'.loader-bars {
  display: flex;
  gap: 4px;
  align-items: center;
  height: 40px;
}
.loader-bars span {
  width: 6px;
  height: 100%;
  background: #8b5cf6;
  border-radius: 3px;
  animation: bar-wave 1s ease-in-out infinite;
}
.loader-bars span:nth-child(2) { animation-delay: 0.1s; }
.loader-bars span:nth-child(3) { animation-delay: 0.2s; }
.loader-bars span:nth-child(4) { animation-delay: 0.3s; }
.loader-bars span:nth-child(5) { animation-delay: 0.4s; }
@keyframes bar-wave {
  0%, 100% { transform: scaleY(0.4); }
  50% { transform: scaleY(1); }
}'),

('双环旋转', '加载动画', '两个圆环反向旋转',
'.loader-dual {
  width: 50px;
  height: 50px;
  border: 4px solid transparent;
  border-top-color: #8b5cf6;
  border-bottom-color: #f72585;
  border-radius: 50%;
  animation: dual-spin 1.2s linear infinite;
}
@keyframes dual-spin {
  to { transform: rotate(360deg); }
}'),

('进度条', '加载动画', '循环滚动的进度条',
'.loader-progress {
  width: 120px;
  height: 6px;
  background: rgba(139, 92, 246, 0.2);
  border-radius: 3px;
  overflow: hidden;
}
.loader-progress::after {
  content: "";
  display: block;
  width: 40%;
  height: 100%;
  background: linear-gradient(90deg, #8b5cf6, #f72585);
  border-radius: 3px;
  animation: progress-move 1.5s ease-in-out infinite;
}
@keyframes progress-move {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(350%); }
}'),

('追逐圆点', '加载动画', '圆点追逐旋转',
'.loader-chase {
  width: 40px;
  height: 40px;
  position: relative;
  animation: chase-rotate 2s linear infinite;
}
.loader-chase::before, .loader-chase::after {
  content: "";
  position: absolute;
  width: 12px;
  height: 12px;
  background: #8b5cf6;
  border-radius: 50%;
}
.loader-chase::before { top: 0; left: 50%; transform: translateX(-50%); }
.loader-chase::after { bottom: 0; left: 50%; transform: translateX(-50%); background: #f72585; }
@keyframes chase-rotate {
  to { transform: rotate(360deg); }
}'),

('方块旋转', '加载动画', '方块旋转变形',
'.loader-square {
  width: 30px;
  height: 30px;
  background: #8b5cf6;
  animation: square-spin 1.5s ease-in-out infinite;
}
@keyframes square-spin {
  0% { transform: rotate(0) scale(1); }
  50% { transform: rotate(180deg) scale(0.5); }
  100% { transform: rotate(360deg) scale(1); }
}'),

('环形进度', '加载动画', '环形加载进度',
'.loader-ring {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(139, 92, 246, 0.1);
  border-radius: 50%;
  position: relative;
}
.loader-ring::after {
  content: "";
  position: absolute;
  inset: -4px;
  border: 4px solid transparent;
  border-top-color: #8b5cf6;
  border-radius: 50%;
  animation: ring-spin 1s linear infinite;
}
@keyframes ring-spin {
  to { transform: rotate(360deg); }
}'),

('脉冲环', '加载动画', '脉冲扩散的圆环',
'.loader-pulse-ring {
  width: 40px;
  height: 40px;
  border: 3px solid #8b5cf6;
  border-radius: 50%;
  animation: pulse-expand 1.5s ease-out infinite;
}
@keyframes pulse-expand {
  0% { transform: scale(0.5); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}'),

('翻转方块', '加载动画', '3D翻转的方块',
'.loader-flip {
  width: 30px;
  height: 30px;
  background: linear-gradient(135deg, #8b5cf6, #f72585);
  animation: flip-load 1.2s ease-in-out infinite;
}
@keyframes flip-load {
  0% { transform: perspective(200px) rotateX(0) rotateY(0); }
  50% { transform: perspective(200px) rotateX(180deg) rotateY(0); }
  100% { transform: perspective(200px) rotateX(180deg) rotateY(180deg); }
}'),

('弹性球', '加载动画', '弹性变形的球',
'.loader-elastic {
  width: 30px;
  height: 30px;
  background: #8b5cf6;
  border-radius: 50%;
  animation: elastic-ball 0.8s ease-in-out infinite;
}
@keyframes elastic-ball {
  0%, 100% { transform: scale(1, 1); }
  25% { transform: scale(1.2, 0.8); }
  50% { transform: scale(0.8, 1.2); }
  75% { transform: scale(1.1, 0.9); }
}');

-- ==================== 按钮特效 (10个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('按压反馈', '按钮特效', '点击时产生按压效果',
'.btn-press {
  transition: transform 0.15s ease;
}
.btn-press:active {
  transform: scale(0.95);
}'),

('渐变流动', '按钮特效', '背景渐变色持续流动',
'.btn-gradient {
  background: linear-gradient(90deg, #8b5cf6, #f72585, #06b6d4, #8b5cf6);
  background-size: 300% 100%;
  animation: gradient-shift 4s linear infinite;
}
@keyframes gradient-shift {
  0% { background-position: 0% 50%; }
  100% { background-position: 300% 50%; }
}'),

('波纹扩散', '按钮特效', '点击产生波纹扩散效果',
'.btn-ripple {
  position: relative;
  overflow: hidden;
}
.btn-ripple::after {
  content: "";
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, transparent 60%);
  transform: scale(0);
  opacity: 0;
}
.btn-ripple:active::after {
  animation: ripple 0.6s ease-out;
}
@keyframes ripple {
  0% { transform: scale(0); opacity: 1; }
  100% { transform: scale(2.5); opacity: 0; }
}'),

('霓虹边框', '按钮特效', '霓虹灯风格的发光边框',
'.btn-neon {
  border: 2px solid #8b5cf6;
  box-shadow: 0 0 10px #8b5cf6, inset 0 0 10px rgba(139, 92, 246, 0.1);
  animation: neon-pulse 2s ease-in-out infinite;
}
@keyframes neon-pulse {
  0%, 100% { box-shadow: 0 0 10px #8b5cf6, 0 0 20px #8b5cf6; }
  50% { box-shadow: 0 0 20px #8b5cf6, 0 0 40px #8b5cf6; }
}'),

('抖动按钮', '按钮特效', '吸引注意力的抖动效果',
'.btn-shake {
  animation: btn-shake 0.5s ease-in-out infinite;
}
@keyframes btn-shake {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-4px) rotate(-2deg); }
  40% { transform: translateX(4px) rotate(2deg); }
  60% { transform: translateX(-4px) rotate(-2deg); }
  80% { transform: translateX(4px) rotate(2deg); }
}'),

('光泽滑过', '按钮特效', '光泽从左到右滑过',
'.btn-shine {
  position: relative;
  overflow: hidden;
}
.btn-shine::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.3), transparent);
  animation: shine-slide 2s infinite;
}
@keyframes shine-slide {
  0% { left: -100%; }
  50%, 100% { left: 100%; }
}'),

('边框旋转', '按钮特效', '边框颜色旋转变化',
'.btn-border-rotate {
  border: 3px solid;
  border-image: linear-gradient(45deg, #8b5cf6, #f72585, #06b6d4) 1;
  animation: border-hue 3s linear infinite;
}
@keyframes border-hue {
  to { filter: hue-rotate(360deg); }
}'),

('弹性按钮', '按钮特效', '弹性缩放效果',
'.btn-elastic {
  animation: elastic-btn 2s ease-in-out infinite;
}
@keyframes elastic-btn {
  0%, 100% { transform: scale(1); }
  30% { transform: scale(1.05); }
  40% { transform: scale(0.98); }
  50% { transform: scale(1.02); }
  60% { transform: scale(1); }
}'),

('填充动画', '按钮特效', '背景从左到右填充',
'.btn-fill {
  background: linear-gradient(90deg, #8b5cf6 50%, transparent 50%);
  background-size: 200% 100%;
  background-position: 100% 0;
  transition: background-position 0.4s ease;
}
.btn-fill:hover {
  background-position: 0 0;
}'),

('脉冲按钮', '按钮特效', '脉冲扩散效果',
'.btn-pulse {
  animation: btn-pulse-anim 2s ease-out infinite;
}
@keyframes btn-pulse-anim {
  0% { box-shadow: 0 0 0 0 rgba(139, 92, 246, 0.5); }
  70% { box-shadow: 0 0 0 15px rgba(139, 92, 246, 0); }
  100% { box-shadow: 0 0 0 0 rgba(139, 92, 246, 0); }
}');

-- ==================== 文字动画 (10个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('打字机', '文字动画', '逐字显示的打字效果',
'.text-typewriter {
  overflow: hidden;
  white-space: nowrap;
  border-right: 3px solid #8b5cf6;
  animation: typing 3s steps(12) infinite, cursor-blink 0.5s step-end infinite alternate;
}
@keyframes typing {
  0%, 90%, 100% { width: 0; }
  30%, 60% { width: 100%; }
}
@keyframes cursor-blink {
  50% { border-color: transparent; }
}'),

('渐变文字', '文字动画', '文字颜色渐变流动',
'.text-gradient {
  background: linear-gradient(90deg, #8b5cf6, #f72585, #06b6d4, #8b5cf6);
  background-size: 300% 100%;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: text-flow 4s linear infinite;
}
@keyframes text-flow {
  0% { background-position: 0% 50%; }
  100% { background-position: 300% 50%; }
}'),

('弹跳字母', '文字动画', '字母依次弹跳',
'.text-bounce span {
  display: inline-block;
  animation: letter-bounce 0.6s ease infinite;
}
.text-bounce span:nth-child(2) { animation-delay: 0.1s; }
.text-bounce span:nth-child(3) { animation-delay: 0.2s; }
.text-bounce span:nth-child(4) { animation-delay: 0.3s; }
.text-bounce span:nth-child(5) { animation-delay: 0.4s; }
@keyframes letter-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}'),

('霓虹文字', '文字动画', '霓虹灯发光效果',
'.text-neon {
  color: #fff;
  text-shadow: 0 0 10px #8b5cf6, 0 0 20px #8b5cf6, 0 0 40px #f72585, 0 0 80px #f72585;
  animation: neon-glow 2s ease-in-out infinite alternate;
}
@keyframes neon-glow {
  from { text-shadow: 0 0 10px #8b5cf6, 0 0 20px #8b5cf6, 0 0 40px #f72585; }
  to { text-shadow: 0 0 20px #8b5cf6, 0 0 40px #8b5cf6, 0 0 80px #f72585, 0 0 100px #f72585; }
}'),

('波浪文字', '文字动画', '文字波浪起伏',
'.text-wave span {
  display: inline-block;
  animation: text-wave-anim 1.5s ease-in-out infinite;
}
.text-wave span:nth-child(2) { animation-delay: 0.1s; }
.text-wave span:nth-child(3) { animation-delay: 0.2s; }
.text-wave span:nth-child(4) { animation-delay: 0.3s; }
.text-wave span:nth-child(5) { animation-delay: 0.4s; }
@keyframes text-wave-anim {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}'),

('闪烁文字', '文字动画', '文字闪烁效果',
'.text-blink {
  animation: text-blink-anim 1s ease-in-out infinite;
}
@keyframes text-blink-anim {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}'),

('抖动文字', '文字动画', '文字抖动效果',
'.text-shake {
  animation: text-shake-anim 0.3s ease-in-out infinite;
}
@keyframes text-shake-anim {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-3px); }
  75% { transform: translateX(3px); }
}'),

('旋转文字', '文字动画', '文字旋转进入',
'.text-rotate {
  animation: text-rotate-anim 1s ease-out forwards;
}
@keyframes text-rotate-anim {
  from { transform: rotate(-10deg) scale(0.8); opacity: 0; }
  to { transform: rotate(0) scale(1); opacity: 1; }
}'),

('模糊清晰', '文字动画', '从模糊到清晰',
'.text-blur {
  animation: text-blur-anim 2s ease-in-out infinite;
}
@keyframes text-blur-anim {
  0%, 100% { filter: blur(0); }
  50% { filter: blur(3px); }
}'),

('描边文字', '文字动画', '描边动画效果',
'.text-stroke {
  -webkit-text-stroke: 2px #8b5cf6;
  color: transparent;
  animation: stroke-fill 3s ease infinite;
}
@keyframes stroke-fill {
  0%, 100% { -webkit-text-stroke-color: #8b5cf6; }
  50% { -webkit-text-stroke-color: #f72585; }
}');

-- ==================== 图形变换 (10个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('旋转方块', '图形变换', '持续旋转的方块',
'.shape-rotate {
  animation: shape-rotate360 3s linear infinite;
}
@keyframes shape-rotate360 {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}'),

('呼吸缩放', '图形变换', '模拟呼吸的缩放效果',
'.shape-breathe {
  animation: shape-breathe-anim 2.5s ease-in-out infinite;
}
@keyframes shape-breathe-anim {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}'),

('浮动效果', '图形变换', '上下浮动漂浮',
'.shape-float {
  animation: shape-float-anim 3s ease-in-out infinite;
}
@keyframes shape-float-anim {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}'),

('翻转卡片', '图形变换', '3D翻转效果',
'.shape-flip {
  animation: shape-flip-anim 2s ease-in-out infinite;
}
@keyframes shape-flip-anim {
  0% { transform: perspective(400px) rotateY(0); }
  50% { transform: perspective(400px) rotateY(180deg); }
  100% { transform: perspective(400px) rotateY(360deg); }
}'),

('弹性变形', '图形变换', '弹性挤压变形',
'.shape-elastic {
  animation: shape-elastic-anim 1s ease-in-out infinite;
}
@keyframes shape-elastic-anim {
  0%, 100% { transform: scale(1, 1); }
  25% { transform: scale(1.2, 0.8); }
  50% { transform: scale(0.8, 1.2); }
  75% { transform: scale(1.1, 0.9); }
}'),

('摇摆钟摆', '图形变换', '左右摇摆效果',
'.shape-swing {
  transform-origin: top center;
  animation: shape-swing-anim 1.5s ease-in-out infinite;
}
@keyframes shape-swing-anim {
  0%, 100% { transform: rotate(15deg); }
  50% { transform: rotate(-15deg); }
}'),

('果冻效果', '图形变换', '果冻般的弹性',
'.shape-jelly {
  animation: shape-jelly-anim 0.8s ease infinite;
}
@keyframes shape-jelly-anim {
  0%, 100% { transform: scale(1, 1); }
  25% { transform: scale(0.9, 1.1); }
  50% { transform: scale(1.1, 0.9); }
  75% { transform: scale(0.95, 1.05); }
}'),

('心跳效果', '图形变换', '心跳般的跳动',
'.shape-heartbeat {
  animation: shape-heartbeat-anim 1.2s ease-in-out infinite;
}
@keyframes shape-heartbeat-anim {
  0%, 100% { transform: scale(1); }
  14% { transform: scale(1.15); }
  28% { transform: scale(1); }
  42% { transform: scale(1.15); }
  70% { transform: scale(1); }
}'),

('变形方圆', '图形变换', '方形和圆形之间变换',
'.shape-morph {
  animation: shape-morph-anim 2s ease-in-out infinite;
}
@keyframes shape-morph-anim {
  0%, 100% { border-radius: 10%; }
  50% { border-radius: 50%; }
}'),

('螺旋旋转', '图形变换', '螺旋式旋转缩放',
'.shape-spiral {
  animation: shape-spiral-anim 3s linear infinite;
}
@keyframes shape-spiral-anim {
  0% { transform: rotate(0) scale(1); }
  50% { transform: rotate(180deg) scale(0.5); }
  100% { transform: rotate(360deg) scale(1); }
}');

-- ==================== 入场动画 (10个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('淡入', '入场动画', '透明度渐变淡入',
'.enter-fade {
  animation: enter-fadeIn 0.6s ease forwards;
}
@keyframes enter-fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}'),

('上滑入场', '入场动画', '从下方滑入',
'.enter-slide-up {
  animation: enter-slideUp 0.5s ease forwards;
}
@keyframes enter-slideUp {
  from { opacity: 0; transform: translateY(40px); }
  to { opacity: 1; transform: translateY(0); }
}'),

('下滑入场', '入场动画', '从上方滑入',
'.enter-slide-down {
  animation: enter-slideDown 0.5s ease forwards;
}
@keyframes enter-slideDown {
  from { opacity: 0; transform: translateY(-40px); }
  to { opacity: 1; transform: translateY(0); }
}'),

('左滑入场', '入场动画', '从右侧滑入',
'.enter-slide-left {
  animation: enter-slideLeft 0.5s ease forwards;
}
@keyframes enter-slideLeft {
  from { opacity: 0; transform: translateX(40px); }
  to { opacity: 1; transform: translateX(0); }
}'),

('右滑入场', '入场动画', '从左侧滑入',
'.enter-slide-right {
  animation: enter-slideRight 0.5s ease forwards;
}
@keyframes enter-slideRight {
  from { opacity: 0; transform: translateX(-40px); }
  to { opacity: 1; transform: translateX(0); }
}'),

('缩放入场', '入场动画', '从小到大缩放进入',
'.enter-zoom {
  animation: enter-zoomIn 0.4s ease forwards;
}
@keyframes enter-zoomIn {
  from { opacity: 0; transform: scale(0.5); }
  to { opacity: 1; transform: scale(1); }
}'),

('弹性入场', '入场动画', '带弹性的进入效果',
'.enter-elastic {
  animation: enter-elasticIn 0.8s cubic-bezier(0.68, -0.55, 0.265, 1.55) forwards;
}
@keyframes enter-elasticIn {
  from { opacity: 0; transform: scale(0.3); }
  to { opacity: 1; transform: scale(1); }
}'),

('旋转入场', '入场动画', '旋转进入',
'.enter-rotate {
  animation: enter-rotateIn 0.6s ease forwards;
}
@keyframes enter-rotateIn {
  from { opacity: 0; transform: rotate(-180deg) scale(0.5); }
  to { opacity: 1; transform: rotate(0) scale(1); }
}'),

('翻转入场', '入场动画', '3D翻转进入',
'.enter-flip {
  animation: enter-flipIn 0.6s ease forwards;
}
@keyframes enter-flipIn {
  from { opacity: 0; transform: perspective(400px) rotateX(90deg); }
  to { opacity: 1; transform: perspective(400px) rotateX(0); }
}'),

('弹跳入场', '入场动画', '弹跳进入效果',
'.enter-bounce {
  animation: enter-bounceIn 0.8s ease forwards;
}
@keyframes enter-bounceIn {
  0% { opacity: 0; transform: scale(0.3); }
  50% { transform: scale(1.1); }
  70% { transform: scale(0.9); }
  100% { opacity: 1; transform: scale(1); }
}');

-- ==================== 退出动画 (8个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('淡出', '退出动画', '透明度渐变淡出',
'.exit-fade {
  animation: exit-fadeOut 0.5s ease forwards;
}
@keyframes exit-fadeOut {
  from { opacity: 1; }
  to { opacity: 0; }
}'),

('上滑退出', '退出动画', '向上滑出',
'.exit-slide-up {
  animation: exit-slideUp 0.5s ease forwards;
}
@keyframes exit-slideUp {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(-40px); }
}'),

('下滑退出', '退出动画', '向下滑出',
'.exit-slide-down {
  animation: exit-slideDown 0.5s ease forwards;
}
@keyframes exit-slideDown {
  from { opacity: 1; transform: translateY(0); }
  to { opacity: 0; transform: translateY(40px); }
}'),

('缩小退出', '退出动画', '缩小消失',
'.exit-zoom {
  animation: exit-zoomOut 0.4s ease forwards;
}
@keyframes exit-zoomOut {
  from { opacity: 1; transform: scale(1); }
  to { opacity: 0; transform: scale(0.5); }
}'),

('旋转退出', '退出动画', '旋转消失',
'.exit-rotate {
  animation: exit-rotateOut 0.5s ease forwards;
}
@keyframes exit-rotateOut {
  from { opacity: 1; transform: rotate(0) scale(1); }
  to { opacity: 0; transform: rotate(180deg) scale(0.5); }
}'),

('翻转退出', '退出动画', '3D翻转退出',
'.exit-flip {
  animation: exit-flipOut 0.5s ease forwards;
}
@keyframes exit-flipOut {
  from { opacity: 1; transform: perspective(400px) rotateX(0); }
  to { opacity: 0; transform: perspective(400px) rotateX(90deg); }
}'),

('爆炸退出', '退出动画', '爆炸式消失',
'.exit-explode {
  animation: exit-explodeOut 0.5s ease forwards;
}
@keyframes exit-explodeOut {
  from { opacity: 1; transform: scale(1); }
  to { opacity: 0; transform: scale(2); }
}'),

('收缩退出', '退出动画', '收缩到中心消失',
'.exit-shrink {
  animation: exit-shrinkOut 0.4s ease forwards;
}
@keyframes exit-shrinkOut {
  from { opacity: 1; transform: scale(1); }
  to { opacity: 0; transform: scale(0); }
}');

-- ==================== 强调效果 (10个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('心跳强调', '强调效果', '心跳般的跳动',
'.emphasis-heartbeat {
  animation: emph-heartbeat 1.2s ease-in-out infinite;
}
@keyframes emph-heartbeat {
  0%, 100% { transform: scale(1); }
  14% { transform: scale(1.15); }
  28% { transform: scale(1); }
  42% { transform: scale(1.15); }
  70% { transform: scale(1); }
}'),

('抖动提醒', '强调效果', '左右抖动吸引注意',
'.emphasis-shake {
  animation: emph-shake 0.8s ease-in-out infinite;
}
@keyframes emph-shake {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-5px); }
  20%, 40%, 60%, 80% { transform: translateX(5px); }
}'),

('闪烁强调', '强调效果', '闪烁提醒效果',
'.emphasis-flash {
  animation: emph-flash 1.5s ease-in-out infinite;
}
@keyframes emph-flash {
  0%, 50%, 100% { opacity: 1; }
  25%, 75% { opacity: 0.3; }
}'),

('弹跳强调', '强调效果', '弹跳吸引注意',
'.emphasis-bounce {
  animation: emph-bounce 1s ease infinite;
}
@keyframes emph-bounce {
  0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-20px); }
  60% { transform: translateY(-10px); }
}'),

('脉冲强调', '强调效果', '脉冲扩散效果',
'.emphasis-pulse {
  animation: emph-pulse 1.5s ease-out infinite;
}
@keyframes emph-pulse {
  0% { box-shadow: 0 0 0 0 rgba(139, 92, 246, 0.6); }
  100% { box-shadow: 0 0 0 20px rgba(139, 92, 246, 0); }
}'),

('摇晃强调', '强调效果', '左右摇晃',
'.emphasis-wobble {
  animation: emph-wobble 1s ease-in-out infinite;
}
@keyframes emph-wobble {
  0%, 100% { transform: rotate(0); }
  15% { transform: rotate(-10deg); }
  30% { transform: rotate(8deg); }
  45% { transform: rotate(-6deg); }
  60% { transform: rotate(4deg); }
  75% { transform: rotate(-2deg); }
}'),

('橡皮筋', '强调效果', '橡皮筋弹性效果',
'.emphasis-rubber {
  animation: emph-rubber 1s ease-in-out infinite;
}
@keyframes emph-rubber {
  0%, 100% { transform: scale(1, 1); }
  30% { transform: scale(1.25, 0.75); }
  40% { transform: scale(0.75, 1.25); }
  50% { transform: scale(1.15, 0.85); }
  65% { transform: scale(0.95, 1.05); }
  75% { transform: scale(1.05, 0.95); }
}'),

('摆动强调', '强调效果', '钟摆式摆动',
'.emphasis-swing {
  transform-origin: top center;
  animation: emph-swing 1s ease-in-out infinite;
}
@keyframes emph-swing {
  20% { transform: rotate(15deg); }
  40% { transform: rotate(-10deg); }
  60% { transform: rotate(5deg); }
  80% { transform: rotate(-5deg); }
  100% { transform: rotate(0); }
}'),

('跳跃强调', '强调效果', '跳跃效果',
'.emphasis-jump {
  animation: emph-jump 0.5s ease infinite;
}
@keyframes emph-jump {
  0%, 100% { transform: translateY(0) scale(1, 1); }
  30% { transform: translateY(-15px) scale(1, 1.1); }
  50% { transform: translateY(0) scale(1.1, 0.9); }
  70% { transform: translateY(-5px) scale(1, 1); }
}'),

('旋转强调', '强调效果', '旋转吸引注意',
'.emphasis-spin {
  animation: emph-spin 1s linear infinite;
}
@keyframes emph-spin {
  from { transform: rotate(0); }
  to { transform: rotate(360deg); }
}');

-- ==================== 背景特效 (10个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('渐变背景', '背景特效', '背景色渐变流动',
'.bg-gradient {
  background: linear-gradient(-45deg, #8b5cf6, #f72585, #06b6d4, #3b82f6);
  background-size: 400% 400%;
  animation: bg-shift 8s ease infinite;
}
@keyframes bg-shift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}'),

('星光闪烁', '背景特效', '星星闪烁效果',
'.bg-stars {
  background: radial-gradient(circle, #fff 1px, transparent 1px);
  background-size: 50px 50px;
  animation: bg-twinkle 3s ease-in-out infinite;
}
@keyframes bg-twinkle {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}'),

('波纹背景', '背景特效', '水波纹扩散',
'.bg-ripple {
  background: radial-gradient(circle, rgba(139, 92, 246, 0.3) 0%, transparent 70%);
  animation: bg-ripple-expand 2s ease-out infinite;
}
@keyframes bg-ripple-expand {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(2); opacity: 0; }
}'),

('光晕移动', '背景特效', '光晕缓慢移动',
'.bg-glow {
  background: radial-gradient(circle at 30% 30%, rgba(139, 92, 246, 0.4), transparent 50%);
  animation: bg-glow-move 6s ease-in-out infinite;
}
@keyframes bg-glow-move {
  0%, 100% { background-position: 30% 30%; }
  50% { background-position: 70% 70%; }
}'),

('条纹滚动', '背景特效', '斜条纹滚动',
'.bg-stripes {
  background: repeating-linear-gradient(45deg, #8b5cf6 0px, #8b5cf6 10px, transparent 10px, transparent 20px);
  background-size: 200% 200%;
  animation: bg-stripes-move 2s linear infinite;
}
@keyframes bg-stripes-move {
  from { background-position: 0 0; }
  to { background-position: 40px 40px; }
}'),

('网格呼吸', '背景特效', '网格背景呼吸',
'.bg-grid {
  background-image: linear-gradient(rgba(139, 92, 246, 0.3) 1px, transparent 1px),
                    linear-gradient(90deg, rgba(139, 92, 246, 0.3) 1px, transparent 1px);
  background-size: 20px 20px;
  animation: bg-grid-pulse 3s ease-in-out infinite;
}
@keyframes bg-grid-pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}'),

('极光效果', '背景特效', '极光般的流动',
'.bg-aurora {
  background: linear-gradient(45deg, #8b5cf6, #06b6d4, #f72585, #8b5cf6);
  background-size: 400% 400%;
  animation: bg-aurora-flow 10s ease infinite;
  filter: blur(30px);
}
@keyframes bg-aurora-flow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}'),

('噪点闪烁', '背景特效', '噪点闪烁效果',
'.bg-noise {
  background: url("data:image/svg+xml,%3Csvg viewBox=\'0 0 256 256\' xmlns=\'http://www.w3.org/2000/svg\'%3E%3Cfilter id=\'noise\'%3E%3CfeTurbulence type=\'fractalNoise\' baseFrequency=\'0.8\'/%3E%3C/filter%3E%3Crect width=\'100%25\' height=\'100%25\' filter=\'url(%23noise)\'/%3E%3C/svg%3E");
  animation: bg-noise-flicker 0.2s steps(10) infinite;
}
@keyframes bg-noise-flicker {
  0%, 100% { opacity: 0.05; }
  50% { opacity: 0.1; }
}'),

('彩虹渐变', '背景特效', '彩虹色渐变流动',
'.bg-rainbow {
  background: linear-gradient(90deg, #ff0000, #ff7f00, #ffff00, #00ff00, #0000ff, #4b0082, #9400d3, #ff0000);
  background-size: 800% 100%;
  animation: bg-rainbow-flow 6s linear infinite;
}
@keyframes bg-rainbow-flow {
  from { background-position: 0% 50%; }
  to { background-position: 800% 50%; }
}'),

('聚光灯', '背景特效', '聚光灯移动效果',
'.bg-spotlight {
  background: radial-gradient(circle at 50% 50%, rgba(255,255,255,0.2) 0%, transparent 50%);
  animation: bg-spotlight-move 4s ease-in-out infinite;
}
@keyframes bg-spotlight-move {
  0%, 100% { background-position: 0% 0%; }
  25% { background-position: 100% 0%; }
  50% { background-position: 100% 100%; }
  75% { background-position: 0% 100%; }
}');

-- ==================== 边框动画 (8个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('边框流光', '边框动画', '边框颜色流动',
'.border-flow {
  border: 3px solid;
  border-image: linear-gradient(90deg, #8b5cf6, #f72585, #06b6d4, #8b5cf6) 1;
  animation: border-flow-anim 3s linear infinite;
}
@keyframes border-flow-anim {
  to { filter: hue-rotate(360deg); }
}'),

('边框脉冲', '边框动画', '边框脉冲扩散',
'.border-pulse {
  border: 2px solid #8b5cf6;
  animation: border-pulse-anim 1.5s ease-out infinite;
}
@keyframes border-pulse-anim {
  0% { box-shadow: 0 0 0 0 rgba(139, 92, 246, 0.5); }
  100% { box-shadow: 0 0 0 15px rgba(139, 92, 246, 0); }
}'),

('虚线旋转', '边框动画', '虚线边框旋转',
'.border-dash {
  border: 3px dashed #8b5cf6;
  animation: border-dash-rotate 10s linear infinite;
}
@keyframes border-dash-rotate {
  to { transform: rotate(360deg); }
}'),

('边框呼吸', '边框动画', '边框宽度呼吸',
'.border-breathe {
  border: 2px solid #8b5cf6;
  animation: border-breathe-anim 2s ease-in-out infinite;
}
@keyframes border-breathe-anim {
  0%, 100% { border-width: 2px; }
  50% { border-width: 5px; }
}'),

('霓虹边框', '边框动画', '霓虹灯边框效果',
'.border-neon {
  border: 2px solid #8b5cf6;
  animation: border-neon-glow 2s ease-in-out infinite;
}
@keyframes border-neon-glow {
  0%, 100% { box-shadow: 0 0 5px #8b5cf6, 0 0 10px #8b5cf6, inset 0 0 5px rgba(139, 92, 246, 0.1); }
  50% { box-shadow: 0 0 20px #8b5cf6, 0 0 40px #8b5cf6, inset 0 0 10px rgba(139, 92, 246, 0.2); }
}'),

('边框闪烁', '边框动画', '边框闪烁效果',
'.border-blink {
  border: 2px solid #8b5cf6;
  animation: border-blink-anim 1s ease-in-out infinite;
}
@keyframes border-blink-anim {
  0%, 100% { border-color: #8b5cf6; }
  50% { border-color: transparent; }
}'),

('渐变边框', '边框动画', '渐变色边框旋转',
'.border-gradient {
  border: 4px solid transparent;
  background: linear-gradient(#0c0c1d, #0c0c1d) padding-box,
              linear-gradient(90deg, #8b5cf6, #f72585, #06b6d4) border-box;
  animation: border-gradient-rotate 3s linear infinite;
}
@keyframes border-gradient-rotate {
  to { filter: hue-rotate(360deg); }
}'),

('描边动画', '边框动画', 'SVG描边动画效果',
'.border-draw {
  stroke-dasharray: 300;
  stroke-dashoffset: 300;
  animation: border-draw-anim 2s ease forwards infinite;
}
@keyframes border-draw-anim {
  to { stroke-dashoffset: 0; }
}');

-- ==================== 阴影效果 (8个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('阴影呼吸', '阴影效果', '阴影大小呼吸变化',
'.shadow-breathe {
  animation: shadow-breathe-anim 2s ease-in-out infinite;
}
@keyframes shadow-breathe-anim {
  0%, 100% { box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2); }
  50% { box-shadow: 0 15px 40px rgba(0, 0, 0, 0.4); }
}'),

('彩色阴影', '阴影效果', '阴影颜色变化',
'.shadow-color {
  animation: shadow-color-shift 4s ease-in-out infinite;
}
@keyframes shadow-color-shift {
  0%, 100% { box-shadow: 0 10px 30px rgba(139, 92, 246, 0.5); }
  33% { box-shadow: 0 10px 30px rgba(247, 37, 133, 0.5); }
  66% { box-shadow: 0 10px 30px rgba(6, 182, 212, 0.5); }
}'),

('多层阴影', '阴影效果', '多层阴影脉冲',
'.shadow-layers {
  animation: shadow-layers-pulse 2s ease-in-out infinite;
}
@keyframes shadow-layers-pulse {
  0%, 100% { box-shadow: 0 0 0 5px rgba(139, 92, 246, 0.2), 0 0 0 10px rgba(139, 92, 246, 0.1); }
  50% { box-shadow: 0 0 0 10px rgba(139, 92, 246, 0.3), 0 0 0 20px rgba(139, 92, 246, 0.15); }
}'),

('内阴影', '阴影效果', '内阴影呼吸',
'.shadow-inset {
  animation: shadow-inset-anim 2s ease-in-out infinite;
}
@keyframes shadow-inset-anim {
  0%, 100% { box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.2); }
  50% { box-shadow: inset 0 0 30px rgba(0, 0, 0, 0.4); }
}'),

('发光阴影', '阴影效果', '发光效果',
'.shadow-glow {
  animation: shadow-glow-anim 2s ease-in-out infinite;
}
@keyframes shadow-glow-anim {
  0%, 100% { box-shadow: 0 0 20px rgba(139, 92, 246, 0.5); }
  50% { box-shadow: 0 0 40px rgba(139, 92, 246, 0.8), 0 0 60px rgba(139, 92, 246, 0.4); }
}'),

('阴影移动', '阴影效果', '阴影位置移动',
'.shadow-move {
  animation: shadow-move-anim 3s ease-in-out infinite;
}
@keyframes shadow-move-anim {
  0%, 100% { box-shadow: -10px 10px 20px rgba(0, 0, 0, 0.3); }
  50% { box-shadow: 10px -10px 20px rgba(0, 0, 0, 0.3); }
}'),

('霓虹阴影', '阴影效果', '霓虹灯阴影效果',
'.shadow-neon {
  animation: shadow-neon-anim 2s ease-in-out infinite alternate;
}
@keyframes shadow-neon-anim {
  from { box-shadow: 0 0 10px #8b5cf6, 0 0 20px #8b5cf6, 0 0 30px #f72585; }
  to { box-shadow: 0 0 20px #8b5cf6, 0 0 40px #8b5cf6, 0 0 60px #f72585; }
}'),

('长阴影', '阴影效果', '长阴影动画',
'.shadow-long {
  animation: shadow-long-anim 3s ease-in-out infinite;
}
@keyframes shadow-long-anim {
  0%, 100% { box-shadow: 5px 5px 0 #8b5cf6, 10px 10px 0 #7c3aed; }
  50% { box-shadow: -5px 5px 0 #8b5cf6, -10px 10px 0 #7c3aed; }
}');

-- ==================== 3D特效 (10个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('3D旋转X', '3D特效', 'X轴3D旋转',
'.d3-rotate-x {
  animation: d3-rotateX 3s linear infinite;
}
@keyframes d3-rotateX {
  from { transform: perspective(500px) rotateX(0); }
  to { transform: perspective(500px) rotateX(360deg); }
}'),

('3D旋转Y', '3D特效', 'Y轴3D旋转',
'.d3-rotate-y {
  animation: d3-rotateY 3s linear infinite;
}
@keyframes d3-rotateY {
  from { transform: perspective(500px) rotateY(0); }
  to { transform: perspective(500px) rotateY(360deg); }
}'),

('3D翻转', '3D特效', '3D翻转效果',
'.d3-flip {
  animation: d3-flip-anim 2s ease-in-out infinite;
}
@keyframes d3-flip-anim {
  0%, 100% { transform: perspective(500px) rotateY(0); }
  50% { transform: perspective(500px) rotateY(180deg); }
}'),

('3D摇摆', '3D特效', '3D摇摆效果',
'.d3-swing {
  animation: d3-swing-anim 2s ease-in-out infinite;
}
@keyframes d3-swing-anim {
  0%, 100% { transform: perspective(500px) rotateX(0) rotateY(0); }
  25% { transform: perspective(500px) rotateX(10deg) rotateY(10deg); }
  75% { transform: perspective(500px) rotateX(-10deg) rotateY(-10deg); }
}'),

('3D浮动', '3D特效', '3D浮动效果',
'.d3-float {
  animation: d3-float-anim 3s ease-in-out infinite;
}
@keyframes d3-float-anim {
  0%, 100% { transform: perspective(500px) translateZ(0) rotateX(0); }
  50% { transform: perspective(500px) translateZ(30px) rotateX(5deg); }
}'),

('3D倾斜', '3D特效', '3D倾斜效果',
'.d3-tilt {
  animation: d3-tilt-anim 4s ease-in-out infinite;
}
@keyframes d3-tilt-anim {
  0%, 100% { transform: perspective(500px) rotateX(0) rotateY(0); }
  25% { transform: perspective(500px) rotateX(15deg) rotateY(-15deg); }
  50% { transform: perspective(500px) rotateX(0) rotateY(0); }
  75% { transform: perspective(500px) rotateX(-15deg) rotateY(15deg); }
}'),

('3D弹跳', '3D特效', '3D弹跳效果',
'.d3-bounce {
  animation: d3-bounce-anim 1s ease-in-out infinite;
}
@keyframes d3-bounce-anim {
  0%, 100% { transform: perspective(500px) translateZ(0) scale(1); }
  50% { transform: perspective(500px) translateZ(50px) scale(1.1); }
}'),

('3D脉冲', '3D特效', '3D脉冲效果',
'.d3-pulse {
  animation: d3-pulse-anim 2s ease-in-out infinite;
}
@keyframes d3-pulse-anim {
  0%, 100% { transform: perspective(500px) scale3d(1, 1, 1); }
  50% { transform: perspective(500px) scale3d(1.1, 1.1, 1.1); }
}'),

('3D旋转立方', '3D特效', '立方体旋转',
'.d3-cube {
  animation: d3-cube-rotate 4s linear infinite;
}
@keyframes d3-cube-rotate {
  from { transform: perspective(500px) rotateX(0) rotateY(0); }
  to { transform: perspective(500px) rotateX(360deg) rotateY(360deg); }
}'),

('3D波浪', '3D特效', '3D波浪效果',
'.d3-wave {
  animation: d3-wave-anim 2s ease-in-out infinite;
}
@keyframes d3-wave-anim {
  0%, 100% { transform: perspective(500px) rotateX(0); }
  25% { transform: perspective(500px) rotateX(10deg); }
  75% { transform: perspective(500px) rotateX(-10deg); }
}');
