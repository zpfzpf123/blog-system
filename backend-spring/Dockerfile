# 使用 Maven 镜像来构建项目
FROM maven:3.8.5-openjdk-8 AS builder

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 构建应用（跳过测试以加快构建速度）
RUN mvn clean package -DskipTests

# 使用轻量级的 JRE 镜像来运行应用
FROM openjdk:8-jre-alpine

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 jar 文件
COPY --from=builder /app/target/*.jar app.jar

# 暴露后端端口
EXPOSE 4567

# 启动应用
# 使用环境变量来配置数据库连接
ENTRYPOINT ["java", "-jar", "app.jar"]
