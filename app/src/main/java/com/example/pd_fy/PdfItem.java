package com.example.pd_fy;
public class PdfItem {
    private String name;
    private String url;

    public PdfItem(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String newName)
    {
        this.name = newName;
    }
}
