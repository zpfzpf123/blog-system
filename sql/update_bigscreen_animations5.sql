-- 新增10个大屏动画 (第五批)
SET NAMES utf8mb4;

-- 308 量子隧道
INSERT INTO css_animations (title, category, description, css_code) VALUES ('量子隧道', '大屏动画', '穿越隧道的光效', '.quantum-tunnel {
  width: 150px;
  height: 150px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  perspective: 500px;
}
.quantum-tunnel::before,
.quantum-tunnel::after {
  content: "";
  position: absolute;
  border: 2px solid #00f0ff;
  border-radius: 50%;
  animation: tunnel-zoom 2s linear infinite;
}
.quantum-tunnel::before {
  width: 20px;
  height: 20px;
  animation-delay: 0s;
}
.quantum-tunnel::after {
  width: 20px;
  height: 20px;
  animation-delay: 1s;
}
@keyframes tunnel-zoom {
  0% { transform: scale(0.2) rotateX(80deg); opacity: 1; border-color: #a855f7; }
  50% { border-color: #00f0ff; }
  100% { transform: scale(6) rotateX(80deg); opacity: 0; border-color: #00ff88; }
}');

-- 309 电磁脉冲
INSERT INTO css_animations (title, category, description, css_code) VALUES ('电磁脉冲', '大屏动画', '电磁波脉冲扩散', '.emp-pulse {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.emp-pulse::before {
  content: "";
  position: absolute;
  width: 30px;
  height: 30px;
  background: radial-gradient(circle, #00f0ff 0%, transparent 70%);
  border-radius: 50%;
  animation: emp-core 1.5s ease-in-out infinite;
}
.emp-pulse::after {
  content: "";
  position: absolute;
  width: 100px;
  height: 100px;
  border: 2px solid transparent;
  border-top-color: #00f0ff;
  border-bottom-color: #00ff88;
  border-radius: 50%;
  animation: emp-ring 1.5s linear infinite;
}
@keyframes emp-core {
  0%, 100% { transform: scale(1); box-shadow: 0 0 20px #00f0ff; }
  50% { transform: scale(1.3); box-shadow: 0 0 40px #00f0ff, 0 0 60px #00ff88; }
}
@keyframes emp-ring {
  to { transform: rotate(360deg); }
}');

-- 310 数据立方体
INSERT INTO css_animations (title, category, description, css_code) VALUES ('数据立方体', '大屏动画', '3D旋转立方体', '.data-cube {
  width: 80px;
  height: 80px;
  position: relative;
  transform-style: preserve-3d;
  animation: cube-spin 4s linear infinite;
}
.data-cube::before,
.data-cube::after {
  content: "";
  position: absolute;
  width: 100%;
  height: 100%;
  border: 2px solid #00f0ff;
  background: rgba(0, 240, 255, 0.1);
  box-shadow: inset 0 0 20px rgba(0, 240, 255, 0.3);
}
.data-cube::before {
  transform: translateZ(40px);
}
.data-cube::after {
  transform: translateZ(-40px);
  border-color: #00ff88;
  background: rgba(0, 255, 136, 0.1);
}
@keyframes cube-spin {
  0% { transform: rotateX(0deg) rotateY(0deg); }
  100% { transform: rotateX(360deg) rotateY(360deg); }
}');

-- 311 神经网络
INSERT INTO css_animations (title, category, description, css_code) VALUES ('神经网络', '大屏动画', '神经元连接动画', '.neural-net {
  width: 180px;
  height: 100px;
  position: relative;
  background: #0a1628;
  border-radius: 8px;
  border: 1px solid rgba(0, 200, 255, 0.2);
  overflow: hidden;
}
.neural-net::before {
  content: "";
  position: absolute;
  width: 12px;
  height: 12px;
  background: #00f0ff;
  border-radius: 50%;
  top: 20px;
  left: 20px;
  box-shadow: 0 0 15px #00f0ff, 60px 30px 0 #00ff88, 120px 10px 0 #a855f7, 40px 60px 0 #ec4899, 100px 50px 0 #00f0ff, 140px 40px 0 #00ff88;
  animation: neuron-pulse 2s ease-in-out infinite;
}
.neural-net::after {
  content: "";
  position: absolute;
  top: 26px;
  left: 32px;
  width: 100px;
  height: 1px;
  background: linear-gradient(90deg, #00f0ff, #00ff88, #a855f7);
  animation: synapse-fire 1.5s ease-in-out infinite;
  box-shadow: 0 30px 0 linear-gradient(90deg, #ec4899, #00f0ff);
}
@keyframes neuron-pulse {
  0%, 100% { filter: brightness(1); }
  50% { filter: brightness(1.5); }
}
@keyframes synapse-fire {
  0%, 100% { opacity: 0.3; transform: scaleX(1); }
  50% { opacity: 1; transform: scaleX(1.1); }
}');

-- 312 引力波
INSERT INTO css_animations (title, category, description, css_code) VALUES ('引力波', '大屏动画', '引力波扭曲效果', '.gravity-wave {
  width: 150px;
  height: 150px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.gravity-wave::before,
.gravity-wave::after {
  content: "";
  position: absolute;
  border: 2px solid;
  border-radius: 50%;
  animation: gravity-distort 3s ease-in-out infinite;
}
.gravity-wave::before {
  width: 60px;
  height: 60px;
  border-color: #00f0ff;
  animation-delay: 0s;
}
.gravity-wave::after {
  width: 100px;
  height: 100px;
  border-color: #00ff88;
  animation-delay: 0.5s;
}
@keyframes gravity-distort {
  0%, 100% { transform: scaleX(1) scaleY(1); opacity: 1; }
  25% { transform: scaleX(1.2) scaleY(0.8); }
  50% { transform: scaleX(0.8) scaleY(1.2); opacity: 0.6; }
  75% { transform: scaleX(1.1) scaleY(0.9); }
}');


-- 313 等离子球
INSERT INTO css_animations (title, category, description, css_code) VALUES ('等离子球', '大屏动画', '等离子能量球效果', '.plasma-ball {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #1a3a5c, #0a1628);
  position: relative;
  box-shadow: 0 0 30px rgba(0, 240, 255, 0.3), inset 0 0 30px rgba(0, 240, 255, 0.2);
  overflow: hidden;
}
.plasma-ball::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 2px;
  height: 40px;
  background: linear-gradient(to top, transparent, #00f0ff, #a855f7);
  transform-origin: bottom center;
  animation: plasma-arc 0.5s ease-in-out infinite;
  box-shadow: 0 0 10px #00f0ff;
}
.plasma-ball::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 2px;
  height: 35px;
  background: linear-gradient(to top, transparent, #00ff88, #ec4899);
  transform-origin: bottom center;
  animation: plasma-arc 0.7s ease-in-out infinite reverse;
  box-shadow: 0 0 10px #00ff88;
}
@keyframes plasma-arc {
  0% { transform: translate(-50%, -100%) rotate(-60deg); }
  50% { transform: translate(-50%, -100%) rotate(60deg); }
  100% { transform: translate(-50%, -100%) rotate(-60deg); }
}');

-- 314 黑洞吸引
INSERT INTO css_animations (title, category, description, css_code) VALUES ('黑洞吸引', '大屏动画', '黑洞吸引物质效果', '.black-hole {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.black-hole::before {
  content: "";
  position: absolute;
  width: 30px;
  height: 30px;
  background: radial-gradient(circle, #000 0%, #0a1628 50%, transparent 70%);
  border-radius: 50%;
  box-shadow: 0 0 20px #000, 0 0 40px rgba(168, 85, 247, 0.5);
}
.black-hole::after {
  content: "";
  position: absolute;
  width: 100px;
  height: 100px;
  border: 2px dashed rgba(0, 240, 255, 0.5);
  border-radius: 50%;
  animation: accretion-disk 3s linear infinite;
  box-shadow: inset 0 0 20px rgba(0, 240, 255, 0.3);
}
@keyframes accretion-disk {
  0% { transform: rotateX(70deg) rotate(0deg); }
  100% { transform: rotateX(70deg) rotate(360deg); }
}');

-- 315 能量护盾
INSERT INTO css_animations (title, category, description, css_code) VALUES ('能量护盾', '大屏动画', '六边形能量护盾', '.energy-shield {
  width: 120px;
  height: 104px;
  position: relative;
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  background: linear-gradient(135deg, rgba(0, 240, 255, 0.2), rgba(168, 85, 247, 0.2));
  animation: shield-pulse 2s ease-in-out infinite;
}
.energy-shield::before {
  content: "";
  position: absolute;
  inset: 4px;
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  background: #0a1628;
}
.energy-shield::after {
  content: "";
  position: absolute;
  inset: 0;
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  animation: shield-scan 2s linear infinite;
}
@keyframes shield-pulse {
  0%, 100% { box-shadow: 0 0 20px rgba(0, 240, 255, 0.5); }
  50% { box-shadow: 0 0 40px rgba(0, 240, 255, 0.8), 0 0 60px rgba(168, 85, 247, 0.4); }
}
@keyframes shield-scan {
  0% { opacity: 0.5; }
  50% { opacity: 1; }
  100% { opacity: 0.5; }
}');

-- 316 虫洞传送
INSERT INTO css_animations (title, category, description, css_code) VALUES ('虫洞传送', '大屏动画', '虫洞时空隧道', '.wormhole {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.wormhole::before {
  content: "";
  position: absolute;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: conic-gradient(from 0deg, #00f0ff, #a855f7, #ec4899, #00ff88, #00f0ff);
  animation: wormhole-spin 2s linear infinite;
  filter: blur(2px);
}
.wormhole::after {
  content: "";
  position: absolute;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: radial-gradient(circle, #0a1628 0%, #000 100%);
  box-shadow: 0 0 30px #000;
}
@keyframes wormhole-spin {
  0% { transform: rotate(0deg) scale(1); }
  50% { transform: rotate(180deg) scale(0.9); }
  100% { transform: rotate(360deg) scale(1); }
}');

-- 317 全息地球
INSERT INTO css_animations (title, category, description, css_code) VALUES ('全息地球', '大屏动画', '旋转的全息地球', '.holo-earth {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  position: relative;
  background: radial-gradient(circle at 30% 30%, #1a4a6e, #0a2840);
  box-shadow: 0 0 30px rgba(0, 240, 255, 0.3);
  overflow: hidden;
}
.holo-earth::before {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: 
    repeating-linear-gradient(0deg, transparent 0px, transparent 8px, rgba(0, 240, 255, 0.1) 8px, rgba(0, 240, 255, 0.1) 10px),
    repeating-linear-gradient(90deg, transparent 0px, transparent 8px, rgba(0, 240, 255, 0.1) 8px, rgba(0, 240, 255, 0.1) 10px);
  animation: earth-rotate 10s linear infinite;
}
.holo-earth::after {
  content: "";
  position: absolute;
  top: 10%;
  left: 10%;
  width: 30%;
  height: 30%;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  filter: blur(5px);
}
@keyframes earth-rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}');

