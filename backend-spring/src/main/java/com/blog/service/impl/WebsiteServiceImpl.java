package com.blog.service.impl;

import com.blog.dto.PaginationDTO;
import com.blog.dto.WebsiteCreateRequest;
import com.blog.dto.WebsiteDTO;
import com.blog.dto.WebsiteQueryRequest;
import com.blog.dto.WebsitesResponseDTO;
import com.blog.dto.WebsiteScrapeResponse;
import com.blog.entity.Website;
import com.blog.entity.WebsiteCategory;

import com.blog.repository.WebsiteCategoryRepository;
import com.blog.repository.WebsiteRepository;
import com.blog.repository.WebsiteCategoryRelationRepository;
import com.blog.entity.WebsiteCategoryRelation;

import com.blog.service.WebsiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 网站业务服务实现类
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
@Service
@Transactional
public class WebsiteServiceImpl implements WebsiteService {
    
    @Autowired
    private WebsiteRepository websiteRepository;
    
    @Autowired
    private WebsiteCategoryRepository categoryRepository;
    
    @Autowired
    private WebsiteCategoryRelationRepository websiteCategoryRelationRepository;
    

    
    @Override
    public WebsiteDTO createWebsite(WebsiteCreateRequest request) {
        System.out.println("=== 服务层创建网站开始 ===");
        System.out.println("请求参数: " + request);
        
        // 检查URL是否已存在（只有当URL不为空时才检查）
        String url = request.getUrl();
        System.out.println("检查URL: " + url);
        
        if (StringUtils.hasText(url) && websiteRepository.findByUrlAndIsActiveTrue(url).isPresent()) {
            System.err.println("URL已存在: " + url);
            throw new RuntimeException("网站URL已存在: " + url);
        }
        System.out.println("URL检查通过");
        
        // 检查分类是否存在
        List<WebsiteCategory> categories = new ArrayList<>();
        System.out.println("分类IDs: " + request.getCategoryIds());
        
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (Long categoryId : request.getCategoryIds()) {
                System.out.println("检查分类ID: " + categoryId);
                WebsiteCategory category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("分类不存在: " + categoryId));
                categories.add(category);
                System.out.println("分类检查通过: " + category.getName());
            }
        } else {
            System.err.println("没有提供分类ID");
            throw new RuntimeException("至少需要选择一个分类");
        }
        
        // 创建网站实体
        System.out.println("开始创建网站实体...");
        Website website = new Website();
        website.setName(request.getName());
        System.out.println("设置网站名称: " + request.getName());
        
        // 处理URL：如果为空字符串，设置为null（数据库允许null但不允许空字符串）
        if (url != null && url.trim().isEmpty()) {
            url = null;
        }
        website.setUrl(url);
        System.out.println("设置网站URL: " + url);
        website.setDescription(request.getDescription());
        System.out.println("设置网站描述: " + request.getDescription());
        
        // 手动处理分类关系的设置，确保级联操作正确执行
        System.out.println("开始设置分类关系，分类数量: " + categories.size());
        if (categories != null && !categories.isEmpty()) {
            for (WebsiteCategory category : categories) {
                System.out.println("添加分类: " + category.getName() + " (ID: " + category.getId() + ")");
                website.addCategory(category);
            }
        }
        
        website.setIcon(request.getIcon());
        website.setFavicon(request.getFavicon());
        website.setScreenshot(request.getScreenshot());
        website.setIsFavorite(request.getIsFavorite());
        System.out.println("网站实体创建完成，准备保存...");
        
        // 保存网站
        System.out.println("开始保存网站到数据库...");
        Website savedWebsite = websiteRepository.save(website);
        System.out.println("网站保存成功，ID: " + savedWebsite.getId());
        
        System.out.println("创建WebsiteDTO...");
        WebsiteDTO result = new WebsiteDTO(savedWebsite);
        System.out.println("WebsiteDTO创建完成: " + result);
        System.out.println("=== 服务层创建网站结束 ===");
        
        return result;
    }
    
    @Override
    public Optional<WebsiteDTO> findWebsiteById(Long id) {
        return websiteRepository.findByIdWithCategories(id)
                .map(WebsiteDTO::new);
    }
    
    @Override
    public Optional<WebsiteDTO> findWebsiteByUrl(String url) {
        return websiteRepository.findByUrlWithCategories(url)
                .map(WebsiteDTO::new);
    }
    
    @Override
    @Transactional
    public WebsiteDTO updateWebsite(Long id, WebsiteCreateRequest request) {
        Website website = websiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("网站不存在: " + id));
        
        // 检查URL是否被其他网站使用（只有当URL不为空时才检查）
        String url = request.getUrl();
        if (StringUtils.hasText(url)) {
            Optional<Website> existingWebsite = websiteRepository.findByUrl(url);
            if (existingWebsite.isPresent() && !existingWebsite.get().getId().equals(id)) {
                throw new RuntimeException("网站URL已被其他网站使用: " + url);
            }
        }
        
        // 检查分类是否存在
        List<WebsiteCategory> categories = new ArrayList<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (Long categoryId : request.getCategoryIds()) {
                WebsiteCategory category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new RuntimeException("分类不存在: " + categoryId));
                categories.add(category);
            }
        } else {
            throw new RuntimeException("至少需要选择一个分类");
        }
        
        // 更新网站信息
        website.setName(request.getName());
        // 处理URL：如果为空字符串，设置为null（数据库允许null但不允许空字符串）
        if (url != null && url.trim().isEmpty()) {
            url = null;
        }
        website.setUrl(url);
        website.setDescription(request.getDescription());
        
        // 先保存网站基本信息（不包含分类关系）
        website.setIcon(request.getIcon());
        website.setFavicon(request.getFavicon());
        website.setScreenshot(request.getScreenshot());
        website.setIsFavorite(request.getIsFavorite());
        website.setUpdatedAt(LocalDateTime.now());
        
        // 保存网站基本信息
        Website savedWebsite = websiteRepository.save(website);
        
        // 删除现有的分类关系
        websiteCategoryRelationRepository.deleteByWebsiteId(id);
        
        // 重新创建分类关系
        if (categories != null && !categories.isEmpty()) {
            for (WebsiteCategory category : categories) {
                // 检查关系是否已存在，避免违反唯一约束
                WebsiteCategoryRelation existingRelation = websiteCategoryRelationRepository
                    .findByWebsiteIdAndCategoryId(savedWebsite.getId(), category.getId());
                if (existingRelation == null) {
                    WebsiteCategoryRelation relation = new WebsiteCategoryRelation(savedWebsite, category);
                    websiteCategoryRelationRepository.save(relation);
                }
            }
        }
        
        return new WebsiteDTO(savedWebsite);
    }
    
    @Override
    public void deleteWebsite(Long id) {
        Website website = websiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("网站不存在: " + id));
        
        // 软删除
        website.setIsActive(false);
        websiteRepository.save(website);
    }
    
    @Override
    public void deleteWebsites(List<Long> ids) {
        websiteRepository.softDeleteByIds(ids);
    }
    
    @Override
    public List<WebsiteDTO> findAllWebsites() {
        List<Website> websites = websiteRepository.findAllActiveWithCategories();
        return websites.stream()
                .map(WebsiteDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public WebsitesResponseDTO findWebsites(WebsiteQueryRequest request) {
        // 执行查询（使用优化版本，返回List而不是Page）
        List<Website> websites;
        
        if (request.hasKeyword()) {
            if (request.hasCategoryFilter()) {
                // 支持多分类查询
                if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
                    if (request.getCategoryIds().size() == 1) {
                        // 单个分类ID，使用单分类查询方法（优化版本）
                        websites = websiteRepository.findByCategoryIdAndKeywordWithCategories(
                            request.getCategoryIds().get(0), request.getKeyword());
                    } else {
                        // 多个分类ID，使用多分类查询方法（优化版本）
                        websites = websiteRepository.findByCategoryIdsAndKeywordWithCategories(
                            request.getCategoryIds(), request.getKeyword());
                    }
                } else if (request.getCategoryId() != null) {
                    // 兼容旧版本的单分类查询（优化版本）
                    websites = websiteRepository.findByCategoryIdAndKeywordWithCategories(
                        request.getCategoryId(), request.getKeyword());
                } else {
                    // 默认关键词查询（优化版本）
                    websites = websiteRepository.findByKeywordWithCategories(request.getKeyword());
                }
            } else if (request.hasFavoriteFilter()) {
                websites = websiteRepository.findByIsFavoriteAndKeywordWithCategories(
                    request.getIsFavorite(), request.getKeyword());
            } else {
                websites = websiteRepository.findByKeywordWithCategories(request.getKeyword());
            }
        } else if (request.hasCategoryFilter()) {
            // 支持多分类查询
            if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
                if (request.getCategoryIds().size() == 1) {
                    // 单个分类ID，使用单分类查询方法（优化版本）
                    websites = websiteRepository.findByCategoryIdWithCategories(request.getCategoryIds().get(0));
                } else {
                    // 多个分类ID，使用多分类查询方法（优化版本）
                    websites = websiteRepository.findByCategoryIdsWithCategories(request.getCategoryIds());
                }
            } else if (request.getCategoryId() != null) {
                // 兼容旧版本的单分类查询（优化版本）
                websites = websiteRepository.findByCategoryIdWithCategories(request.getCategoryId());
            } else {
                // 默认查询所有活跃网站（优化版本）
                websites = websiteRepository.findAllActiveWithCategories();
            }
        } else if (request.hasFavoriteFilter()) {
            websites = websiteRepository.findByIsFavoriteWithCategories(request.getIsFavorite());
        } else if (request.hasTimeRange()) {
            websites = websiteRepository.findByCreatedAtBetweenWithCategories(
                request.getStartTime(), request.getEndTime());
        } else {
            // 默认查询所有活跃网站（优化版本）
            websites = websiteRepository.findAllActiveWithCategories();
        }
        
        // 手动排序
        websites = sortWebsites(websites, request.getSortBy(), request.getSortOrder());
        
        // 手动分页
        int totalSize = websites.size();
        int startIndex = (request.getPage() - 1) * request.getSize();
        int endIndex = Math.min(startIndex + request.getSize(), totalSize);
        
        List<Website> paginatedWebsites = startIndex < totalSize ? 
            websites.subList(startIndex, endIndex) : new ArrayList<>();
        
        // 转换为DTO
        List<WebsiteDTO> websiteDTOs = paginatedWebsites.stream()
                .map(WebsiteDTO::new)
                .collect(Collectors.toList());
        
        // 创建分页信息
        PaginationDTO pagination = new PaginationDTO(
            request.getPage(), 
            request.getSize(), 
            (long) totalSize
        );
        
        return new WebsitesResponseDTO(websiteDTOs, pagination, (long) totalSize);
    }
    
    @Override
    public List<WebsiteDTO> findWebsitesByCategory(Long categoryId) {
        List<Website> websites = websiteRepository.findByCategoryIdAndIsActiveTrueWithCategories(categoryId);
        return websites.stream()
                .map(WebsiteDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<WebsiteDTO> findWebsitesByFavorite(Boolean isFavorite) {
        List<Website> websites = websiteRepository.findByIsFavoriteAndIsActiveTrueWithCategories(isFavorite);
        return websites.stream()
                .map(WebsiteDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<WebsiteDTO> searchWebsites(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }
        
        List<Website> websites = websiteRepository.findByKeywordWithCategories(keyword);
        
        // 限制返回100条
        if (websites.size() > 100) {
            websites = websites.subList(0, 100);
        }
        
        return websites.stream()
                .map(WebsiteDTO::new)
                .collect(Collectors.toList());
    }
    

    
    @Override
    public List<WebsiteDTO> getPopularWebsites(int limit) {
        // 由于移除了visit_count，改为返回最新创建的网站
        List<Website> websites = websiteRepository.findRecentWebsitesWithCategories();
        
        // 限制返回数量
        if (websites.size() > limit) {
            websites = websites.subList(0, limit);
        }
        
        return websites.stream()
                .map(WebsiteDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<WebsiteDTO> getRecentWebsites(int limit) {
        List<Website> websites = websiteRepository.findRecentWebsitesWithCategories();
        
        // 限制返回数量
        if (websites.size() > limit) {
            websites = websites.subList(0, limit);
        }
        
        return websites.stream()
                .map(WebsiteDTO::new)
                .collect(Collectors.toList());
    }
    

    
    @Override
    public void toggleFavorite(Long id) {
        websiteRepository.toggleFavorite(id);
    }
    
    @Override
    public void checkWebsiteStatus(Long id) {
        // TODO: 实现网站状态检查逻辑
        Website website = websiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("网站不存在: " + id));
        
        website.setLastCheckTime(LocalDateTime.now());
        websiteRepository.save(website);
    }
    
    @Override
    public void batchCheckWebsiteStatus() {
        // TODO: 实现批量网站状态检查逻辑
        LocalDateTime checkTime = LocalDateTime.now().minusDays(7);
        List<Website> websites = websiteRepository.findWebsitesNeedingStatusCheck(checkTime);
        
        for (Website website : websites) {
            website.setLastCheckTime(LocalDateTime.now());
            websiteRepository.save(website);
        }
    }
    
    @Override
    public WebsiteService.WebsiteStatistics getWebsiteStatistics() {
        Long totalWebsites = websiteRepository.countActiveWebsites();
        Long totalCategories = categoryRepository.countByIsActiveTrue();


        Long favoriteWebsites = websiteRepository.countFavoriteWebsites();
        Long activeWebsites = websiteRepository.countActiveWebsites();
        
        return new WebsiteService.WebsiteStatistics(
            totalWebsites, totalCategories, 
            favoriteWebsites, activeWebsites
        );
    }
    
    @Override
    public WebsiteService.ImportResult importWebsites(List<WebsiteCreateRequest> requests) {
        int totalCount = requests.size();
        int successCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();
        
        for (WebsiteCreateRequest request : requests) {
            try {
                createWebsite(request);
                successCount++;
            } catch (Exception e) {
                failedCount++;
                errors.add("导入失败: " + request.getName() + " - " + e.getMessage());
            }
        }
        
        return new WebsiteService.ImportResult(totalCount, successCount, failedCount, errors);
    }
    
    @Override
    public List<WebsiteDTO> exportWebsites(WebsiteQueryRequest request) {
        // 设置较大的页面大小以获取所有数据
        request.setSize(10000);
        request.setPage(1);
        
        WebsitesResponseDTO response = findWebsites(request);
        return response.getWebsites();
    }
    
    // 私有辅助方法
    

    
    private Sort createSort(String sortBy, String sortOrder) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? 
            Sort.Direction.DESC : Sort.Direction.ASC;
        
        switch (sortBy) {
            case "name":
                return Sort.by(direction, "name");
            case "url":
                return Sort.by(direction, "url");

            case "updatedAt":
                return Sort.by(direction, "updatedAt");
            case "createdAt":
            default:
                return Sort.by(direction, "createdAt");
        }
    }
    
    /**
     * 手动排序网站列表
     */
    private List<Website> sortWebsites(List<Website> websites, String sortBy, String sortOrder) {
        if (websites == null || websites.isEmpty()) {
            return websites;
        }
        
        boolean isDesc = "desc".equalsIgnoreCase(sortOrder);
        
        websites.sort((w1, w2) -> {
            int result = 0;
            
            switch (sortBy) {
                case "name":
                    result = compareStrings(w1.getName(), w2.getName());
                    break;
                case "url":
                    result = compareStrings(w1.getUrl(), w2.getUrl());
                    break;

                case "updatedAt":
                    result = compareDates(w1.getUpdatedAt(), w2.getUpdatedAt());
                    break;
                case "createdAt":
                default:
                    result = compareDates(w1.getCreatedAt(), w2.getCreatedAt());
                    break;
            }
            
            return isDesc ? -result : result;
        });
        
        return websites;
    }
    
    private int compareStrings(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return -1;
        if (s2 == null) return 1;
        return s1.compareToIgnoreCase(s2);
    }
    
    private int compareLongs(Long l1, Long l2) {
        if (l1 == null && l2 == null) return 0;
        if (l1 == null) return -1;
        if (l2 == null) return 1;
        return l1.compareTo(l2);
    }
    
    private int compareDates(LocalDateTime d1, LocalDateTime d2) {
        if (d1 == null && d2 == null) return 0;
        if (d1 == null) return -1;
        if (d2 == null) return 1;
        return d1.compareTo(d2);
    }
    
    private WebsitesResponseDTO createResponseFromList(List<Website> websites, WebsiteQueryRequest request) {
        List<WebsiteDTO> websiteDTOs = websites.stream()
                .map(WebsiteDTO::new)
                .collect(Collectors.toList());
        
        // 手动分页
        int start = request.getOffset();
        int end = Math.min(start + request.getSize(), websiteDTOs.size());
        
        List<WebsiteDTO> paginatedWebsites = websiteDTOs.subList(start, end);
        
        PaginationDTO pagination = new PaginationDTO(
            request.getPage(), 
            request.getSize(), 
            (long) websiteDTOs.size()
        );
        
        return new WebsitesResponseDTO(paginatedWebsites, pagination, (long) websiteDTOs.size());
    }
    
    @Override
    public WebsiteScrapeResponse scrapeWebsiteInfo(String url) {
        try {
            String normalizedUrl = url;
            if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
                normalizedUrl = "https://" + normalizedUrl;
            }

            // 使用 Jsoup 抓取页面
            org.jsoup.Connection connection = org.jsoup.Jsoup.connect(normalizedUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(8000)
                .followRedirects(true);

            org.jsoup.nodes.Document doc = connection.get();

            // 标题
            String title = Optional.ofNullable(doc.title()).filter(StringUtils::hasText)
                    .orElse(extractDomainFromUrl(normalizedUrl));

            // 描述
            String description = Optional.ofNullable(doc.selectFirst("meta[name=description]"))
                    .map(e -> e.attr("content")).filter(StringUtils::hasText).orElse("");

            // 关键词
            String keywords = Optional.ofNullable(doc.selectFirst("meta[name=keywords]"))
                    .map(e -> e.attr("content")).filter(StringUtils::hasText).orElse("");

            // 图标（优先顺序：apple-touch-icon, mask-icon, icon, shortcut icon）
            String favicon = findBestFavicon(doc, normalizedUrl);

            WebsiteScrapeResponse response = new WebsiteScrapeResponse(title, description, keywords, favicon);
            response.setSuccess(true);
            return response;
        } catch (Exception e) {
            return new WebsiteScrapeResponse("抓取失败: " + e.getMessage());
        }
    }

    private String findBestFavicon(org.jsoup.nodes.Document doc, String pageUrl) {
        // 1) 从<link>标签中找
        List<String> relPriority = Arrays.asList("apple-touch-icon", "apple-touch-icon-precomposed", "mask-icon", "icon", "shortcut icon");
        String best = null;
        int bestSizeScore = -1;

        for (String rel : relPriority) {
            for (org.jsoup.nodes.Element el : doc.select("link[rel~=(?i)^" + rel.replace(" ", "|") + "$]")) {
                String href = el.hasAttr("href") ? el.attr("href") : null;
                if (!StringUtils.hasText(href)) continue;

                String absHref = el.hasAttr("href") ? el.absUrl("href") : href;
                if (!StringUtils.hasText(absHref)) {
                    absHref = toAbsoluteUrl(pageUrl, href);
                }

                int score = scoreBySizes(el.attr("sizes"));
                if (score > bestSizeScore) {
                    bestSizeScore = score;
                    best = absHref;
                }
            }
            if (best != null) break; // 找到更高优先级直接结束
        }

        // 2) 回退到 /favicon.ico
        if (!StringUtils.hasText(best)) {
            String origin = getOrigin(pageUrl);
            String fallback = origin + "/favicon.ico";
            best = fallback;
        }

        return best;
    }

    private int scoreBySizes(String sizesAttr) {
        if (!StringUtils.hasText(sizesAttr)) return 0;
        // 目标接近 32x32 给更高分
        try {
            String[] parts = sizesAttr.toLowerCase(Locale.ROOT).split(" ");
            int best = 0;
            for (String p : parts) {
                if (p.contains("x")) {
                    String[] wh = p.split("x");
                    int w = Integer.parseInt(wh[0].replaceAll("[^0-9]", ""));
                    int h = Integer.parseInt(wh[1].replaceAll("[^0-9]", ""));
                    int diff = Math.abs(w - 32) + Math.abs(h - 32);
                    int score = 100 - diff;
                    if (score > best) best = score;
                }
            }
            return best;
        } catch (Exception ignored) {
            return 0;
        }
    }

    private String toAbsoluteUrl(String baseUrl, String href) {
        try {
            java.net.URL base = new java.net.URL(baseUrl);
            java.net.URL abs = new java.net.URL(base, href);
            return abs.toString();
        } catch (Exception e) {
            return href;
        }
    }

    private String getOrigin(String anyUrl) {
        try {
            java.net.URL u = new java.net.URL(anyUrl);
            String portPart = (u.getPort() == -1 || u.getPort() == u.getDefaultPort()) ? "" : ":" + u.getPort();
            return u.getProtocol() + "://" + u.getHost() + portPart;
        } catch (Exception e) {
            return anyUrl;
        }
    }
    
    private String extractDomainFromUrl(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            java.net.URL urlObj = new java.net.URL(url);
            return urlObj.getHost().replace("www.", "");
        } catch (Exception e) {
            return url;
        }
    }
}
