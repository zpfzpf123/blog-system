-- 新增10个大屏动画
SET NAMES utf8mb4;

-- 298 水波纹
INSERT INTO css_animations (title, category, description, css_code) VALUES ('水波纹', '大屏动画', '水面波纹扩散效果', '.water-ripple {
  width: 150px;
  height: 150px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.water-ripple::before,
.water-ripple::after {
  content: "";
  position: absolute;
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: water-expand 3s ease-out infinite;
}
.water-ripple::before {
  width: 30px;
  height: 30px;
  animation-delay: 0s;
}
.water-ripple::after {
  width: 30px;
  height: 30px;
  animation-delay: 1s;
}
@keyframes water-expand {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(4); opacity: 0; }
}');

-- 299 闪电效果
INSERT INTO css_animations (title, category, description, css_code) VALUES ('闪电效果', '大屏动画', '闪烁的闪电光效', '.lightning {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.lightning::before {
  content: "⚡";
  font-size: 60px;
  color: #00f0ff;
  text-shadow: 0 0 20px #00f0ff, 0 0 40px #00f0ff;
  animation: lightning-flash 2s ease-in-out infinite;
}
@keyframes lightning-flash {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  10% { opacity: 1; transform: scale(1.2); }
  20% { opacity: 0.5; transform: scale(1); }
  30% { opacity: 1; transform: scale(1.1); }
  40% { opacity: 0.3; transform: scale(1); }
}');

-- 300 齿轮旋转
INSERT INTO css_animations (title, category, description, css_code) VALUES ('齿轮旋转', '大屏动画', '机械齿轮转动效果', '.gear-rotate {
  width: 80px;
  height: 80px;
  position: relative;
}
.gear-rotate::before {
  content: "⚙";
  font-size: 70px;
  color: #00f0ff;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: gear-spin 3s linear infinite;
  text-shadow: 0 0 10px #00f0ff;
}
.gear-rotate::after {
  content: "⚙";
  font-size: 40px;
  color: #00ff88;
  position: absolute;
  top: -10px;
  right: -20px;
  animation: gear-spin 2s linear infinite reverse;
  text-shadow: 0 0 10px #00ff88;
}
@keyframes gear-spin {
  to { transform: translate(-50%, -50%) rotate(360deg); }
}');

-- 301 心跳监测
INSERT INTO css_animations (title, category, description, css_code) VALUES ('心跳监测', '大屏动画', '心电图波形动画', '.heartbeat {
  width: 200px;
  height: 80px;
  position: relative;
  overflow: hidden;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.3);
}
.heartbeat::before {
  content: "";
  position: absolute;
  width: 400px;
  height: 2px;
  background: repeating-linear-gradient(
    90deg,
    #00f0ff 0px, #00f0ff 30px,
    transparent 30px, transparent 35px,
    #00f0ff 35px, #00f0ff 40px,
    #00ff88 40px, #00ff88 45px,
    #00f0ff 45px, #00f0ff 50px,
    transparent 50px, transparent 100px
  );
  top: 50%;
  left: 0;
  animation: heartbeat-move 2s linear infinite;
  box-shadow: 0 0 10px #00f0ff;
}
@keyframes heartbeat-move {
  0% { transform: translateX(0); }
  100% { transform: translateX(-200px); }
}');

-- 302 指南针
INSERT INTO css_animations (title, category, description, css_code) VALUES ('指南针', '大屏动画', '旋转的指南针效果', '.compass {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid rgba(0, 200, 255, 0.5);
  position: relative;
  background: radial-gradient(circle, #0d2137 0%, #0a1628 100%);
}
.compass::before {
  content: "";
  position: absolute;
  top: 10%;
  left: 50%;
  width: 4px;
  height: 40%;
  background: linear-gradient(to bottom, #ec4899, #00f0ff);
  transform-origin: bottom center;
  transform: translateX(-50%);
  animation: compass-swing 3s ease-in-out infinite;
  border-radius: 2px;
}
.compass::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 10px;
  height: 10px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 10px #00f0ff;
}
@keyframes compass-swing {
  0%, 100% { transform: translateX(-50%) rotate(-30deg); }
  50% { transform: translateX(-50%) rotate(30deg); }
}');

-- 303 时钟动画
INSERT INTO css_animations (title, category, description, css_code) VALUES ('时钟动画', '大屏动画', '科技感时钟指针', '.tech-clock {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid #00f0ff;
  position: relative;
  background: #0a1628;
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.3);
}
.tech-clock::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 3px;
  height: 30px;
  background: #00f0ff;
  transform-origin: bottom center;
  transform: translate(-50%, -100%);
  animation: clock-hour 12s linear infinite;
  border-radius: 2px;
  box-shadow: 0 0 10px #00f0ff;
}
.tech-clock::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 2px;
  height: 40px;
  background: #00ff88;
  transform-origin: bottom center;
  transform: translate(-50%, -100%);
  animation: clock-minute 60s linear infinite;
  border-radius: 2px;
  box-shadow: 0 0 10px #00ff88;
}
@keyframes clock-hour {
  to { transform: translate(-50%, -100%) rotate(360deg); }
}
@keyframes clock-minute {
  to { transform: translate(-50%, -100%) rotate(360deg); }
}');

