package com.example.stockwatch;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NameRunnable implements Runnable{
    private static final String nameURL = "https://api.iextrading.com/1.0/ref-data/symbols";
    private static final HashMap<String, String> hashMap = new HashMap<>();

    @Override
    public void run() {
        Uri dataUri = Uri.parse(nameURL);
        String urlToUse = dataUri.toString();
        StringBuilder stringBuilder = new StringBuilder();
        try{
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader((new InputStreamReader(inputStream)));

            String line;
            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line).append('\n');
            }
            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            for(int i =0; i<jsonArray.length(); i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                String symbol = jsonObject.getString("symbol");
                String name = jsonObject.getString("name");
                hashMap.put(symbol,name);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<Stock> symbolMatch(String s){
        ArrayList<Stock> arrayList = new ArrayList<>();
        for(Map.Entry<String,String> entry : hashMap.entrySet()){
            String k = entry.getKey();
            String v = entry.getValue();
            String rightSymbol = k.toLowerCase();
            String rightName = v.toLowerCase();
            String keyWord = s.toLowerCase();
            if(rightSymbol.contains(keyWord) || rightName.contains(keyWord)){
                if(!(k.isEmpty() || v.isEmpty())){
                    arrayList.add(new Stock(k,v, 0.0, 0.0, 0.0));
                }
            }
        }
        return arrayList;
    }






}