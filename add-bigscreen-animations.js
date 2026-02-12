// 批量添加大屏动画到数据库
const API = 'http://localhost:4567/api/animations';

const bigscreenAnimations = [
  {
    title: '科技边框',
    category: '大屏动画',
    description: '带有流光效果的科技感边框面板',
    cssCode: `.tech-border {
  width: 180px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
}
.tech-border::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 8px;
  padding: 2px;
  background: linear-gradient(90deg, #00f0ff, #00ff88, #a855f7, #00f0ff);
  background-size: 300% 100%;
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  animation: border-flow 3s linear infinite;
}
.tech-border::after {
  content: "PANEL";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #00f0ff;
  font-size: 14px;
  font-weight: bold;
  letter-spacing: 4px;
}
@keyframes border-flow {
  0% { background-position: 0% 50%; }
  100% { background-position: 300% 50%; }
}`
  },
  {
    title: '数据瀑布',
    category: '大屏动画',
    description: '垂直流动的数据瀑布效果',
    cssCode: `.data-waterfall {
  width: 150px;
  height: 120px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(0, 240, 255, 0.2);
}
.data-waterfall::before {
  content: "01001 10110 00101 11010 01101 10011";
  position: absolute;
  top: -100%;
  left: 10px;
  right: 10px;
  height: 200%;
  color: #00f0ff;
  font-family: monospace;
  font-size: 10px;
  line-height: 1.8;
  word-wrap: break-word;
  animation: waterfall-drop 4s linear infinite;
  text-shadow: 0 0 5px #00f0ff;
}
@keyframes waterfall-drop {
  0% { transform: translateY(0); }
  100% { transform: translateY(50%); }
}`
  },
  {
    title: '环形仪表',
    category: '大屏动画',
    description: '带有刻度的环形仪表盘动画',
    cssCode: `.ring-gauge {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  position: relative;
  background: conic-gradient(from 135deg, #00f0ff 0deg, #00ff88 180deg, transparent 180deg);
  animation: gauge-rotate 3s ease-in-out infinite alternate;
}
.ring-gauge::before {
  content: "";
  position: absolute;
  inset: 10px;
  background: #0a1628;
  border-radius: 50%;
}
.ring-gauge::after {
  content: "78%";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #00f0ff;
  font-size: 18px;
  font-weight: bold;
}
@keyframes gauge-rotate {
  0% { background: conic-gradient(from 135deg, #00f0ff 0deg, #00ff88 90deg, transparent 90deg); }
  100% { background: conic-gradient(from 135deg, #00f0ff 0deg, #00ff88 270deg, transparent 270deg); }
}`
  },
  {
    title: '粒子星云',
    category: '大屏动画',
    description: '漂浮的粒子星云效果',
    cssCode: `.particle-nebula {
  width: 150px;
  height: 150px;
  position: relative;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}
.particle-nebula::before,
.particle-nebula::after {
  content: "";
  position: absolute;
  width: 8px;
  height: 8px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 
    30px 20px 0 #00ff88,
    -20px 40px 0 #a855f7,
    50px -30px 0 #ec4899,
    -40px -20px 0 #00f0ff,
    60px 50px 0 #00ff88;
  animation: nebula-float 4s ease-in-out infinite;
}
.particle-nebula::after {
  animation-delay: -2s;
  opacity: 0.6;
}
@keyframes nebula-float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(10px, -10px) rotate(180deg); }
}`
  },
  {
    title: '数据隧道',
    category: '大屏动画',
    description: '3D透视数据隧道效果',
    cssCode: `.data-tunnel {
  width: 120px;
  height: 120px;
  position: relative;
  perspective: 200px;
}
.data-tunnel::before,
.data-tunnel::after {
  content: "";
  position: absolute;
  inset: 0;
  border: 2px solid #00f0ff;
  animation: tunnel-zoom 2s linear infinite;
}
.data-tunnel::after {
  animation-delay: -1s;
}
@keyframes tunnel-zoom {
  0% { transform: translateZ(-100px) scale(0.5); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translateZ(50px) scale(1.5); opacity: 0; }
}`
  },
  {
    title: '脉冲网格',
    category: '大屏动画',
    description: '脉冲扩散的网格背景',
    cssCode: `.pulse-grid {
  width: 150px;
  height: 100px;
  position: relative;
  background: 
    linear-gradient(rgba(0, 240, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  border-radius: 8px;
  overflow: hidden;
}
.pulse-grid::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(0, 240, 255, 0.3) 0%, transparent 50%);
  transform: translate(-50%, -50%);
  animation: grid-pulse 2s ease-out infinite;
}
@keyframes grid-pulse {
  0% { transform: translate(-50%, -50%) scale(0); opacity: 1; }
  100% { transform: translate(-50%, -50%) scale(1); opacity: 0; }
}`
  },
  {
    title: '旋转星环',
    category: '大屏动画',
    description: '多层旋转的星形光环',
    cssCode: `.star-ring {
  width: 100px;
  height: 100px;
  position: relative;
}
.star-ring::before,
.star-ring::after {
  content: "";
  position: absolute;
  inset: 0;
  border: 2px dashed #00f0ff;
  border-radius: 50%;
  animation: star-spin 4s linear infinite;
}
.star-ring::before {
  border-color: #00ff88;
  inset: 15px;
  animation-direction: reverse;
  animation-duration: 3s;
}
.star-ring::after {
  border-style: dotted;
  border-color: #a855f7;
  inset: 30px;
  animation-duration: 2s;
}
@keyframes star-spin {
  to { transform: rotate(360deg); }
}`
  },
  {
    title: '数据波动',
    category: '大屏动画',
    description: '波动的数据曲线效果',
    cssCode: `.data-wave {
  width: 180px;
  height: 80px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(0, 240, 255, 0.2);
}
.data-wave::before {
  content: "";
  position: absolute;
  bottom: 20px;
  left: -100%;
  width: 200%;
  height: 40px;
  background: repeating-linear-gradient(
    90deg,
    transparent 0px,
    transparent 20px,
    #00f0ff 20px,
    #00f0ff 22px,
    transparent 22px,
    transparent 40px
  );
  animation: wave-move 2s linear infinite;
  mask: linear-gradient(to top, black, transparent);
}
@keyframes wave-move {
  0% { transform: translateX(0); }
  100% { transform: translateX(50%); }
}`
  },
  {
    title: '能量柱',
    category: '大屏动画',
    description: '跳动的能量柱状图',
    cssCode: `.energy-bar {
  width: 150px;
  height: 80px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
}
.energy-bar::before,
.energy-bar::after {
  content: "";
  width: 20px;
  background: linear-gradient(to top, #00f0ff, #00ff88);
  border-radius: 4px 4px 0 0;
  animation: bar-bounce 1s ease-in-out infinite;
}
.energy-bar::before {
  height: 50px;
  animation-delay: 0s;
}
.energy-bar::after {
  height: 35px;
  animation-delay: 0.2s;
}
@keyframes bar-bounce {
  0%, 100% { transform: scaleY(0.5); }
  50% { transform: scaleY(1); }
}`
  },
  {
    title: '科技圆环',
    category: '大屏动画',
    description: '多层旋转的科技圆环',
    cssCode: `.tech-circle {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 50%;
}
.tech-circle::before {
  content: "";
  position: absolute;
  inset: 0;
  border: 3px solid transparent;
  border-top-color: #00f0ff;
  border-right-color: #00f0ff;
  border-radius: 50%;
  animation: tech-rotate 1.5s linear infinite;
}
.tech-circle::after {
  content: "";
  position: absolute;
  inset: 15px;
  border: 3px solid transparent;
  border-bottom-color: #00ff88;
  border-left-color: #00ff88;
  border-radius: 50%;
  animation: tech-rotate 1s linear infinite reverse;
}
@keyframes tech-rotate {
  to { transform: rotate(360deg); }
}`
  }
];

async function addAnimations() {
  console.log('开始添加大屏动画...');
  
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
