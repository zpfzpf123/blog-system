-- 新增10个大屏动画 (第六批)
SET NAMES utf8mb4;

-- 349 星云漩涡
INSERT INTO css_animations (title, category, description, css_code) VALUES ('星云漩涡', '大屏动画', '旋转的星云效果', '.nebula-vortex {
  width: 120px;
  height: 120px;
  position: relative;
  border-radius: 50%;
  background: conic-gradient(from 0deg, transparent, #a855f7 20%, transparent 40%, #00f0ff 60%, transparent 80%, #ec4899);
  animation: nebula-spin 4s linear infinite;
  filter: blur(1px);
}
.nebula-vortex::before {
  content: "";
  position: absolute;
  inset: 15px;
  border-radius: 50%;
  background: #0a1628;
}
.nebula-vortex::after {
  content: "";
  position: absolute;
  inset: 30px;
  border-radius: 50%;
  background: radial-gradient(circle, #00f0ff 0%, transparent 70%);
  animation: nebula-pulse 2s ease-in-out infinite;
}
@keyframes nebula-spin {
  to { transform: rotate(360deg); }
}
@keyframes nebula-pulse {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.2); }
}');

-- 350 激光扫描
INSERT INTO css_animations (title, category, description, css_code) VALUES ('激光扫描', '大屏动画', '激光线扫描效果', '.laser-scan {
  width: 180px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 8px;
  overflow: hidden;
}
.laser-scan::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 3px;
  height: 100%;
  background: linear-gradient(to bottom, transparent, #00ff88, #00f0ff, #00ff88, transparent);
  box-shadow: 0 0 20px #00f0ff, 0 0 40px #00ff88;
  animation: laser-move 2s ease-in-out infinite;
}
.laser-scan::after {
  content: "SCANNING";
  position: absolute;
  bottom: 10px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: #00f0ff;
  letter-spacing: 2px;
  animation: scan-blink 1s steps(2) infinite;
}
@keyframes laser-move {
  0% { left: -3px; }
  100% { left: 100%; }
}
@keyframes scan-blink {
  50% { opacity: 0.3; }
}');

-- 351 原子轨道
INSERT INTO css_animations (title, category, description, css_code) VALUES ('原子轨道', '大屏动画', '原子电子轨道动画', '.atom-orbit {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.atom-orbit::before {
  content: "";
  position: absolute;
  width: 20px;
  height: 20px;
  background: radial-gradient(circle, #00f0ff, #0a1628);
  border-radius: 50%;
  box-shadow: 0 0 20px #00f0ff;
}
.atom-orbit::after {
  content: "";
  position: absolute;
  width: 100px;
  height: 100px;
  border: 2px solid rgba(0, 240, 255, 0.3);
  border-radius: 50%;
  animation: orbit-rotate 3s linear infinite;
}
@keyframes orbit-rotate {
  to { transform: rotateZ(360deg) rotateX(70deg); }
}');

-- 352 频谱分析
INSERT INTO css_animations (title, category, description, css_code) VALUES ('频谱分析', '大屏动画', '音频频谱分析器', '.spectrum-analyzer {
  width: 180px;
  height: 80px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 4px;
  padding: 10px;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
}
.spectrum-analyzer::before,
.spectrum-analyzer::after {
  content: "";
  width: 12px;
  background: linear-gradient(to top, #00f0ff, #00ff88, #a855f7);
  border-radius: 2px 2px 0 0;
  animation: spectrum-bar 0.8s ease-in-out infinite;
}
.spectrum-analyzer::before {
  height: 50px;
  animation-delay: 0s;
}
.spectrum-analyzer::after {
  height: 35px;
  animation-delay: 0.2s;
}
@keyframes spectrum-bar {
  0%, 100% { transform: scaleY(0.3); }
  50% { transform: scaleY(1); }
}');

-- 353 力场屏障
INSERT INTO css_animations (title, category, description, css_code) VALUES ('力场屏障', '大屏动画', '能量力场屏障', '.force-field {
  width: 140px;
  height: 100px;
  position: relative;
  border-radius: 50%;
  background: transparent;
  overflow: hidden;
}
.force-field::before {
  content: "";
  position: absolute;
  inset: 0;
  border: 3px solid transparent;
  border-radius: 50%;
  background: linear-gradient(#0a1628, #0a1628) padding-box,
              linear-gradient(90deg, #00f0ff, #a855f7, #ec4899, #00f0ff) border-box;
  background-size: 100% 100%, 400% 100%;
  animation: force-flow 3s linear infinite;
}
.force-field::after {
  content: "";
  position: absolute;
  inset: 10px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.1) 0%, transparent 70%);
  animation: force-pulse 2s ease-in-out infinite;
}
@keyframes force-flow {
  to { background-position: 100% 100%, 400% 100%; }
}
@keyframes force-pulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 1; }
}');


