-- 更新大屏动画的完整CSS代码 (第三部分)

-- 288 电路板
UPDATE css_animations SET css_code = '.circuit-board {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.3);
  overflow: hidden;
}
.circuit-board::before {
  content: \"\";
  position: absolute;
  inset: 10px;
  background: 
    linear-gradient(90deg, transparent 49%, rgba(0, 240, 255, 0.3) 49%, rgba(0, 240, 255, 0.3) 51%, transparent 51%),
    linear-gradient(0deg, transparent 49%, rgba(0, 240, 255, 0.3) 49%, rgba(0, 240, 255, 0.3) 51%, transparent 51%);
  background-size: 20px 20px;
}
.circuit-board::after {
  content: \"\";
  position: absolute;
  width: 8px;
  height: 8px;
  background: #00f0ff;
  border-radius: 50%;
  top: 20px;
  left: 20px;
  box-shadow: 40px 0 0 #00ff88, 80px 20px 0 #a855f7, 120px 0 0 #ec4899, 160px 20px 0 #00f0ff, 0 40px 0 #00ff88, 40px 60px 0 #a855f7, 80px 40px 0 #00f0ff, 120px 60px 0 #00ff88, 160px 40px 0 #ec4899;
  animation: circuit-blink 1.5s ease-in-out infinite;
}
@keyframes circuit-blink {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}' WHERE id = 288;

-- 289 DNA螺旋
UPDATE css_animations SET css_code = '.dna-helix {
  width: 60px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  align-items: center;
}
.dna-helix span {
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #00f0ff, transparent 40%, transparent 60%, #00ff88);
  border-radius: 2px;
  animation: dna-twist 2s ease-in-out infinite;
}
.dna-helix span:nth-child(1) { animation-delay: 0s; }
.dna-helix span:nth-child(2) { animation-delay: 0.2s; }
.dna-helix span:nth-child(3) { animation-delay: 0.4s; }
.dna-helix span:nth-child(4) { animation-delay: 0.6s; }
.dna-helix span:nth-child(5) { animation-delay: 0.8s; }
@keyframes dna-twist {
  0%, 100% { transform: scaleX(1); }
  50% { transform: scaleX(-1); }
}' WHERE id = 289;

-- 290 音频波形
UPDATE css_animations SET css_code = '.audio-wave {
  width: 180px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}
.audio-wave span {
  width: 8px;
  background: linear-gradient(to top, #00f0ff, #00ff88);
  border-radius: 4px;
  animation: audio-bounce 1s ease-in-out infinite;
}
.audio-wave span:nth-child(1) { height: 20px; animation-delay: 0s; }
.audio-wave span:nth-child(2) { height: 40px; animation-delay: 0.1s; }
.audio-wave span:nth-child(3) { height: 60px; animation-delay: 0.2s; }
.audio-wave span:nth-child(4) { height: 80px; animation-delay: 0.3s; }
.audio-wave span:nth-child(5) { height: 60px; animation-delay: 0.4s; }
.audio-wave span:nth-child(6) { height: 40px; animation-delay: 0.5s; }
.audio-wave span:nth-child(7) { height: 20px; animation-delay: 0.6s; }
@keyframes audio-bounce {
  0%, 100% { transform: scaleY(0.3); }
  50% { transform: scaleY(1); }
}' WHERE id = 290;

-- 291 星空背景
UPDATE css_animations SET css_code = '.starfield {
  width: 200px;
  height: 120px;
  background: radial-gradient(ellipse at center, #0d2137 0%, #0a1628 100%);
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}
.starfield::before,
.starfield::after {
  content: \"\";
  position: absolute;
  width: 2px;
  height: 2px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 20px 10px 0 #fff, 50px 30px 0 #00f0ff, 80px 15px 0 #fff, 120px 40px 0 #00ff88, 150px 20px 0 #fff, 180px 50px 0 #a855f7, 30px 60px 0 #fff, 70px 80px 0 #00f0ff, 110px 70px 0 #fff, 140px 90px 0 #ec4899, 170px 75px 0 #fff, 10px 100px 0 #00ff88;
  animation: twinkle 2s ease-in-out infinite;
}
.starfield::after {
  animation-delay: 1s;
  box-shadow: 15px 25px 0 #00f0ff, 45px 45px 0 #fff, 75px 35px 0 #00ff88, 105px 55px 0 #fff, 135px 30px 0 #a855f7, 165px 65px 0 #fff, 25px 85px 0 #ec4899, 55px 95px 0 #fff, 95px 105px 0 #00f0ff;
}
@keyframes twinkle {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}' WHERE id = 291;

-- 292 加载圆环
UPDATE css_animations SET css_code = '.tech-loader {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 4px solid rgba(0, 240, 255, 0.1);
  border-top-color: #00f0ff;
  position: relative;
  animation: tech-spin 1s linear infinite;
}
.tech-loader::before {
  content: \"\";
  position: absolute;
  inset: 8px;
  border-radius: 50%;
  border: 3px solid rgba(0, 255, 136, 0.1);
  border-bottom-color: #00ff88;
  animation: tech-spin 1.5s linear infinite reverse;
}
.tech-loader::after {
  content: \"\";
  position: absolute;
  inset: 20px;
  border-radius: 50%;
  border: 2px solid rgba(168, 85, 247, 0.1);
  border-left-color: #a855f7;
  animation: tech-spin 2s linear infinite;
}
@keyframes tech-spin {
  to { transform: rotate(360deg); }
}' WHERE id = 292;

