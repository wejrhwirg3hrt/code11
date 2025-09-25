package org.example.dto;

/**
 * 视频内容块DTO
 */
public class VideoContentBlock {
    private String type; // RICH_TEXT, HTML, IMAGE, VIDEO
    private String data; // 内容数据
    private String url;  // 文件URL（用于图片和视频）
    private int order;   // 排序

    public VideoContentBlock() {}

    public VideoContentBlock(String type, String data, String url, int order) {
        this.type = type;
        this.data = data;
        this.url = url;
        this.order = order;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
