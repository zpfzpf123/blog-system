// 批量添加更多各类动画到数据库
const API = 'http://localhost:4567/api/animations';

const animations = [
  // ========== 加载动画 ==========
  {
    title: '原子轨道加载',
    category: '加载动画',
    description: '原子轨道旋转加载效果',
    cssCode: `.atom-loader {
  width: 60px;
  height: 60px;
  position: relative;
}
.atom-loader::before,
.atom-loader::after {
  content: "";
  position: absolute;
  inset: 0;
  border: 2px solid transparent;
  border-top-color: #8b5cf6;
  border-radius: 50%;
  animation: atom-spin 1.5s linear infinite;
}
.atom-loader::after {
  border-top-color: #ec4899;
  animation-duration: 1s;
  animation-direction: reverse;
}
@keyframes atom-spin {
  to { transform: rotate(360deg); }
}`
  },
  {
    title: '方块堆叠',
    category: '加载动画',
    description: '方块依次堆叠加载',
    cssCode: `.stack-loader {
  display: flex;
  gap: 4px;
}
.stack-loader span {
  width: 12px;
  height: 12px;
  background: #10b981;
  animation: stack-bounce 1.2s ease-in-out infinite;
}
.stack-loader span:nth-child(2) { animation-delay: 0.1s; }
.stack-loader span:nth-child(3) { animation-delay: 0.2s; }
@keyframes stack-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); background: #34d399; }
}`
  },
  {
    title: '水滴加载',
    category: '加载动画',
    description: '水滴形态变化加载',
    cssCode: `.droplet-loader {
  width: 30px;
  height: 30px;
  background: #3b82f6;
  border-radius: 50% 50% 50% 50%;
  animation: droplet 1s ease-in-out infinite;
}
@keyframes droplet {
  0%, 100% { border-radius: 50%; transform: translateY(0); }
  50% { border-radius: 50% 50% 20% 20%; transform: translateY(-15px); }
}`
  },
  {
    title: '信号加载',
    category: '加载动画',
    description: 'WiFi信号样式加载',
    cssCode: `.signal-loader {
  width: 40px;
  height: 40px;
  position: relative;
}
.signal-loader::before,
.signal-loader::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: transla