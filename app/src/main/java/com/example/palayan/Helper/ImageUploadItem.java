package com.example.palayan.Helper;

public class ImageUploadItem {
    private String imageUrl;
    private boolean placeholder;

    public ImageUploadItem() {}

    public ImageUploadItem(String imageUrl, boolean placeholder) {
        this.imageUrl = imageUrl;
        this.placeholder = placeholder;
    }

    public String getImageUrl() { return imageUrl; }
    public boolean isPlaceholder() { return placeholder; }
}
