# Python 爬虫服务

依赖安装：

```
pip install -r requirements.txt
```

启动服务：

```
uvicorn main:app --host 0.0.0.0 --port 8001
```

接口：

- POST /scrape
  - 入参：{ "url": "https://example.com" }
  - 返回：{ success, title, description, keywords, favicon, error }
