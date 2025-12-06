package com.blog.controller;

import com.blog.entity.Project;
import com.blog.entity.ProjectApi;
import com.blog.repository.ProjectApiRepository;
import com.blog.service.ProjectService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

/**
 * 项目API文档管理控制器
 */
@RestController
@RequestMapping("/api/projects/{projectId}/apis")
@CrossOrigin(originPatterns = {"http://localhost:*", "https://localhost:*"})
public class ProjectApiController {
    
    @Autowired
    private ProjectApiRepository apiRepository;
    
    @Autowired
    private ProjectService projectService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 获取项目的所有API
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getApis(
            @PathVariable Long projectId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ProjectApi> apis;
            if (category != null && !category.isEmpty()) {
                apis = apiRepository.findByProjectIdAndCategoryOrderBySortOrderAsc(projectId, category);
            } else if (status != null && !status.isEmpty()) {
                apis = apiRepository.findByProjectIdAndStatusOrderBySortOrderAsc(projectId, status);
            } else {
                apis = apiRepository.findByProjectIdOrderBySortOrderAsc(projectId);
            }
            
            // 获取分类列表
            List<String> categories = apiRepository.findDistinctCategoriesByProjectId(projectId);
            
            // 统计各状态数量
            Map<String, Long> statusCount = new HashMap<>();
            List<ProjectApi> allApis = apiRepository.findByProjectIdOrderBySortOrderAsc(projectId);
            statusCount.put("total", (long) allApis.size());
            statusCount.put("pending", allApis.stream().filter(a -> "pending".equals(a.getStatus())).count());
            statusCount.put("developing", allApis.stream().filter(a -> "developing".equals(a.getStatus())).count());
            statusCount.put("testing", allApis.stream().filter(a -> "testing".equals(a.getStatus())).count());
            statusCount.put("completed", allApis.stream().filter(a -> "completed".equals(a.getStatus())).count());
            
            result.put("success", true);
            result.put("apis", apis);
            result.put("categories", categories);
            result.put("statusCount", statusCount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取API列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 获取单个API详情
     */
    @GetMapping("/{apiId}")
    public ResponseEntity<Map<String, Object>> getApi(
            @PathVariable Long projectId,
            @PathVariable Long apiId) {
        Map<String, Object> result = new HashMap<>();
        try {
            ProjectApi api = apiRepository.findById(apiId)
                .orElseThrow(() -> new RuntimeException("API不存在"));
            result.put("success", true);
            result.put("api", api);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }
    
    /**
     * 添加API
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addApi(
            @PathVariable Long projectId,
            @RequestBody ProjectApi api) {
        Map<String, Object> result = new HashMap<>();
        try {
            api.setProjectId(projectId);
            ProjectApi saved = apiRepository.save(api);
            result.put("success", true);
            result.put("api", saved);
            result.put("message", "API添加成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加API失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 批量添加API
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> addApis(
            @PathVariable Long projectId,
            @RequestBody List<ProjectApi> apis) {
        Map<String, Object> result = new HashMap<>();
        try {
            for (ProjectApi api : apis) {
                api.setProjectId(projectId);
            }
            List<ProjectApi> saved = apiRepository.saveAll(apis);
            result.put("success", true);
            result.put("count", saved.size());
            result.put("message", "成功添加 " + saved.size() + " 个API");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "批量添加失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 更新API
     */
    @PutMapping("/{apiId}")
    public ResponseEntity<Map<String, Object>> updateApi(
            @PathVariable Long projectId,
            @PathVariable Long apiId,
            @RequestBody ProjectApi api) {
        Map<String, Object> result = new HashMap<>();
        try {
            api.setId(apiId);
            api.setProjectId(projectId);
            ProjectApi saved = apiRepository.save(api);
            result.put("success", true);
            result.put("api", saved);
            result.put("message", "API更新成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新API失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 删除API
     */
    @DeleteMapping("/{apiId}")
    public ResponseEntity<Map<String, Object>> deleteApi(
            @PathVariable Long projectId,
            @PathVariable Long apiId) {
        Map<String, Object> result = new HashMap<>();
        try {
            apiRepository.deleteById(apiId);
            result.put("success", true);
            result.put("message", "API删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除API失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 更新API状态
     */
    @PatchMapping("/{apiId}/status")
    public ResponseEntity<Map<String, Object>> updateApiStatus(
            @PathVariable Long projectId,
            @PathVariable Long apiId,
            @RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            ProjectApi api = apiRepository.findById(apiId)
                .orElseThrow(() -> new RuntimeException("API不存在"));
            api.setStatus(body.get("status"));
            apiRepository.save(api);
            result.put("success", true);
            result.put("message", "状态更新成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 切换Mock状态
     */
    @PatchMapping("/{apiId}/mock")
    public ResponseEntity<Map<String, Object>> toggleMock(
            @PathVariable Long projectId,
            @PathVariable Long apiId,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            ProjectApi api = apiRepository.findById(apiId)
                .orElseThrow(() -> new RuntimeException("API不存在"));
            
            if (body.containsKey("enabled")) {
                api.setMockEnabled((Boolean) body.get("enabled"));
            }
            if (body.containsKey("mockData")) {
                api.setMockData((String) body.get("mockData"));
            }
            if (body.containsKey("mockDelay")) {
                api.setMockDelay((Integer) body.get("mockDelay"));
            }
            if (body.containsKey("mockStatusCode")) {
                api.setMockStatusCode((Integer) body.get("mockStatusCode"));
            }
            
            apiRepository.save(api);
            result.put("success", true);
            result.put("message", "Mock配置更新成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 调试API接口（类似Postman）
     */
    @PostMapping("/{apiId}/test")
    public ResponseEntity<Map<String, Object>> testApi(
            @PathVariable Long projectId,
            @PathVariable Long apiId,
            @RequestBody Map<String, Object> testRequest) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            ProjectApi api = apiRepository.findById(apiId)
                .orElseThrow(() -> new RuntimeException("API不存在"));
            
            // 如果启用了Mock，返回Mock数据
            if (api.getMockEnabled() != null && api.getMockEnabled()) {
                if (api.getMockDelay() != null && api.getMockDelay() > 0) {
                    Thread.sleep(api.getMockDelay());
                }
                
                result.put("success", true);
                result.put("isMock", true);
                result.put("statusCode", api.getMockStatusCode());
                result.put("responseTime", System.currentTimeMillis() - startTime);
                
                try {
                    result.put("data", objectMapper.readTree(api.getMockData()));
                } catch (Exception e) {
                    result.put("data", api.getMockData());
                }
                
                return ResponseEntity.ok(result);
            }
            
            // 真实请求
            String baseUrl = (String) testRequest.getOrDefault("baseUrl", "http://localhost:8080");
            String url = baseUrl + api.getPath();
            
            // 替换路径参数
            Map<String, String> pathParams = (Map<String, String>) testRequest.get("pathParams");
            if (pathParams != null) {
                for (Map.Entry<String, String> entry : pathParams.entrySet()) {
                    url = url.replace("{" + entry.getKey() + "}", entry.getValue());
                }
            }
            
            // 添加查询参数
            Map<String, String> queryParams = (Map<String, String>) testRequest.get("queryParams");
            if (queryParams != null && !queryParams.isEmpty()) {
                StringBuilder sb = new StringBuilder(url);
                sb.append("?");
                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
                url = sb.substring(0, sb.length() - 1);
            }
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            Map<String, String> requestHeaders = (Map<String, String>) testRequest.get("headers");
            if (requestHeaders != null) {
                requestHeaders.forEach(headers::set);
            }
            if (!headers.containsKey("Content-Type")) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }
            
            // 构建请求体
            Object requestBody = testRequest.get("body");
            HttpEntity<?> entity = new HttpEntity<>(requestBody, headers);
            
            // 发送请求
            HttpMethod method = HttpMethod.valueOf(api.getMethod().toUpperCase());
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
            
            result.put("success", true);
            result.put("isMock", false);
            result.put("statusCode", response.getStatusCodeValue());
            result.put("responseTime", System.currentTimeMillis() - startTime);
            result.put("responseHeaders", response.getHeaders().toSingleValueMap());
            
            try {
                result.put("data", objectMapper.readTree(response.getBody()));
            } catch (Exception e) {
                result.put("data", response.getBody());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "请求失败: " + e.getMessage());
            result.put("responseTime", System.currentTimeMillis() - startTime);
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * 从项目代码自动扫描API
     */
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanApis(@PathVariable Long projectId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Project project = projectService.getProjectById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.isEmpty()) {
                result.put("success", false);
                result.put("message", "项目没有配置本地路径，请先在项目设置中配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查路径是否存在
            Path projectPath = Paths.get(localPath);
            if (!Files.exists(projectPath)) {
                result.put("success", false);
                result.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            if (!Files.isDirectory(projectPath)) {
                result.put("success", false);
                result.put("message", "本地路径不是目录: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            List<ProjectApi> scannedApis = new ArrayList<>();
            List<String> scanLogs = new ArrayList<>();
            
            // 扫描Spring Boot Controller
            try {
                List<ProjectApi> springApis = scanSpringControllers(localPath, projectId);
                scannedApis.addAll(springApis);
                scanLogs.add("扫描Spring Controller: 发现 " + springApis.size() + " 个API");
            } catch (Exception e) {
                scanLogs.add("扫描Spring Controller失败: " + e.getMessage());
            }
            
            // 扫描Vue/React API调用
            try {
                List<ProjectApi> frontendApis = scanFrontendApis(localPath, projectId);
                scannedApis.addAll(frontendApis);
                scanLogs.add("扫描前端API调用: 发现 " + frontendApis.size() + " 个API");
            } catch (Exception e) {
                scanLogs.add("扫描前端API失败: " + e.getMessage());
            }
            
            // 去重（根据method+path）
            Map<String, ProjectApi> uniqueApis = new LinkedHashMap<>();
            for (ProjectApi api : scannedApis) {
                String key = api.getMethod() + ":" + api.getPath();
                if (!uniqueApis.containsKey(key)) {
                    uniqueApis.put(key, api);
                }
            }
            List<ProjectApi> dedupedApis = new ArrayList<>(uniqueApis.values());
            
            result.put("success", true);
            result.put("scannedApis", dedupedApis);
            result.put("count", dedupedApis.size());
            result.put("scanLogs", scanLogs);
            result.put("message", "扫描完成，发现 " + dedupedApis.size() + " 个API（去重后）");
            result.put("localPath", localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "扫描失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 导入扫描到的API
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importApis(
            @PathVariable Long projectId,
            @RequestBody List<ProjectApi> apis) {
        Map<String, Object> result = new HashMap<>();
        try {
            int imported = 0;
            for (ProjectApi api : apis) {
                api.setProjectId(projectId);
                api.setId(null); // 确保是新建
                apiRepository.save(api);
                imported++;
            }
            
            result.put("success", true);
            result.put("imported", imported);
            result.put("message", "成功导入 " + imported + " 个API");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 导出API文档
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportApis(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "json") String format) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<ProjectApi> apis = apiRepository.findByProjectIdOrderBySortOrderAsc(projectId);
            Project project = projectService.getProjectById(projectId)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            if ("markdown".equals(format)) {
                String markdown = generateMarkdownDoc(project, apis);
                result.put("success", true);
                result.put("format", "markdown");
                result.put("content", markdown);
            } else {
                // JSON格式（OpenAPI风格）
                ObjectNode openApi = generateOpenApiDoc(project, apis);
                result.put("success", true);
                result.put("format", "json");
                result.put("content", openApi);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "导出失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    // ========== 辅助方法 ==========
    
    private List<ProjectApi> scanSpringControllers(String projectPath, Long projectId) throws Exception {
        List<ProjectApi> apis = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        Path basePath = Paths.get(projectPath);
        if (!Files.exists(basePath)) {
            throw new RuntimeException("项目路径不存在: " + projectPath);
        }
        
        // 查找所有Java文件
        Files.walk(basePath)
            .filter(p -> p.toString().endsWith(".java"))
            .filter(p -> {
                String pathStr = p.toString().toLowerCase();
                return pathStr.contains("controller");
            })
            .forEach(path -> {
                try {
                    String content = Files.readString(path);
                    List<ProjectApi> parsed = parseSpringController(content, projectId);
                    // 添加来源文件信息
                    String relativePath = basePath.relativize(path).toString();
                    for (ProjectApi api : parsed) {
                        api.setDescription("来源: " + relativePath);
                    }
                    apis.addAll(parsed);
                } catch (Exception e) {
                    errors.add("读取文件失败 " + path + ": " + e.getMessage());
                }
            });
        
        if (!errors.isEmpty()) {
            System.err.println("扫描Spring Controller时出现警告: " + String.join("; ", errors));
        }
        
        return apis;
    }
    
    private List<ProjectApi> parseSpringController(String content, Long projectId) {
        List<ProjectApi> apis = new ArrayList<>();
        
        // 提取类级别的RequestMapping
        String classPath = "";
        Pattern classPattern = Pattern.compile("@RequestMapping\\s*\\(\\s*[\"']([^\"']+)[\"']\\s*\\)");
        Matcher classMatcher = classPattern.matcher(content);
        if (classMatcher.find()) {
            classPath = classMatcher.group(1);
        }
        
        // 提取方法级别的映射
        Pattern methodPattern = Pattern.compile(
            "@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\\s*\\(([^)]*?)\\)\\s*" +
            "(?:public\\s+)?\\S+\\s+(\\w+)\\s*\\(",
            Pattern.DOTALL
        );
        
        Matcher methodMatcher = methodPattern.matcher(content);
        while (methodMatcher.find()) {
            String annotation = methodMatcher.group(1);
            String params = methodMatcher.group(2);
            String methodName = methodMatcher.group(3);
            
            ProjectApi api = new ProjectApi();
            api.setProjectId(projectId);
            api.setName(methodName);
            api.setStatus("pending");
            
            // 解析HTTP方法
            switch (annotation) {
                case "GetMapping": api.setMethod("GET"); break;
                case "PostMapping": api.setMethod("POST"); break;
                case "PutMapping": api.setMethod("PUT"); break;
                case "DeleteMapping": api.setMethod("DELETE"); break;
                case "PatchMapping": api.setMethod("PATCH"); break;
                default: api.setMethod("GET");
            }
            
            // 解析路径
            String methodPath = "";
            Pattern pathPattern = Pattern.compile("[\"']([^\"']+)[\"']");
            Matcher pathMatcher = pathPattern.matcher(params);
            if (pathMatcher.find()) {
                methodPath = pathMatcher.group(1);
            }
            
            api.setPath(classPath + methodPath);
            api.setCategory("后端API");
            
            apis.add(api);
        }
        
        return apis;
    }
    
    private List<ProjectApi> scanFrontendApis(String projectPath, Long projectId) throws Exception {
        List<ProjectApi> apis = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        Path basePath = Paths.get(projectPath);
        if (!Files.exists(basePath)) {
            throw new RuntimeException("项目路径不存在: " + projectPath);
        }
        
        // 查找所有TS/JS/Vue文件
        Files.walk(basePath)
            .filter(p -> {
                String name = p.toString();
                return name.endsWith(".ts") || name.endsWith(".js") || name.endsWith(".vue");
            })
            .filter(p -> !p.toString().contains("node_modules"))
            .filter(p -> !p.toString().contains(".venv"))
            .filter(p -> !p.toString().contains("dist"))
            .forEach(path -> {
                try {
                    String content = Files.readString(path);
                    List<ProjectApi> parsed = parseFrontendApi(content, projectId);
                    // 添加来源文件信息
                    String relativePath = basePath.relativize(path).toString();
                    for (ProjectApi api : parsed) {
                        api.setDescription("来源: " + relativePath);
                    }
                    apis.addAll(parsed);
                } catch (Exception e) {
                    errors.add("读取文件失败 " + path + ": " + e.getMessage());
                }
            });
        
        if (!errors.isEmpty()) {
            System.err.println("扫描前端API时出现警告: " + String.join("; ", errors));
        }
        
        return apis;
    }
    
    private List<ProjectApi> parseFrontendApi(String content, Long projectId) {
        List<ProjectApi> apis = new ArrayList<>();
        
        // 匹配axios调用
        Pattern axiosPattern = Pattern.compile(
            "axios\\.(get|post|put|delete|patch)\\s*\\(\\s*[`'\"]([^`'\"]+)[`'\"]",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher matcher = axiosPattern.matcher(content);
        while (matcher.find()) {
            String method = matcher.group(1).toUpperCase();
            String path = matcher.group(2);
            
            ProjectApi api = new ProjectApi();
            api.setProjectId(projectId);
            api.setMethod(method);
            api.setPath(path);
            api.setName(path);
            api.setStatus("pending");
            api.setCategory("前端调用");
            
            apis.add(api);
        }
        
        return apis;
    }
    
    private String generateMarkdownDoc(Project project, List<ProjectApi> apis) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(project.getName()).append(" API文档\n\n");
        sb.append("## 概述\n\n");
        sb.append("- 总接口数: ").append(apis.size()).append("\n");
        sb.append("- 生成时间: ").append(java.time.LocalDateTime.now()).append("\n\n");
        
        // 按分类分组
        Map<String, List<ProjectApi>> grouped = new LinkedHashMap<>();
        for (ProjectApi api : apis) {
            String category = api.getCategory() != null ? api.getCategory() : "未分类";
            grouped.computeIfAbsent(category, k -> new ArrayList<>()).add(api);
        }
        
        for (Map.Entry<String, List<ProjectApi>> entry : grouped.entrySet()) {
            sb.append("## ").append(entry.getKey()).append("\n\n");
            
            for (ProjectApi api : entry.getValue()) {
                sb.append("### ").append(api.getName()).append("\n\n");
                sb.append("- **方法**: `").append(api.getMethod()).append("`\n");
                sb.append("- **路径**: `").append(api.getPath()).append("`\n");
                sb.append("- **状态**: ").append(api.getStatus()).append("\n");
                
                if (api.getDescription() != null) {
                    sb.append("- **描述**: ").append(api.getDescription()).append("\n");
                }
                
                sb.append("\n");
            }
        }
        
        return sb.toString();
    }
    
    private ObjectNode generateOpenApiDoc(Project project, List<ProjectApi> apis) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("openapi", "3.0.0");
        
        ObjectNode info = root.putObject("info");
        info.put("title", project.getName() + " API");
        info.put("version", "1.0.0");
        
        ObjectNode paths = root.putObject("paths");
        
        for (ProjectApi api : apis) {
            ObjectNode pathItem = paths.has(api.getPath()) 
                ? (ObjectNode) paths.get(api.getPath())
                : paths.putObject(api.getPath());
            
            ObjectNode operation = pathItem.putObject(api.getMethod().toLowerCase());
            operation.put("summary", api.getName());
            operation.put("description", api.getDescription());
            
            ArrayNode tags = operation.putArray("tags");
            if (api.getCategory() != null) {
                tags.add(api.getCategory());
            }
            
            ObjectNode responses = operation.putObject("responses");
            ObjectNode response200 = responses.putObject("200");
            response200.put("description", "成功");
        }
        
        return root;
    }
}
