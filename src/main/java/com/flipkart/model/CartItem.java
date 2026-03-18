package com.flipkart.model;

public class CartItem {
    private String name;
    private String price;
    private int    qty;

    public CartItem(String name, String price, int qty) {
        this.name  = name;
        this.price = price;
        this.qty   = qty;
    }

    public String getName()  { return name;  }
    public String getPrice() { return price; }
    public int    getQty()   { return qty;   }

    @Override
    public String toString() { return qty + "x " + name + " @ " + price; }
}