-- 304 火焰效果
INSERT INTO css_animations (title, category, description, css_code) VALUES ('火焰效果', '大屏动画', '跳动的火焰动画', '.flame {
  width: 60px;
  height: 80px;
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.flame::before {
  content: "";
  width: 40px;
  height: 60px;
  background: linear-gradient(to top, #ff6b35, #f7931e, #ffcc02);
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  animation: flame-flicker 0.5s ease-in-out infinite alternate;
  box-shadow: 0 0 30px #ff6b35, 0 0 60px #f7931e;
}
.flame::after {
  content: "";
  position: absolute;
  bottom: 0;
  width: 20px;
  height: 30px;
  background: linear-gradient(to top, #00f0ff, #00ff88);
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
  animation: flame-flicker 0.3s ease-in-out infinite alternate-reverse;
}
@keyframes flame-flicker {
  0% { transform: scaleY(1) scaleX(1); }
  100% { transform: scaleY(1.1) scaleX(0.9); }
}');

-- 305 卫星轨道
INSERT INTO css_animations (title, category, description, css_code) VALUES ('卫星轨道', '大屏动画', '环绕的卫星轨道', '.satellite-orbit {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.satellite-orbit::before {
  content: "";
  position: absolute;
  width: 100px;
  height: 100px;
  border: 2px solid rgba(0, 240, 255, 0.3);
  border-radius: 50%;
  animation: orbit-tilt 10s linear infinite;
}
.satellite-orbit::after {
  content: "";
  position: absolute;
  width: 10px;
  height: 10px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 15px #00f0ff;
  animation: satellite-move 3s linear infinite;
}
@keyframes orbit-tilt {
  to { transform: rotateX(60deg) rotateZ(360deg); }
}
@keyframes satellite-move {
  0% { transform: rotate(0deg) translateX(50px) rotate(0deg); }
  100% { transform: rotate(360deg) translateX(50px) rotate(-360deg); }
}');

-- 306 音量指示
INSERT INTO css_animations (title, category, description, css_code) VALUES ('音量指示', '大屏动画', '跳动的音量条', '.volume-bar {
  width: 150px;
  height: 80px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 8px;
}
.volume-bar::before,
.volume-bar::after {
  content: "";
  width: 20px;
  background: linear-gradient(to top, #00f0ff, #00ff88, #a855f7);
  border-radius: 4px 4px 0 0;
  animation: volume-bounce 0.8s ease-in-out infinite;
}
.volume-bar::before {
  height: 60px;
  animation-delay: 0s;
}
.volume-bar::after {
  height: 40px;
  animation-delay: 0.2s;
}
@keyframes volume-bounce {
  0%, 100% { transform: scaleY(0.3); }
  50% { transform: scaleY(1); }
}');

-- 307 目标锁定
INSERT INTO css_animations (title, category, description, css_code) VALUES ('目标锁定', '大屏动画', '瞄准镜锁定效果', '.target-lock {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.target-lock::before {
  content: "";
  position: absolute;
  width: 80px;
  height: 80px;
  border: 3px solid #00f0ff;
  border-radius: 50%;
  animation: target-pulse 1.5s ease-in-out infinite;
}
.target-lock::after {
  content: "+";
  font-size: 40px;
  color: #00f0ff;
  font-weight: 100;
  text-shadow: 0 0 10px #00f0ff;
  animation: target-rotate 4s linear infinite;
}
@keyframes target-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.7; }
}
@keyframes target-rotate {
  to { transform: rotate(360deg); }
}');
