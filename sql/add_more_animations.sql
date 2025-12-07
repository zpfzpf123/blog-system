-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ==================== 更多悬停效果 (15个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('反色悬停', '悬停效果', '悬停时颜色反转',
'.hover-invert {
  transition: filter 0.3s ease;
}
.hover-invert:hover {
  filter: invert(1);
}'),

('灰度悬停', '悬停效果', '悬停时变为彩色',
'.hover-grayscale {
  filter: grayscale(100%);
  transition: filter 0.3s ease;
}
.hover-grayscale:hover {
  filter: grayscale(0%);
}'),

('对比度悬停', '悬停效果', '悬停时增加对比度',
'.hover-contrast {
  transition: filter 0.3s ease;
}
.hover-contrast:hover {
  filter: contrast(1.3);
}'),

('色相旋转', '悬停效果', '悬停时色相变化',
'.hover-hue {
  transition: filter 0.5s ease;
}
.hover-hue:hover {
  filter: hue-rotate(90deg);
}'),

('下沉悬停', '悬停效果', '悬停时元素下沉',
'.hover-sink {
  transition: transform 0.3s ease;
}
.hover-sink:hover {
  transform: translateY(5px);
}'),

('左移悬停', '悬停效果', '悬停时向左移动',
'.hover-left {
  transition: transform 0.3s ease;
}
.hover-left:hover {
  transform: translateX(-10px);
}'),

('右移悬停', '悬停效果', '悬停时向右移动',
'.hover-right {
  transition: transform 0.3s ease;
}
.hover-right:hover {
  transform: translateX(10px);
}'),

('放大旋转', '悬停效果', '悬停时放大并旋转',
'.hover-grow-rotate {
  transition: transform 0.4s ease;
}
.hover-grow-rotate:hover {
  transform: scale(1.1) rotate(5deg);
}'),

('透视悬停', '悬停效果', '悬停时产生透视效果',
'.hover-perspective {
  transition: transform 0.4s ease;
}
.hover-perspective:hover {
  transform: perspective(300px) rotateX(10deg);
}'),

('弹簧悬停', '悬停效果', '悬停时弹簧效果',
'.hover-spring {
  transition: transform 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.hover-spring:hover {
  transform: scale(1.15);
}'),

('闪光悬停', '悬停效果', '悬停时闪光效果',
'.hover-flash {
  position: relative;
  overflow: hidden;
}
.hover-flash::after {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent 40%, rgba(255,255,255,0.3) 50%, transparent 60%);
  transform: translateX(-100%);
  transition: transform 0.6s;
}
.hover-flash:hover::after {
  transform: translateX(100%);
}'),

('边框悬停', '悬停效果', '悬停时显示边框',
'.hover-border-show {
  border: 3px solid transparent;
  transition: border-color 0.3s ease;
}
.hover-border-show:hover {
  border-color: #8b5cf6;
}'),

('圆角悬停', '悬停效果', '悬停时圆角变化',
'.hover-radius {
  border-radius: 8px;
  transition: border-radius 0.3s ease;
}
.hover-radius:hover {
  border-radius: 50%;
}'),

('下划线悬停', '悬停效果', '悬停时出现下划线',
'.hover-underline {
  position: relative;
}
.hover-underline::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 3px;
  background: #8b5cf6;
  transition: width 0.3s ease;
}
.hover-underline:hover::after {
  width: 100%;
}'),

('背景滑入', '悬停效果', '悬停时背景滑入',
'.hover-bg-slide {
  position: relative;
  overflow: hidden;
  z-index: 1;
}
.hover-bg-slide::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: rgba(139, 92, 246, 0.2);
  transition: left 0.3s ease;
  z-index: -1;
}
.hover-bg-slide:hover::before {
  left: 0;
}');

