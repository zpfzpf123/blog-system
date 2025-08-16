package com.blog.dto;

public class PaginationDTO {
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private long totalPosts;
    private boolean hasNext;
    private boolean hasPrev;

    // Constructors
    public PaginationDTO() {}

    public PaginationDTO(int currentPage, int pageSize, long totalPosts) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPosts = totalPosts;
        this.totalPages = (int) Math.ceil((double) totalPosts / pageSize);
        this.hasNext = currentPage < totalPages;
        this.hasPrev = currentPage > 1;
    }

    // Getters and Setters
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(long totalPosts) {
        this.totalPosts = totalPosts;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrev() {
        return hasPrev;
    }

    public void setHasPrev(boolean hasPrev) {
        this.hasPrev = hasPrev;
    }
}