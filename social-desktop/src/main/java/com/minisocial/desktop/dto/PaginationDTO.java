package com.minisocial.desktop.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaginationDTO<T> {
    
    private Meta meta;
    private List<T> result;
    
    // Constructors
    public PaginationDTO() {}
    
    public PaginationDTO(Meta meta, List<T> result) {
        this.meta = meta;
        this.result = result;
    }
    
    // Getters and Setters
    public Meta getMeta() { return meta; }
    public void setMeta(Meta meta) { this.meta = meta; }
    
    public List<T> getResult() { return result; }
    public void setResult(List<T> result) { this.result = result; }
    
    // Nested Meta class
    public static class Meta {
        private int page;
        private int pageSize;
        private int pages;
        private long total;
        
        public Meta() {}
        
        public Meta(int page, int pageSize, int pages, long total) {
            this.page = page;
            this.pageSize = pageSize;
            this.pages = pages;
            this.total = total;
        }
        
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        
        public int getPages() { return pages; }
        public void setPages(int pages) { this.pages = pages; }
        
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
    }
}