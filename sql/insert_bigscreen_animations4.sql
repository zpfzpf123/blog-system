-- 插入10个新的大屏动画

-- 1. 水波纹
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
  animation: water-spread 3s ease-out infinite;
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
@keyframes water-spread {
  0% { transform: scale(1); opacity: 1; border-width: 3px; }
  100% { transform: scale(4); opacity: 0; border-width: 1px; }
}');

-- 2. 心电图
INSERT INTO css_animations (title, category, description, css_code) VALUES ('心电图', '大屏动画', '医疗监护心电图波形', '.heartbeat {
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
  height: 100%;
  background: linear-gradient(90deg, 
    transparent 0%, transparent 10%,
    #00ff88 10%, #00ff88 10.5%,
    transparent 10.5%, transparent 20%,
    #00ff88 20%, #00ff88 22%,
    transparent 22%, transparent 25%,
    #00ff88 25%, #00ff88 30%,
    transparent 30%, transparent 50%
  );
  background-size: 100px 100%;
  animation: heartbeat-move 2s linear infinite;
}
@keyframes heartbeat-move {
  0% { transform: translateX(0); }
  100% { transform: translateX(-200px); }
}');

-- 3. 指纹扫描
INSERT INTO css_animations (title, category, description, css_code) VALUES ('指纹扫描', '大屏动画', '生物识别指纹扫描效果', '.fingerprint {
  width: 100px;
  height: 120px;
  position: relative;
  background: #0a1628;
  border-radius: 50px 50px 8px 8px;
  border: 2px solid rgba(0, 200, 255, 0.3);
  overflow: hidden;
}
.fingerprint::before {
  content: "";
  position: absolute;
  inset: 10px;
  background: repeating-linear-gradient(
    0deg,
    transparent 0px,
    transparent 3px,
    rgba(0, 240, 255, 0.2) 3px,
    rgba(0, 240, 255, 0.2) 4px
  );
  border-radius: 40px 40px 4px 4px;
}
.fingerprint::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
  box-shadow: 0 0 20px #00f0ff;
  animation: finger-scan 2s ease-in-out infinite;
}
@keyframes finger-scan {
  0%, 100% { top: 10px; }
  50% { top: calc(100% - 14px); }
}');

-- 4. 齿轮转动
INSERT INTO css_animations (title, category, description, css_code) VALUES ('齿轮转动', '大屏动画', '机械齿轮联动旋转', '.gear-spin {
  width: 80px;
  height: 80px;
  position: relative;
}
.gear-spin::before {
  content: "";
  position: absolute;
  width: 60px;
  height: 60px;
  border: 8px dashed #00f0ff;
  border-radius: 50%;
  animation: gear-rotate 3s linear infinite;
}
.gear-spin::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 20px;
  height: 20px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 15px #00f0ff;
}
@keyframes gear-rotate {
  to { transform: rotate(360deg); }
}');

-- 5. 闪电效果
INSERT INTO css_animations (title, category, description, css_code) VALUES ('闪电效果', '大屏动画', '电流闪电闪烁效果', '.lightning {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80px;
  color: #00f0ff;
  text-shadow: 0 0 20px #00f0ff, 0 0 40px #00f0ff;
  animation: lightning-flash 2s ease-in-out infinite;
}
.lightning::before {
  content: "⚡";
}
.lightning::after {
  content: "";
  position: absolute;
  inset: 0;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.2) 0%, transparent 70%);
  animation: lightning-glow 2s ease-in-out infinite;
}
@keyframes lightning-flash {
  0%, 100% { opacity: 1; }
  10% { opacity: 0.3; }
  20% { opacity: 1; }
  30% { opacity: 0.5; }
  40% { opacity: 1; }
}
@keyframes lightning-glow {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}');

