package com.blog.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/uploads")
@CrossOrigin(originPatterns = "http://localhost:*")
public class UploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDirProp;

    private Path getUploadDir() throws IOException {
        Path uploadDir = Paths.get(uploadDirProp).toAbsolutePath().normalize();
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        return uploadDir;
    }

    @PostMapping(path = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "customName", required = false) String customName) throws IOException {
        if (file == null || file.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "文件不能为空");
            return ResponseEntity.badRequest().body(err);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            Map<String, Object> err = new HashMap<>();
            err.put("message", "仅支持图片类型上传");
            return ResponseEntity.badRequest().body(err);
        }

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }

        String filename;
        boolean forcePng = false;
        if (customName != null && !customName.trim().isEmpty()) {
            // 强制使用自定义名称并保存为 .png
            String base = customName.trim();
            if (base.contains(".")) {
                base = base.substring(0, base.lastIndexOf('.'));
            }
            filename = base + ".png";
            forcePng = true;
        } else {
            filename = UUID.randomUUID().toString().replace("-", "") + ext;
        }

        Path target = getUploadDir().resolve(filename);

        if (forcePng) {
            // 将上传图片转为 PNG 存储
            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image == null) {
                    // 不是图片或读取失败，退回原样复制
                    Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    ImageIO.write(image, "png", target.toFile());
                    contentType = MediaType.IMAGE_PNG_VALUE;
                }
            } catch (Exception ex) {
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        }

        Map<String, Object> ok = new HashMap<>();
        // 返回相对路径，前后端通过代理统一访问
        ok.put("url", "/api/uploads/" + filename);
        ok.put("filename", filename);
        ok.put("contentType", contentType);
        return ResponseEntity.ok(ok);
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename, HttpServletRequest request)
            throws MalformedURLException {
        try {
            Path file = getUploadDir().resolve(filename).normalize();
            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }
            Resource resource = new UrlResource(file.toUri());
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(file.toString());
            } catch (Exception ignore) {}
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 从传入的 URL 或路径中提取文件名，仅允许删除上传目录中的文件
     */
    private String extractFilename(String urlOrPath) {
        if (urlOrPath == null || urlOrPath.trim().isEmpty()) {
            return null;
        }
        String path = urlOrPath.trim();
        // 去掉协议和域名
        try {
            if (path.startsWith("http://") || path.startsWith("https://")) {
                java.net.URL u = new java.net.URL(path);
                path = u.getPath();
            }
        } catch (Exception ignore) {}

        // 如果包含已知前缀 /api/uploads/ 则截取其后的部分
        String prefix = "/api/uploads/";
        int idx = path.indexOf(prefix);
        if (idx >= 0) {
            path = path.substring(idx + prefix.length());
        }

        // 仅取最后一段作为文件名，防止目录遍历
        int slash = path.lastIndexOf('/');
        if (slash >= 0) {
            path = path.substring(slash + 1);
        }

        // 简单校验：不允许包含 .. 或 路径分隔符
        if (path.contains("..") || path.contains("/") || path.contains("\\")) {
            return null;
        }
        return path;
    }

    /**
     * 删除单个图片文件
     */
    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<?> deleteImage(@PathVariable("filename") String filename) {
        try {
            if (filename == null || filename.trim().isEmpty()) {
                Map<String, Object> body = new HashMap<>();
                body.put("message", "文件名不能为空");
                return ResponseEntity.badRequest().body(body);
            }
            Path file = getUploadDir().resolve(filename).normalize();
            if (!file.startsWith(getUploadDir())) {
                Map<String, Object> body = new HashMap<>();
                body.put("message", "非法路径");
                return ResponseEntity.badRequest().body(body);
            }
            boolean deleted = Files.deleteIfExists(file);
            Map<String, Object> ok = new HashMap<>();
            ok.put("success", true);
            ok.put("deleted", deleted);
            ok.put("filename", filename);
            try {
                ok.put("uploadDir", getUploadDir().toString());
            } catch (IOException ignore) {}
            return ResponseEntity.ok(ok);
        } catch (IOException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }

    /**
     * 批量删除未使用的图片
     */
    @PostMapping(path = "/delete-unused", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteUnused(@RequestBody DeleteUnusedRequest request) {
        if (request == null || request.getImageUrls() == null) {
            Map<String, Object> body = new HashMap<>();
            body.put("message", "请求体不合法");
            return ResponseEntity.badRequest().body(body);
        }
        List<String> failed = new ArrayList<>();
        int deletedCount = 0;
        for (String url : request.getImageUrls()) {
            try {
                String filename = extractFilename(url);
                if (filename == null || filename.isEmpty()) {
                    failed.add(url);
                    continue;
                }
                Path file = getUploadDir().resolve(filename).normalize();
                if (!file.startsWith(getUploadDir())) {
                    failed.add(url);
                    continue;
                }
                if (Files.deleteIfExists(file)) {
                    deletedCount++;
                }
            } catch (Exception ex) {
                failed.add(url);
            }
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("deletedCount", deletedCount);
        resp.put("failedUrls", failed);
        try {
            resp.put("uploadDir", getUploadDir().toString());
        } catch (IOException ignore) {}
        return ResponseEntity.ok(resp);
    }

    /** 请求体 DTO */
    public static class DeleteUnusedRequest {
        private List<String> imageUrls;

        public List<String> getImageUrls() {
            return imageUrls;
        }

        public void setImageUrls(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }
    }
}

