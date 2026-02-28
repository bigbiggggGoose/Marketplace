package com.example.marketplace.dto;

import java.util.List;

public class PageResult<T> {
    private List<T> list;
    private Long total;
    private Integer page;
    private Integer pageSize;

    public PageResult() {}

    public PageResult(List<T> list, Long total, Integer page, Integer pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}

