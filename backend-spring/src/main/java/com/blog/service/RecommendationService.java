/*
 * @Author: Your Name your.email@example.com
 * @Date: 2025-08-06 09:42:14
 * @LastEditors: Your Name your.email@example.com
 * @LastEditTime: 2025-08-06 09:42:14
 * @FilePath: \blog\backend-spring\src\main\java\com\blog\service\RecommendationService.java
 * @Description: 推荐服务类
 */
package com.blog.service;

import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    
    /**
     * 搜索网络相关内容
     * 注意：这是一个简化的实现，在实际生产环境中，您可能需要使用专门的搜索引擎API
     * 如Google Custom Search API、Bing Search API等
     * 
     * @param query 搜索关键词
     * @return 搜索结果列表
     */
    public List<Map<String, Object>> searchWebContent(String query) {
        // 在实际应用中，这里应该调用真正的搜索引擎API
        // 为演示目的，我们返回模拟数据
        return getMockSearchResults(query);
    }
    
    /**
     * 基于文章标题和内容生成更智能的搜索关键词
     * 
     * @param title 文章标题
     * @param content 文章内容
     * @param tags 文章标签
     * @return 优化后的搜索关键词列表
     */
    public List<String> generateSearchQueries(String title, String content, List<String> tags) {
        List<String> queries = new ArrayList<>();
        
        // 基于标题的查询
        queries.add(title);
        
        // 提取标题中的关键词
        String[] titleWords = title.split("[\\s\\p{Punct}]+");
        if (titleWords.length > 1) {
            // 添加标题的前几个词组合作为查询
            StringBuilder phrase = new StringBuilder();
            for (int i = 0; i < Math.min(3, titleWords.length); i++) {
                if (!titleWords[i].isEmpty()) {
                    phrase.append(titleWords[i]).append(" ");
                }
            }
            queries.add(phrase.toString().trim());
        }
        
        // 基于标签的查询
        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                queries.add(tag);
                queries.add(title + " " + tag);
            }
        }
        
        // 基于内容的查询（提取前几句作为关键词）
        if (content != null && !content.isEmpty()) {
            String cleanContent = content.replaceAll("<[^>]*>", ""); // 移除HTML标签
            String[] sentences = cleanContent.split("[.!?。！？]");
            if (sentences.length > 0) {
                // 使用第一句话作为查询词
                String firstSentence = sentences[0].trim();
                if (firstSentence.length() > 10) {
                    queries.add(firstSentence.substring(0, Math.min(50, firstSentence.length())));
                }
            }
        }
        
        return queries.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * 获取外部网页内容摘要
     * 
     * @param url 网页URL
     * @return 网页内容摘要
     */
    public String fetchExternalContentPreview(String url) {
        try {
            // 使用Jsoup获取网页内容
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(5000)
                    .get();
            
            // 提取主要内容
            Element content = doc.select("main").first();
            if (content == null) {
                content = doc.select("article").first();
            }
            if (content == null) {
                content = doc.select("body").first();
            }
            
            if (content != null) {
                // 提取文本并截取前500个字符作为预览
                String text = content.text();
                String preview = text.length() > 500 ? text.substring(0, 500) + "..." : text;
                
                // 检查是否为英文内容，如果是则尝试翻译成中文
                if (isEnglishText(preview)) {
                    return translateToChinese(preview);
                }
                return preview;
            }
        } catch (IOException e) {
            System.err.println("获取网页内容失败: " + e.getMessage());
        }
        
        return "无法获取网页内容";
    }
    
    /**
     * 检查文本是否为英文
     * 
     * @param text 待检查的文本
     * @return 如果文本主要是英文则返回true，否则返回false
     */
    private boolean isEnglishText(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 统计英文字符数量
        long englishChars = text.chars()
                .filter(c -> (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))
                .count();
        
        // 如果英文字符占比超过50%，则认为是英文内容
        return ((double) englishChars / text.length()) > 0.5;
    }
    
    /**
     * 翻译英文文本为中文（模拟实现）
     * 在实际应用中，这里应该调用真正的翻译API，如Google Translate API、百度翻译API等
     * 
     * @param englishText 英文文本
     * @return 翻译后的中文文本
     */
    private String translateToChinese(String englishText) {
        // 这里是模拟翻译，实际项目中应该接入真正的翻译服务
        // 例如使用Google Translate API或百度翻译API
        System.out.println("检测到英文内容，需要翻译: " + englishText);
        
        // 模拟翻译结果
        if (englishText.contains("performance")) {
            return "Vue 3 性能优化技巧。了解如何优化Vue 3应用的性能，包括响应式系统优化、组件优化等重要技巧。";
        } else if (englishText.contains("Composition API")) {
            return "Vue 3 Composition API 完全指南。深入学习Vue 3的Composition API，掌握新的组件组织方式和逻辑复用机制。";
        } else if (englishText.contains("migration")) {
            return "Vue 3 与 Vue 2 的区别。详细了解Vue 3相比Vue 2的主要变化，帮助您顺利迁移现有项目。";
        } else if (englishText.contains("Web Development")) {
            return "现代Web开发技术栈。了解现代Web开发所需的核心技术，包括HTML、CSS、JavaScript等。";
        } else if (englishText.contains("Responsive Design")) {
            return "响应式网页设计基础。学习如何创建在各种设备上都能良好显示的响应式网页。";
        }
        
        // 默认返回原文
        return englishText;
    }
    
    /**
     * 获取模拟搜索结果
     * 
     * @param query 搜索关键词
     * @return 模拟搜索结果
     */
    private List<Map<String, Object>> getMockSearchResults(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // 根据查询关键词返回相关结果
        if (query.toLowerCase().contains("vue") || query.toLowerCase().contains("前端")) {
            Map<String, Object> result1 = new HashMap<>();
            result1.put("title", "Vue 3 性能优化技巧");
            result1.put("url", "https://vuejs.org/guide/best-practices/performance.html");
            result1.put("snippet", "了解如何优化Vue 3应用的性能，包括响应式系统优化、组件优化等重要技巧。");
            result1.put("score", 0.95); // 相关性评分
            results.add(result1);
            
            Map<String, Object> result2 = new HashMap<>();
            result2.put("title", "Vue 3 Composition API 完全指南");
            result2.put("url", "https://vuejs.org/guide/extras/composition-api-faq.html");
            result2.put("snippet", "深入学习Vue 3的Composition API，掌握新的组件组织方式和逻辑复用机制。");
            result2.put("score", 0.92);
            results.add(result2);
            
            Map<String, Object> result3 = new HashMap<>();
            result3.put("title", "Vue 3 与 Vue 2 的区别");
            result3.put("url", "https://v3-migration.vuejs.org/");
            result3.put("snippet", "详细了解Vue 3相比Vue 2的主要变化，帮助您顺利迁移现有项目。");
            result3.put("score", 0.88);
            results.add(result3);
            
            Map<String, Object> result4 = new HashMap<>();
            result4.put("title", "Vue 3 响应式原理深入解析");
            result4.put("url", "https://vuejs.org/guide/extras/reactivity-in-depth.html");
            result4.put("snippet", "深入了解Vue 3响应式系统的实现原理和工作机制。");
            result4.put("score", 0.85);
            results.add(result4);
        } else if (query.toLowerCase().contains("java") || query.toLowerCase().contains("spring")) {
            Map<String, Object> result1 = new HashMap<>();
            result1.put("title", "Spring Boot 实战指南");
            result1.put("url", "https://spring.io/guides");
            result1.put("snippet", "Spring Boot 应用开发完整指南，包括配置、数据访问、安全等方面。");
            result1.put("score", 0.93);
            results.add(result1);
            
            Map<String, Object> result2 = new HashMap<>();
            result2.put("title", "Java 核心技术深入解析");
            result2.put("url", "https://docs.oracle.com/javase/tutorial/");
            result2.put("snippet", "深入学习Java核心技术，包括集合框架、并发编程、JVM原理等重要知识点。");
            result2.put("score", 0.88);
            results.add(result2);
            
            Map<String, Object> result3 = new HashMap<>();
            result3.put("title", "微服务架构设计与实现");
            result3.put("url", "https://microservices.io/");
            result3.put("snippet", "微服务架构设计模式详解，包括服务拆分、通信、部署等关键环节的最佳实践。");
            result3.put("score", 0.85);
            results.add(result3);
        } else if (query.toLowerCase().contains("数据库") || query.toLowerCase().contains("database")) {
            Map<String, Object> result1 = new HashMap<>();
            result1.put("title", "MySQL 性能优化实战");
            result1.put("url", "https://dev.mysql.com/doc/");
            result1.put("snippet", "MySQL数据库性能优化技巧，包括索引设计、查询优化、配置调优等关键内容。");
            result1.put("score", 0.91);
            results.add(result1);
            
            Map<String, Object> result2 = new HashMap<>();
            result2.put("title", "Redis 设计与实现");
            result2.put("url", "https://redis.io/documentation");
            result2.put("snippet", "Redis 内部数据结构和实现原理详解，以及在实际项目中的应用案例。");
            result2.put("score", 0.87);
            results.add(result2);
        } else {
            // 默认推荐内容
            Map<String, Object> result1 = new HashMap<>();
            result1.put("title", "现代Web开发技术栈");
            result1.put("url", "https://developer.mozilla.org/zh-CN/docs/Learn");
            result1.put("snippet", "了解现代Web开发所需的核心技术，包括HTML、CSS、JavaScript等。");
            result1.put("score", 0.8);
            results.add(result1);
            
            Map<String, Object> result2 = new HashMap<>();
            result2.put("title", "响应式网页设计基础");
            result2.put("url", "https://developer.mozilla.org/zh-CN/docs/Learn/CSS/CSS_layout/Responsive_Design");
            result2.put("snippet", "学习如何创建在各种设备上都能良好显示的响应式网页。");
            result2.put("score", 0.75);
            results.add(result2);
        }
        
        return results;
    }
}