package com.example.memory_gam_ca_team8_.domains;

public class Image  {

    // 图片路径
    private String imgSrc;
    // 选中状态
    private boolean isSel;
    // 图片资源


    public String getImgSrc() {
        return imgSrc;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public boolean isSel() {
        return isSel;
    }

    public void setSel(boolean sel) {
        isSel = sel;
    }

    public Image(String imgSrc, boolean isSel) {
        this.imgSrc = imgSrc;
        this.isSel = isSel;
    }

    public Image() {
    }
}
