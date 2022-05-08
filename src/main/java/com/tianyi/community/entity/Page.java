package com.tianyi.community.entity;

/**
 * information needed for paging
 * */

public class Page {
    // get from the frontend page
    // current page
    private int current = 1;
    // limit number of posts on one page
    private int limit = 10;


    // return to the frontend page
    // total posts (used to calculate number of pages)
    private int rows;
    // path template
    private String path;


    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get the current page's offset
     * @return
     * */
    public int getOffSet() {
        return (current - 1) * limit;
    }

    /**
     * Get the total number of pages
     * */

    public int getTotal() {
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    /**
     * return the pages index near current page shown on the website
     * */
    public int getFrom() {
        int from = current - 2;
        return from < 1? 1 : from;

    }

    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        return to > total? total : to;
    }



}
