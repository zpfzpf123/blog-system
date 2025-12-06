package com.blog.service;

import com.blog.entity.DevServiceConfig;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 项目扫描服务 - 自动识别项目类型并生成服务配置
 */
@Service
public class ProjectScanService {

    /**
     * 扫描项目目录，自动识别项目类型并生成服务配置
     */
    public Map<String, Object> scanProject(String projectPath, Long projectId) {
        Map<String, Object> result = new HashMap<>();
        List<DevServiceConfig> services = new ArrayList<>();
        List<String> detectedTypes = new ArrayList<>();
        
        Path basePath = Paths.get(projectPath);
        if (!Files.exists(basePath)) {
            result.put("success", false);
            result.put("message", "项目路径不存在: " + projectPath);
            return result;
        }
        
        int startOrder = 1;
        
        // 检测各种项目类型
        // 1. 检测前端项目
        DevServiceConfig frontendService = detectFrontendProject(basePath, projectId);
        if (frontendService != null) {
            frontendService.setStartOrder(startOrder++);
            services.add(frontendService);
            detectedTypes.add(frontendService.getDescription());
        }
        
        // 2. 检测后端项目
        List<DevServiceConfig> backendServices = detectBackendProjects(basePath, projectId);
        for (DevServiceConfig service : backendServices) {
            service.setStartOrder(startOrder++);
            services.add(service);
            detectedTypes.add(service.getDescription());
        }
        
        // 3. 检测数据库配置
        DevServiceConfig dbService = detectDatabaseConfig(basePath, projectId);
        if (dbService != null) {
            dbService.setStartOrder(0); // 数据库优先启动
            services.add(dbService);
            detectedTypes.add(dbService.getDescription());
        }

        result.put("success", true);
        result.put("services", services);
        result.put("detectedTypes", detectedTypes);
        result.put("message", "扫描完成，发现 " + services.size() + " 个服务");
        
        return result;
    }
    
