package com.blog.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 网站信息抓取请求DTO
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
public class WebsiteScrapeRequest {
    
    @NotBlank(message = "网站地址不能为空")
    // 允许 http/https 开头，后续接受任何非空白字符（支持中文、#、?、% 等）
    @Pattern(regexp = "^(?i)https?://\\S+$", 
             message = "请输入有效的 http/https 网站地址，例如：https://example.com/path#hash")
    private String url;
    
    public WebsiteScrapeRequest() {}
    
    public WebsiteScrapeRequest(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    @Override
    public String toString() {
        return "WebsiteScrapeRequest{" +
                "url='" + url + '\'' +
                '}';
    }
}