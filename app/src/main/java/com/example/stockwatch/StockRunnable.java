package com.example.stockwatch;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class StockRunnable implements Runnable{
    private final MainActivity mainActivity;
    private final String symbol;
    private static final String stockURL = "https://cloud.iexapis.com/stable/stock/%s/quote?token=pk_8d8caf314ad4436aa7e84b3597b4732a";

    public StockRunnable(MainActivity mainActivity, String symbol){
        this.mainActivity = mainActivity;
        this.symbol = symbol;
    }

    @Override
    public void run() {
        StringBuilder stringBuilder = new StringBuilder();
        String urlToUse = String.format(stockURL, symbol);
        try{
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader((new InputStreamReader(inputStream)));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line).append('\n');
            }
            Stock stock = parseJson(stringBuilder.toString());
            if(stock != null) {
                mainActivity.updateStock(stock);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Stock parseJson(String s){
        Stock stock;
        try {
            JSONObject jsonObject = new JSONObject((s));
            String symbol = jsonObject.getString("symbol");
            String companyName = jsonObject.getString("companyName");

            String priceStr = jsonObject.getString("latestPrice");
            double price = (priceStr.equals("null") ? 0.0 : Double.parseDouble(priceStr));

            String changePriceStr = jsonObject.getString("change");
            double change = (priceStr.equals("null") ? 0.0 : Double.parseDouble(changePriceStr));

            String changePercentStr = jsonObject.getString("changePercent");
            double changePercent = (priceStr.equals("null") ? 0.0 : Double.parseDouble(changePercentStr));

            stock = new Stock(symbol, companyName, price, change, changePercent);
            return stock;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