-- 354 数据矩阵
INSERT INTO css_animations (title, category, description, css_code) VALUES ('数据矩阵', '大屏动画', '数据矩阵网格', '.data-matrix {
  width: 160px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  overflow: hidden;
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  grid-template-rows: repeat(5, 1fr);
  gap: 2px;
  padding: 8px;
}
.data-matrix::before {
  content: "";
  position: absolute;
  inset: 8px;
  background: 
    repeating-linear-gradient(90deg, rgba(0, 240, 255, 0.8) 0px, rgba(0, 240, 255, 0.8) 2px, transparent 2px, transparent 18px);
  animation: matrix-scan 2s steps(8) infinite;
  opacity: 0.3;
}
@keyframes matrix-scan {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}');

-- 355 脉冲雷达
INSERT INTO css_animations (title, category, description, css_code) VALUES ('脉冲雷达', '大屏动画', '脉冲式雷达扫描', '.pulse-radar {
  width: 120px;
  height: 120px;
  position: relative;
  border-radius: 50%;
  border: 2px solid rgba(0, 240, 255, 0.3);
  background: radial-gradient(circle, #0d2137 0%, #0a1628 100%);
}
.pulse-radar::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 50%;
  height: 2px;
  background: linear-gradient(90deg, #00f0ff, transparent);
  transform-origin: left center;
  animation: radar-pulse-sweep 2s linear infinite;
  box-shadow: 0 0 10px #00f0ff;
}
.pulse-radar::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 8px;
  height: 8px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 15px #00f0ff;
  animation: radar-center-pulse 1s ease-in-out infinite;
}
@keyframes radar-pulse-sweep {
  to { transform: rotate(360deg); }
}
@keyframes radar-center-pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.3); }
}');

-- 356 晶体生长
INSERT INTO css_animations (title, category, description, css_code) VALUES ('晶体生长', '大屏动画', '晶体结构生长动画', '.crystal-grow {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.crystal-grow::before {
  content: "";
  position: absolute;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.8), rgba(168, 85, 247, 0.8));
  transform: rotate(45deg);
  animation: crystal-expand 2s ease-in-out infinite;
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.5);
}
.crystal-grow::after {
  content: "";
  position: absolute;
  width: 25px;
  height: 25px;
  background: linear-gradient(135deg, rgba(0, 255, 136, 0.8), rgba(236, 72, 153, 0.8));
  transform: rotate(45deg);
  animation: crystal-expand 2s ease-in-out infinite 0.5s;
  box-shadow: 0 0 15px rgba(0, 255, 136, 0.5);
}
@keyframes crystal-expand {
  0%, 100% { transform: rotate(45deg) scale(0.8); opacity: 0.6; }
  50% { transform: rotate(45deg) scale(1.2); opacity: 1; }
}');

-- 357 传送门
INSERT INTO css_animations (title, category, description, css_code) VALUES ('传送门', '大屏动画', '科幻传送门效果', '.portal-gate {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.portal-gate::before {
  content: "";
  position: absolute;
  width: 80px;
  height: 100px;
  border: 3px solid #00f0ff;
  border-radius: 40px 40px 0 0;
  background: linear-gradient(180deg, rgba(0, 240, 255, 0.1) 0%, rgba(168, 85, 247, 0.2) 100%);
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.5), inset 0 0 30px rgba(168, 85, 247, 0.3);
  animation: portal-glow 2s ease-in-out infinite;
}
.portal-gate::after {
  content: "";
  position: absolute;
  width: 60px;
  height: 80px;
  background: conic-gradient(from 0deg, transparent, #00f0ff, transparent, #a855f7, transparent);
  border-radius: 30px 30px 0 0;
  animation: portal-spin 3s linear infinite;
  opacity: 0.6;
}
@keyframes portal-glow {
  0%, 100% { box-shadow: 0 0 20px rgba(0, 240, 255, 0.5), inset 0 0 30px rgba(168, 85, 247, 0.3); }
  50% { box-shadow: 0 0 40px rgba(0, 240, 255, 0.8), inset 0 0 50px rgba(168, 85, 247, 0.5); }
}
@keyframes portal-spin {
  to { transform: rotate(360deg); }
}');

-- 358 能量核心
INSERT INTO css_animations (title, category, description, css_code) VALUES ('能量核心', '大屏动画', '能量核心反应堆', '.energy-core {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.energy-core::before {
  content: "";
  position: absolute;
  width: 30px;
  height: 30px;
  background: radial-gradient(circle, #fff 0%, #00f0ff 50%, transparent 70%);
  border-radius: 50%;
  box-shadow: 0 0 30px #00f0ff, 0 0 60px #00ff88;
  animation: core-pulse 1s ease-in-out infinite;
}
.energy-core::after {
  content: "";
  position: absolute;
  width: 80px;
  height: 80px;
  border: 2px dashed rgba(0, 240, 255, 0.5);
  border-radius: 50%;
  animation: core-orbit 4s linear infinite;
}
@keyframes core-pulse {
  0%, 100% { transform: scale(1); box-shadow: 0 0 30px #00f0ff, 0 0 60px #00ff88; }
  50% { transform: scale(1.2); box-shadow: 0 0 50px #00f0ff, 0 0 80px #00ff88, 0 0 100px #a855f7; }
}
@keyframes core-orbit {
  to { transform: rotate(360deg); }
}');

