// 批量添加全新大屏动画到数据库
const API = 'http://localhost:4567/api/animations';

const bigscreenAnimations = [
  {
    title: '赛博朋克线条',
    category: '大屏动画',
    description: '赛博朋克风格的斜线装饰动画',
    cssCode: `.cyber-lines {
  width: 160px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  overflow: hidden;
}
.cyber-lines::before {
  content: "";
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    -45deg,
    transparent 0px,
    transparent 10px,
    rgba(0, 240, 255, 0.1) 10px,
    rgba(0, 240, 255, 0.1) 11px
  );
  animation: cyber-shift 3s linear infinite;
}
.cyber-lines::after {
  content: "CYBER";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #ec4899;
  font-size: 16px;
  font-weight: bold;
  text-shadow: 0 0 10px #ec4899, 2px 2px 0 #00f0ff;
}
@keyframes cyber-shift {
  0% { background-position: 0 0; }
  100% { background-position: 28px 28px; }
}`
  },
  {
    title: '故障文字',
    category: '大屏动画',
    description: 'Glitch故障风格的文字效果',
    cssCode: `.glitch-text {
  position: relative;
  font-size: 28px;
  font-weight: bold;
  color: #fff;
  text-shadow: 2px 0 #00f0ff, -2px 0 #ec4899;
  animation: glitch-shake 0.5s infinite;
}
.glitch-text::before,
.glitch-text::after {
  content: "ERROR";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
.glitch-text::before {
  color: #00f0ff;
  animation: glitch-clip 2s infinite;
  clip-path: polygon(0 0, 100% 0, 100% 45%, 0 45%);
  transform: translate(-3px, 0);
}
.glitch-text::after {
  color: #ec4899;
  animation: glitch-clip 2s infinite reverse;
  clip-path: polygon(0 55%, 100% 55%, 100% 100%, 0 100%);
  transform: translate(3px, 0);
}
@keyframes glitch-shake {
  0%, 100% { transform: translate(0); }
  20% { transform: translate(-2px, 2px); }
  40% { transform: translate(2px, -2px); }
  60% { transform: translate(-1px, 1px); }
  80% { transform: translate(1px, -1px); }
}
@keyframes glitch-clip {
  0%, 100% { clip-path: polygon(0 0, 100% 0, 100% 45%, 0 45%); }
  50% { clip-path: polygon(0 20%, 100% 20%, 100% 60%, 0 60%); }
}`
  },
  {
    title: '液态金属',
    category: '大屏动画',
    description: '流动的液态金属球效果',
    cssCode: `.liquid-metal {
  width: 100px;
  height: 100px;
  position: relative;
  background: linear-gradient(135deg, #1a1a2e, #16213e);
  border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  animation: liquid-morph 4s ease-in-out infinite;
  box-shadow: 
    inset -20px -20px 40px rgba(0, 240, 255, 0.2),
    inset 20px 20px 40px rgba(255, 255, 255, 0.1),
    0 0 30px rgba(0, 240, 255, 0.3);
}
.liquid-metal::before {
  content: "";
  position: absolute;
  top: 20%;
  left: 20%;
  width: 30%;
  height: 30%;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  filter: blur(5px);
}
@keyframes liquid-morph {
  0%, 100% { border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%; }
  25% { border-radius: 58% 42% 75% 25% / 76% 46% 54% 24%; }
  50% { border-radius: 50% 50% 33% 67% / 55% 27% 73% 45%; }
  75% { border-radius: 33% 67% 58% 42% / 63% 68% 32% 37%; }
}`
  },
  {
    title: '音乐均衡器',
    category: '大屏动画',
    description: '跳动的音乐均衡器柱状图',
    cssCode: `.music-equalizer {
  width: 140px;
  height: 80px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  background: #0a1628;
  border-radius: 8px;
}
.music-equalizer::before,
.music-equalizer::after {
  content: "";
  width: 12px;
  background: linear-gradient(to top, #00f0ff, #a855f7, #ec4899);
  border-radius: 3px;
  animation: eq-bar 0.6s ease-in-out infinite alternate;
}
.music-equalizer::before {
  height: 60px;
  animation-delay: 0s;
}
.music-equalizer::after {
  height: 40px;
  animation-delay: 0.15s;
}
@keyframes eq-bar {
  0% { transform: scaleY(0.2); }
  100% { transform: scaleY(1); }
}`
  },
  {
    title: '棱镜折射',
    category: '大屏动画',
    description: '光线穿过棱镜的折射效果',
    cssCode: `.prism-refract {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.prism-refract::before {
  content: "";
  width: 0;
  height: 0;
  border-left: 40px solid transparent;
  border-right: 40px solid transparent;
  border-bottom: 70px solid rgba(255, 255, 255, 0.1);
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.3));
}
.prism-refract::after {
  content: "";
  position: absolute;
  right: 10px;
  top: 50%;
  width: 50px;
  height: 30px;
  background: linear-gradient(to bottom, 
    #ff0000 0%, #ff7f00 17%, #ffff00 33%, 
    #00ff00 50%, #0000ff 67%, #8b00ff 83%, #ff00ff 100%);
  transform: translateY(-50%) skewY(-15deg);
  opacity: 0.8;
  animation: rainbow-pulse 2s ease-in-out infinite;
}
@keyframes rainbow-pulse {
  0%, 100% { opacity: 0.5; width: 40px; }
  50% { opacity: 0.9; width: 60px; }
}`
  },
  {
    title: '太空站舱门',
    category: '大屏动画',
    description: '科幻风格的舱门开合动画',
    cssCode: `.airlock-door {
  width: 120px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border: 2px solid #00f0ff;
  border-radius: 8px;
  overflow: hidden;
}
.airlock-door::before,
.airlock-door::after {
  content: "";
  position: absolute;
  top: 0;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, #0d2137, #1a3a5c);
  border: 1px solid rgba(0, 240, 255, 0.3);
  animation: door-open 3s ease-in-out infinite;
}
.airlock-door::before {
  left: 0;
  border-right: 2px solid #00f0ff;
  transform-origin: left;
}
.airlock-door::after {
  right: 0;
  border-left: 2px solid #00f0ff;
  transform-origin: right;
  animation-name: door-open-right;
}
@keyframes door-open {
  0%, 20%, 80%, 100% { transform: scaleX(1); }
  40%, 60% { transform: scaleX(0.1); }
}
@keyframes door-open-right {
  0%, 20%, 80%, 100% { transform: scaleX(1); }
  40%, 60% { transform: scaleX(0.1); }
}`
  },
  {
    title: '全息投影仪',
    category: '大屏动画',
    description: '全息投影仪发射光束效果',
    cssCode: `.holo-projector {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.holo-projector::before {
  content: "";
  width: 60px;
  height: 20px;
  background: linear-gradient(to top, #0d2137, #1a3a5c);
  border-radius: 4px;
  border: 1px solid #00f0ff;
  margin-top: auto;
}
.holo-projector::after {
  content: "";
  position: absolute;
  bottom: 20px;
  width: 40px;
  height: 80px;
  background: linear-gradient(to top, rgba(0, 240, 255, 0.4), transparent);
  clip-path: polygon(20% 100%, 80% 100%, 100% 0%, 0% 0%);
  animation: holo-flicker 0.1s infinite;
}
@keyframes holo-flicker {
  0%, 100% { opacity: 0.8; }
  50% { opacity: 0.6; }
}`
  },
  {
    title: '量子纠缠',
    category: '大屏动画',
    description: '两个量子态粒子的纠缠效果',
    cssCode: `.quantum-entangle {
  width: 150px;
  height: 80px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.quantum-entangle::before,
.quantum-entangle::after {
  content: "";
  width: 25px;
  height: 25px;
  background: radial-gradient(circle, #00f0ff, transparent);
  border-radius: 50%;
  box-shadow: 0 0 20px #00f0ff;
  animation: quantum-pulse 1s ease-in-out infinite;
}
.quantum-entangle::after {
  background: radial-gradient(circle, #ec4899, transparent);
  box-shadow: 0 0 20px #ec4899;
  animation-delay: 0.5s;
}
@keyframes quantum-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(0.5); opacity: 0.5; }
}`
  },
  {
    title: '数据加密',
    category: '大屏动画',
    description: '数据加密锁定动画效果',
    cssCode: `.data-encrypt {
  width: 80px;
  height: 100px;
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.data-encrypt::before {
  content: "";
  width: 50px;
  height: 40px;
  border: 4px solid #00f0ff;
  border-bottom: none;
  border-radius: 25px 25px 0 0;
  animation: lock-shake 2s ease-in-out infinite;
}
.data-encrypt::after {
  content: "🔒";
  font-size: 40px;
  margin-top: -5px;
  animation: lock-glow 2s ease-in-out infinite;
}
@keyframes lock-shake {
  0%, 100% { transform: translateY(0); }
  10%, 30% { transform: translateY(-5px); }
  20% { transform: translateY(-3px); }
  40%, 100% { transform: translateY(0); }
}
@keyframes lock-glow {
  0%, 100% { filter: drop-shadow(0 0 5px #00f0ff); }
  50% { filter: drop-shadow(0 0 15px #00f0ff); }
}`
  },
  {
    title: '星际跃迁',
    category: '大屏动画',
    description: '星际穿越的光速跃迁效果',
    cssCode: `.warp-drive {
  width: 150px;
  height: 100px;
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  perspective: 500px;
}
.warp-drive::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 4px;
  height: 4px;
  background: #fff;
  border-radius: 50%;
  box-shadow:
    20px -30px 0 #00f0ff,
    -30px 20px 0 #fff,
    40px 10px 0 #a855f7,
    -20px -20px 0 #00ff88,
    50px -10px 0 #ec4899,
    -40px 30px 0 #fff,
    10px 40px 0 #00f0ff;
  animation: warp-zoom 1s linear infinite;
}
@keyframes warp-zoom {
  0% { transform: translate(-50%, -50%) scale(0.1); opacity: 0; }
  50% { opacity: 1; }
  100% { transform: translate(-50%, -50%) scale(3); opacity: 0; }
}`
  },
  {
    title: '能量充能',
    category: '大屏动画',
    description: '能量逐渐充满的充能效果',
    cssCode: `.energy-charge {
  width: 60px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border: 2px solid #00f0ff;
  border-radius: 30px;
  overflow: hidden;
}
.energy-charge::before {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 0%;
  background: linear-gradient(to top, #00f0ff, #00ff88, #a855f7);
  animation: charge-fill 3s ease-in-out infinite;
  box-shadow: 0 0 20px #00f0ff;
}
.energy-charge::after {
  content: "";
  position: absolute;
  top: 10px;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 10px;
  background: #00f0ff;
  border-radius: 0 0 10px 10px;
}
@keyframes charge-fill {
  0%, 100% { height: 10%; }
  50% { height: 90%; }
}`
  },
  {
    title: '分子结构',
    category: '大屏动画',
    description: '旋转的分子结构模型',
    cssCode: `.molecule {
  width: 100px;
  height: 100px;
  position: relative;
  animation: molecule-rotate 4s linear infinite;
}
.molecule::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 30px;
  height: 30px;
  background: #00f0ff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 15px #00f0ff;
}
.molecule::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 80px;
  height: 80px;
  border: 2px dashed rgba(0, 240, 255, 0.5);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow:
    inset 40px 0 0 -32px #00ff88,
    inset -40px 0 0 -32px #a855f7,
    inset 0 40px 0 -32px #ec4899,
    inset 0 -40px 0 -32px #00f0ff;
}
@keyframes molecule-rotate {
  to { transform: rotate(360deg); }
}`
  },
  {
    title: '信息流',
    category: '大屏动画',
    description: '横向流动的信息数据流',
    cssCode: `.info-stream {
  width: 180px;
  height: 60px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(0, 240, 255, 0.2);
}
.info-stream::before {
  content: ">>> DATA STREAM >>> LOADING >>> SYNC >>> ";
  position: absolute;
  top: 50%;
  left: 0;
  transform: translateY(-50%);
  white-space: nowrap;
  color: #00f0ff;
  font-family: monospace;
  font-size: 12px;
  animation: stream-scroll 5s linear infinite;
  text-shadow: 0 0 5px #00f0ff;
}
@keyframes stream-scroll {
  0% { transform: translateY(-50%) translateX(100%); }
  100% { transform: translateY(-50%) translateX(-100%); }
}`
  },
  {
    title: '防火墙',
    category: '大屏动画',
    description: '网络防火墙保护动画',
    cssCode: `.firewall {
  width: 100px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.firewall::before {
  content: "🛡️";
  font-size: 50px;
  animation: shield-pulse 2s ease-in-out infinite;
  filter: drop-shadow(0 0 10px #00f0ff);
}
.firewall::after {
  content: "";
  position: absolute;
  width: 90px;
  height: 90px;
  border: 2px solid transparent;
  border-top-color: #00f0ff;
  border-right-color: #00ff88;
  border-radius: 50%;
  animation: shield-rotate 3s linear infinite;
}
@keyframes shield-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}
@keyframes shield-rotate {
  to { transform: rotate(360deg); }
}`
  },
  {
    title: '波形示波器',
    category: '大屏动画',
    description: '示波器波形显示动画',
    cssCode: `.oscilloscope {
  width: 180px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 2px solid #1a3a5c;
  overflow: hidden;
}
.oscilloscope::before {
  content: "";
  position: absolute;
  inset: 0;
  background: 
    linear-gradient(rgba(0, 240, 255, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 240, 255, 0.05) 1px, transparent 1px);
  background-size: 20px 20px;
}
.oscilloscope::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 0;
  width: 200%;
  height: 2px;
  background: #00f0ff;
  box-shadow: 0 0 10px #00f0ff;
  clip-path: polygon(
    0% 50%, 5% 20%, 10% 80%, 15% 30%, 20% 70%, 
    25% 40%, 30% 60%, 35% 45%, 40% 55%, 45% 50%,
    50% 50%, 55% 20%, 60% 80%, 65% 30%, 70% 70%,
    75% 40%, 80% 60%, 85% 45%, 90% 55%, 95% 50%, 100% 50%
  );
  animation: wave-scroll 2s linear infinite;
}
@keyframes wave-scroll {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}`
  }
];

async function addAnimations() {
  console.log('开始添加全新大屏动画...');
  
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
