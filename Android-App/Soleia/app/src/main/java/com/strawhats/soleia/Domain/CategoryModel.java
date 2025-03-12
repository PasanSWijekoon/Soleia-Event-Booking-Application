package com.strawhats.soleia.Domain;

import java.io.Serializable;

public class CategoryModel implements Serializable {
    private int Id;
    private String Name;
    private String Picture;

    public CategoryModel() {
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPicture() {
        return Picture;
    }

    public void setPicture(String picture) {
        Picture = picture;
    }
}
