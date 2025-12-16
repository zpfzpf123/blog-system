-- 更新大屏动画的完整CSS代码 (第二部分)

-- 278 数字滚动
UPDATE css_animations SET css_code = '.digit-roll {
  font-size: 48px;
  font-weight: bold;
  color: #00f0ff;
  font-family: \"Courier New\", monospace;
  text-shadow: 0 0 20px #00f0ff;
  animation: digit-flicker 0.1s steps(1) infinite;
}
@keyframes digit-flicker {
  0% { opacity: 1; }
  25% { opacity: 0.8; }
  50% { opacity: 1; }
  75% { opacity: 0.9; }
  100% { opacity: 1; }
}' WHERE id = 278;

-- 279 六边形网格
UPDATE css_animations SET css_code = '.hex-grid {
  width: 200px;
  height: 120px;
  background: 
    linear-gradient(30deg, #0a1628 12%, transparent 12.5%, transparent 87%, #0a1628 87.5%, #0a1628),
    linear-gradient(150deg, #0a1628 12%, transparent 12.5%, transparent 87%, #0a1628 87.5%, #0a1628),
    linear-gradient(30deg, #0a1628 12%, transparent 12.5%, transparent 87%, #0a1628 87.5%, #0a1628),
    linear-gradient(150deg, #0a1628 12%, transparent 12.5%, transparent 87%, #0a1628 87.5%, #0a1628),
    linear-gradient(60deg, rgba(0, 240, 255, 0.1) 25%, transparent 25.5%, transparent 75%, rgba(0, 240, 255, 0.1) 75%);
  background-size: 40px 70px;
  background-position: 0 0, 0 0, 20px 35px, 20px 35px, 0 0, 20px 35px;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.3);
  animation: hex-pulse 2s ease-in-out infinite;
}
@keyframes hex-pulse {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 1; }
}' WHERE id = 279;

-- 280 能量环
UPDATE css_animations SET css_code = '.energy-ring {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 50%;
}
.energy-ring::before {
  content: \"\";
  position: absolute;
  inset: 0;
  border: 4px solid transparent;
  border-top-color: #00f0ff;
  border-right-color: #00ff88;
  border-radius: 50%;
  animation: energy-spin 1s linear infinite;
}
.energy-ring::after {
  content: \"\";
  position: absolute;
  inset: 15px;
  border: 3px solid transparent;
  border-bottom-color: #a855f7;
  border-left-color: #ec4899;
  border-radius: 50%;
  animation: energy-spin 1.5s linear infinite reverse;
}
@keyframes energy-spin {
  to { transform: rotate(360deg); }
}' WHERE id = 280;

-- 281 数据流
UPDATE css_animations SET css_code = '.data-flow {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
}
.data-flow::before,
.data-flow::after {
  content: \"\";
  position: absolute;
  width: 2px;
  height: 30px;
  background: linear-gradient(to bottom, transparent, #00f0ff, transparent);
  animation: data-fall 1.5s linear infinite;
}
.data-flow::before {
  left: 30%;
  animation-delay: 0s;
}
.data-flow::after {
  left: 70%;
  animation-delay: 0.7s;
}
@keyframes data-fall {
  0% { top: -30px; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { top: 120px; opacity: 0; }
}' WHERE id = 281;

-- 282 闪烁光点
UPDATE css_animations SET css_code = '.blink-dots {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
}
.blink-dots::before,
.blink-dots::after {
  content: \"\";
  position: absolute;
  width: 4px;
  height: 4px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 10px #00f0ff, 40px 20px 0 #00ff88, 80px 50px 0 #a855f7, 120px 30px 0 #ec4899, 160px 70px 0 #00f0ff;
  animation: blink-star 2s ease-in-out infinite;
}
.blink-dots::before {
  top: 20px;
  left: 20px;
}
.blink-dots::after {
  top: 60px;
  left: 10px;
  animation-delay: 1s;
  box-shadow: 0 0 10px #00ff88, 50px -20px 0 #00f0ff, 100px 10px 0 #ec4899, 140px -10px 0 #a855f7;
}
@keyframes blink-star {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}' WHERE id = 282;

-- 283 波浪线条
UPDATE css_animations SET css_code = '.wave-line {
  width: 200px;
  height: 80px;
  position: relative;
  overflow: hidden;
}
.wave-line::before {
  content: \"\";
  position: absolute;
  width: 400px;
  height: 100%;
  background: repeating-linear-gradient(90deg, transparent 0px, transparent 10px, #00f0ff 10px, #00f0ff 12px);
  animation: wave-move 2s linear infinite;
}
@keyframes wave-move {
  0% { transform: translateX(0); }
  100% { transform: translateX(-200px); }
}' WHERE id = 283;

-- 284 旋转方块
UPDATE css_animations SET css_code = '.rotate-cube {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #00f0ff, #00ff88);
  animation: cube-rotate 3s ease-in-out infinite;
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.5);
}
@keyframes cube-rotate {
  0% { transform: rotate(0deg) scale(1); border-radius: 10px; }
  25% { transform: rotate(90deg) scale(0.8); border-radius: 50%; }
  50% { transform: rotate(180deg) scale(1); border-radius: 10px; }
  75% { transform: rotate(270deg) scale(0.8); border-radius: 50%; }
  100% { transform: rotate(360deg) scale(1); border-radius: 10px; }
}' WHERE id = 284;

-- 285 扫描线
UPDATE css_animations SET css_code = '.scan-line {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.3);
}
.scan-line::before {
  content: \"\";
  position: absolute;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
  box-shadow: 0 0 20px #00f0ff;
  animation: scan-move 2s ease-in-out infinite;
}
@keyframes scan-move {
  0%, 100% { top: 0; }
  50% { top: calc(100% - 3px); }
}' WHERE id = 285;

-- 286 环形进度
UPDATE css_animations SET css_code = '.ring-progress {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: conic-gradient(#00f0ff 0deg, #00ff88 120deg, #a855f7 240deg, #00f0ff 360deg);
  position: relative;
  animation: ring-rotate 3s linear infinite;
}
.ring-progress::before {
  content: \"\";
  position: absolute;
  inset: 8px;
  background: #0a1628;
  border-radius: 50%;
}
.ring-progress::after {
  content: \"75%\";
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00f0ff;
  font-size: 16px;
  font-weight: bold;
}
@keyframes ring-rotate {
  to { transform: rotate(360deg); }
}' WHERE id = 286;

-- 287 粒子连线
UPDATE css_animations SET css_code = '.particle-line {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
  overflow: hidden;
}
.particle-line::before {
  content: \"\";
  position: absolute;
  width: 8px;
  height: 8px;
  background: #00f0ff;
  border-radius: 50%;
  top: 30px;
  left: 30px;
  box-shadow: 0 0 10px #00f0ff, 60px 40px 0 #00ff88, 120px 20px 0 #a855f7, 140px 60px 0 #ec4899;
  animation: particle-pulse 2s ease-in-out infinite;
}
.particle-line::after {
  content: \"\";
  position: absolute;
  top: 34px;
  left: 34px;
  width: 120px;
  height: 1px;
  background: linear-gradient(90deg, #00f0ff, #00ff88, #a855f7);
  transform-origin: left;
  animation: line-draw 2s ease-in-out infinite;
}
@keyframes particle-pulse {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}
@keyframes line-draw {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}' WHERE id = 287;
