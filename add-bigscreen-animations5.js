// 批量添加更多全新大屏动画
const API = 'http://localhost:4567/api/animations';

const bigscreenAnimations = [
  {
    title: '内存条',
    category: '大屏动画',
    description: '内存使用率动态显示',
    cssCode: `.memory-bar {
  width: 160px;
  height: 40px;
  position: relative;
  background: #0a1628;
  border: 2px solid #00f0ff;
  border-radius: 6px;
  overflow: hidden;
}
.memory-bar::before {
  content: "";
  position: absolute;
  top: 4px;
  left: 4px;
  bottom: 4px;
  width: 0%;
  background: linear-gradient(90deg, #00f0ff, #00ff88);
  border-radius: 3px;
  animation: memory-fill 3s ease-in-out infinite;
  box-shadow: 0 0 10px #00f0ff;
}
.memory-bar::after {
  content: "RAM 64%";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 0 5px #000;
  z-index: 1;
}
@keyframes memory-fill {
  0%, 100% { width: 40%; }
  50% { width: 80%; }
}`
  },
  {
    title: '磁盘扫描',
    category: '大屏动画',
    description: '磁盘扫描进度动画',
    cssCode: `.disk-scan {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 50%;
  background: conic-gradient(#00f0ff 0deg, transparent 0deg);
  animation: disk-progress 3s linear infinite;
}
.disk-scan::before {
  content: "";
  position: absolute;
  inset: 8px;
  background: #0a1628;
  border-radius: 50%;
}
.disk-scan::after {
  content: "SCAN";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 14px;
  font-weight: bold;
  color: #00f0ff;
}
@keyframes disk-progress {
  0% { background: conic-gradient(#00f0ff 0deg, transparent 0deg); }
  25% { background: conic-gradient(#00f0ff 90deg, transparent 90deg); }
  50% { background: conic-gradient(#00f0ff 180deg, transparent 180deg); }
  75% { background: conic-gradient(#00f0ff 270deg, transparent 270deg); }
  100% { background: conic-gradient(#00f0ff 360deg, transparent 360deg); }
}`
  },
  {
    title: '网速监测',
    category: '大屏动画',
    description: '网络速度实时监测',
    cssCode: `.network-speed {
  width: 140px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.network-speed::before {
  content: "↓ 125 Mb/s";
  font-size: 16px;
  font-weight: bold;
  color: #00ff88;
  text-shadow: 0 0 10px #00ff88;
  animation: speed-update 2s steps(1) infinite;
}
.network-speed::after {
  content: "↑ 48 Mb/s";
  font-size: 14px;
  color: #00f0ff;
  text-shadow: 0 0 8px #00f0ff;
}
@keyframes speed-update {
  0% { content: "↓ 125 Mb/s"; }
  33% { content: "↓ 132 Mb/s"; }
  66% { content: "↓ 118 Mb/s"; }
}`
  },
  {
    title: '服务器状态',
    category: '大屏动画',
    description: '服务器在线状态指示',
    cssCode: `.server-status {
  width: 120px;
  height: 80px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 255, 136, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.server-status::before {
  content: "";
  width: 12px;
  height: 12px;
  background: #00ff88;
  border-radius: 50%;
  box-shadow: 0 0 10px #00ff88, 0 0 20px #00ff88;
  animation: status-blink 2s ease-in-out infinite;
}
.server-status::after {
  content: "ONLINE";
  font-size: 14px;
  font-weight: bold;
  color: #00ff88;
  letter-spacing: 2px;
}
@keyframes status-blink {
  0%, 100% { opacity: 1; box-shadow: 0 0 10px #00ff88, 0 0 20px #00ff88; }
  50% { opacity: 0.6; box-shadow: 0 0 5px #00ff88; }
}`
  },
  {
    title: '时间戳',
    category: '大屏动画',
    description: '动态时间戳显示',
    cssCode: `.timestamp {
  width: 160px;
  height: 60px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: monospace;
}
.timestamp::before {
  content: "12:34:56";
  font-size: 28px;
  font-weight: bold;
  color: #00f0ff;
  text-shadow: 0 0 15px #00f0ff;
  animation: time-tick 1s steps(1) infinite;
}
@keyframes time-tick {
  0% { content: "12:34:56"; }
  50% { content: "12:34:57"; }
}`
  },
  {
    title: '坐标定位',
    category: '大屏动画',
    description: 'GPS坐标定位动画',
    cssCode: `.gps-location {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.gps-location::before {
  content: "📍";
  font-size: 40px;
  animation: pin-bounce 1s ease-in-out infinite;
  filter: drop-shadow(0 0 10px #ec4899);
}
.gps-location::after {
  content: "";
  position: absolute;
  bottom: 20px;
  width: 40px;
  height: 10px;
  background: rgba(236, 72, 153, 0.3);
  border-radius: 50%;
  animation: pin-shadow 1s ease-in-out infinite;
}
@keyframes pin-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}
@keyframes pin-shadow {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(0.6); opacity: 0.3; }
}`
  },
  {
    title: '二维码',
    category: '大屏动画',
    description: '二维码扫描动画',
    cssCode: `.qr-code {
  width: 100px;
  height: 100px;
  position: relative;
  background: #fff;
  border-radius: 8px;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 2px;
  padding: 8px;
}
.qr-code::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #00f0ff, transparent);
  animation: qr-scan 2s linear infinite;
  box-shadow: 0 0 10px #00f0ff;
}
@keyframes qr-scan {
  0% { top: 0; }
  100% { top: 100%; }
}`
  },
  {
    title: '蓝牙连接',
    category: '大屏动画',
    description: '蓝牙设备连接动画',
    cssCode: `.bluetooth {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.bluetooth::before {
  content: "";
  width: 40px;
  height: 60px;
  background: transparent;
  border: 3px solid #00f0ff;
  clip-path: polygon(50% 0%, 100% 25%, 50% 50%, 100% 75%, 50% 100%, 50% 60%, 0% 75%, 50% 50%, 0% 25%, 50% 40%);
  animation: bt-pulse 2s ease-in-out infinite;
  filter: drop-shadow(0 0 10px #00f0ff);
}
.bluetooth::after {
  content: "";
  position: absolute;
  width: 80px;
  height: 80px;
  border: 2px solid rgba(0, 240, 255, 0.3);
  border-radius: 50%;
  animation: bt-wave 1.5s ease-out infinite;
}
@keyframes bt-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}
@keyframes bt-wave {
  0% { transform: scale(0.5); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}`
  },
  {
    title: 'WiFi信号',
    category: '大屏动画',
    description: 'WiFi信号强度动画',
    cssCode: `.wifi-signal {
  width: 80px;
  height: 80px;
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}
.wifi-signal::before {
  content: "";
  width: 10px;
  height: 10px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 10px #00f0ff;
}
.wifi-signal::after {
  content: "";
  position: absolute;
  bottom: 15px;
  width: 60px;
  height: 45px;
  border: 3px solid transparent;
  border-top-color: #00f0ff;
  border-radius: 50% 50% 0 0;
  box-shadow: 
    0 -15px 0 -12px #00f0ff,
    0 -30px 0 -9px #00f0ff;
  animation: wifi-pulse 1.5s ease-in-out infinite;
}
@keyframes wifi-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}`
  },
  {
    title: '音频播放',
    category: '大屏动画',
    description: '音频播放进度条',
    cssCode: `.audio-player {
  width: 180px;
  height: 60px;
  position: relative;
  background: #0a1628;
  border-radius: 30px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  display: flex;
  align-items: center;
  padding: 0 15px;
  gap: 10px;
}
.audio-player::before {
  content: "▶";
  font-size: 16px;
  color: #00f0ff;
}
.audio-player::after {
  content: "";
  flex: 1;
  height: 4px;
  background: rgba(0, 240, 255, 0.2);
  border-radius: 2px;
  position: relative;
  overflow: hidden;
}
@keyframes progress-move {
  0% { width: 0%; }
  100% { width: 100%; }
}`
  },
  {
    title: '视频帧',
    category: '大屏动画',
    description: '视频帧播放动画',
    cssCode: `.video-frame {
  width: 160px;
  height: 100px;
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #1a3a5c;
}
.video-frame::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(45deg, 
    rgba(0, 240, 255, 0.1) 25%, transparent 25%,
    transparent 75%, rgba(0, 240, 255, 0.1) 75%);
  background-size: 20px 20px;
  animation: video-noise 0.5s steps(2) infinite;
}
.video-frame::after {
  content: "▶";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 30px;
  color: rgba(255, 255, 255, 0.8);
  text-shadow: 0 0 20px #00f0ff;
}
@keyframes video-noise {
  0% { background-position: 0 0; }
  100% { background-position: 20px 20px; }
}`
  },
  {
    title: '3D立方体',
    category: '大屏动画',
    description: '3D旋转立方体效果',
    cssCode: `.cube-3d-rotate {
  width: 80px;
  height: 80px;
  position: relative;
  transform-style: preserve-3d;
  animation: cube-spin 4s linear infinite;
}
.cube-3d-rotate::before,
.cube-3d-rotate::after {
  content: "";
  position: absolute;
  width: 100%;
  height: 100%;
  border: 2px solid #00f0ff;
  background: rgba(0, 240, 255, 0.1);
}
.cube-3d-rotate::before {
  transform: translateZ(40px);
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.5);
}
.cube-3d-rotate::after {
  transform: translateZ(-40px);
}
@keyframes cube-spin {
  0% { transform: rotateX(0deg) rotateY(0deg); }
  100% { transform: rotateX(360deg) rotateY(360deg); }
}`
  },
  {
    title: '数据饼图',
    category: '大屏动画',
    description: '动态数据饼图',
    cssCode: `.pie-chart {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 50%;
  background: conic-gradient(
    #00f0ff 0deg 120deg,
    #00ff88 120deg 200deg,
    #a855f7 200deg 280deg,
    #ec4899 280deg 360deg
  );
  animation: pie-rotate 8s linear infinite;
}
.pie-chart::before {
  content: "";
  position: absolute;
  inset: 20px;
  background: #0a1628;
  border-radius: 50%;
}
.pie-chart::after {
  content: "DATA";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 12px;
  font-weight: bold;
  color: #fff;
}
@keyframes pie-rotate {
  to { transform: rotate(360deg); }
}`
  },
  {
    title: '折线图',
    category: '大屏动画',
    description: '动态折线图表',
    cssCode: `.line-chart {
  width: 180px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  overflow: hidden;
}
.line-chart::before {
  content: "";
  position: absolute;
  inset: 10px;
  background: 
    linear-gradient(rgba(0, 240, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
}
.line-chart::after {
  content: "";
  position: absolute;
  bottom: 20px;
  left: 10px;
  width: 160px;
  height: 2px;
  background: linear-gradient(90deg, #00f0ff, #00ff88, #a855f7);
  clip-path: polygon(0% 100%, 10% 60%, 25% 80%, 40% 30%, 55% 50%, 70% 20%, 85% 40%, 100% 10%, 100% 100%);
  animation: chart-draw 2s ease-out infinite;
}
@keyframes chart-draw {
  0% { clip-path: polygon(0% 100%, 0% 100%, 0% 100%, 0% 100%, 0% 100%, 0% 100%, 0% 100%, 0% 100%, 0% 100%); }
  100% { clip-path: polygon(0% 100%, 10% 60%, 25% 80%, 40% 30%, 55% 50%, 70% 20%, 85% 40%, 100% 10%, 100% 100%); }
}`
  },
  {
    title: '柱状图',
    category: '大屏动画',
    description: '动态柱状图表',
    cssCode: `.bar-chart {
  width: 160px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 8px;
  padding: 15px;
}
.bar-chart::before,
.bar-chart::after {
  content: "";
  width: 25px;
  background: linear-gradient(to top, #00f0ff, #00ff88);
  border-radius: 4px 4px 0 0;
  animation: bar-grow 2s ease-out infinite;
}
.bar-chart::before {
  height: 60px;
  animation-delay: 0s;
}
.bar-chart::after {
  height: 45px;
  animation-delay: 0.2s;
}
@keyframes bar-grow {
  0% { transform: scaleY(0); }
  50%, 100% { transform: scaleY(1); }
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
