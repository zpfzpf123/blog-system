-- 更新大屏动画的完整CSS代码

-- 267 流光边框
UPDATE css_animations SET css_code = '.glow-border {
  width: 200px;
  height: 120px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}
.glow-border::before {
  content: "";
  position: absolute;
  inset: -2px;
  background: conic-gradient(from 0deg, #00f0ff, #00ff88, #a855f7, #00f0ff);
  animation: glow-rotate 3s linear infinite;
  z-index: 1;
}
.glow-border::after {
  content: "";
  position: absolute;
  inset: 2px;
  background: #0a1628;
  border-radius: 6px;
  z-index: 2;
}
@keyframes glow-rotate {
  to { transform: rotate(360deg); }
}' WHERE id = 267;

-- 268 扫光效果
UPDATE css_animations SET css_code = '.shine-box {
  width: 180px;
  height: 100px;
  background: linear-gradient(135deg, #0a2540, #0d3050);
  border: 1px solid rgba(0, 200, 255, 0.3);
  border-radius: 8px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #00ff88;
}
.shine-box::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shine-sweep 2s infinite;
}
@keyframes shine-sweep {
  to { left: 150%; }
}' WHERE id = 268;

-- 269 呼吸光晕
UPDATE css_animations SET css_code = '.breath-glow {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: radial-gradient(circle, #00f0ff 0%, transparent 70%);
  animation: breathe-glow 2s ease-in-out infinite;
  display: flex;
  align-items: center;
  justify-content: center;
}
.breath-glow::after {
  content: "";
  width: 40px;
  height: 40px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 20px #00f0ff;
}
@keyframes breathe-glow {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.2); opacity: 1; }
}' WHERE id = 269;

-- 270 雷达扫描
UPDATE css_animations SET css_code = '.radar-scan {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 2px solid rgba(0, 200, 255, 0.3);
  position: relative;
  background: #0a1628;
}
.radar-scan::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: conic-gradient(from 0deg, transparent 0%, rgba(0, 240, 255, 0.4) 10%, transparent 20%);
  animation: radar-sweep 2s linear infinite;
}
.radar-scan::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 8px;
  height: 8px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 10px #00f0ff;
}
@keyframes radar-sweep {
  to { transform: rotate(360deg); }
}' WHERE id = 270;

-- 271 粒子漂浮
UPDATE css_animations SET css_code = '.particle-float {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
}
.particle-float::before,
.particle-float::after {
  content: "";
  position: absolute;
  width: 6px;
  height: 6px;
  background: #00f0ff;
  border-radius: 50%;
  animation: particle-move 3s ease-in-out infinite;
  box-shadow: 0 0 10px #00f0ff;
}
.particle-float::before {
  top: 20%;
  left: 30%;
  animation-delay: 0s;
}
.particle-float::after {
  top: 60%;
  left: 70%;
  animation-delay: 1.5s;
}
@keyframes particle-move {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}' WHERE id = 271;

-- 272 波纹扩散
UPDATE css_animations SET css_code = '.ripple-wave {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ripple-wave::before,
.ripple-wave::after {
  content: "";
  position: absolute;
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: ripple-expand 2s ease-out infinite;
}
.ripple-wave::before {
  width: 40px;
  height: 40px;
}
.ripple-wave::after {
  width: 40px;
  height: 40px;
  animation-delay: 1s;
}
@keyframes ripple-expand {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(3); opacity: 0; }
}' WHERE id = 272;

-- 273 霓虹文字
UPDATE css_animations SET css_code = '.neon-text {
  font-size: 36px;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 0 10px #00f0ff, 0 0 20px #00f0ff, 0 0 40px #00f0ff, 0 0 80px #00f0ff;
  animation: neon-flicker 1.5s ease-in-out infinite alternate;
}
@keyframes neon-flicker {
  from { text-shadow: 0 0 10px #00f0ff, 0 0 20px #00f0ff, 0 0 40px #00f0ff; }
  to { text-shadow: 0 0 5px #00f0ff, 0 0 10px #00f0ff, 0 0 20px #00f0ff, 0 0 40px #00f0ff, 0 0 80px #00f0ff; }
}' WHERE id = 273;

-- 274 进度条动画
UPDATE css_animations SET css_code = '.progress-bar {
  width: 200px;
  height: 12px;
  background: rgba(0, 50, 100, 0.4);
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}
.progress-bar::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  width: 70%;
  height: 100%;
  background: linear-gradient(90deg, #00f0ff, #00ff88);
  border-radius: 6px;
  box-shadow: 0 0 10px #00f0ff;
}
.progress-bar::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: progress-shine 2s infinite;
}
@keyframes progress-shine {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}' WHERE id = 274;

-- 275 数据卡片悬浮
UPDATE css_animations SET css_code = '.hover-card {
  width: 160px;
  height: 100px;
  background: linear-gradient(135deg, rgba(0, 100, 200, 0.3), rgba(0, 50, 100, 0.2));
  border: 1px solid rgba(0, 200, 255, 0.3);
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  animation: hover-float 3s ease-in-out infinite;
}
@keyframes hover-float {
  0%, 100% { transform: translateY(0); box-shadow: 0 5px 15px rgba(0, 200, 255, 0.2); }
  50% { transform: translateY(-10px); box-shadow: 0 20px 40px rgba(0, 200, 255, 0.3); }
}' WHERE id = 275;

-- 276 渐变流动边框
UPDATE css_animations SET css_code = '.gradient-border {
  width: 180px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.gradient-border::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 8px;
  padding: 2px;
  background: linear-gradient(90deg, #00f0ff, #00ff88, #a855f7, #00f0ff);
  background-size: 300% 100%;
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: gradient-flow 3s linear infinite;
}
@keyframes gradient-flow {
  0% { background-position: 0% 0%; }
  100% { background-position: 300% 0%; }
}' WHERE id = 276;

-- 277 脉冲圆环
UPDATE css_animations SET css_code = '.pulse-ring {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 3px solid #00f0ff;
  position: relative;
  animation: pulse-scale 2s ease-out infinite;
}
.pulse-ring::before {
  content: "";
  position: absolute;
  inset: -10px;
  border-radius: 50%;
  border: 2px solid #00f0ff;
  animation: pulse-scale 2s ease-out infinite 0.5s;
  opacity: 0.6;
}
@keyframes pulse-scale {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}' WHERE id = 277;