-- 6. 时钟指针
INSERT INTO css_animations (title, category, description, css_code) VALUES ('时钟指针', '大屏动画', '科技感时钟指针转动', '.clock-hand {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid rgba(0, 200, 255, 0.3);
  position: relative;
  background: #0a1628;
}
.clock-hand::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 3px;
  height: 35px;
  background: #00f0ff;
  transform-origin: bottom center;
  transform: translateX(-50%);
  animation: clock-tick 4s linear infinite;
  box-shadow: 0 0 10px #00f0ff;
}
.clock-hand::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 10px;
  height: 10px;
  background: #00ff88;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 10px #00ff88;
}
@keyframes clock-tick {
  to { transform: translateX(-50%) rotate(360deg); }
}');

-- 7. 目标锁定
INSERT INTO css_animations (title, category, description, css_code) VALUES ('目标锁定', '大屏动画', '瞄准镜目标锁定效果', '.target-lock {
  width: 100px;
  height: 100px;
  position: relative;
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: target-pulse 2s ease-in-out infinite;
}
.target-lock::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #00f0ff 30%, transparent 30%, transparent 70%, #00f0ff 70%);
}
.target-lock::after {
  content: "";
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, #00f0ff 30%, transparent 30%, transparent 70%, #00f0ff 70%);
}
@keyframes target-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(0.9); opacity: 0.7; }
}');

-- 8. 数据上传
INSERT INTO css_animations (title, category, description, css_code) VALUES ('数据上传', '大屏动画', '数据向上传输动画', '.data-upload {
  width: 80px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}
.data-upload::before {
  content: "";
  width: 60px;
  height: 40px;
  border: 3px solid #00f0ff;
  border-radius: 8px;
  border-bottom: none;
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
}
.data-upload::after {
  content: "";
  width: 8px;
  height: 8px;
  background: #00ff88;
  border-radius: 50%;
  position: absolute;
  bottom: 50px;
  animation: upload-move 1s ease-in-out infinite;
  box-shadow: 0 0 10px #00ff88;
}
@keyframes upload-move {
  0% { bottom: 10px; opacity: 1; }
  100% { bottom: 80px; opacity: 0; }
}');

-- 9. 声波扩散
INSERT INTO css_animations (title, category, description, css_code) VALUES ('声波扩散', '大屏动画', '声音波纹扩散效果', '.sound-wave {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.sound-wave::before {
  content: "";
  position: absolute;
  width: 30px;
  height: 30px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 20px #00f0ff;
}
.sound-wave::after {
  content: "";
  position: absolute;
  width: 30px;
  height: 30px;
  border: 3px solid #00f0ff;
  border-radius: 50%;
  animation: sound-expand 1.5s ease-out infinite;
  box-shadow: 
    0 0 0 15px rgba(0, 240, 255, 0.3),
    0 0 0 30px rgba(0, 240, 255, 0.2),
    0 0 0 45px rgba(0, 240, 255, 0.1);
}
@keyframes sound-expand {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(2.5); opacity: 0; }
}');

-- 10. 温度计
INSERT INTO css_animations (title, category, description, css_code) VALUES ('温度计', '大屏动画', '温度指示器动画效果', '.thermometer {
  width: 40px;
  height: 120px;
  position: relative;
  background: #0a1628;
  border: 2px solid rgba(0, 200, 255, 0.3);
  border-radius: 20px 20px 40px 40px;
  overflow: hidden;
}
.thermometer::before {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 30px;
  height: 30px;
  background: #ec4899;
  border-radius: 50%;
  box-shadow: 0 0 15px #ec4899;
}
.thermometer::after {
  content: "";
  position: absolute;
  bottom: 25px;
  left: 50%;
  transform: translateX(-50%);
  width: 12px;
  background: linear-gradient(to top, #ec4899, #00f0ff);
  border-radius: 6px;
  animation: temp-rise 3s ease-in-out infinite;
}
@keyframes temp-rise {
  0%, 100% { height: 30px; }
  50% { height: 70px; }
}');
