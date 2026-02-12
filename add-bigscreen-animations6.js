// 批量添加更多全新大屏动画
const API = 'http://localhost:4567/api/animations';

const bigscreenAnimations = [
  {
    title: '指纹解锁',
    category: '大屏动画',
    description: '指纹识别解锁动画',
    cssCode: `.fingerprint-unlock {
  width: 80px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.fingerprint-unlock::before {
  content: "";
  width: 60px;
  height: 80px;
  background: repeating-linear-gradient(
    0deg,
    transparent 0px,
    transparent 3px,
    rgba(0, 240, 255, 0.3) 3px,
    rgba(0, 240, 255, 0.3) 4px
  );
  border-radius: 30px 30px 40px 40px;
  animation: fp-scan 2s ease-in-out infinite;
}
.fingerprint-unlock::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: #00f0ff;
  box-shadow: 0 0 15px #00f0ff;
  animation: fp-line 2s ease-in-out infinite;
}
@keyframes fp-scan {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}
@keyframes fp-line {
  0%, 100% { top: 0; }
  50% { top: calc(100% - 4px); }
}`
  },
  {
    title: '虹膜扫描',
    category: '大屏动画',
    description: '虹膜识别扫描动画',
    cssCode: `.iris-scan {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 50%;
  background: radial-gradient(circle, #0a1628 30%, #1a3a5c 60%, #0a1628 100%);
  border: 3px solid #00f0ff;
  overflow: hidden;
}
.iris-scan::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 30px;
  height: 30px;
  background: #000;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 0 8px #00f0ff, 0 0 0 12px rgba(0, 240, 255, 0.3);
}
.iris-scan::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00ff88, transparent);
  animation: iris-line 1.5s linear infinite;
  box-shadow: 0 0 10px #00ff88;
}
@keyframes iris-line {
  0% { top: 0; }
  100% { top: 100%; }
}`
  },
  {
    title: '心率监测',
    category: '大屏动画',
    description: '实时心率数值显示',
    cssCode: `.heart-rate {
  width: 140px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(236, 72, 153, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.heart-rate::before {
  content: "❤️";
  font-size: 30px;
  animation: heart-beat 1s ease-in-out infinite;
}
.heart-rate::after {
  content: "72 BPM";
  font-size: 20px;
  font-weight: bold;
  color: #ec4899;
  text-shadow: 0 0 10px #ec4899;
  margin-top: 5px;
}
@keyframes heart-beat {
  0%, 100% { transform: scale(1); }
  15% { transform: scale(1.3); }
  30% { transform: scale(1); }
  45% { transform: scale(1.2); }
  60% { transform: scale(1); }
}`
  },
  {
    title: '血氧饱和',
    category: '大屏动画',
    description: '血氧饱和度显示',
    cssCode: `.oxygen-level {
  width: 120px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.oxygen-level::before {
  content: "SpO₂";
  font-size: 14px;
  color: #64748b;
  margin-bottom: 5px;
}
.oxygen-level::after {
  content: "98%";
  font-size: 36px;
  font-weight: bold;
  color: #00f0ff;
  text-shadow: 0 0 15px #00f0ff;
  animation: oxy-pulse 2s ease-in-out infinite;
}
@keyframes oxy-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}`
  },
  {
    title: '步数统计',
    category: '大屏动画',
    description: '运动步数统计显示',
    cssCode: `.step-counter {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.step-counter::before {
  content: "👟";
  font-size: 35px;
  animation: step-walk 0.5s ease-in-out infinite alternate;
}
.step-counter::after {
  content: "8,542";
  font-size: 24px;
  font-weight: bold;
  color: #00ff88;
  text-shadow: 0 0 10px #00ff88;
  margin-top: 8px;
}
@keyframes step-walk {
  0% { transform: translateX(-5px) rotate(-10deg); }
  100% { transform: translateX(5px) rotate(10deg); }
}`
  },
  {
    title: '卡路里',
    category: '大屏动画',
    description: '卡路里消耗显示',
    cssCode: `.calorie-burn {
  width: 120px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(255, 136, 0, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.calorie-burn::before {
  content: "🔥";
  font-size: 30px;
  animation: flame-dance 0.3s ease-in-out infinite alternate;
}
.calorie-burn::after {
  content: "486 kcal";
  font-size: 18px;
  font-weight: bold;
  color: #ff8800;
  text-shadow: 0 0 10px #ff8800;
  margin-top: 5px;
}
@keyframes flame-dance {
  0% { transform: scale(1) rotate(-5deg); }
  100% { transform: scale(1.1) rotate(5deg); }
}`
  },
  {
    title: '天气图标',
    category: '大屏动画',
    description: '动态天气图标显示',
    cssCode: `.weather-icon {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.weather-icon::before {
  content: "☀️";
  font-size: 50px;
  animation: sun-rotate 10s linear infinite;
  filter: drop-shadow(0 0 15px #ffcc00);
}
.weather-icon::after {
  content: "28°C";
  font-size: 20px;
  font-weight: bold;
  color: #ffcc00;
  margin-top: 8px;
}
@keyframes sun-rotate {
  to { transform: rotate(360deg); }
}`
  },
  {
    title: '风速指示',
    category: '大屏动画',
    description: '风速风向指示器',
    cssCode: `.wind-speed {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.wind-speed::before {
  content: "💨";
  font-size: 40px;
  animation: wind-blow 1s ease-in-out infinite;
}
.wind-speed::after {
  content: "12 km/h";
  position: absolute;
  bottom: 10px;
  font-size: 14px;
  font-weight: bold;
  color: #00f0ff;
}
@keyframes wind-blow {
  0%, 100% { transform: translateX(0) scale(1); }
  50% { transform: translateX(10px) scale(1.1); }
}`
  },
  {
    title: '湿度计',
    category: '大屏动画',
    description: '空气湿度显示',
    cssCode: `.humidity-gauge {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.humidity-gauge::before {
  content: "💧";
  font-size: 40px;
  animation: drop-bounce 2s ease-in-out infinite;
  filter: drop-shadow(0 0 10px #00f0ff);
}
.humidity-gauge::after {
  content: "65%";
  font-size: 24px;
  font-weight: bold;
  color: #00f0ff;
  text-shadow: 0 0 10px #00f0ff;
  margin-top: 8px;
}
@keyframes drop-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}`
  },
  {
    title: '气压表',
    category: '大屏动画',
    description: '大气压力显示',
    cssCode: `.pressure-meter {
  width: 120px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(168, 85, 247, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.pressure-meter::before {
  content: "hPa";
  font-size: 12px;
  color: #64748b;
}
.pressure-meter::after {
  content: "1013";
  font-size: 32px;
  font-weight: bold;
  color: #a855f7;
  text-shadow: 0 0 15px #a855f7;
  animation: pressure-flicker 3s ease-in-out infinite;
}
@keyframes pressure-flicker {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}`
  },
  {
    title: '紫外线',
    category: '大屏动画',
    description: '紫外线指数显示',
    cssCode: `.uv-index {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.uv-index::before {
  content: "";
  width: 60px;
  height: 60px;
  background: radial-gradient(circle, #a855f7 0%, transparent 70%);
  border-radius: 50%;
  animation: uv-glow 2s ease-in-out infinite;
}
.uv-index::after {
  content: "UV 6";
  position: absolute;
  font-size: 18px;
  font-weight: bold;
  color: #a855f7;
  text-shadow: 0 0 10px #a855f7;
}
@keyframes uv-glow {
  0%, 100% { transform: scale(1); opacity: 0.8; }
  50% { transform: scale(1.2); opacity: 1; }
}`
  },
  {
    title: '日出日落',
    category: '大屏动画',
    description: '日出日落时间显示',
    cssCode: `.sunrise-sunset {
  width: 160px;
  height: 80px;
  position: relative;
  background: linear-gradient(to top, #0a1628 50%, #1a3a5c 100%);
  border-radius: 8px;
  overflow: hidden;
}
.sunrise-sunset::before {
  content: "";
  position: absolute;
  bottom: 20px;
  left: 20px;
  width: 30px;
  height: 30px;
  background: radial-gradient(circle, #ffcc00, #ff8800);
  border-radius: 50%;
  box-shadow: 0 0 20px #ffcc00;
  animation: sun-rise 4s ease-in-out infinite;
}
.sunrise-sunset::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 20px;
  background: #0a1628;
}
@keyframes sun-rise {
  0%, 100% { transform: translateX(0) translateY(0); }
  50% { transform: translateX(100px) translateY(-30px); }
}`
  },
  {
    title: '月相显示',
    category: '大屏动画',
    description: '月亮相位显示',
    cssCode: `.moon-phase {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.moon-phase::before {
  content: "";
  width: 70px;
  height: 70px;
  background: linear-gradient(90deg, #f0f0f0 50%, transparent 50%);
  border-radius: 50%;
  box-shadow: inset -5px 0 10px rgba(0, 0, 0, 0.3), 0 0 20px rgba(240, 240, 240, 0.3);
  animation: moon-glow 3s ease-in-out infinite;
}
.moon-phase::after {
  content: "🌙";
  position: absolute;
  font-size: 12px;
  bottom: 5px;
  color: #94a3b8;
}
@keyframes moon-glow {
  0%, 100% { box-shadow: inset -5px 0 10px rgba(0, 0, 0, 0.3), 0 0 20px rgba(240, 240, 240, 0.3); }
  50% { box-shadow: inset -5px 0 10px rgba(0, 0, 0, 0.3), 0 0 30px rgba(240, 240, 240, 0.5); }
}`
  },
  {
    title: '潮汐指示',
    category: '大屏动画',
    description: '潮汐高低显示',
    cssCode: `.tide-indicator {
  width: 120px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(0, 240, 255, 0.2);
}
.tide-indicator::before {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40%;
  background: linear-gradient(to top, #00f0ff, rgba(0, 240, 255, 0.3));
  animation: tide-wave 3s ease-in-out infinite;
}
.tide-indicator::after {
  content: "HIGH";
  position: absolute;
  top: 15px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 14px;
  font-weight: bold;
  color: #00f0ff;
}
@keyframes tide-wave {
  0%, 100% { height: 30%; }
  50% { height: 60%; }
}`
  },
  {
    title: '空气质量',
    category: '大屏动画',
    description: 'AQI空气质量指数',
    cssCode: `.air-quality {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.air-quality::before {
  content: "AQI";
  font-size: 14px;
  color: #64748b;
  margin-bottom: 5px;
}
.air-quality::after {
  content: "42";
  font-size: 42px;
  font-weight: bold;
  color: #00ff88;
  text-shadow: 0 0 20px #00ff88;
  animation: aqi-glow 2s ease-in-out infinite;
}
@keyframes aqi-glow {
  0%, 100% { text-shadow: 0 0 20px #00ff88; }
  50% { text-shadow: 0 0 30px #00ff88, 0 0 40px #00ff88; }
}`
  }
];

async function addAnimations() {
  console.log('开始添加更多大屏动画...');
  
  for (const anim of bigscreenAnimations) {
    try {
      const res = await fetch(API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(anim)
      });
      
      if (res.ok) {
        const data = await res.json();
        console.log(`✅ 添加成功: ${anim.title} (ID: ${data.id})`);
      } else {
        console.log(`❌ 添加失败: ${anim.title} - ${res.status}`);
      }
    } catch (e) {
      console.log(`❌ 添加失败: ${anim.title} - ${e.message}`);
    }
  }
  
  console.log('完成!');
}

addAnimations();
