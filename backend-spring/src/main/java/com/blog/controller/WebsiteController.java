package com.blog.controller;

import com.blog.dto.WebsiteCreateRequest;
import com.blog.dto.WebsiteDTO;
import com.blog.dto.WebsiteQueryRequest;
import com.blog.dto.WebsitesResponseDTO;
import com.blog.dto.WebsiteScrapeRequest;
import com.blog.dto.WebsiteScrapeResponse;
import com.blog.service.WebsiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import java.util.List;
import java.util.Optional;

/**
 * 网站管理控制器
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
@RestController
@RequestMapping("/api/websites")
@CrossOrigin(originPatterns = "http://localhost:*")
public class WebsiteController {
    
    @Autowired
    private WebsiteService websiteService;
    
    /**
     * 创建网站
     * POST /api/websites
     */
    @PostMapping
    public ResponseEntity<WebsiteDTO> createWebsite(@Valid @RequestBody WebsiteCreateRequest request) {
        System.out.println("=== 创建网站请求开始 ===");
        System.out.println("请求参数: " + request);
        System.out.println("分类IDs: " + request.getCategoryIds());
        
        try {
            System.out.println("开始调用服务层创建网站...");
            WebsiteDTO website = websiteService.createWebsite(request);
            System.out.println("网站创建成功，返回结果: " + website);
            System.out.println("=== 创建网站请求结束 ===");
            return ResponseEntity.status(HttpStatus.CREATED).body(website);
        } catch (Exception e) {
            System.err.println("=== 创建网站失败 ===");
            System.err.println("请求参数: " + request);
            System.err.println("错误类型: " + e.getClass().getSimpleName());
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== 错误详情结束 ===");
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 根据ID获取网站详情
     * GET /api/websites/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<WebsiteDTO> getWebsiteById(@PathVariable Long id) {
        Optional<WebsiteDTO> website = websiteService.findWebsiteById(id);
        return website.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 根据URL获取网站详情
     * GET /api/websites/url?url={url}
     */
    @GetMapping("/url")
    public ResponseEntity<WebsiteDTO> getWebsiteByUrl(@RequestParam String url) {
        Optional<WebsiteDTO> website = websiteService.findWebsiteByUrl(url);
        return website.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新网站
     * PUT /api/websites/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<WebsiteDTO> updateWebsite(@PathVariable Long id, 
                                                  @Valid @RequestBody WebsiteCreateRequest request) {
        try {
            WebsiteDTO website = websiteService.updateWebsite(id, request);
            return ResponseEntity.ok(website);
        } catch (Exception e) {
            // 添加详细的错误日志
            System.err.println("更新网站失败 - ID: " + id);
            System.err.println("请求参数: " + request);
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 删除网站
     * DELETE /api/websites/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebsite(@PathVariable Long id) {
        try {
            websiteService.deleteWebsite(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 批量删除网站
     * DELETE /api/websites/batch
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteWebsites(@RequestBody List<Long> ids) {
        try {
            websiteService.deleteWebsites(ids);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 查询网站列表（支持分页、搜索、筛选）
     * GET /api/websites
     */
    @GetMapping
    public ResponseEntity<WebsitesResponseDTO> getWebsites(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Boolean isFavorite,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(name = "page", required = false, defaultValue = "1") String pageStr,
            @RequestParam(name = "size", required = false, defaultValue = "12") String sizeStr) {
        
        // 添加调试日志
        System.out.println("=== getWebsites 方法被调用 ===");
        System.out.println("接收到的参数:");
        System.out.println("  keyword: " + keyword);
        System.out.println("  categoryId: " + categoryId + " (类型: " + (categoryId != null ? categoryId.getClass().getSimpleName() : "null") + ")");
        System.out.println("  categoryIds: " + categoryIds);
        System.out.println("  isFavorite: " + isFavorite);
        System.out.println("  isActive: " + isActive);
        System.out.println("  status: " + status);
        System.out.println("  sortBy: " + sortBy);
        System.out.println("  sortOrder: " + sortOrder);
        System.out.println("  pageStr: " + pageStr);
        System.out.println("  sizeStr: " + sizeStr);
        
        // 参数验证和转换
        Integer page;
        Integer size;
        
        try {
            page = Integer.parseInt(pageStr);
            size = Integer.parseInt(sizeStr);
            
            // 验证分页参数的有效性
            if (page < 1) {
                return ResponseEntity.badRequest().build();
            }
            if (size < 1 || size > 100) { // 限制每页最大数量为100
                return ResponseEntity.badRequest().build();
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        
        WebsiteQueryRequest request = new WebsiteQueryRequest();
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setCategoryIds(categoryIds);
        request.setIsFavorite(isFavorite);
        request.setIsActive(isActive);
        request.setStatus(status);
        request.setSortBy(sortBy);
        request.setSortOrder(sortOrder);
        request.setPage(page);
        request.setSize(size);
        
        System.out.println("构建的请求对象:");
        System.out.println("  request.categoryId: " + request.getCategoryId());
        System.out.println("  request.categoryIds: " + request.getCategoryIds());
        System.out.println("  request.hasCategoryFilter(): " + request.hasCategoryFilter());
        
        try {
            WebsitesResponseDTO response = websiteService.findWebsites(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 打印异常信息以便调试
            System.err.println("Error in getWebsites: " + e.getMessage());
            System.err.println("Request parameters: " + request.toString());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 测试分页参数处理
     * GET /api/websites/test-pagination
     */
    @GetMapping("/test-pagination")
    public ResponseEntity<String> testPagination(
            @RequestParam(defaultValue = "1") String pageStr,
            @RequestParam(defaultValue = "12") String sizeStr) {
        
        try {
            Integer page = Integer.parseInt(pageStr);
            Integer size = Integer.parseInt(sizeStr);
            
            if (page < 1 || size < 1 || size > 100) {
                return ResponseEntity.badRequest().body("Invalid pagination parameters");
            }
            
            return ResponseEntity.ok("Pagination test successful: page=" + page + ", size=" + size);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid number format: " + e.getMessage());
        }
    }
    
    /**
     * 根据分类ID获取网站列表
     * GET /api/websites/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<WebsiteDTO>> getWebsitesByCategory(@PathVariable Long categoryId) {
        try {
            List<WebsiteDTO> websites = websiteService.findWebsitesByCategory(categoryId);
            return ResponseEntity.ok(websites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 根据收藏状态获取网站列表
     * GET /api/websites/favorite?isFavorite={isFavorite}
     */
    @GetMapping("/favorite")
    public ResponseEntity<List<WebsiteDTO>> getWebsitesByFavorite(@RequestParam Boolean isFavorite) {
        try {
            List<WebsiteDTO> websites = websiteService.findWebsitesByFavorite(isFavorite);
            return ResponseEntity.ok(websites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 搜索网站
     * GET /api/websites/search?keyword={keyword}
     */
    @GetMapping("/search")
    public ResponseEntity<List<WebsiteDTO>> searchWebsites(@RequestParam String keyword) {
        try {
            List<WebsiteDTO> websites = websiteService.searchWebsites(keyword);
            return ResponseEntity.ok(websites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    

    
    /**
     * 获取热门网站
     * GET /api/websites/popular?limit={limit}
     */
    @GetMapping("/popular")
    public ResponseEntity<List<WebsiteDTO>> getPopularWebsites(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<WebsiteDTO> websites = websiteService.getPopularWebsites(limit);
            return ResponseEntity.ok(websites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取最新网站
     * GET /api/websites/recent?limit={limit}
     */
    @GetMapping("/recent")
    public ResponseEntity<List<WebsiteDTO>> getRecentWebsites(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<WebsiteDTO> websites = websiteService.getRecentWebsites(limit);
            return ResponseEntity.ok(websites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 增加网站访问次数
     * POST /api/websites/{id}/visit
     */
    @PostMapping("/{id}/visit")
    public ResponseEntity<Void> incrementVisitCount(@PathVariable Long id) {
        try {
            websiteService.incrementVisitCount(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 切换网站收藏状态
     * POST /api/websites/{id}/favorite
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<Void> toggleFavorite(@PathVariable Long id) {
        try {
            websiteService.toggleFavorite(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 检查网站状态
     * POST /api/websites/{id}/check
     */
    @PostMapping("/{id}/check")
    public ResponseEntity<Void> checkWebsiteStatus(@PathVariable Long id) {
        try {
            websiteService.checkWebsiteStatus(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 批量检查网站状态
     * POST /api/websites/check/batch
     */
    @PostMapping("/check/batch")
    public ResponseEntity<Void> batchCheckWebsiteStatus() {
        try {
            websiteService.batchCheckWebsiteStatus();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取网站统计信息
     * GET /api/websites/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<WebsiteService.WebsiteStatistics> getWebsiteStatistics() {
        try {
            WebsiteService.WebsiteStatistics statistics = websiteService.getWebsiteStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导入网站数据
     * POST /api/websites/import
     */
    @PostMapping("/import")
    public ResponseEntity<WebsiteService.ImportResult> importWebsites(@RequestBody List<WebsiteCreateRequest> requests) {
        try {
            WebsiteService.ImportResult result = websiteService.importWebsites(requests);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导出网站数据
     * GET /api/websites/export
     */
    @GetMapping("/export")
    public ResponseEntity<List<WebsiteDTO>> exportWebsites(WebsiteQueryRequest request) {
        try {
            List<WebsiteDTO> websites = websiteService.exportWebsites(request);
            return ResponseEntity.ok(websites);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 抓取网站信息
     * POST /api/websites/scrape
     */
    @PostMapping("/scrape")
    public ResponseEntity<WebsiteScrapeResponse> scrapeWebsiteInfo(@Valid @RequestBody WebsiteScrapeRequest request) {
        try {
            WebsiteScrapeResponse scrapedInfo = websiteService.scrapeWebsiteInfo(request.getUrl());
            return ResponseEntity.ok(scrapedInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
