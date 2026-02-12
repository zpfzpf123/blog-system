// 批量添加各类动画到数据库
const API = 'http://localhost:4567/api/animations';

const animations = [
  // ========== 滤镜特效 ==========
  {
    title: '毛玻璃模糊',
    category: '滤镜特效',
    description: '动态毛玻璃模糊效果',
    cssCode: `.glass-blur {
  width: 120px;
  height: 80px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: blur-pulse 3s ease-in-out infinite;
}
@keyframes blur-pulse {
  0%, 100% { backdrop-filter: blur(10px); }
  50% { backdrop-filter: blur(20px); }
}`
  },
  {
    title: '色相旋转',
    category: '滤镜特效',
    description: '色相循环变化效果',
    cssCode: `.hue-rotate {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #ff6b6b, #4ecdc4);
  border-radius: 16px;
  animation: hue-cycle 4s linear infinite;
}
@keyframes hue-cycle {
  0% { filter: hue-rotate(0deg); }
  100% { filter: hue-rotate(360deg); }
}`
  },
  {
    title: '饱和度呼吸',
    category: '滤镜特效',
    description: '饱和度渐变呼吸效果',
    cssCode: `.saturate-breath {
  width: 100px;
  height: 60px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 12px;
  animation: saturate-pulse 2s ease-in-out infinite;
}
@keyframes saturate-pulse {
  0%, 100% { filter: saturate(1); }
  50% { filter: saturate(2); }
}`
  },
  {
    title: '对比度闪烁',
    category: '滤镜特效',
    description: '对比度变化闪烁效果',
    cssCode: `.contrast-flash {
  width: 80px;
  height: 80px;
  background: conic-gradient(#ff6b6b, #feca57, #48dbfb, #ff6b6b);
  border-radius: 50%;
  animation: contrast-blink 1.5s ease-in-out infinite;
}
@keyframes contrast-blink {
  0%, 100% { filter: contrast(1); }
  50% { filter: contrast(1.5) brightness(1.2); }
}`
  },
  {
    title: '反色切换',
    category: '滤镜特效',
    description: '颜色反转切换效果',
    cssCode: `.invert-toggle {
  width: 100px;
  height: 40px;
  background: linear-gradient(90deg, #2d3436, #636e72);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  animation: invert-switch 2s steps(1) infinite;
}
.invert-toggle::before {
  content: "DARK";
}
@keyframes invert-switch {
  0%, 49% { filter: invert(0); }
  50%, 100% { filter: invert(1); }
}`
  },

  // ========== 变形特效 ==========
  {
    title: '果冻弹跳',
    category: '变形特效',
    description: '果冻般的弹性变形',
    cssCode: `.jelly-bounce {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #a29bfe, #6c5ce7);
  border-radius: 20px;
  animation: jelly 1s ease-in-out infinite;
}
@keyframes jelly {
  0%, 100% { transform: scale(1, 1); }
  25% { transform: scale(0.9, 1.1); }
  50% { transform: scale(1.1, 0.9); }
  75% { transform: scale(0.95, 1.05); }
}`
  },
  {
    title: '扭曲波动',
    category: '变形特效',
    description: '扭曲变形波动效果',
    cssCode: `.twist-wave {
  width: 100px;
  height: 60px;
  background: linear-gradient(90deg, #fd79a8, #e84393);
  border-radius: 12px;
  animation: twist 2s ease-in-out infinite;
}
@keyframes twist {
  0%, 100% { transform: skewX(0deg); }
  25% { transform: skewX(10deg); }
  75% { transform: skewX(-10deg); }
}`
  },
  {
    title: '膨胀收缩',
    category: '变形特效',
    description: '膨胀收缩呼吸效果',
    cssCode: `.inflate-deflate {
  width: 70px;
  height: 70px;
  background: radial-gradient(circle, #74b9ff, #0984e3);
  border-radius: 50%;
  animation: inflate 1.5s ease-in-out infinite;
}
@keyframes inflate {
  0%, 100% { transform: scale(1); border-radius: 50%; }
  50% { transform: scale(1.3); border-radius: 30%; }
}`
  },
  {
    title: '透视翻转',
    category: '变形特效',
    description: '3D透视翻转效果',
    cssCode: `.perspective-flip {
  width: 100px;
  height: 60px;
  background: linear-gradient(135deg, #00cec9, #81ecec);
  border-radius: 8px;
  animation: persp-flip 3s ease-in-out infinite;
  transform-style: preserve-3d;
}
@keyframes persp-flip {
  0%, 100% { transform: perspective(400px) rotateY(0deg); }
  50% { transform: perspective(400px) rotateY(180deg); }
}`
  },
  {
    title: '弹性拉伸',
    category: '变形特效',
    description: '橡皮筋般的拉伸效果',
    cssCode: `.elastic-stretch {
  width: 40px;
  height: 80px;
  background: linear-gradient(to bottom, #fdcb6e, #e17055);
  border-radius: 20px;
  animation: elastic 1.2s cubic-bezier(0.68, -0.55, 0.265, 1.55) infinite;
}
@keyframes elastic {
  0%, 100% { transform: scaleY(1) scaleX(1); }
  30% { transform: scaleY(1.4) scaleX(0.8); }
  60% { transform: scaleY(0.8) scaleX(1.2); }
}`
  },

  // ========== 边框动画 ==========
  {
    title: '虚线旋转',
    category: '边框动画',
    description: '虚线边框旋转效果',
    cssCode: `.dashed-rotate {
  width: 80px;
  height: 80px;
  border: 3px dashed #8b5cf6;
  border-radius: 50%;
  animation: dash-spin 4s linear infinite;
}
@keyframes dash-spin {
  to { transform: rotate(360deg); }
}`
  },
  {
    title: '边框描边',
    category: '边框动画',
    description: 'SVG风格边框描边动画',
    cssCode: `.stroke-draw {
  width: 100px;
  height: 60px;
  background: transparent;
  border: 3px solid transparent;
  border-radius: 8px;
  position: relative;
}
.stroke-draw::before {
  content: "";
  position: absolute;
  inset: -3px;
  border: 3px solid #10b981;
  border-radius: 8px;
  clip-path: inset(0 100% 0 0);
  animation: stroke-anim 2s linear infinite;
}
@keyframes stroke-anim {
  0% { clip-path: inset(0 100% 0 0); }
  25% { clip-path: inset(0 0 100% 0); }
  50% { clip-path: inset(0 0 0 100%); }
  75% { clip-path: inset(100% 0 0 0); }
  100% { clip-path: inset(0 100% 0 0); }
}`
  },
  {
    title: '双层边框',
    category: '边框动画',
    description: '双层边框交替动画',
    cssCode: `.double-border {
  width: 80px;
  height: 80px;
  border: 3px solid #f59e0b;
  border-radius: 12px;
  position: relative;
  animation: border-pulse 2s ease-in-out infinite;
}
.double-border::before {
  content: "";
  position: absolute;
  inset: 6px;
  border: 2px solid #fbbf24;
  border-radius: 8px;
  animation: border-pulse 2s ease-in-out infinite reverse;
}
@keyframes border-pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.95); }
}`
  },
  {
    title: '彩虹边框',
    category: '边框动画',
    description: '彩虹渐变边框动画',
    cssCode: `.rainbow-border {
  width: 100px;
  height: 60px;
  background: #1a1a2e;
  border-radius: 12px;
  position: relative;
}
.rainbow-border::before {
  content: "";
  position: absolute;
  inset: -3px;
  background: linear-gradient(90deg, #ff0000, #ff7f00, #ffff00, #00ff00, #0000ff, #8b00ff, #ff0000);
  background-size: 400% 100%;
  border-radius: 14px;
  z-index: -1;
  animation: rainbow-move 3s linear infinite;
}
@keyframes rainbow-move {
  0% { background-position: 0% 50%; }
  100% { background-position: 400% 50%; }
}`
  },

  // ========== 阴影效果 ==========
  {
    title: '霓虹阴影',
    category: '阴影效果',
    description: '霓虹灯光阴影效果',
    cssCode: `.neon-shadow {
  width: 100px;
  height: 40px;
  background: #1a1a2e;
  border-radius: 8px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  animation: neon-glow 2s ease-in-out infinite;
}
.neon-shadow::before {
  content: "NEON";
}
@keyframes neon-glow {
  0%, 100% { box-shadow: 0 0 5px #ff00ff, 0 0 10px #ff00ff, 0 0 20px #ff00ff; }
  50% { box-shadow: 0 0 10px #00ffff, 0 0 20px #00ffff, 0 0 40px #00ffff; }
}`
  },
  {
    title: '浮动阴影',
    category: '阴影效果',
    description: '悬浮浮动阴影效果',
    cssCode: `.float-shadow {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16px;
  animation: float-up 2s ease-in-out infinite;
}
@keyframes float-up {
  0%, 100% { 
    transform: translateY(0);
    box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
  }
  50% { 
    transform: translateY(-15px);
    box-shadow: 0 25px 30px rgba(102, 126, 234, 0.2);
  }
}`
  },
  {
    title: '脉冲阴影',
    category: '阴影效果',
    description: '脉冲扩散阴影效果',
    cssCode: `.pulse-shadow {
  width: 60px;
  height: 60px;
  background: #ef4444;
  border-radius: 50%;
  animation: shadow-pulse 1.5s ease-out infinite;
}
@keyframes shadow-pulse {
  0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.7); }
  70% { box-shadow: 0 0 0 20px rgba(239, 68, 68, 0); }
  100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
}`
  },
  {
    title: '多层阴影',
    category: '阴影效果',
    description: '多层叠加阴影效果',
    cssCode: `.multi-shadow {
  width: 80px;
  height: 80px;
  background: #3b82f6;
  border-radius: 12px;
  animation: multi-shadow-anim 3s ease-in-out infinite;
}
@keyframes multi-shadow-anim {
  0%, 100% {
    box-shadow: 
      5px 5px 0 #60a5fa,
      10px 10px 0 #93c5fd,
      15px 15px 0 #bfdbfe;
  }
  50% {
    box-shadow: 
      2px 2px 0 #60a5fa,
      4px 4px 0 #93c5fd,
      6px 6px 0 #bfdbfe;
  }
}`
  },

  // ========== 退出动画 ==========
  {
    title: '缩小消失',
    category: '退出动画',
    description: '缩小并淡出消失',
    cssCode: `.shrink-out {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #f093fb, #f5576c);
  border-radius: 16px;
  animation: shrink-fade 2s ease-in-out infinite;
}
@keyframes shrink-fade {
  0%, 40% { transform: scale(1); opacity: 1; }
  60%, 100% { transform: scale(0); opacity: 0; }
}`
  },
  {
    title: '旋转飞出',
    category: '退出动画',
    description: '旋转飞出屏幕效果',
    cssCode: `.spin-out {
  width: 60px;
  height: 60px;
  background: #8b5cf6;
  border-radius: 12px;
  animation: spin-fly 2.5s ease-in infinite;
}
@keyframes spin-fly {
  0%, 30% { transform: rotate(0deg) translateX(0); opacity: 1; }
  100% { transform: rotate(720deg) translateX(100px); opacity: 0; }
}`
  },
  {
    title: '碎片分散',
    category: '退出动画',
    description: '碎片化分散消失',
    cssCode: `.shatter-out {
  width: 80px;
  height: 80px;
  background: #10b981;
  border-radius: 8px;
  position: relative;
  animation: shatter 3s ease-in-out infinite;
}
@keyframes shatter {
  0%, 40% { 
    clip-path: inset(0);
    opacity: 1;
  }
  100% { 
    clip-path: inset(50% 50% 50% 50%);
    opacity: 0;
  }
}`
  },
  {
    title: '下沉消失',
    category: '退出动画',
    description: '下沉并淡出效果',
    cssCode: `.sink-out {
  width: 100px;
  height: 50px;
  background: linear-gradient(90deg, #4facfe, #00f2fe);
  border-radius: 25px;
  animation: sink-fade 2s ease-in infinite;
}
@keyframes sink-fade {
  0%, 30% { transform: translateY(0) scaleX(1); opacity: 1; }
  100% { transform: translateY(30px) scaleX(0.5); opacity: 0; }
}`
  },

  // ========== 入场动画 ==========
  {
    title: '弹性入场',
    category: '入场动画',
    description: '弹性缩放入场效果',
    cssCode: `.bounce-in {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16px;
  animation: bounce-enter 1.5s cubic-bezier(0.68, -0.55, 0.265, 1.55) infinite;
}
@keyframes bounce-enter {
  0% { transform: scale(0); opacity: 0; }
  50% { transform: scale(1.2); }
  70% { transform: scale(0.9); }
  100% { transform: scale(1); opacity: 1; }
}`
  },
  {
    title: '翻转入场',
    category: '入场动画',
    description: '3D翻转入场效果',
    cssCode: `.flip-in {
  width: 100px;
  height: 60px;
  background: #f59e0b;
  border-radius: 8px;
  animation: flip-enter 2s ease-out infinite;
  transform-style: preserve-3d;
}
@keyframes flip-enter {
  0% { transform: perspective(400px) rotateX(-90deg); opacity: 0; }
  40% { transform: perspective(400px) rotateX(20deg); }
  60% { transform: perspective(400px) rotateX(-10deg); }
  80% { transform: perspective(400px) rotateX(5deg); }
  100% { transform: perspective(400px) rotateX(0deg); opacity: 1; }
}`
  },
  {
    title: '展开入场',
    category: '入场动画',
    description: '从中心展开入场',
    cssCode: `.unfold-in {
  width: 120px;
  height: 60px;
  background: linear-gradient(90deg, #11998e, #38ef7d);
  border-radius: 8px;
  animation: unfold-enter 2s ease-out infinite;
}
@keyframes unfold-enter {
  0% { transform: scaleX(0); opacity: 0; }
  60% { transform: scaleX(1.1); }
  100% { transform: scaleX(1); opacity: 1; }
}`
  },
  {
    title: '弹跳落下',
    category: '入场动画',
    description: '从上方弹跳落下',
    cssCode: `.drop-bounce {
  width: 60px;
  height: 60px;
  background: #ec4899;
  border-radius: 50%;
  animation: drop-enter 1.5s ease-out infinite;
}
@keyframes drop-enter {
  0% { transform: translateY(-100px); opacity: 0; }
  50% { transform: translateY(20px); }
  70% { transform: translateY(-10px); }
  85% { transform: translateY(5px); }
  100% { transform: translateY(0); opacity: 1; }
}`
  }
];

async function addAnimations() {
  console.log('开始添加各类动画...');
  
  for (const anim of animations) {
    try {
      const res = await fetch(API, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(anim)
      });
      
      if (res.ok) {
        const data = await res.json();
        console.log(`✅ [${anim.category}] ${anim.title} (ID: ${data.id})`);
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
