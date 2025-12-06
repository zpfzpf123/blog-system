package com.blog.controller;

import com.blog.entity.Project;
import com.blog.entity.DevServiceConfig;
import com.blog.repository.DevServiceConfigRepository;
import com.blog.service.ProjectScanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 开发环境管理控制器
 */
@RestController
@RequestMapping("/api/projects/{projectId}/dev")
@CrossOrigin(originPatterns = {"http://localhost:*", "https://localhost:*"})
public class DevEnvironmentController {
    
    @Autowired
    private DevServiceConfigRepository serviceRepository;
    
    @Autowired
    private com.blog.service.ProjectService projectService;
    
    @Autowired
    private ProjectScanService projectScanService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 存储运行中的进程
    private static final Map<Long, Process> runningProcesses = new ConcurrentHashMap<>();
    private static final Map<Long, List<String>> processLogs = new ConcurrentHashMap<>();
    
    /**
     * 获取项目的所有服务配置
     */
    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getServices(@PathVariable Long projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<DevServiceConfig> services = serviceRepository.findByProjectIdOrderByStartOrderAsc(projectId);
            
            // 检查每个服务的运行状态
            List<Map<String, Object>> servicesWithStatus = new ArrayList<>();
            for (DevServiceConfig service : services) {
                Map<String, Object> serviceMap = new HashMap<>();
                serviceMap.put("id", service.getId());
                serviceMap.put("name", service.getName());
                serviceMap.put("type", service.getType());
                serviceMap.put("startCommand", service.getStartCommand());
                serviceMap.put("stopCommand", service.getStopCommand());
                serviceMap.put("workingDirectory", service.getWorkingDirectory());
                serviceMap.put("port", service.getPort());
                serviceMap.put("healthCheckUrl", service.getHealthCheckUrl());
                serviceMap.put("envVariables", service.getEnvVariables());
                serviceMap.put("autoRestart", service.getAutoRestart());
                serviceMap.put("startOrder", service.getStartOrder());
                serviceMap.put("enabled", service.getEnabled());
                serviceMap.put("description", service.getDescription());
                
                // 检查运行状态
                String status = checkServiceStatus(service);
                serviceMap.put("status", status);
                serviceMap.put("isRunning", "running".equals(status));
                
                servicesWithStatus.add(serviceMap);
            }
            
            result.put("success", true);
            result.put("services", servicesWithStatus);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取服务列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 扫描项目自动识别服务配置
     */
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanServices(@PathVariable Long projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Project project = projectService.getProjectById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.isEmpty()) {
                result.put("success", false);
                result.put("message", "项目没有配置本地路径，请先配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 调用扫描服务
            Map<String, Object> scanResult = projectScanService.scanProject(localPath, projectId);
            return ResponseEntity.ok(scanResult);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "扫描失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 导入扫描到的服务配置
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importServices(
            @PathVariable Long projectId,
            @RequestBody List<DevServiceConfig> services) {
        Map<String, Object> result = new HashMap<>();
        try {
            int imported = 0;
            for (DevServiceConfig service : services) {
                service.setProjectId(projectId);
                service.setId(null); // 确保是新建
                serviceRepository.save(service);
                imported++;
            }
            
            result.put("success", true);
            result.put("imported", imported);
            result.put("message", "成功导入 " + imported + " 个服务配置");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 添加服务配置
     */
    @PostMapping("/services")
    public ResponseEntity<Map<String, Object>> addService(
            @PathVariable Long projectId,
            @RequestBody DevServiceConfig service) {
        Map<String, Object> result = new HashMap<>();
        try {
            service.setProjectId(projectId);
            DevServiceConfig saved = serviceRepository.save(service);
            result.put("success", true);
            result.put("service", saved);
            result.put("message", "服务配置添加成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加服务配置失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 更新服务配置
     */
    @PutMapping("/services/{serviceId}")
    public ResponseEntity<Map<String, Object>> updateService(
            @PathVariable Long projectId,
            @PathVariable Long serviceId,
            @RequestBody DevServiceConfig service) {
        Map<String, Object> result = new HashMap<>();
        try {
            service.setId(serviceId);
            service.setProjectId(projectId);
            DevServiceConfig saved = serviceRepository.save(service);
            result.put("success", true);
            result.put("service", saved);
            result.put("message", "服务配置更新成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新服务配置失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 删除服务配置
     */
    @DeleteMapping("/services/{serviceId}")
    public ResponseEntity<Map<String, Object>> deleteService(
            @PathVariable Long projectId,
            @PathVariable Long serviceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 先停止服务
            stopServiceProcess(serviceId);
            serviceRepository.deleteById(serviceId);
            result.put("success", true);
            result.put("message", "服务配置删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除服务配置失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 启动单个服务
     */
    @PostMapping("/services/{serviceId}/start")
    public ResponseEntity<Map<String, Object>> startService(
            @PathVariable Long projectId,
            @PathVariable Long serviceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            DevServiceConfig service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("服务不存在"));
            
            // 检查端口是否被占用，如果占用则尝试杀死占用进程
            if (service.getPort() != null && isPortInUse(service.getPort())) {
                System.out.println("⚠️ 端口 " + service.getPort() + " 已被占用，正在杀死占用进程...");
                
                String processInfo = getPortProcessInfo(service.getPort());
                System.out.println("占用进程信息: " + processInfo);
                
                boolean killed = killProcessByPort(service.getPort());
                
                if (!killed) {
                    result.put("success", false);
                    result.put("message", "端口 " + service.getPort() + " 已被占用，且无法杀死占用进程。请手动关闭占用该端口的程序。");
                    result.put("portConflict", true);
                    result.put("processInfo", processInfo);
                    return ResponseEntity.badRequest().body(result);
                }
                
                System.out.println("✅ 成功杀死占用端口 " + service.getPort() + " 的进程");
                result.put("killedProcess", true);
            }
            
            // 获取项目路径
            Project project = projectService.getProjectById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String workDir = service.getWorkingDirectory();
            if (workDir == null || workDir.isEmpty()) {
                workDir = project.getLocalPath();
            } else if (!new File(workDir).isAbsolute()) {
                workDir = project.getLocalPath() + File.separator + workDir;
            }
            
            // 启动进程
            ProcessBuilder pb = createProcessBuilder(service.getStartCommand(), workDir);
            
            // 设置环境变量
            if (service.getEnvVariables() != null && !service.getEnvVariables().isEmpty()) {
                try {
                    Map<String, String> envVars = objectMapper.readValue(
                        service.getEnvVariables(), Map.class);
                    pb.environment().putAll(envVars);
                } catch (Exception e) {
                    System.err.println("解析环境变量失败: " + e.getMessage());
                }
            }
            
            Process process = pb.start();
            runningProcesses.put(serviceId, process);
            
            // 启动日志收集线程
            startLogCollector(serviceId, process);
            
            result.put("success", true);
            String successMsg = "服务 " + service.getName() + " 启动成功";
            if (result.containsKey("killedProcess")) {
                successMsg += " (已自动杀死占用端口的进程)";
            }
            result.put("message", successMsg);
            result.put("pid", process.pid());
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "启动服务失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 停止单个服务
     */
    @PostMapping("/services/{serviceId}/stop")
    public ResponseEntity<Map<String, Object>> stopService(
            @PathVariable Long projectId,
            @PathVariable Long serviceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            stopServiceProcess(serviceId);
            result.put("success", true);
            result.put("message", "服务已停止");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "停止服务失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 重启单个服务
     */
    @PostMapping("/services/{serviceId}/restart")
    public ResponseEntity<Map<String, Object>> restartService(
            @PathVariable Long projectId,
            @PathVariable Long serviceId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 先停止
            stopServiceProcess(serviceId);
            Thread.sleep(1000); // 等待1秒
            
            // 再启动
            return startService(projectId, serviceId);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "重启服务失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 启动所有服务
     */
    @PostMapping("/start-all")
    public ResponseEntity<Map<String, Object>> startAllServices(@PathVariable Long projectId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            List<DevServiceConfig> services = serviceRepository
                .findByProjectIdAndEnabledTrueOrderByStartOrderAsc(projectId);
            
            for (DevServiceConfig service : services) {
                try {
                    ResponseEntity<Map<String, Object>> response = startService(projectId, service.getId());
                    Map<String, Object> serviceResult = new HashMap<>();
                    serviceResult.put("serviceId", service.getId());
                    serviceResult.put("serviceName", service.getName());
                    serviceResult.put("success", response.getBody().get("success"));
                    serviceResult.put("message", response.getBody().get("message"));
                    results.add(serviceResult);
                    
                    // 等待一下再启动下一个
                    Thread.sleep(500);
                } catch (Exception e) {
                    Map<String, Object> serviceResult = new HashMap<>();
                    serviceResult.put("serviceId", service.getId());
                    serviceResult.put("serviceName", service.getName());
                    serviceResult.put("success", false);
                    serviceResult.put("message", e.getMessage());
                    results.add(serviceResult);
                }
            }
            
            result.put("success", true);
            result.put("results", results);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量启动失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 停止所有服务
     */
    @PostMapping("/stop-all")
    public ResponseEntity<Map<String, Object>> stopAllServices(@PathVariable Long projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<DevServiceConfig> services = serviceRepository.findByProjectIdOrderByStartOrderAsc(projectId);
            
            for (DevServiceConfig service : services) {
                stopServiceProcess(service.getId());
            }
            
            result.put("success", true);
            result.put("message", "所有服务已停止");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量停止失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 获取服务日志
     */
    @GetMapping("/services/{serviceId}/logs")
    public ResponseEntity<Map<String, Object>> getServiceLogs(
            @PathVariable Long projectId,
            @PathVariable Long serviceId,
            @RequestParam(defaultValue = "100") int lines) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> logs = processLogs.getOrDefault(serviceId, new ArrayList<>());
            int start = Math.max(0, logs.size() - lines);
            List<String> recentLogs = logs.subList(start, logs.size());
            
            result.put("success", true);
            result.put("logs", recentLogs);
            result.put("totalLines", logs.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取日志失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 清除服务日志
     */
    @DeleteMapping("/services/{serviceId}/logs")
    public ResponseEntity<Map<String, Object>> clearServiceLogs(
            @PathVariable Long projectId,
            @PathVariable Long serviceId) {
        Map<String, Object> result = new HashMap<>();
        processLogs.remove(serviceId);
        result.put("success", true);
        result.put("message", "日志已清除");
        return ResponseEntity.ok(result);
    }
    
    /**
     * 检查端口占用
     */
    @GetMapping("/check-port/{port}")
    public ResponseEntity<Map<String, Object>> checkPort(
            @PathVariable Long projectId,
            @PathVariable int port) {
        Map<String, Object> result = new HashMap<>();
        boolean inUse = isPortInUse(port);
        result.put("port", port);
        result.put("inUse", inUse);
        
        if (inUse) {
            // 尝试获取占用进程信息
            String processInfo = getPortProcessInfo(port);
            result.put("processInfo", processInfo);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 批量检查端口
     */
    @PostMapping("/check-ports")
    public ResponseEntity<Map<String, Object>> checkPorts(
            @PathVariable Long projectId,
            @RequestBody List<Integer> ports) {
        Map<String, Object> result = new HashMap<>();
        Map<Integer, Boolean> portStatus = new HashMap<>();
        
        for (Integer port : ports) {
            portStatus.put(port, isPortInUse(port));
        }
        
        result.put("success", true);
        result.put("ports", portStatus);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 杀死占用指定端口的进程
     */
    @PostMapping("/kill-port/{port}")
    public ResponseEntity<Map<String, Object>> killPort(
            @PathVariable Long projectId,
            @PathVariable int port) {
        Map<String, Object> result = new HashMap<>();
        
        if (!isPortInUse(port)) {
            result.put("success", false);
            result.put("message", "端口 " + port + " 未被占用");
            return ResponseEntity.badRequest().body(result);
        }
        
        String processInfo = getPortProcessInfo(port);
        boolean killed = killProcessByPort(port);
        
        if (killed) {
            result.put("success", true);
            result.put("message", "成功杀死占用端口 " + port + " 的进程");
            result.put("processInfo", processInfo);
        } else {
            result.put("success", false);
            result.put("message", "无法杀死占用端口 " + port + " 的进程");
            result.put("processInfo", processInfo);
        }
        
        return ResponseEntity.ok(result);
    }
    
    // ========== 辅助方法 ==========
    
    private String checkServiceStatus(DevServiceConfig service) {
        // 检查进程是否在运行
        Process process = runningProcesses.get(service.getId());
        if (process != null && process.isAlive()) {
            return "running";
        }
        
        // 检查端口是否在监听
        if (service.getPort() != null && isPortInUse(service.getPort())) {
            return "running";
        }
        
        // 检查健康检查URL
        if (service.getHealthCheckUrl() != null && !service.getHealthCheckUrl().isEmpty()) {
            if (checkHealthUrl(service.getHealthCheckUrl())) {
                return "running";
            }
        }
        
        return "stopped";
    }
    
    private boolean isPortInUse(int port) {
        try (Socket socket = new Socket("localhost", port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String getPortProcessInfo(int port) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", "netstat -ano | findstr :" + port);
            } else {
                pb = new ProcessBuilder("sh", "-c", "lsof -i :" + port);
            }
            
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            process.waitFor(5, TimeUnit.SECONDS);
            return output.toString().trim();
        } catch (Exception e) {
            return "无法获取进程信息";
        }
    }
    
    /**
     * 杀死占用指定端口的进程
     */
    private boolean killProcessByPort(int port) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows: 先获取PID，再杀死进程
                // 第一步：获取占用端口的PID
                ProcessBuilder pb1 = new ProcessBuilder("cmd", "/c", "netstat -ano | findstr :" + port);
                Process p1 = pb1.start();
                
                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(p1.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                }
                p1.waitFor(3, TimeUnit.SECONDS);
                
                // 解析PID（最后一列）
                String[] lines = output.toString().split("\n");
                for (String line : lines) {
                    if (line.contains("LISTENING") || line.contains("ESTABLISHED")) {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 5) {
                            String pid = parts[parts.length - 1];
                            try {
                                // 第二步：杀死进程
                                ProcessBuilder pb2 = new ProcessBuilder("cmd", "/c", "taskkill /F /PID " + pid);
                                Process p2 = pb2.start();
                                p2.waitFor(3, TimeUnit.SECONDS);
                                System.out.println("已杀死 PID: " + pid);
                            } catch (Exception e) {
                                System.err.println("杀死 PID " + pid + " 失败: " + e.getMessage());
                            }
                        }
                    }
                }
                
                // 等待一下确保进程被杀死
                Thread.sleep(1000);
                
                return !isPortInUse(port);
            } else {
                // Linux/Mac: 使用 lsof 和 kill
                ProcessBuilder pb = new ProcessBuilder("sh", "-c", "kill -9 $(lsof -t -i:" + port + ")");
                Process process = pb.start();
                process.waitFor(3, TimeUnit.SECONDS);
                
                Thread.sleep(500);
                
                return !isPortInUse(port);
            }
        } catch (Exception e) {
            System.err.println("杀死端口 " + port + " 的进程失败: " + e.getMessage());
            return false;
        }
    }
    
    private boolean checkHealthUrl(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            int responseCode = conn.getResponseCode();
            return responseCode >= 200 && responseCode < 400;
        } catch (Exception e) {
            return false;
        }
    }
    
    private ProcessBuilder createProcessBuilder(String command, String workDir) {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder pb;
        
        if (os.contains("win")) {
            pb = new ProcessBuilder("cmd", "/c", command);
        } else {
            pb = new ProcessBuilder("sh", "-c", command);
        }
        
        pb.directory(new File(workDir));
        pb.redirectErrorStream(true);
        return pb;
    }
    
    private void startLogCollector(Long serviceId, Process process) {
        processLogs.put(serviceId, Collections.synchronizedList(new ArrayList<>()));
        
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    List<String> logs = processLogs.get(serviceId);
                    if (logs != null) {
                        logs.add("[" + java.time.LocalDateTime.now().toString() + "] " + line);
                        // 限制日志行数
                        while (logs.size() > 1000) {
                            logs.remove(0);
                        }
                    }
                }
            } catch (Exception e) {
                // 进程结束
            }
        }).start();
    }
    
    private void stopServiceProcess(Long serviceId) {
        Process process = runningProcesses.remove(serviceId);
        if (process != null && process.isAlive()) {
            process.descendants().forEach(ProcessHandle::destroy);
            process.destroy();
            try {
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                process.destroyForcibly();
            }
        }
    }
}
