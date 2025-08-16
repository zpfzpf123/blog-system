#!/bin/bash

echo "========================================"
echo "Ollama 安装和配置脚本"
echo "========================================"
echo

# 检查操作系统
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macos"
else
    echo "不支持的操作系统: $OSTYPE"
    exit 1
fi

echo "检测到操作系统: $OS"
echo

# 检查Ollama是否已安装
if command -v ollama &> /dev/null; then
    echo "Ollama 已安装，版本信息："
    ollama --version
    echo
else
    echo "Ollama 未安装，正在安装..."
    
    if [[ "$OS" == "linux" ]]; then
        # Linux 安装
        curl -fsSL https://ollama.ai/install.sh | sh
    elif [[ "$OS" == "macos" ]]; then
        # macOS 安装
        if command -v brew &> /dev/null; then
            brew install ollama
        else
            echo "请先安装 Homebrew: https://brew.sh/"
            echo "然后运行: brew install ollama"
            exit 1
        fi
    fi
    
    echo "安装完成！请重新运行此脚本"
    exit 0
fi

# 检查Ollama服务状态
echo "检查 Ollama 服务状态..."
if pgrep -f "ollama serve" > /dev/null; then
    echo "Ollama 服务正在运行"
else
    echo "Ollama 服务未运行，正在启动..."
    ollama serve &
    sleep 5
fi

# 检查端口是否监听
if netstat -an 2>/dev/null | grep -q ":11434 "; then
    echo "Ollama 服务正在监听端口 11434"
elif ss -an 2>/dev/null | grep -q ":11434 "; then
    echo "Ollama 服务正在监听端口 11434"
else
    echo "警告: 端口 11434 未监听，服务可能未正常启动"
fi

echo
echo "正在下载推荐的中文模型..."

# 尝试下载 qwen 模型
echo "下载 qwen 模型 (推荐中文使用)..."
if ollama pull qwen; then
    echo "qwen 模型下载成功！"
    MODEL="qwen"
else
    echo "qwen 模型下载失败，尝试下载 llama2..."
    if ollama pull llama2; then
        echo "llama2 模型下载成功！"
        MODEL="llama2"
    else
        echo "模型下载失败，请检查网络连接"
        exit 1
    fi
fi

echo
echo "正在测试模型..."
echo "测试提示: '你好，请用中文回答'"

# 测试模型
if ollama run $MODEL "你好，请用中文回答" > /dev/null 2>&1; then
    echo "模型测试成功！"
else
    echo "模型测试失败，请检查服务状态"
fi

echo
echo "========================================"
echo "配置完成！"
echo "========================================"
echo
echo "在博客系统中配置 Ollama："
echo "1. 服务提供商: 选择 'Ollama (本地)'"
echo "2. 服务地址: http://localhost:11434"
echo "3. 模型名称: $MODEL"
echo
echo "如果遇到问题，请检查："
echo "- Ollama 服务是否正在运行"
echo "- 端口 11434 是否被占用"
echo "- 模型是否下载成功"
echo
echo "启动服务命令: ollama serve"
echo "停止服务: pkill -f 'ollama serve'"
echo

# 设置开机自启动 (可选)
read -p "是否设置开机自启动? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if [[ "$OS" == "linux" ]]; then
        # 创建 systemd 服务文件
        sudo tee /etc/systemd/system/ollama.service > /dev/null <<EOF
[Unit]
Description=Ollama Service
After=network.target

[Service]
Type=simple
User=$USER
ExecStart=/usr/local/bin/ollama serve
Restart=always
RestartSec=3

[Install]
WantedBy=multi-user.target
EOF
        
        sudo systemctl daemon-reload
        sudo systemctl enable ollama
        sudo systemctl start ollama
        echo "已设置开机自启动"
    elif [[ "$OS" == "macos" ]]; then
        # 创建 launchd 配置文件
        cat > ~/Library/LaunchAgents/com.ollama.serve.plist <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>com.ollama.serve</string>
    <key>ProgramArguments</key>
    <array>
        <string>/usr/local/bin/ollama</string>
        <string>serve</string>
    </array>
    <key>RunAtLoad</key>
    <true/>
    <key>KeepAlive</key>
    <true/>
</dict>
</plist>
EOF
        
        launchctl load ~/Library/LaunchAgents/com.ollama.serve.plist
        echo "已设置开机自启动"
    fi
fi

echo
echo "安装和配置完成！"
