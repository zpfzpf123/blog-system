// 批量添加更多全新大屏动画
const API = 'http://localhost:4567/api/animations';

const bigscreenAnimations = [
  {
    title: '代码雨',
    category: '大屏动画',
    description: '黑客帝国风格的代码下落效果',
    cssCode: `.code-rain {
  width: 160px;
  height: 120px;
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  font-family: monospace;
}
.code-rain::before {
  content: "10101 01010 11001 00110 10101 01010";
  position: absolute;
  top: -100%;
  left: 0;
  right: 0;
  height: 200%;
  color: #00ff00;
  font-size: 10px;
  line-height: 1.5;
  text-align: center;
  animation: code-fall 3s linear infinite;
  text-shadow: 0 0 5px #00ff00;
}
@keyframes code-fall {
  0% { transform: translateY(0); }
  100% { transform: translateY(50%); }
}`
  },
  {
    title: '脑电波',
    category: '大屏动画',
    description: '脑电波信号监测动画',
    cssCode: `.brain-wave {
  width: 180px;
  height: 80px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(168, 85, 247, 0.3);
  overflow: hidden;
}
.brain-wave::before {
  content: "";
  position: absolute;
  top: 50%;
  left: -100%;
  width: 200%;
  height: 3px;
  background: linear-gradient(90deg, 
    transparent 0%, #a855f7 20%, #ec4899 40%, 
    #a855f7 60%, transparent 80%);
  animation: brain-scan 2s linear infinite;
  box-shadow: 0 0 15px #a855f7;
}
.brain-wave::after {
  content: "BRAIN ACTIVITY";
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: #a855f7;
  letter-spacing: 1px;
}
@keyframes brain-scan {
  0% { transform: translateX(0) translateY(-50%); }
  100% { transform: translateX(50%) translateY(-50%); }
}`
  },
  {
    title: '声纹识别',
    category: '大屏动画',
    description: '声纹波形识别动画',
    cssCode: `.voice-print {
  width: 150px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 3px;
  border: 1px solid rgba(0, 240, 255, 0.2);
}
.voice-print::before,
.voice-print::after {
  content: "";
  width: 4px;
  background: linear-gradient(to top, #00f0ff, #a855f7);
  border-radius: 2px;
  animation: voice-bar 0.4s ease-in-out infinite alternate;
}
.voice-print::before {
  height: 60px;
  animation-delay: 0s;
}
.voice-print::after {
  height: 40px;
  animation-delay: 0.1s;
}
@keyframes voice-bar {
  0% { transform: scaleY(0.3); }
  100% { transform: scaleY(1); }
}`
  },
  {
    title: '虚拟键盘',
    category: '大屏动画',
    description: '科技感虚拟键盘输入效果',
    cssCode: `.virtual-keyboard {
  width: 160px;
  height: 80px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 4px;
  padding: 10px;
}
.virtual-keyboard::before {
  content: "";
  position: absolute;
  width: 20px;
  height: 20px;
  background: rgba(0, 240, 255, 0.3);
  border-radius: 4px;
  animation: key-press 1s steps(6) infinite;
  box-shadow: 0 0 10px #00f0ff;
}
@keyframes key-press {
  0% { top: 10px; left: 10px; }
  16% { top: 10px; left: 35px; }
  33% { top: 10px; left: 60px; }
  50% { top: 40px; left: 85px; }
  66% { top: 40px; left: 110px; }
  83% { top: 40px; left: 135px; }
  100% { top: 10px; left: 10px; }
}`
  },
  {
    title: '人脸识别',
    category: '大屏动画',
    description: '人脸扫描识别框动画',
    cssCode: `.face-scan {
  width: 100px;
  height: 120px;
  position: relative;
  border: 2px solid #00f0ff;
  border-radius: 50% 50% 45% 45%;
  background: rgba(0, 240, 255, 0.05);
}
.face-scan::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
  animation: face-scan-line 2s linear infinite;
  box-shadow: 0 0 15px #00f0ff;
}
.face-scan::after {
  content: "SCANNING...";
  position: absolute;
  bottom: -25px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: #00f0ff;
  animation: blink 1s infinite;
}
@keyframes face-scan-line {
  0%, 100% { top: 0; }
  50% { top: calc(100% - 3px); }
}
@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}`
  },
  {
    title: '数据同步',
    category: '大屏动画',
    description: '双向数据同步动画',
    cssCode: `.data-sync {
  width: 150px;
  height: 80px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
}
.data-sync::before,
.data-sync::after {
  content: "";
  width: 40px;
  height: 40px;
  background: #0a1628;
  border: 2px solid #00f0ff;
  border-radius: 8px;
  box-shadow: 0 0 10px rgba(0, 240, 255, 0.3);
}
@keyframes sync-pulse {
  0%, 100% { transform: scale(1); box-shadow: 0 0 10px rgba(0, 240, 255, 0.3); }
  50% { transform: scale(1.1); box-shadow: 0 0 20px rgba(0, 240, 255, 0.6); }
}`
  },
  {
    title: '网络拓扑',
    category: '大屏动画',
    description: '网络节点连接拓扑图',
    cssCode: `.network-topology {
  width: 140px;
  height: 140px;
  position: relative;
}
.network-topology::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 30px;
  height: 30px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 20px #00f0ff;
  animation: node-pulse 2s ease-in-out infinite;
}
.network-topology::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100px;
  height: 100px;
  border: 2px dashed rgba(0, 240, 255, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: topology-rotate 8s linear infinite;
  box-shadow:
    inset 50px 0 0 -42px #00ff88,
    inset -50px 0 0 -42px #a855f7,
    inset 0 50px 0 -42px #ec4899;
}
@keyframes node-pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.2); }
}
@keyframes topology-rotate {
  to { transform: translate(-50%, -50%) rotate(360deg); }
}`
  },
  {
    title: '云端存储',
    category: '大屏动画',
    description: '云端数据上传下载动画',
    cssCode: `.cloud-storage {
  width: 120px;
  height: 100px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.cloud-storage::before {
  content: "☁️";
  font-size: 50px;
  filter: drop-shadow(0 0 10px #00f0ff);
  animation: cloud-float 3s ease-in-out infinite;
}
.cloud-storage::after {
  content: "↑↓";
  position: absolute;
  bottom: 10px;
  font-size: 20px;
  color: #00f0ff;
  animation: arrow-blink 1s infinite;
  text-shadow: 0 0 10px #00f0ff;
}
@keyframes cloud-float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
@keyframes arrow-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}`
  },
  {
    title: '安全验证',
    category: '大屏动画',
    description: '安全验证通过动画',
    cssCode: `.security-check {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.security-check::before {
  content: "✓";
  font-size: 50px;
  color: #00ff88;
  text-shadow: 0 0 20px #00ff88;
  animation: check-pop 2s ease-in-out infinite;
}
.security-check::after {
  content: "";
  position: absolute;
  width: 80px;
  height: 80px;
  border: 3px solid #00ff88;
  border-radius: 50%;
  animation: ring-expand 2s ease-out infinite;
}
@keyframes check-pop {
  0%, 100% { transform: scale(1); }
  10% { transform: scale(1.3); }
  20% { transform: scale(1); }
}
@keyframes ring-expand {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}`
  },
  {
    title: '温度监控',
    category: '大屏动画',
    description: '温度数值监控显示',
    cssCode: `.temp-monitor {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.temp-monitor::before {
  content: "36.5°";
  font-size: 28px;
  font-weight: bold;
  color: #00ff88;
  text-shadow: 0 0 15px #00ff88;
  animation: temp-flicker 3s ease-in-out infinite;
}
.temp-monitor::after {
  content: "NORMAL";
  font-size: 12px;
  color: #00ff88;
  margin-top: 10px;
  padding: 4px 12px;
  border: 1px solid #00ff88;
  border-radius: 20px;
}
@keyframes temp-flicker {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}`
  },
  {
    title: '电量显示',
    category: '大屏动画',
    description: '电池电量百分比显示',
    cssCode: `.power-display {
  width: 80px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.power-display::before {
  content: "⚡";
  font-size: 40px;
  animation: power-flash 1s ease-in-out infinite;
}
.power-display::after {
  content: "85%";
  font-size: 24px;
  font-weight: bold;
  color: #00f0ff;
  text-shadow: 0 0 10px #00f0ff;
  margin-top: 10px;
}
@keyframes power-flash {
  0%, 100% { filter: drop-shadow(0 0 5px #ffcc00); transform: scale(1); }
  50% { filter: drop-shadow(0 0 20px #ffcc00); transform: scale(1.1); }
}`
  },
  {
    title: '速度仪表',
    category: '大屏动画',
    description: '速度指针仪表盘',
    cssCode: `.speed-gauge {
  width: 120px;
  height: 120px;
  position: relative;
  border-radius: 50%;
  background: conic-gradient(from 180deg, #00ff88 0deg, #00f0ff 90deg, #a855f7 180deg, transparent 180deg);
}
.speed-gauge::before {
  content: "";
  position: absolute;
  inset: 10px;
  background: #0a1628;
  border-radius: 50%;
}
.speed-gauge::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 4px;
  height: 40px;
  background: linear-gradient(to top, #ec4899, transparent);
  transform-origin: bottom center;
  transform: translate(-50%, -100%);
  animation: needle-swing 2s ease-in-out infinite;
  border-radius: 2px;
}
@keyframes needle-swing {
  0%, 100% { transform: translate(-50%, -100%) rotate(-60deg); }
  50% { transform: translate(-50%, -100%) rotate(60deg); }
}`
  },
  {
    title: '信号塔',
    category: '大屏动画',
    description: '信号发射塔动画',
    cssCode: `.signal-tower {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}
.signal-tower::before {
  content: "";
  width: 8px;
  height: 60px;
  background: linear-gradient(to top, #1a3a5c, #00f0ff);
  border-radius: 4px;
}
.signal-tower::after {
  content: "";
  position: absolute;
  top: 10px;
  width: 80px;
  height: 40px;
  border: 2px solid transparent;
  border-top-color: #00f0ff;
  border-radius: 50%;
  animation: signal-emit 1.5s ease-out infinite;
  opacity: 0;
}
@keyframes signal-emit {
  0% { transform: scale(0.3); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}`
  },
  {
    title: '数据库',
    category: '大屏动画',
    description: '数据库存储动画',
    cssCode: `.database-icon {
  width: 80px;
  height: 100px;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.database-icon::before,
.database-icon::after {
  content: "";
  width: 80px;
  height: 25px;
  background: linear-gradient(to bottom, #1a3a5c, #0d2137);
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: db-pulse 2s ease-in-out infinite;
}
.database-icon::after {
  animation-delay: 0.3s;
}
@keyframes db-pulse {
  0%, 100% { box-shadow: 0 0 5px rgba(0, 240, 255, 0.3); }
  50% { box-shadow: 0 0 15px rgba(0, 240, 255, 0.6); }
}`
  },
  {
    title: 'CPU监控',
    category: '大屏动画',
    description: 'CPU使用率监控动画',
    cssCode: `.cpu-monitor {
  width: 100px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border: 2px solid #00f0ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.cpu-monitor::before {
  content: "CPU";
  font-size: 14px;
  color: #00f0ff;
  position: absolute;
  top: 10px;
}
.cpu-monitor::after {
  content: "78%";
  font-size: 28px;
  font-weight: bold;
  color: #00ff88;
  text-shadow: 0 0 10px #00ff88;
  animation: cpu-update 3s steps(1) infinite;
}
@keyframes cpu-update {
  0% { content: "78%"; }
  33% { content: "82%"; }
  66% { content: "75%"; }
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
