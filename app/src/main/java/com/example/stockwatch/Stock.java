package com.example.stockwatch;

import java.io.Serializable;

public class Stock implements Serializable {
    private String symbol;
    private String name;
    private double price;
    private double priceChange;
    private double changePercent;

    public Stock(String symbol, String name, double price, double priceChange, double changePercent){
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.priceChange = priceChange;
        this.changePercent = changePercent;
    }

    public String getSymbol(){
        return symbol;
    }

    public String getName(){
        return name;
    }

    public double getPrice(){
        return price;
    }

    public double getPriceChange(){
        return priceChange;
    }

    public double getChangePercent(){
        return changePercent;
    }

}