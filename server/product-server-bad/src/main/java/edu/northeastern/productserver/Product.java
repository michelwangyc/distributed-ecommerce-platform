package edu.northeastern.productserver;

public class Product {
    private Integer product_id;
    private String sku;
    private String manufacturer;
    private Integer category_id;
    private Integer weight;
    private Integer some_other_id;

    public Integer getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Integer product_id) {
        this.product_id = product_id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getCategory_id() {
        return category_id;
    }

    public void setCategory_id(Integer category_id) {
        this.category_id = category_id;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getSome_other_id() {
        return some_other_id;
    }

    public void setSome_other_id(Integer some_other_id) {
        this.some_other_id = some_other_id;
    }
}