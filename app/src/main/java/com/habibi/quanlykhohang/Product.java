package com.habibi.quanlykhohang;
public class Product {
    private int id;
    private String barcode;
    private String name;
    private double price;
    private int quantity;
    private String location;

    public Product() {}

    public Product(String barcode, String name, double price, int quantity, String location) {
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.location = location;
    }

    public int getId() { return id; }
    public String getBarcode() { return barcode; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getLocation() { return location; }

    public void setId(int id) { this.id = id; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setLocation(String location) { this.location = location; }
}