-- ==================== 更多加载动画 (15个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('三角旋转', '加载动画', '三角形旋转加载',
'.loader-triangle {
  width: 0;
  height: 0;
  border-left: 20px solid transparent;
  border-right: 20px solid transparent;
  border-bottom: 35px solid #8b5cf6;
  animation: triangle-spin 1.5s linear infinite;
}
@keyframes triangle-spin {
  to { transform: rotate(360deg); }
}'),

('菱形旋转', '加载动画', '菱形旋转加载',
'.loader-diamond {
  width: 30px;
  height: 30px;
  background: #8b5cf6;
  transform: rotate(45deg);
  animation: diamond-pulse 1s ease-in-out infinite;
}
@keyframes diamond-pulse {
  0%, 100% { transform: rotate(45deg) scale(1); }
  50% { transform: rotate(45deg) scale(0.7); }
}'),

('环形追逐', '加载动画', '环形追逐效果',
'.loader-orbit {
  width: 50px;
  height: 50px;
  position: relative;
}
.loader-orbit::before {
  content: "";
  position: absolute;
  width: 10px;
  height: 10px;
  background: #8b5cf6;
  border-radius: 50%;
  animation: orbit 1s linear infinite;
}
@keyframes orbit {
  to { transform: rotate(360deg) translateX(20px); }
}'),

('DNA螺旋', '加载动画', 'DNA双螺旋效果',
'.loader-dna {
  display: flex;
  gap: 5px;
}
.loader-dna span {
  width: 8px;
  height: 8px;
  background: #8b5cf6;
  border-radius: 50%;
  animation: dna-wave 1s ease-in-out infinite;
}
.loader-dna span:nth-child(2) { animation-delay: 0.2s; }
.loader-dna span:nth-child(3) { animation-delay: 0.4s; }
@keyframes dna-wave {
  0%, 100% { transform: translateY(-10px); background: #8b5cf6; }
  50% { transform: translateY(10px); background: #f72585; }
}'),

('心电图', '加载动画', '心电图波形效果',
'.loader-heartbeat {
  width: 60px;
  height: 30px;
  background: linear-gradient(90deg, #8b5cf6 50%, transparent 50%);
  background-size: 10px 100%;
  animation: heartbeat-move 0.5s linear infinite;
}
@keyframes heartbeat-move {
  to { background-position: 10px 0; }
}'),

('沙漏', '加载动画', '沙漏翻转效果',
'.loader-hourglass {
  width: 30px;
  height: 30px;
  border: 3px solid #8b5cf6;
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  animation: hourglass-flip 2s ease-in-out infinite;
}
@keyframes hourglass-flip {
  0% { transform: rotate(0); }
  50% { transform: rotate(180deg); }
  100% { transform: rotate(360deg); }
}'),

('齿轮转动', '加载动画', '齿轮转动效果',
'.loader-gear {
  width: 40px;
  height: 40px;
  border: 5px solid #8b5cf6;
  border-radius: 50%;
  position: relative;
  animation: gear-rotate 2s linear infinite;
}
.loader-gear::before {
  content: "";
  position: absolute;
  inset: 5px;
  border: 3px dashed #8b5cf6;
  border-radius: 50%;
}
@keyframes gear-rotate {
  to { transform: rotate(360deg); }
}'),

('水滴', '加载动画', '水滴滴落效果',
'.loader-drop {
  width: 20px;
  height: 20px;
  background: #8b5cf6;
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  animation: drop-fall 1s ease-in-out infinite;
}
@keyframes drop-fall {
  0% { transform: translateY(-20px) scale(1); opacity: 1; }
  100% { transform: translateY(20px) scale(0.5); opacity: 0; }
}'),

('音波', '加载动画', '音波震动效果',
'.loader-sound {
  display: flex;
  align-items: center;
  gap: 3px;
  height: 30px;
}
.loader-sound span {
  width: 4px;
  background: #8b5cf6;
  border-radius: 2px;
  animation: sound-wave 0.8s ease-in-out infinite;
}
.loader-sound span:nth-child(1) { animation-delay: 0s; }
.loader-sound span:nth-child(2) { animation-delay: 0.1s; }
.loader-sound span:nth-child(3) { animation-delay: 0.2s; }
.loader-sound span:nth-child(4) { animation-delay: 0.3s; }
.loader-sound span:nth-child(5) { animation-delay: 0.4s; }
@keyframes sound-wave {
  0%, 100% { height: 10px; }
  50% { height: 30px; }
}'),

('原子轨道', '加载动画', '原子轨道效果',
'.loader-atom {
  width: 50px;
  height: 50px;
  position: relative;
}
.loader-atom::before, .loader-atom::after {
  content: "";
  position: absolute;
  inset: 0;
  border: 2px solid transparent;
  border-top-color: #8b5cf6;
  border-radius: 50%;
  animation: atom-orbit 1.5s linear infinite;
}
.loader-atom::after {
  transform: rotate(60deg);
  animation-delay: 0.5s;
}
@keyframes atom-orbit {
  to { transform: rotate(360deg); }
}'),

('方块堆叠', '加载动画', '方块堆叠效果',
'.loader-stack {
  width: 30px;
  height: 30px;
  position: relative;
}
.loader-stack::before, .loader-stack::after {
  content: "";
  position: absolute;
  width: 100%;
  height: 100%;
  background: #8b5cf6;
  animation: stack-move 1s ease-in-out infinite;
}
.loader-stack::after {
  animation-delay: 0.5s;
  opacity: 0.5;
}
@keyframes stack-move {
  0%, 100% { transform: translate(0, 0); }
  25% { transform: translate(10px, 10px); }
  50% { transform: translate(0, 0); }
  75% { transform: translate(-10px, -10px); }
}'),

('涟漪扩散', '加载动画', '涟漪扩散效果',
'.loader-ripple {
  width: 40px;
  height: 40px;
  position: relative;
}
.loader-ripple::before, .loader-ripple::after {
  content: "";
  position: absolute;
  inset: 0;
  border: 3px solid #8b5cf6;
  border-radius: 50%;
  animation: ripple-expand 1.5s ease-out infinite;
}
.loader-ripple::after {
  animation-delay: 0.75s;
}
@keyframes ripple-expand {
  0% { transform: scale(0); opacity: 1; }
  100% { transform: scale(2); opacity: 0; }
}'),

('时钟指针', '加载动画', '时钟指针转动',
'.loader-clock {
  width: 40px;
  height: 40px;
  border: 3px solid #8b5cf6;
  border-radius: 50%;
  position: relative;
}
.loader-clock::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 2px;
  height: 15px;
  background: #8b5cf6;
  transform-origin: bottom center;
  animation: clock-tick 2s linear infinite;
}
@keyframes clock-tick {
  to { transform: rotate(360deg); }
}'),

('无限符号', '加载动画', '无限符号动画',
'.loader-infinity {
  width: 60px;
  height: 30px;
  position: relative;
}
.loader-infinity::before {
  content: "";
  position: absolute;
  width: 20px;
  height: 20px;
  border: 3px solid #8b5cf6;
  border-radius: 50%;
  animation: infinity-move 2s linear infinite;
}
@keyframes infinity-move {
  0% { left: 0; top: 0; }
  25% { left: 40px; top: 0; }
  50% { left: 40px; top: 10px; }
  75% { left: 0; top: 10px; }
  100% { left: 0; top: 0; }
}'),

('魔方旋转', '加载动画', '魔方旋转效果',
'.loader-cube {
  width: 30px;
  height: 30px;
  background: linear-gradient(135deg, #8b5cf6 25%, #f72585 25%, #f72585 50%, #06b6d4 50%, #06b6d4 75%, #8b5cf6 75%);
  animation: cube-rotate 1.5s linear infinite;
}
@keyframes cube-rotate {
  0% { transform: perspective(200px) rotateX(0) rotateY(0); }
  100% { transform: perspective(200px) rotateX(360deg) rotateY(360deg); }
}');

-- ==================== 更多按钮特效 (12个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('滑动填充', '按钮特效', '从左到右滑动填充',
'.btn-slide-fill {
  position: relative;
  overflow: hidden;
  z-index: 1;
}
.btn-slide-fill::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: rgba(255,255,255,0.2);
  transition: left 0.3s ease;
  z-index: -1;
}
.btn-slide-fill:hover::before {
  left: 0;
}'),

('底部上滑', '按钮特效', '从底部上滑填充',
'.btn-bottom-up {
  position: relative;
  overflow: hidden;
  z-index: 1;
}
.btn-bottom-up::before {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 0;
  background: rgba(255,255,255,0.2);
  transition: height 0.3s ease;
  z-index: -1;
}
.btn-bottom-up:hover::before {
  height: 100%;
}'),

('中心扩散', '按钮特效', '从中心向外扩散',
'.btn-center-out {
  position: relative;
  overflow: hidden;
  z-index: 1;
}
.btn-center-out::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: width 0.4s ease, height 0.4s ease;
  z-index: -1;
}
.btn-center-out:hover::before {
  width: 300%;
  height: 300%;
}'),

('双边框', '按钮特效', '双边框动画效果',
'.btn-double-border {
  border: 2px solid #8b5cf6;
  position: relative;
}
.btn-double-border::after {
  content: "";
  position: absolute;
  inset: 4px;
  border: 2px solid transparent;
  transition: border-color 0.3s ease;
}
.btn-double-border:hover::after {
  border-color: #f72585;
}'),

('旋转边框', '按钮特效', '边框旋转效果',
'.btn-rotate-border {
  position: relative;
  overflow: hidden;
}
.btn-rotate-border::before {
  content: "";
  position: absolute;
  inset: -50%;
  background: conic-gradient(#8b5cf6, #f72585, #06b6d4, #8b5cf6);
  animation: rotate-border 3s linear infinite;
  z-index: -2;
}
.btn-rotate-border::after {
  content: "";
  position: absolute;
  inset: 3px;
  background: #1a1a3e;
  border-radius: inherit;
  z-index: -1;
}
@keyframes rotate-border {
  to { transform: rotate(360deg); }
}'),

('文字滑动', '按钮特效', '文字滑动切换',
'.btn-text-slide {
  position: relative;
  overflow: hidden;
}
.btn-text-slide span {
  display: block;
  transition: transform 0.3s ease;
}
.btn-text-slide:hover span {
  transform: translateY(-100%);
}'),

('闪电效果', '按钮特效', '闪电闪烁效果',
'.btn-lightning {
  animation: lightning-flash 2s ease-in-out infinite;
}
@keyframes lightning-flash {
  0%, 90%, 100% { opacity: 1; }
  92%, 94%, 96% { opacity: 0.5; }
  93%, 95%, 97% { opacity: 1; }
}'),

('磁性吸附', '按钮特效', '磁性吸附效果',
'.btn-magnetic {
  transition: transform 0.2s ease-out;
}
.btn-magnetic:hover {
  transform: scale(1.05);
}
.btn-magnetic:active {
  transform: scale(0.98);
}'),

('液态按钮', '按钮特效', '液态流动效果',
'.btn-liquid {
  animation: liquid-morph 3s ease-in-out infinite;
}
@keyframes liquid-morph {
  0%, 100% { border-radius: 12px; }
  25% { border-radius: 12px 20px 12px 20px; }
  50% { border-radius: 20px 12px 20px 12px; }
  75% { border-radius: 12px 20px 12px 20px; }
}'),

('粒子爆发', '按钮特效', '点击粒子爆发',
'.btn-particle {
  position: relative;
}
.btn-particle::before {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, rgba(139,92,246,0.5) 0%, transparent 70%);
  opacity: 0;
  transform: scale(0);
}
.btn-particle:active::before {
  animation: particle-burst 0.5s ease-out;
}
@keyframes particle-burst {
  0% { opacity: 1; transform: scale(0); }
  100% { opacity: 0; transform: scale(2); }
}'),

('霓虹闪烁', '按钮特效', '霓虹灯闪烁',
'.btn-neon-blink {
  animation: neon-blink 1.5s ease-in-out infinite;
}
@keyframes neon-blink {
  0%, 100% { box-shadow: 0 0 5px #8b5cf6, 0 0 10px #8b5cf6; }
  50% { box-shadow: 0 0 20px #8b5cf6, 0 0 40px #8b5cf6, 0 0 60px #f72585; }
}'),

('玻璃按钮', '按钮特效', '玻璃质感效果',
'.btn-glass {
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255,255,255,0.2);
  transition: all 0.3s ease;
}
.btn-glass:hover {
  background: rgba(255,255,255,0.2);
  box-shadow: 0 8px 32px rgba(139, 92, 246, 0.3);
}');

-- ==================== 更多文字动画 (12个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('字母旋转', '文字动画', '字母依次旋转',
'.text-rotate-letters span {
  display: inline-block;
  animation: letter-rotate 1s ease infinite;
}
.text-rotate-letters span:nth-child(2) { animation-delay: 0.1s; }
.text-rotate-letters span:nth-child(3) { animation-delay: 0.2s; }
.text-rotate-letters span:nth-child(4) { animation-delay: 0.3s; }
.text-rotate-letters span:nth-child(5) { animation-delay: 0.4s; }
@keyframes letter-rotate {
  0%, 100% { transform: rotateY(0); }
  50% { transform: rotateY(180deg); }
}'),

('字母缩放', '文字动画', '字母依次缩放',
'.text-scale-letters span {
  display: inline-block;
  animation: letter-scale 0.8s ease infinite;
}
.text-scale-letters span:nth-child(2) { animation-delay: 0.1s; }
.text-scale-letters span:nth-child(3) { animation-delay: 0.2s; }
.text-scale-letters span:nth-child(4) { animation-delay: 0.3s; }
.text-scale-letters span:nth-child(5) { animation-delay: 0.4s; }
@keyframes letter-scale {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.3); }
}'),

('打散重组', '文字动画', '文字打散重组',
'.text-scatter span {
  display: inline-block;
  animation: scatter-reform 2s ease infinite;
}
.text-scatter span:nth-child(odd) { animation-delay: 0.1s; }
@keyframes scatter-reform {
  0%, 100% { transform: translate(0, 0) rotate(0); opacity: 1; }
  50% { transform: translate(var(--x, 10px), var(--y, -10px)) rotate(10deg); opacity: 0.5; }
}'),

('故障文字', '文字动画', '故障艺术效果',
'.text-glitch {
  position: relative;
  animation: glitch-skew 1s infinite linear alternate-reverse;
}
@keyframes glitch-skew {
  0% { transform: skew(0deg); }
  20% { transform: skew(-2deg); }
  40% { transform: skew(2deg); }
  60% { transform: skew(0deg); }
  80% { transform: skew(1deg); }
  100% { transform: skew(-1deg); }
}'),

('3D翻转文字', '文字动画', '3D翻转效果',
'.text-flip-3d {
  animation: text-flip3d 2s ease-in-out infinite;
}
@keyframes text-flip3d {
  0%, 100% { transform: perspective(400px) rotateX(0); }
  50% { transform: perspective(400px) rotateX(180deg); }
}'),

('橡皮擦', '文字动画', '橡皮擦擦除效果',
'.text-eraser {
  background: linear-gradient(90deg, transparent 50%, currentColor 50%);
  background-size: 200% 100%;
  -webkit-background-clip: text;
  background-clip: text;
  animation: eraser-wipe 2s linear infinite;
}
@keyframes eraser-wipe {
  0% { background-position: 100% 0; }
  100% { background-position: -100% 0; }
}'),

('弹性文字', '文字动画', '弹性伸缩效果',
'.text-elastic {
  animation: text-elastic-anim 1s ease-in-out infinite;
}
@keyframes text-elastic-anim {
  0%, 100% { transform: scaleX(1); }
  30% { transform: scaleX(1.2); }
  50% { transform: scaleX(0.9); }
  70% { transform: scaleX(1.05); }
}'),

('浮动文字', '文字动画', '文字上下浮动',
'.text-float {
  animation: text-float-anim 3s ease-in-out infinite;
}
@keyframes text-float-anim {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}'),

('脉冲文字', '文字动画', '文字脉冲效果',
'.text-pulse {
  animation: text-pulse-anim 1.5s ease-in-out infinite;
}
@keyframes text-pulse-anim {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.05); opacity: 0.8; }
}'),

('阴影文字', '文字动画', '阴影移动效果',
'.text-shadow-move {
  animation: shadow-text-move 2s ease-in-out infinite;
}
@keyframes shadow-text-move {
  0%, 100% { text-shadow: 2px 2px 0 #8b5cf6; }
  50% { text-shadow: -2px -2px 0 #f72585; }
}'),

('轮廓文字', '文字动画', '轮廓闪烁效果',
'.text-outline {
  -webkit-text-stroke: 2px #8b5cf6;
  color: transparent;
  animation: outline-blink 1s ease-in-out infinite;
}
@keyframes outline-blink {
  0%, 100% { -webkit-text-stroke-color: #8b5cf6; }
  50% { -webkit-text-stroke-color: #f72585; }
}'),

('镜像文字', '文字动画', '镜像翻转效果',
'.text-mirror {
  animation: mirror-flip 2s ease-in-out infinite;
}
@keyframes mirror-flip {
  0%, 100% { transform: scaleX(1); }
  50% { transform: scaleX(-1); }
}');

-- ==================== 更多图形变换 (12个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('六边形旋转', '图形变换', '六边形旋转效果',
'.shape-hexagon {
  width: 60px;
  height: 35px;
  background: #8b5cf6;
  position: relative;
  animation: hex-rotate 3s linear infinite;
}
.shape-hexagon::before, .shape-hexagon::after {
  content: "";
  position: absolute;
  width: 0;
  border-left: 30px solid transparent;
  border-right: 30px solid transparent;
}
.shape-hexagon::before {
  top: -17px;
  border-bottom: 17px solid #8b5cf6;
}
.shape-hexagon::after {
  bottom: -17px;
  border-top: 17px solid #8b5cf6;
}
@keyframes hex-rotate {
  to { transform: rotate(360deg); }
}'),

('星形脉冲', '图形变换', '星形脉冲效果',
'.shape-star {
  width: 0;
  height: 0;
  border-left: 25px solid transparent;
  border-right: 25px solid transparent;
  border-bottom: 50px solid #8b5cf6;
  position: relative;
  animation: star-pulse 1.5s ease-in-out infinite;
}
@keyframes star-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}'),

('波浪变形', '图形变换', '波浪形状变化',
'.shape-wave {
  width: 80px;
  height: 40px;
  background: #8b5cf6;
  animation: wave-morph 2s ease-in-out infinite;
}
@keyframes wave-morph {
  0%, 100% { border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%; }
  50% { border-radius: 50% 50% 50% 50% / 40% 40% 60% 60%; }
}'),

('十字旋转', '图形变换', '十字形旋转',
'.shape-cross {
  width: 60px;
  height: 60px;
  position: relative;
  animation: cross-spin 2s linear infinite;
}
.shape-cross::before, .shape-cross::after {
  content: "";
  position: absolute;
  background: #8b5cf6;
}
.shape-cross::before {
  width: 20px;
  height: 60px;
  left: 20px;
}
.shape-cross::after {
  width: 60px;
  height: 20px;
  top: 20px;
}
@keyframes cross-spin {
  to { transform: rotate(360deg); }
}'),

('环形缩放', '图形变换', '环形缩放效果',
'.shape-ring {
  width: 60px;
  height: 60px;
  border: 8px solid #8b5cf6;
  border-radius: 50%;
  animation: ring-scale 1.5s ease-in-out infinite;
}
@keyframes ring-scale {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.3); opacity: 0.5; }
}'),

('箭头移动', '图形变换', '箭头移动效果',
'.shape-arrow {
  width: 0;
  height: 0;
  border-top: 20px solid transparent;
  border-bottom: 20px solid transparent;
  border-left: 30px solid #8b5cf6;
  animation: arrow-move 1s ease-in-out infinite;
}
@keyframes arrow-move {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(20px); }
}'),

('月牙旋转', '图形变换', '月牙形旋转',
'.shape-moon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  box-shadow: 15px 0 0 0 #8b5cf6;
  animation: moon-orbit 3s linear infinite;
}
@keyframes moon-orbit {
  to { transform: rotate(360deg); }
}'),

('闪电形状', '图形变换', '闪电闪烁效果',
'.shape-lightning {
  width: 30px;
  height: 60px;
  background: linear-gradient(135deg, #8b5cf6 50%, transparent 50%);
  animation: lightning-blink 0.5s ease-in-out infinite;
}
@keyframes lightning-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}'),

('水滴形状', '图形变换', '水滴弹跳效果',
'.shape-droplet {
  width: 40px;
  height: 40px;
  background: #8b5cf6;
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  animation: droplet-bounce 1s ease-in-out infinite;
}
@keyframes droplet-bounce {
  0%, 100% { transform: translateY(0) scale(1, 1); }
  50% { transform: translateY(-20px) scale(0.9, 1.1); }
}'),

('叶子飘动', '图形变换', '叶子飘动效果',
'.shape-leaf {
  width: 40px;
  height: 40px;
  background: #8b5cf6;
  border-radius: 0 50% 50% 50%;
  animation: leaf-sway 2s ease-in-out infinite;
}
@keyframes leaf-sway {
  0%, 100% { transform: rotate(0); }
  25% { transform: rotate(10deg); }
  75% { transform: rotate(-10deg); }
}'),

('气泡上升', '图形变换', '气泡上升效果',
'.shape-bubble {
  width: 30px;
  height: 30px;
  background: rgba(139, 92, 246, 0.5);
  border-radius: 50%;
  animation: bubble-rise 2s ease-in-out infinite;
}
@keyframes bubble-rise {
  0% { transform: translateY(0) scale(1); opacity: 1; }
  100% { transform: translateY(-50px) scale(0.5); opacity: 0; }
}'),

('DNA双螺旋', '图形变换', 'DNA螺旋旋转',
'.shape-dna {
  width: 40px;
  height: 40px;
  border: 4px solid transparent;
  border-top-color: #8b5cf6;
  border-bottom-color: #f72585;
  border-radius: 50%;
  animation: dna-twist 1.5s linear infinite;
}
@keyframes dna-twist {
  to { transform: rotateY(360deg); }
}');

-- ==================== 更多入场动画 (10个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('螺旋入场', '入场动画', '螺旋旋转进入',
'.enter-spiral {
  animation: enter-spiral-anim 0.8s ease forwards;
}
@keyframes enter-spiral-anim {
  from { opacity: 0; transform: rotate(720deg) scale(0); }
  to { opacity: 1; transform: rotate(0) scale(1); }
}'),

('弹簧入场', '入场动画', '弹簧弹入效果',
'.enter-spring {
  animation: enter-spring-anim 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}
@keyframes enter-spring-anim {
  from { opacity: 0; transform: scale(0.5) translateY(50px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}'),

('展开入场', '入场动画', '从中心展开',
'.enter-unfold {
  animation: enter-unfold-anim 0.5s ease forwards;
}
@keyframes enter-unfold-anim {
  from { opacity: 0; transform: scaleY(0); }
  to { opacity: 1; transform: scaleY(1); }
}'),

('滚动入场', '入场动画', '滚动进入',
'.enter-roll {
  animation: enter-roll-anim 0.6s ease forwards;
}
@keyframes enter-roll-anim {
  from { opacity: 0; transform: translateX(-100%) rotate(-360deg); }
  to { opacity: 1; transform: translateX(0) rotate(0); }
}'),

('闪现入场', '入场动画', '闪现出现',
'.enter-flash {
  animation: enter-flash-anim 0.5s ease forwards;
}
@keyframes enter-flash-anim {
  0% { opacity: 0; }
  50% { opacity: 1; }
  75% { opacity: 0.5; }
  100% { opacity: 1; }
}'),

('抖动入场', '入场动画', '抖动进入',
'.enter-shake {
  animation: enter-shake-anim 0.6s ease forwards;
}
@keyframes enter-shake-anim {
  0% { opacity: 0; transform: translateX(-20px); }
  20% { transform: translateX(15px); }
  40% { transform: translateX(-10px); }
  60% { transform: translateX(5px); }
  80% { transform: translateX(-2px); }
  100% { opacity: 1; transform: translateX(0); }
}'),

('果冻入场', '入场动画', '果冻弹性进入',
'.enter-jelly {
  animation: enter-jelly-anim 0.8s ease forwards;
}
@keyframes enter-jelly-anim {
  0% { opacity: 0; transform: scale(0); }
  50% { transform: scale(1.2, 0.8); }
  70% { transform: scale(0.9, 1.1); }
  100% { opacity: 1; transform: scale(1); }
}'),

('滑落入场', '入场动画', '从上方滑落',
'.enter-drop {
  animation: enter-drop-anim 0.5s ease forwards;
}
@keyframes enter-drop-anim {
  from { opacity: 0; transform: translateY(-100px); }
  to { opacity: 1; transform: translateY(0); }
}'),

('扇形入场', '入场动画', '扇形展开进入',
'.enter-fan {
  animation: enter-fan-anim 0.6s ease forwards;
  transform-origin: bottom center;
}
@keyframes enter-fan-anim {
  from { opacity: 0; transform: rotateX(-90deg); }
  to { opacity: 1; transform: rotateX(0); }
}'),

('波纹入场', '入场动画', '波纹扩散进入',
'.enter-ripple {
  animation: enter-ripple-anim 0.6s ease forwards;
}
@keyframes enter-ripple-anim {
  from { opacity: 0; transform: scale(0); box-shadow: 0 0 0 0 rgba(139, 92, 246, 0.5); }
  to { opacity: 1; transform: scale(1); box-shadow: 0 0 0 20px rgba(139, 92, 246, 0); }
}');

-- ==================== 更多退出动画 (8个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('螺旋退出', '退出动画', '螺旋旋转退出',
'.exit-spiral {
  animation: exit-spiral-anim 0.6s ease forwards;
}
@keyframes exit-spiral-anim {
  from { opacity: 1; transform: rotate(0) scale(1); }
  to { opacity: 0; transform: rotate(720deg) scale(0); }
}'),

('弹出退出', '退出动画', '弹出消失',
'.exit-pop {
  animation: exit-pop-anim 0.4s ease forwards;
}
@keyframes exit-pop-anim {
  0% { opacity: 1; transform: scale(1); }
  50% { transform: scale(1.2); }
  100% { opacity: 0; transform: scale(0); }
}'),

('滑出左边', '退出动画', '向左滑出',
'.exit-slide-left {
  animation: exit-slideLeft 0.5s ease forwards;
}
@keyframes exit-slideLeft {
  from { opacity: 1; transform: translateX(0); }
  to { opacity: 0; transform: translateX(-100px); }
}'),

('滑出右边', '退出动画', '向右滑出',
'.exit-slide-right {
  animation: exit-slideRight 0.5s ease forwards;
}
@keyframes exit-slideRight {
  from { opacity: 1; transform: translateX(0); }
  to { opacity: 0; transform: translateX(100px); }
}'),

('折叠退出', '退出动画', '折叠消失',
'.exit-fold {
  animation: exit-fold-anim 0.5s ease forwards;
  transform-origin: top center;
}
@keyframes exit-fold-anim {
  from { opacity: 1; transform: scaleY(1); }
  to { opacity: 0; transform: scaleY(0); }
}'),

('抖动退出', '退出动画', '抖动消失',
'.exit-shake {
  animation: exit-shake-anim 0.5s ease forwards;
}
@keyframes exit-shake-anim {
  0% { opacity: 1; transform: translateX(0); }
  20% { transform: translateX(-10px); }
  40% { transform: translateX(10px); }
  60% { transform: translateX(-5px); }
  80% { transform: translateX(5px); opacity: 0.5; }
  100% { opacity: 0; transform: translateX(0); }
}'),

('下坠退出', '退出动画', '下坠消失',
'.exit-fall {
  animation: exit-fall-anim 0.5s ease forwards;
}
@keyframes exit-fall-anim {
  from { opacity: 1; transform: translateY(0) rotate(0); }
  to { opacity: 0; transform: translateY(100px) rotate(30deg); }
}'),

('闪烁退出', '退出动画', '闪烁消失',
'.exit-blink {
  animation: exit-blink-anim 0.5s ease forwards;
}
@keyframes exit-blink-anim {
  0% { opacity: 1; }
  25% { opacity: 0; }
  50% { opacity: 1; }
  75% { opacity: 0; }
  100% { opacity: 0; }
}');

-- ==================== 更多强调效果 (10个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('放大缩小', '强调效果', '放大缩小循环',
'.emphasis-zoom {
  animation: emph-zoom 0.8s ease-in-out infinite;
}
@keyframes emph-zoom {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}'),

('左右晃动', '强调效果', '左右晃动效果',
'.emphasis-sway {
  animation: emph-sway 1s ease-in-out infinite;
}
@keyframes emph-sway {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10px); }
  75% { transform: translateX(10px); }
}'),

('上下跳动', '强调效果', '上下跳动效果',
'.emphasis-hop {
  animation: emph-hop 0.5s ease-in-out infinite;
}
@keyframes emph-hop {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}'),

('旋转晃动', '强调效果', '旋转晃动效果',
'.emphasis-wiggle {
  animation: emph-wiggle 0.5s ease-in-out infinite;
}
@keyframes emph-wiggle {
  0%, 100% { transform: rotate(0); }
  25% { transform: rotate(-5deg); }
  75% { transform: rotate(5deg); }
}'),

('颜色闪烁', '强调效果', '颜色闪烁变化',
'.emphasis-color-flash {
  animation: emph-color-flash 1s ease-in-out infinite;
}
@keyframes emph-color-flash {
  0%, 100% { background-color: #8b5cf6; }
  50% { background-color: #f72585; }
}'),

('边框闪烁', '强调效果', '边框闪烁效果',
'.emphasis-border-flash {
  animation: emph-border-flash 1s ease-in-out infinite;
}
@keyframes emph-border-flash {
  0%, 100% { border-color: #8b5cf6; }
  50% { border-color: #f72585; }
}'),

('阴影脉冲', '强调效果', '阴影脉冲效果',
'.emphasis-shadow-pulse {
  animation: emph-shadow-pulse 1.5s ease-in-out infinite;
}
@keyframes emph-shadow-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(139, 92, 246, 0.5); }
  50% { box-shadow: 0 0 0 15px rgba(139, 92, 246, 0); }
}'),

('透明闪烁', '强调效果', '透明度闪烁',
'.emphasis-opacity-flash {
  animation: emph-opacity-flash 1s ease-in-out infinite;
}
@keyframes emph-opacity-flash {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}'),

('倾斜晃动', '强调效果', '倾斜晃动效果',
'.emphasis-tilt {
  animation: emph-tilt 0.5s ease-in-out infinite;
}
@keyframes emph-tilt {
  0%, 100% { transform: skewX(0); }
  25% { transform: skewX(-5deg); }
  75% { transform: skewX(5deg); }
}'),

('弹性挤压', '强调效果', '弹性挤压效果',
'.emphasis-squish {
  animation: emph-squish 0.6s ease-in-out infinite;
}
@keyframes emph-squish {
  0%, 100% { transform: scale(1, 1); }
  25% { transform: scale(1.1, 0.9); }
  50% { transform: scale(0.9, 1.1); }
  75% { transform: scale(1.05, 0.95); }
}');

-- ==================== 更多背景特效 (10个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('棋盘格', '背景特效', '棋盘格移动效果',
'.bg-checkerboard {
  background: repeating-conic-gradient(#8b5cf6 0% 25%, transparent 0% 50%) 0 0 / 20px 20px;
  animation: checker-move 2s linear infinite;
}
@keyframes checker-move {
  to { background-position: 20px 20px; }
}'),

('点阵闪烁', '背景特效', '点阵闪烁效果',
'.bg-dots {
  background: radial-gradient(circle, #8b5cf6 2px, transparent 2px);
  background-size: 20px 20px;
  animation: dots-blink 2s ease-in-out infinite;
}
@keyframes dots-blink {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}'),

('波浪背景', '背景特效', '波浪流动效果',
'.bg-waves {
  background: linear-gradient(90deg, #8b5cf6 25%, #f72585 25%, #f72585 50%, #06b6d4 50%, #06b6d4 75%, #8b5cf6 75%);
  background-size: 400% 100%;
  animation: waves-flow 3s linear infinite;
}
@keyframes waves-flow {
  from { background-position: 0% 0%; }
  to { background-position: 400% 0%; }
}'),

('雷达扫描', '背景特效', '雷达扫描效果',
'.bg-radar {
  background: conic-gradient(from 0deg, transparent 0deg, rgba(139, 92, 246, 0.5) 30deg, transparent 60deg);
  animation: radar-scan 2s linear infinite;
}
@keyframes radar-scan {
  to { transform: rotate(360deg); }
}'),

('心跳背景', '背景特效', '心跳脉冲背景',
'.bg-heartbeat {
  background: radial-gradient(circle, #8b5cf6 0%, transparent 70%);
  animation: bg-heartbeat-pulse 1.2s ease-in-out infinite;
}
@keyframes bg-heartbeat-pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  14%, 42% { transform: scale(1.1); opacity: 0.8; }
  28%, 70% { transform: scale(1); opacity: 0.5; }
}'),

('闪电背景', '背景特效', '闪电闪烁背景',
'.bg-lightning {
  background: linear-gradient(135deg, #8b5cf6 0%, #1a1a3e 50%, #f72585 100%);
  animation: lightning-bg 0.5s ease-in-out infinite;
}
@keyframes lightning-bg {
  0%, 90%, 100% { opacity: 1; }
  92%, 94%, 96% { opacity: 0.5; }
}'),

('烟雾效果', '背景特效', '烟雾飘动效果',
'.bg-smoke {
  background: radial-gradient(ellipse at center, rgba(139, 92, 246, 0.3) 0%, transparent 70%);
  animation: smoke-drift 4s ease-in-out infinite;
}
@keyframes smoke-drift {
  0%, 100% { transform: translateY(0) scale(1); opacity: 0.5; }
  50% { transform: translateY(-20px) scale(1.2); opacity: 0.8; }
}'),

('马赛克', '背景特效', '马赛克闪烁效果',
'.bg-mosaic {
  background: 
    linear-gradient(45deg, #8b5cf6 25%, transparent 25%),
    linear-gradient(-45deg, #8b5cf6 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, #f72585 75%),
    linear-gradient(-45deg, transparent 75%, #f72585 75%);
  background-size: 20px 20px;
  animation: mosaic-shift 1s linear infinite;
}
@keyframes mosaic-shift {
  to { background-position: 20px 0; }
}'),

('光束扫描', '背景特效', '光束扫描效果',
'.bg-beam {
  background: linear-gradient(90deg, transparent 0%, rgba(139, 92, 246, 0.5) 50%, transparent 100%);
  background-size: 200% 100%;
  animation: beam-scan 2s linear infinite;
}
@keyframes beam-scan {
  from { background-position: -100% 0; }
  to { background-position: 200% 0; }
}'),

('粒子漂浮', '背景特效', '粒子漂浮效果',
'.bg-particles {
  background: 
    radial-gradient(circle at 20% 30%, #8b5cf6 2px, transparent 2px),
    radial-gradient(circle at 80% 70%, #f72585 2px, transparent 2px),
    radial-gradient(circle at 50% 50%, #06b6d4 2px, transparent 2px);
  background-size: 100px 100px;
  animation: particles-float 5s ease-in-out infinite;
}
@keyframes particles-float {
  0%, 100% { background-position: 0 0; }
  50% { background-position: 20px -20px; }
}');

-- ==================== 更多边框动画 (8个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('边框追逐', '边框动画', '边框追逐效果',
'.border-chase {
  position: relative;
}
.border-chase::before {
  content: "";
  position: absolute;
  inset: 0;
  border: 3px solid transparent;
  border-top-color: #8b5cf6;
  animation: chase-border 1s linear infinite;
}
@keyframes chase-border {
  to { transform: rotate(360deg); }
}'),

('边框扩散', '边框动画', '边框向外扩散',
'.border-expand {
  position: relative;
}
.border-expand::after {
  content: "";
  position: absolute;
  inset: -5px;
  border: 2px solid #8b5cf6;
  border-radius: inherit;
  animation: expand-border 1.5s ease-out infinite;
}
@keyframes expand-border {
  0% { inset: 0; opacity: 1; }
  100% { inset: -20px; opacity: 0; }
}'),

('边框波动', '边框动画', '边框波动效果',
'.border-wave {
  border: 3px solid #8b5cf6;
  animation: wave-border 2s ease-in-out infinite;
}
@keyframes wave-border {
  0%, 100% { border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%; }
  50% { border-radius: 70% 30% 30% 70% / 70% 70% 30% 30%; }
}'),

('双色边框', '边框动画', '双色边框交替',
'.border-dual-color {
  border: 3px solid #8b5cf6;
  animation: dual-color-border 1s ease-in-out infinite;
}
@keyframes dual-color-border {
  0%, 100% { border-color: #8b5cf6; }
  50% { border-color: #f72585; }
}'),

('边框旋转渐变', '边框动画', '渐变边框旋转',
'.border-gradient-spin {
  position: relative;
  overflow: hidden;
}
.border-gradient-spin::before {
  content: "";
  position: absolute;
  inset: -50%;
  background: conic-gradient(#8b5cf6, #f72585, #06b6d4, #8b5cf6);
  animation: gradient-spin-border 3s linear infinite;
}
.border-gradient-spin::after {
  content: "";
  position: absolute;
  inset: 4px;
  background: #1a1a3e;
  border-radius: inherit;
}
@keyframes gradient-spin-border {
  to { transform: rotate(360deg); }
}'),

('边框闪电', '边框动画', '边框闪电效果',
'.border-lightning {
  border: 2px solid #8b5cf6;
  animation: lightning-border 0.5s ease-in-out infinite;
}
@keyframes lightning-border {
  0%, 90%, 100% { border-color: #8b5cf6; box-shadow: 0 0 5px #8b5cf6; }
  92%, 96% { border-color: #fff; box-shadow: 0 0 20px #fff; }
}'),

('边框呼吸灯', '边框动画', '边框呼吸灯效果',
'.border-breath-light {
  border: 2px solid #8b5cf6;
  animation: breath-light-border 2s ease-in-out infinite;
}
@keyframes breath-light-border {
  0%, 100% { box-shadow: 0 0 5px #8b5cf6; }
  50% { box-shadow: 0 0 20px #8b5cf6, 0 0 40px rgba(139, 92, 246, 0.5); }
}'),

('边框虚实', '边框动画', '边框虚实变化',
'.border-dash-solid {
  border: 3px dashed #8b5cf6;
  animation: dash-solid-border 2s ease-in-out infinite;
}
@keyframes dash-solid-border {
  0%, 100% { border-style: dashed; }
  50% { border-style: solid; }
}');

-- ==================== 更多阴影效果 (8个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('阴影旋转', '阴影效果', '阴影旋转效果',
'.shadow-rotate {
  animation: rotate-shadow 3s linear infinite;
}
@keyframes rotate-shadow {
  0% { box-shadow: 10px 0 20px rgba(139, 92, 246, 0.5); }
  25% { box-shadow: 0 10px 20px rgba(139, 92, 246, 0.5); }
  50% { box-shadow: -10px 0 20px rgba(139, 92, 246, 0.5); }
  75% { box-shadow: 0 -10px 20px rgba(139, 92, 246, 0.5); }
  100% { box-shadow: 10px 0 20px rgba(139, 92, 246, 0.5); }
}'),

('阴影弹跳', '阴影效果', '阴影弹跳效果',
'.shadow-bounce {
  animation: bounce-shadow 1s ease-in-out infinite;
}
@keyframes bounce-shadow {
  0%, 100% { box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3); transform: translateY(0); }
  50% { box-shadow: 0 20px 30px rgba(0, 0, 0, 0.2); transform: translateY(-10px); }
}'),

('阴影扩散', '阴影效果', '阴影扩散效果',
'.shadow-spread {
  animation: spread-shadow 1.5s ease-in-out infinite;
}
@keyframes spread-shadow {
  0%, 100% { box-shadow: 0 0 10px rgba(139, 92, 246, 0.5); }
  50% { box-shadow: 0 0 30px rgba(139, 92, 246, 0.8), 0 0 60px rgba(139, 92, 246, 0.4); }
}'),

('阴影闪烁', '阴影效果', '阴影闪烁效果',
'.shadow-blink {
  animation: blink-shadow 1s ease-in-out infinite;
}
@keyframes blink-shadow {
  0%, 100% { box-shadow: 0 10px 30px rgba(139, 92, 246, 0.5); }
  50% { box-shadow: 0 10px 30px rgba(139, 92, 246, 0); }
}'),

('双层阴影', '阴影效果', '双层阴影动画',
'.shadow-double {
  animation: double-shadow 2s ease-in-out infinite;
}
@keyframes double-shadow {
  0%, 100% { box-shadow: 5px 5px 0 #8b5cf6, 10px 10px 0 #f72585; }
  50% { box-shadow: -5px -5px 0 #8b5cf6, -10px -10px 0 #f72585; }
}'),

('阴影波纹', '阴影效果', '阴影波纹效果',
'.shadow-ripple {
  animation: ripple-shadow 1.5s ease-out infinite;
}
@keyframes ripple-shadow {
  0% { box-shadow: 0 0 0 0 rgba(139, 92, 246, 0.5), 0 0 0 0 rgba(139, 92, 246, 0.3); }
  100% { box-shadow: 0 0 0 20px rgba(139, 92, 246, 0), 0 0 0 40px rgba(139, 92, 246, 0); }
}'),

('立体阴影', '阴影效果', '立体阴影效果',
'.shadow-3d {
  animation: shadow-3d-anim 2s ease-in-out infinite;
}
@keyframes shadow-3d-anim {
  0%, 100% { 
    box-shadow: 
      1px 1px 0 #7c3aed,
      2px 2px 0 #7c3aed,
      3px 3px 0 #7c3aed,
      4px 4px 0 #7c3aed,
      5px 5px 10px rgba(0,0,0,0.3);
  }
  50% { 
    box-shadow: 
      -1px -1px 0 #7c3aed,
      -2px -2px 0 #7c3aed,
      -3px -3px 0 #7c3aed,
      -4px -4px 0 #7c3aed,
      -5px -5px 10px rgba(0,0,0,0.3);
  }
}'),

('阴影呼吸灯', '阴影效果', '阴影呼吸灯效果',
'.shadow-breath-glow {
  animation: breath-glow-shadow 2s ease-in-out infinite;
}
@keyframes breath-glow-shadow {
  0%, 100% { box-shadow: 0 0 10px rgba(139, 92, 246, 0.3), 0 0 20px rgba(139, 92, 246, 0.2); }
  50% { box-shadow: 0 0 30px rgba(139, 92, 246, 0.6), 0 0 60px rgba(139, 92, 246, 0.4); }
}');

-- ==================== 更多3D特效 (10个) ====================
INSERT INTO css_animations (title, category, description, css_code) VALUES
('3D摇晃', '3D特效', '3D摇晃效果',
'.d3-wobble {
  animation: d3-wobble-anim 1s ease-in-out infinite;
}
@keyframes d3-wobble-anim {
  0%, 100% { transform: perspective(500px) rotateX(0) rotateY(0); }
  25% { transform: perspective(500px) rotateX(10deg) rotateY(-10deg); }
  50% { transform: perspective(500px) rotateX(-5deg) rotateY(5deg); }
  75% { transform: perspective(500px) rotateX(5deg) rotateY(-5deg); }
}'),

('3D翻页', '3D特效', '3D翻页效果',
'.d3-page-flip {
  animation: d3-page-flip-anim 2s ease-in-out infinite;
  transform-origin: left center;
}
@keyframes d3-page-flip-anim {
  0%, 100% { transform: perspective(500px) rotateY(0); }
  50% { transform: perspective(500px) rotateY(-180deg); }
}'),

('3D弹簧', '3D特效', '3D弹簧效果',
'.d3-spring {
  animation: d3-spring-anim 1s cubic-bezier(0.175, 0.885, 0.32, 1.275) infinite;
}
@keyframes d3-spring-anim {
  0%, 100% { transform: perspective(500px) translateZ(0) scale(1); }
  50% { transform: perspective(500px) translateZ(50px) scale(1.1); }
}'),

('3D门开', '3D特效', '3D开门效果',
'.d3-door {
  animation: d3-door-anim 2s ease-in-out infinite;
  transform-origin: left center;
}
@keyframes d3-door-anim {
  0%, 100% { transform: perspective(500px) rotateY(0); }
  50% { transform: perspective(500px) rotateY(-90deg); }
}'),

('3D卡片堆叠', '3D特效', '3D卡片堆叠效果',
'.d3-stack {
  animation: d3-stack-anim 2s ease-in-out infinite;
}
@keyframes d3-stack-anim {
  0%, 100% { transform: perspective(500px) translateZ(0) rotateX(0); }
  25% { transform: perspective(500px) translateZ(20px) rotateX(5deg); }
  50% { transform: perspective(500px) translateZ(40px) rotateX(0); }
  75% { transform: perspective(500px) translateZ(20px) rotateX(-5deg); }
}'),

('3D旋转门', '3D特效', '3D旋转门效果',
'.d3-revolve {
  animation: d3-revolve-anim 3s linear infinite;
}
@keyframes d3-revolve-anim {
  from { transform: perspective(500px) rotateY(0); }
  to { transform: perspective(500px) rotateY(360deg); }
}'),

('3D悬浮', '3D特效', '3D悬浮效果',
'.d3-levitate {
  animation: d3-levitate-anim 3s ease-in-out infinite;
}
@keyframes d3-levitate-anim {
  0%, 100% { transform: perspective(500px) translateY(0) translateZ(0) rotateX(5deg); }
  50% { transform: perspective(500px) translateY(-20px) translateZ(30px) rotateX(-5deg); }
}'),

('3D折叠', '3D特效', '3D折叠效果',
'.d3-fold {
  animation: d3-fold-anim 2s ease-in-out infinite;
  transform-origin: top center;
}
@keyframes d3-fold-anim {
  0%, 100% { transform: perspective(500px) rotateX(0); }
  50% { transform: perspective(500px) rotateX(45deg); }
}'),

('3D螺旋', '3D特效', '3D螺旋效果',
'.d3-helix {
  animation: d3-helix-anim 3s linear infinite;
}
@keyframes d3-helix-anim {
  from { transform: perspective(500px) rotateY(0) rotateZ(0); }
  to { transform: perspective(500px) rotateY(360deg) rotateZ(360deg); }
}'),

('3D呼吸', '3D特效', '3D呼吸效果',
'.d3-breathe {
  animation: d3-breathe-anim 2s ease-in-out infinite;
}
@keyframes d3-breathe-anim {
  0%, 100% { transform: perspective(500px) scale3d(1, 1, 1) rotateX(0); }
  50% { transform: perspective(500px) scale3d(1.1, 1.1, 1.1) rotateX(10deg); }
}');

-- ==================== 新增分类：滤镜特效 (10个) ====================
INSERT INTO animation_categories (name) VALUES ('滤镜特效');

INSERT INTO css_animations (title, category, description, css_code) VALUES
('模糊呼吸', '滤镜特效', '模糊度呼吸变化',
'.filter-blur-breathe {
  animation: blur-breathe 2s ease-in-out infinite;
}
@keyframes blur-breathe {
  0%, 100% { filter: blur(0); }
  50% { filter: blur(3px); }
}'),

('亮度闪烁', '滤镜特效', '亮度闪烁效果',
'.filter-brightness-flash {
  animation: brightness-flash 1s ease-in-out infinite;
}
@keyframes brightness-flash {
  0%, 100% { filter: brightness(1); }
  50% { filter: brightness(1.5); }
}'),

('对比度脉冲', '滤镜特效', '对比度脉冲效果',
'.filter-contrast-pulse {
  animation: contrast-pulse 1.5s ease-in-out infinite;
}
@keyframes contrast-pulse {
  0%, 100% { filter: contrast(1); }
  50% { filter: contrast(1.5); }
}'),

('饱和度波动', '滤镜特效', '饱和度波动效果',
'.filter-saturate-wave {
  animation: saturate-wave 2s ease-in-out infinite;
}
@keyframes saturate-wave {
  0%, 100% { filter: saturate(1); }
  50% { filter: saturate(2); }
}'),

('色相旋转', '滤镜特效', '色相持续旋转',
'.filter-hue-rotate {
  animation: hue-rotate-anim 3s linear infinite;
}
@keyframes hue-rotate-anim {
  from { filter: hue-rotate(0deg); }
  to { filter: hue-rotate(360deg); }
}'),

('灰度闪烁', '滤镜特效', '灰度闪烁效果',
'.filter-grayscale-flash {
  animation: grayscale-flash 2s ease-in-out infinite;
}
@keyframes grayscale-flash {
  0%, 100% { filter: grayscale(0); }
  50% { filter: grayscale(1); }
}'),

('反色闪烁', '滤镜特效', '反色闪烁效果',
'.filter-invert-flash {
  animation: invert-flash 1s ease-in-out infinite;
}
@keyframes invert-flash {
  0%, 100% { filter: invert(0); }
  50% { filter: invert(1); }
}'),

('褐色滤镜', '滤镜特效', '褐色滤镜效果',
'.filter-sepia-pulse {
  animation: sepia-pulse 2s ease-in-out infinite;
}
@keyframes sepia-pulse {
  0%, 100% { filter: sepia(0); }
  50% { filter: sepia(1); }
}'),

('投影动画', '滤镜特效', '投影动画效果',
'.filter-drop-shadow {
  animation: drop-shadow-anim 2s ease-in-out infinite;
}
@keyframes drop-shadow-anim {
  0%, 100% { filter: drop-shadow(5px 5px 10px rgba(139, 92, 246, 0.5)); }
  50% { filter: drop-shadow(10px 10px 20px rgba(247, 37, 133, 0.8)); }
}'),

('综合滤镜', '滤镜特效', '多滤镜组合效果',
'.filter-combo {
  animation: filter-combo-anim 4s ease-in-out infinite;
}
@keyframes filter-combo-anim {
  0%, 100% { filter: brightness(1) contrast(1) saturate(1); }
  25% { filter: brightness(1.2) contrast(1.1) saturate(1.2); }
  50% { filter: brightness(0.9) contrast(1.2) saturate(0.8); }
  75% { filter: brightness(1.1) contrast(0.9) saturate(1.3); }
}');

-- ==================== 新增分类：变形特效 (10个) ====================
INSERT INTO animation_categories (name) VALUES ('变形特效');

INSERT INTO css_animations (title, category, description, css_code) VALUES
('扭曲变形', '变形特效', '扭曲变形效果',
'.transform-skew {
  animation: skew-anim 2s ease-in-out infinite;
}
@keyframes skew-anim {
  0%, 100% { transform: skew(0, 0); }
  25% { transform: skew(10deg, 5deg); }
  75% { transform: skew(-10deg, -5deg); }
}'),

('拉伸变形', '变形特效', '拉伸变形效果',
'.transform-stretch {
  animation: stretch-anim 1.5s ease-in-out infinite;
}
@keyframes stretch-anim {
  0%, 100% { transform: scaleX(1) scaleY(1); }
  50% { transform: scaleX(1.3) scaleY(0.7); }
}'),

('压缩变形', '变形特效', '压缩变形效果',
'.transform-compress {
  animation: compress-anim 1.5s ease-in-out infinite;
}
@keyframes compress-anim {
  0%, 100% { transform: scaleX(1) scaleY(1); }
  50% { transform: scaleX(0.7) scaleY(1.3); }
}'),

('波浪变形', '变形特效', '波浪变形效果',
'.transform-wave {
  animation: wave-transform-anim 2s ease-in-out infinite;
}
@keyframes wave-transform-anim {
  0%, 100% { transform: translateY(0) rotate(0); }
  25% { transform: translateY(-10px) rotate(5deg); }
  75% { transform: translateY(10px) rotate(-5deg); }
}'),

('翻滚变形', '变形特效', '翻滚变形效果',
'.transform-tumble {
  animation: tumble-anim 2s ease-in-out infinite;
}
@keyframes tumble-anim {
  0% { transform: rotate(0) translateX(0); }
  25% { transform: rotate(90deg) translateX(20px); }
  50% { transform: rotate(180deg) translateX(0); }
  75% { transform: rotate(270deg) translateX(-20px); }
  100% { transform: rotate(360deg) translateX(0); }
}'),

('弹性变形', '变形特效', '弹性变形效果',
'.transform-elastic {
  animation: elastic-transform-anim 1s ease-in-out infinite;
}
@keyframes elastic-transform-anim {
  0%, 100% { transform: scale(1); }
  30% { transform: scale(1.25, 0.75); }
  40% { transform: scale(0.75, 1.25); }
  50% { transform: scale(1.15, 0.85); }
  65% { transform: scale(0.95, 1.05); }
  75% { transform: scale(1.05, 0.95); }
}'),

('摆动变形', '变形特效', '摆动变形效果',
'.transform-pendulum {
  animation: pendulum-anim 2s ease-in-out infinite;
  transform-origin: top center;
}
@keyframes pendulum-anim {
  0%, 100% { transform: rotate(0); }
  25% { transform: rotate(20deg); }
  75% { transform: rotate(-20deg); }
}'),

('螺旋变形', '变形特效', '螺旋变形效果',
'.transform-spiral {
  animation: spiral-transform-anim 3s linear infinite;
}
@keyframes spiral-transform-anim {
  0% { transform: rotate(0) scale(1); }
  50% { transform: rotate(180deg) scale(0.5); }
  100% { transform: rotate(360deg) scale(1); }
}'),

('抖动变形', '变形特效', '抖动变形效果',
'.transform-jitter {
  animation: jitter-anim 0.3s ease-in-out infinite;
}
@keyframes jitter-anim {
  0%, 100% { transform: translate(0, 0) rotate(0); }
  25% { transform: translate(-2px, 2px) rotate(-1deg); }
  50% { transform: translate(2px, -2px) rotate(1deg); }
  75% { transform: translate(-1px, 1px) rotate(-0.5deg); }
}'),

('翻转变形', '变形特效', '翻转变形效果',
'.transform-flip-flop {
  animation: flip-flop-anim 2s ease-in-out infinite;
}
@keyframes flip-flop-anim {
  0%, 100% { transform: rotateY(0) rotateX(0); }
  25% { transform: rotateY(180deg) rotateX(0); }
  50% { transform: rotateY(180deg) rotateX(180deg); }
  75% { transform: rotateY(0) rotateX(180deg); }
}');
