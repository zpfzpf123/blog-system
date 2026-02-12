// 批量添加更多大屏动画到数据库
const API = 'http://localhost:4567/api/animations';

const bigscreenAnimations = [
  {
    title: '流光卡片',
    category: '大屏动画',
    description: '带有流光边框的数据卡片',
    cssCode: `.glow-card {
  width: 160px;
  height: 100px;
  position: relative;
  background: linear-gradient(135deg, #0d2137 0%, #0a1628 100%);
  border-radius: 12px;
  overflow: hidden;
}
.glow-card::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: conic-gradient(from 0deg, transparent, #00f0ff, transparent 30%);
  animation: card-glow 3s linear infinite;
}
.glow-card::after {
  content: "DATA";
  position: absolute;
  inset: 2px;
  background: linear-gradient(135deg, #0d2137 0%, #0a1628 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #00f0ff;
  font-weight: bold;
  letter-spacing: 2px;
}
@keyframes card-glow {
  to { transform: rotate(360deg); }
}`
  },
  {
    title: '数据球体',
    category: '大屏动画',
    description: '3D旋转的数据球体',
    cssCode: `.data-sphere {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #0d2137, #0a1628);
  box-shadow: inset -10px -10px 30px rgba(0, 240, 255, 0.2), 0 0 30px rgba(0, 240, 255, 0.3);
}
.data-sphere::before {
  content: "";
  position: absolute;
  inset: 10px;
  border: 1px dashed rgba(0, 240, 255, 0.5);
  border-radius: 50%;
  animation: sphere-rotate 3s linear infinite;
}
.data-sphere::after {
  content: "";
  position: absolute;
  inset: 25px;
  border: 1px dashed rgba(0, 255, 136, 0.5);
  border-radius: 50%;
  animation: sphere-rotate 2s linear infinite reverse;
}
@keyframes sphere-rotate {
  to { transform: rotateY(360deg) rotateX(15deg); }
}`
  },
  {
    title: '扫描雷达',
    category: '大屏动画',
    description: '360度扫描的雷达效果',
    cssCode: `.scan-radar {
  width: 120px;
  height: 120px;
  position: relative;
  border-radius: 50%;
  background: #0a1628;
  border: 2px solid rgba(0, 240, 255, 0.3);
}
.scan-radar::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 50%;
  height: 2px;
  background: linear-gradient(90deg, #00f0ff, transparent);
  transform-origin: left center;
  animation: radar-sweep 2s linear infinite;
  box-shadow: 0 0 20px #00f0ff;
}
.scan-radar::after {
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
}`
  },
  {
    title: '能量波纹',
    category: '大屏动画',
    description: '向外扩散的能量波纹',
    cssCode: `.energy-ripple {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.energy-ripple::before,
.energy-ripple::after {
  content: "";
  position: absolute;
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: ripple-expand 2s ease-out infinite;
}
.energy-ripple::before {
  width: 30px;
  height: 30px;
}
.energy-ripple::after {
  width: 30px;
  height: 30px;
  animation-delay: 0.5s;
}
@keyframes ripple-expand {
  0% { transform: scale(1); opacity: 1; border-color: #00f0ff; }
  100% { transform: scale(3); opacity: 0; border-color: #00ff88; }
}`
  },
  {
    title: '科技标题',
    category: '大屏动画',
    description: '带有扫光效果的科技标题',
    cssCode: `.tech-title {
  font-size: 24px;
  font-weight: bold;
  color: #00f0ff;
  position: relative;
  padding: 10px 30px;
  background: linear-gradient(90deg, transparent, rgba(0, 240, 255, 0.1), transparent);
  border-left: 3px solid #00f0ff;
  border-right: 3px solid #00f0ff;
  text-shadow: 0 0 10px #00f0ff;
}
.tech-title::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: title-shine 3s infinite;
}
@keyframes title-shine {
  0% { left: -100%; }
  50%, 100% { left: 100%; }
}`
  },
  {
    title: '粒子轨迹',
    category: '大屏动画',
    description: '沿轨道运动的粒子效果',
    cssCode: `.particle-trail {
  width: 120px;
  height: 120px;
  position: relative;
  border: 1px dashed rgba(0, 240, 255, 0.3);
  border-radius: 50%;
}
.particle-trail::before {
  content: "";
  position: absolute;
  width: 12px;
  height: 12px;
  background: #00f0ff;
  border-radius: 50%;
  top: -6px;
  left: 50%;
  transform: translateX(-50%);
  box-shadow: 0 0 15px #00f0ff, 0 0 30px #00f0ff;
  animation: trail-orbit 3s linear infinite;
}
.particle-trail::after {
  content: "";
  position: absolute;
  width: 8px;
  height: 8px;
  background: #00ff88;
  border-radius: 50%;
  top: 50%;
  left: -4px;
  transform: translateY(-50%);
  box-shadow: 0 0 10px #00ff88;
  animation: trail-orbit 2s linear infinite reverse;
}
@keyframes trail-orbit {
  to { transform: rotate(360deg) translateX(-50%); }
}`
  },
  {
    title: '数据环绕',
    category: '大屏动画',
    description: '环绕中心的数据点动画',
    cssCode: `.data-orbit {
  width: 100px;
  height: 100px;
  position: relative;
}
.data-orbit::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 20px;
  height: 20px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 20px #00f0ff;
}
.data-orbit::after {
  content: "";
  position: absolute;
  top: 0;
  left: 50%;
  width: 10px;
  height: 10px;
  background: #00ff88;
  border-radius: 50%;
  transform: translateX(-50%);
  box-shadow: 0 0 10px #00ff88;
  animation: orbit-spin 2s linear infinite;
  transform-origin: 0 50px;
}
@keyframes orbit-spin {
  to { transform: translateX(-50%) rotate(360deg); }
}`
  },
  {
    title: '光束聚焦',
    category: '大屏动画',
    description: '向中心聚焦的光束效果',
    cssCode: `.beam-focus {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.beam-focus::before {
  content: "";
  position: absolute;
  width: 100%;
  height: 100%;
  background: 
    linear-gradient(0deg, transparent 45%, #00f0ff 50%, transparent 55%),
    linear-gradient(90deg, transparent 45%, #00f0ff 50%, transparent 55%),
    linear-gradient(45deg, transparent 45%, #00ff88 50%, transparent 55%),
    linear-gradient(-45deg, transparent 45%, #00ff88 50%, transparent 55%);
  animation: beam-pulse 2s ease-in-out infinite;
}
.beam-focus::after {
  content: "";
  width: 20px;
  height: 20px;
  background: #00f0ff;
  border-radius: 50%;
  box-shadow: 0 0 20px #00f0ff, 0 0 40px #00f0ff;
  animation: center-pulse 2s ease-in-out infinite;
}
@keyframes beam-pulse {
  0%, 100% { opacity: 0.3; transform: scale(1.2); }
  50% { opacity: 1; transform: scale(0.8); }
}
@keyframes center-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.5); }
}`
  },
  {
    title: '科技按钮',
    category: '大屏动画',
    description: '带有光效的科技风格按钮',
    cssCode: `.tech-button {
  width: 120px;
  height: 50px;
  position: relative;
  background: linear-gradient(135deg, #0d2137 0%, #0a1628 100%);
  border: 1px solid #00f0ff;
  border-radius: 8px;
  color: #00f0ff;
  font-weight: bold;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 0 15px rgba(0, 240, 255, 0.3);
  animation: button-glow 2s ease-in-out infinite;
}
.tech-button::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(0, 240, 255, 0.1), transparent);
  animation: button-shine 2s infinite;
}
@keyframes button-glow {
  0%, 100% { box-shadow: 0 0 15px rgba(0, 240, 255, 0.3); }
  50% { box-shadow: 0 0 25px rgba(0, 240, 255, 0.6), 0 0 35px rgba(0, 255, 136, 0.3); }
}
@keyframes button-shine {
  0% { transform: translateX(-100%) rotate(45deg); }
  50%, 100% { transform: translateX(100%) rotate(45deg); }
}`
  },
  {
    title: '数据矩阵',
    category: '大屏动画',
    description: '闪烁的数据矩阵网格',
    cssCode: `.data-matrix {
  width: 150px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  overflow: hidden;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  grid-template-rows: repeat(4, 1fr);
  gap: 4px;
  padding: 8px;
  border: 1px solid rgba(0, 240, 255, 0.2);
}
.data-matrix::before {
  content: "";
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent 0px,
    transparent 20px,
    rgba(0, 240, 255, 0.05) 20px,
    rgba(0, 240, 255, 0.05) 21px
  );
  animation: matrix-scan 2s linear infinite;
}
@keyframes matrix-scan {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(100%); }
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
