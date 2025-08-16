package com.blog.dto;

/**
 * 网站信息抓取响应DTO
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
public class WebsiteScrapeResponse {
    
    private String title;
    private String description;
    private String keywords;
    private String favicon;
    private boolean success;
    private String error;
    
    public WebsiteScrapeResponse() {}
    
    public WebsiteScrapeResponse(String title, String description, String keywords, String favicon) {
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.favicon = favicon;
        this.success = true;
    }
    
    public WebsiteScrapeResponse(String error) {
        this.error = error;
        this.success = false;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getKeywords() {
        return keywords;
    }
    
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    
    public String getFavicon() {
        return favicon;
    }
    
    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    @Override
    public String toString() {
        return "WebsiteScrapeResponse{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", keywords='" + keywords + '\'' +
                ", favicon='" + favicon + '\'' +
                ", success=" + success +
                ", error='" + error + '\'' +
                '}';
    }
}
