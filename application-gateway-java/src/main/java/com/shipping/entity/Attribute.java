package com.shipping.entity;

/**
 * @className: com.shipping.entity.Attribute
 * @author: Superman
 * @create: 2023-05-17 21:38
 * @description: TODO
 */
public class Attribute {
    private String id;
    private String name;

    private String value;

    public Attribute(String id, String name,String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
