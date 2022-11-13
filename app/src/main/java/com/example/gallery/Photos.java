package com.example.gallery;

public class Photos {
    String description, photoImage, photoName;

    public Photos() {

    }

    public Photos(String description, String photoImage, String photoName) {
        this.description = description;
        this.photoImage = photoImage;
        this.photoName = photoName;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoImage() {
        return photoImage;
    }

    public void setPhotoImage(String photoImage) {
        this.photoImage = photoImage;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String photoName) {
        this.photoName = photoName;
    }
}