-- 293 信号波
UPDATE css_animations SET css_code = '.signal-wave {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.signal-wave::before,
.signal-wave::after {
  content: \"\";
  position: absolute;
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: signal-expand 2s ease-out infinite;
}
.signal-wave::before {
  width: 20px;
  height: 20px;
}
.signal-wave::after {
  width: 20px;
  height: 20px;
  animation-delay: 0.5s;
}
@keyframes signal-expand {
  0% { transform: scale(1); opacity: 1; border-color: #00f0ff; }
  50% { border-color: #00ff88; }
  100% { transform: scale(5); opacity: 0; border-color: #a855f7; }
}' WHERE id = 293;

-- 294 矩阵雨
UPDATE css_animations SET css_code = '.matrix-rain {
  width: 200px;
  height: 120px;
  background: #0a1628;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  font-family: monospace;
}
.matrix-rain::before,
.matrix-rain::after {
  content: \"01001010\";
  position: absolute;
  font-size: 12px;
  color: #00ff88;
  writing-mode: vertical-rl;
  text-orientation: mixed;
  animation: matrix-fall 2s linear infinite;
  text-shadow: 0 0 10px #00ff88;
}
.matrix-rain::before {
  left: 20%;
  animation-delay: 0s;
}
.matrix-rain::after {
  left: 60%;
  content: \"11010101\";
  animation-delay: 1s;
}
@keyframes matrix-fall {
  0% { top: -80px; opacity: 1; }
  100% { top: 120px; opacity: 0.3; }
}' WHERE id = 294;

-- 295 光束扫射
UPDATE css_animations SET css_code = '.light-beam {
  width: 120px;
  height: 120px;
  position: relative;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.1) 0%, transparent 70%);
}
.light-beam::before {
  content: \"\";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #00f0ff, transparent);
  transform-origin: left center;
  animation: beam-rotate 2s linear infinite;
  box-shadow: 0 0 20px #00f0ff;
}
.light-beam::after {
  content: \"\";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 10px;
  height: 10px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 20px #00f0ff;
}
@keyframes beam-rotate {
  to { transform: rotate(360deg); }
}' WHERE id = 295;

-- 296 粒子爆发
UPDATE css_animations SET css_code = '.particle-burst {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.particle-burst::before {
  content: \"\";
  position: absolute;
  width: 10px;
  height: 10px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 20px #00f0ff;
  animation: burst-center 2s ease-in-out infinite;
}
.particle-burst::after {
  content: \"\";
  position: absolute;
  width: 6px;
  height: 6px;
  background: #00ff88;
  border-radius: 50%;
  box-shadow: 30px 0 0 #00f0ff, -30px 0 0 #00ff88, 0 30px 0 #a855f7, 0 -30px 0 #ec4899, 21px 21px 0 #00f0ff, -21px -21px 0 #00ff88, 21px -21px 0 #a855f7, -21px 21px 0 #ec4899;
  animation: burst-particles 2s ease-in-out infinite;
}
@keyframes burst-center {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.5); }
}
@keyframes burst-particles {
  0%, 100% { transform: scale(0.5); opacity: 0; }
  50% { transform: scale(1); opacity: 1; }
}' WHERE id = 296;

-- 297 全息投影
UPDATE css_animations SET css_code = '.hologram {
  width: 160px;
  height: 100px;
  background: linear-gradient(180deg, rgba(0, 240, 255, 0.1) 0%, rgba(0, 240, 255, 0.05) 100%);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: bold;
  color: #00f0ff;
  text-shadow: 0 0 10px #00f0ff;
  position: relative;
  animation: holo-flicker 0.1s steps(2) infinite;
}
.hologram::before {
  content: \"\";
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(0deg, transparent 0px, transparent 2px, rgba(0, 240, 255, 0.03) 2px, rgba(0, 240, 255, 0.03) 4px);
  pointer-events: none;
}
.hologram::after {
  content: \"\";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 50%;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.1) 0%, transparent 100%);
  border-radius: 8px 8px 0 0;
}
@keyframes holo-flicker {
  0% { opacity: 1; }
  50% { opacity: 0.95; }
}' WHERE id = 297;
