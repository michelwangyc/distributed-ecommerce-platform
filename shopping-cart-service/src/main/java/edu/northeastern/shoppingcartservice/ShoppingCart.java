package edu.northeastern.shoppingcartservice;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private Integer shopping_cart_id;
    private Integer customer_id;
    private List<CartItem> items = new ArrayList<>();

    public Integer getShopping_cart_id() {
        return shopping_cart_id;
    }

    public void setShopping_cart_id(Integer shopping_cart_id) {
        this.shopping_cart_id = shopping_cart_id;
    }

    public Integer getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(Integer customer_id) {
        this.customer_id = customer_id;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}