    /**
     * 检测前端项目
     */
    private DevServiceConfig detectFrontendProject(Path basePath, Long projectId) {
        // 首先检查根目录的 package.json
        DevServiceConfig rootFrontend = scanFrontendInDirectory(basePath, projectId);
        if (rootFrontend != null) {
            return rootFrontend;
        }
        
        // 如果根目录没有，尝试在子目录中查找（最多2层深度）
        try {
            return findFrontendInSubdirectories(basePath, projectId, 0, 2);
        } catch (Exception e) {
            System.err.println("扫描前端项目时出错: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 在子目录中查找前端项目
     */
    private DevServiceConfig findFrontendInSubdirectories(Path currentPath, Long projectId, 
                                                          int currentDepth, int maxDepth) throws Exception {
        if (currentDepth >= maxDepth) {
            return null;
        }
        
        List<Path> subDirs = new ArrayList<>();
        Files.list(currentPath)
            .filter(Files::isDirectory)
            .filter(p -> !shouldSkipDirectory(p))
            .forEach(subDirs::add);
        
        for (Path subDir : subDirs) {
            DevServiceConfig service = scanFrontendInDirectory(subDir, projectId);
            if (service != null) {
                return service;
            }
            
            // 继续递归查找
            service = findFrontendInSubdirectories(subDir, projectId, currentDepth + 1, maxDepth);
            if (service != null) {
                return service;
            }
        }
        
        return null;
    }
    
    /**
     * 在指定目录扫描前端项目
     */
    private DevServiceConfig scanFrontendInDirectory(Path directory, Long projectId) {
        File packageJson = directory.resolve("package.json").toFile();
        if (!packageJson.exists()) {
            return null;
        }
        
        try {
            String content = Files.readString(packageJson.toPath());
            
            DevServiceConfig service = new DevServiceConfig();
            service.setProjectId(projectId);
            service.setType("frontend");
            service.setWorkingDirectory(directory.toString());
            service.setEnabled(true);
            
            // 检测具体的前端框架
            if (content.contains("\"vue\"") || content.contains("\"@vue/")) {
                service.setName("Vue 前端");
                service.setDescription("Vue.js 前端项目");
                
                if (content.contains("\"vite\"")) {
                    service.setStartCommand("npm run dev");
                    service.setPort(5173);
                    service.setHealthCheckUrl("http://localhost:5173");
                } else {
                    service.setStartCommand("npm run serve");
                    service.setPort(8080);
                    service.setHealthCheckUrl("http://localhost:8080");
                }
            } else if (content.contains("\"react\"") || content.contains("\"@react/")) {
                service.setName("React 前端");
                service.setDescription("React 前端项目");
                
                if (content.contains("\"vite\"")) {
                    service.setStartCommand("npm run dev");
                    service.setPort(5173);
                } else if (content.contains("\"next\"")) {
                    service.setStartCommand("npm run dev");
                    service.setPort(3000);
                } else {
                    service.setStartCommand("npm start");
                    service.setPort(3000);
                }
                service.setHealthCheckUrl("http://localhost:" + service.getPort());
            } else if (content.contains("\"angular\"") || content.contains("\"@angular/")) {
                service.setName("Angular 前端");
                service.setDescription("Angular 前端项目");
                service.setStartCommand("ng serve");
                service.setPort(4200);
                service.setHealthCheckUrl("http://localhost:4200");
            } else {
                service.setName("Node.js 前端");
                service.setDescription("Node.js 前端项目");
                service.setStartCommand("npm start");
                service.setPort(3000);
                service.setHealthCheckUrl("http://localhost:3000");
            }
            
            return service;
        } catch (Exception e) {
            // 忽略读取错误
        }
        
        return null;
    }

    
    /**
     * 检测后端项目
     */
    private List<DevServiceConfig> detectBackendProjects(Path basePath, Long projectId) {
        List<DevServiceConfig> services = new ArrayList<>();
        Set<String> scannedPaths = new HashSet<>();
        
        // 1. 检测根目录
        DevServiceConfig springService = detectSpringBoot(basePath, projectId);
        if (springService != null) {
            services.add(springService);
            scannedPaths.add(basePath.toString());
        }
        
        DevServiceConfig pythonService = detectPythonProject(basePath, projectId);
        if (pythonService != null) {
            services.add(pythonService);
            scannedPaths.add(basePath.toString());
        }
        
        DevServiceConfig goService = detectGoProject(basePath, projectId);
        if (goService != null) {
            services.add(goService);
            scannedPaths.add(basePath.toString());
        }
        
        // 2. 递归扫描所有子目录（最多3层深度）
        try {
            scanSubDirectories(basePath, projectId, services, scannedPaths, 0, 3);
        } catch (Exception e) {
            System.err.println("扫描子目录时出错: " + e.getMessage());
        }
        
        return services;
    }
    
    /**
     * 递归扫描子目录
     */
    private void scanSubDirectories(Path currentPath, Long projectId, 
                                    List<DevServiceConfig> services, 
                                    Set<String> scannedPaths, 
                                    int currentDepth, int maxDepth) throws Exception {
        if (currentDepth >= maxDepth) {
            return;
        }
        
        Files.list(currentPath)
            .filter(Files::isDirectory)
            .filter(p -> !shouldSkipDirectory(p))
            .forEach(subDir -> {
                try {
                    String subDirPath = subDir.toString();
                    
                    // 避免重复扫描
                    if (scannedPaths.contains(subDirPath)) {
                        return;
                    }
                    
                    // 检测Spring Boot
                    DevServiceConfig subSpring = detectSpringBoot(subDir, projectId);
                    if (subSpring != null && !containsService(services, subSpring)) {
                        services.add(subSpring);
                        scannedPaths.add(subDirPath);
                    }
                    
                    // 检测Python
                    DevServiceConfig subPython = detectPythonProject(subDir, projectId);
                    if (subPython != null && !containsService(services, subPython)) {
                        services.add(subPython);
                        scannedPaths.add(subDirPath);
                    }
                    
                    // 检测Go
                    DevServiceConfig subGo = detectGoProject(subDir, projectId);
                    if (subGo != null && !containsService(services, subGo)) {
                        services.add(subGo);
                        scannedPaths.add(subDirPath);
                    }
                    
                    // 继续递归扫描
                    scanSubDirectories(subDir, projectId, services, scannedPaths, currentDepth + 1, maxDepth);
                } catch (Exception e) {
                    // 忽略单个目录的错误
                }
            });
    }
    
    /**
     * 判断是否应该跳过该目录
     */
    private boolean shouldSkipDirectory(Path dir) {
        String dirName = dir.getFileName().toString();
        
        // 跳过隐藏目录、依赖目录、构建目录等
        return dirName.startsWith(".") 
            || dirName.equals("node_modules")
            || dirName.equals("dist")
            || dirName.equals("build")
            || dirName.equals("target")
            || dirName.equals("__pycache__")
            || dirName.equals(".venv")
            || dirName.equals("venv")
            || dirName.equals("env")
            || dirName.equals("vendor")
            || dirName.equals("bin")
            || dirName.equals("obj");
    }
    
    private boolean containsService(List<DevServiceConfig> services, DevServiceConfig service) {
        return services.stream().anyMatch(s -> 
            s.getWorkingDirectory() != null && 
            s.getWorkingDirectory().equals(service.getWorkingDirectory()) &&
            s.getType() != null && s.getType().equals(service.getType())
        );
    }
    
    /**
     * 检测 Spring Boot 项目
     */
    private DevServiceConfig detectSpringBoot(Path basePath, Long projectId) {
        File pomXml = basePath.resolve("pom.xml").toFile();
        File gradleBuild = basePath.resolve("build.gradle").toFile();
        
        if (pomXml.exists() || gradleBuild.exists()) {
            try {
                DevServiceConfig service = new DevServiceConfig();
                service.setProjectId(projectId);
                service.setType("backend");
                service.setWorkingDirectory(basePath.toString());
                service.setEnabled(true);
                
                // 读取配置文件获取端口
                int port = 8080;
                File appProps = basePath.resolve("src/main/resources/application.properties").toFile();
                File appYaml = basePath.resolve("src/main/resources/application.yml").toFile();
                
                if (appProps.exists()) {
                    String content = Files.readString(appProps.toPath());
                    port = extractPort(content, "server.port");
                } else if (appYaml.exists()) {
                    String content = Files.readString(appYaml.toPath());
                    port = extractPortFromYaml(content);
                }
                
                service.setName("Spring Boot 后端");
                service.setDescription("Spring Boot 后端服务 (端口: " + port + ")");
                service.setPort(port);
                service.setHealthCheckUrl("http://localhost:" + port);
                
                if (pomXml.exists()) {
                    service.setStartCommand("mvn spring-boot:run");
                } else {
                    service.setStartCommand("./gradlew bootRun");
                }
                
                return service;
            } catch (Exception e) {
                // 忽略
            }
        }
        return null;
    }

    
    /**
     * 检测 Python 项目
     */
    private DevServiceConfig detectPythonProject(Path basePath, Long projectId) {
        File requirements = basePath.resolve("requirements.txt").toFile();
        File pipfile = basePath.resolve("Pipfile").toFile();
        File pyproject = basePath.resolve("pyproject.toml").toFile();
        File manageFile = basePath.resolve("manage.py").toFile();
        File appFile = basePath.resolve("app.py").toFile();
        File mainFile = basePath.resolve("main.py").toFile();
        
        if (requirements.exists() || pipfile.exists() || pyproject.exists()) {
            DevServiceConfig service = new DevServiceConfig();
            service.setProjectId(projectId);
            service.setType("backend");
            service.setWorkingDirectory(basePath.toString());
            service.setEnabled(true);
            
            // 检测具体框架
            if (manageFile.exists()) {
                // Django 项目
                service.setName("Django 后端");
                service.setDescription("Django 后端服务");
                service.setStartCommand("python manage.py runserver 0.0.0.0:8000");
                service.setPort(8000);
            } else if (appFile.exists() || mainFile.exists()) {
                try {
                    String content = "";
                    if (appFile.exists()) {
                        content = Files.readString(appFile.toPath());
                    } else if (mainFile.exists()) {
                        content = Files.readString(mainFile.toPath());
                    }
                    
                    if (content.contains("FastAPI") || content.contains("fastapi")) {
                        service.setName("FastAPI 后端");
                        service.setDescription("FastAPI 后端服务");
                        service.setStartCommand("uvicorn main:app --reload --host 0.0.0.0 --port 8000");
                        service.setPort(8000);
                    } else if (content.contains("Flask") || content.contains("flask")) {
                        service.setName("Flask 后端");
                        service.setDescription("Flask 后端服务");
                        service.setStartCommand("flask run --host=0.0.0.0 --port=5000");
                        service.setPort(5000);
                    } else {
                        service.setName("Python 后端");
                        service.setDescription("Python 后端服务");
                        service.setStartCommand("python " + (appFile.exists() ? "app.py" : "main.py"));
                        service.setPort(8000);
                    }
                } catch (Exception e) {
                    service.setName("Python 后端");
                    service.setDescription("Python 后端服务");
                    service.setStartCommand("python main.py");
                    service.setPort(8000);
                }
            } else {
                service.setName("Python 后端");
                service.setDescription("Python 后端服务");
                service.setStartCommand("python main.py");
                service.setPort(8000);
            }
            
            service.setHealthCheckUrl("http://localhost:" + service.getPort());
            return service;
        }
        return null;
    }
    
    /**
     * 检测 Go 项目
     */
    private DevServiceConfig detectGoProject(Path basePath, Long projectId) {
        File goMod = basePath.resolve("go.mod").toFile();
        
        if (goMod.exists()) {
            DevServiceConfig service = new DevServiceConfig();
            service.setProjectId(projectId);
            service.setName("Go 后端");
            service.setDescription("Go 后端服务");
            service.setType("backend");
            service.setStartCommand("go run .");
            service.setWorkingDirectory(basePath.toString());
            service.setPort(8080);
            service.setHealthCheckUrl("http://localhost:8080");
            service.setEnabled(true);
            return service;
        }
        return null;
    }

    
    /**
     * 检测数据库配置
     */
    private DevServiceConfig detectDatabaseConfig(Path basePath, Long projectId) {
        // 检查 docker-compose.yml
        File dockerCompose = basePath.resolve("docker-compose.yml").toFile();
        File dockerComposeYaml = basePath.resolve("docker-compose.yaml").toFile();
        
        if (dockerCompose.exists() || dockerComposeYaml.exists()) {
            try {
                File composeFile = dockerCompose.exists() ? dockerCompose : dockerComposeYaml;
                String content = Files.readString(composeFile.toPath());
                
                if (content.contains("mysql") || content.contains("mariadb")) {
                    DevServiceConfig service = new DevServiceConfig();
                    service.setProjectId(projectId);
                    service.setName("MySQL 数据库");
                    service.setDescription("MySQL 数据库服务 (Docker)");
                    service.setType("database");
                    service.setStartCommand("docker-compose up -d mysql");
                    service.setStopCommand("docker-compose stop mysql");
                    service.setWorkingDirectory(basePath.toString());
                    service.setPort(3306);
                    service.setEnabled(true);
                    return service;
                } else if (content.contains("postgres")) {
                    DevServiceConfig service = new DevServiceConfig();
                    service.setProjectId(projectId);
                    service.setName("PostgreSQL 数据库");
                    service.setDescription("PostgreSQL 数据库服务 (Docker)");
                    service.setType("database");
                    service.setStartCommand("docker-compose up -d postgres");
                    service.setStopCommand("docker-compose stop postgres");
                    service.setWorkingDirectory(basePath.toString());
                    service.setPort(5432);
                    service.setEnabled(true);
                    return service;
                } else if (content.contains("mongo")) {
                    DevServiceConfig service = new DevServiceConfig();
                    service.setProjectId(projectId);
                    service.setName("MongoDB 数据库");
                    service.setDescription("MongoDB 数据库服务 (Docker)");
                    service.setType("database");
                    service.setStartCommand("docker-compose up -d mongo");
                    service.setStopCommand("docker-compose stop mongo");
                    service.setWorkingDirectory(basePath.toString());
                    service.setPort(27017);
                    service.setEnabled(true);
                    return service;
                } else if (content.contains("redis")) {
                    DevServiceConfig service = new DevServiceConfig();
                    service.setProjectId(projectId);
                    service.setName("Redis 缓存");
                    service.setDescription("Redis 缓存服务 (Docker)");
                    service.setType("database");
                    service.setStartCommand("docker-compose up -d redis");
                    service.setStopCommand("docker-compose stop redis");
                    service.setWorkingDirectory(basePath.toString());
                    service.setPort(6379);
                    service.setEnabled(true);
                    return service;
                }
            } catch (Exception e) {
                // 忽略
            }
        }
        return null;
    }
    
    /**
     * 从 properties 文件提取端口
     */
    private int extractPort(String content, String key) {
        try {
            for (String line : content.split("\n")) {
                if (line.trim().startsWith(key + "=")) {
                    String value = line.split("=")[1].trim();
                    return Integer.parseInt(value);
                }
            }
        } catch (Exception e) {
            // 忽略
        }
        return 8080;
    }
    
    /**
     * 从 YAML 文件提取端口
     */
    private int extractPortFromYaml(String content) {
        try {
            for (String line : content.split("\n")) {
                if (line.trim().startsWith("port:")) {
                    String value = line.split(":")[1].trim();
                    return Integer.parseInt(value);
                }
            }
        } catch (Exception e) {
            // 忽略
        }
        return 8080;
    }
}
