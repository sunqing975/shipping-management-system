package com.shipping.entity;

/**
 * @className: com.shipping.entity.Attribute
 * @author: Superman
 * @create: 2023-05-17 21:38
 * @description: TODO
 */
public class Attribute {
    private String ID;
    private String Name;

    private String Value;

    public Attribute(String ID, String name, String value) {
        this.ID = ID;
        Name = name;
        Value = value;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "ID='" + ID + '\'' +
                ", Name='" + Name + '\'' +
                ", Value='" + Value + '\'' +
                '}';
    }
}
