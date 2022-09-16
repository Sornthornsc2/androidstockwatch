package com.example.stockwatch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private final ArrayList<Stock> stockArrayList = new ArrayList<>();
    private ArrayList<Stock> fileStock = new ArrayList<>();
    private RecyclerView recycler;
    private SwipeRefreshLayout swipe;
    private StockAdapter stockAdapter;
    private boolean isTrue = true;
    private ConnectivityManager connectivityManager;
    private String noInternetMessageForLoad = "Please Check Your Internet Connection";
    //TextView symbolTextView, priceTextView, changeTextView, nameTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        launchStock(noInternetMessageForLoad);
        swipe = findViewById(R.id.swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //stockArrayList.clear();
                String noInternetMessageForUpdate = "Please Check Your Internet Connection";
                launchStock(noInternetMessageForUpdate);
                swipe.setRefreshing(false);
            }
        });

        recycler = findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(stockArrayList, this);
        recycler.setAdapter(stockAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void onClick(View view) {
        final int pos = recycler.getChildLayoutPosition(view);
        final Stock stock = stockArrayList.get(pos);
        String url = String.format("http://www.marketwatch.com/investing/stock/%s", stock.getSymbol());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public boolean onLongClick(View view) {
        final int pos = recycler.getChildLayoutPosition(view);
        final Stock removeStock = stockArrayList.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.delete);
        builder.setTitle("Delete Stock");
        builder.setMessage(String.format("Delete Stock Symbol %s?", removeStock.getSymbol()));
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                stockArrayList.remove(pos);
                try{
                    writeJSON();
                }
                catch (IOException | JSONException e){
                    e.printStackTrace();
                }
                stockAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.addMenu){
            if(internetConnected()){
                addStockDialog();
                return true;
            }
            else {
                String noInternet = "Stocks Cannot Be Added Without A Network Connection";
                noInternetDialog(noInternet);
                return false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void launchStock(String message){
        readJSON();
        if(internetConnected()){
            if(message.equals(noInternetMessageForLoad)){
                new Thread(new NameRunnable()).start();
            }
            if(fileStock != null){
                for(Stock stock : fileStock){
                    hasStock(stock);
                }
            }
        }
        else{
            noInternetDialog(message);
            if(fileStock != null){
                for(Stock stock : fileStock){
                    stockArrayList.add(new Stock(stock.getSymbol(), stock.getName(), 0.0, 0.0, 0.0));
                }
                sortStock(fileStock);
                stockAdapter.notifyDataSetChanged();
            }
        }
    }

    public boolean internetConnected(){
        if(connectivityManager == null){
            connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void downloadStock(Stock stock){
        new Thread(new StockRunnable(this, stock.getSymbol())).start();
    }

    public void updateStock(Stock s) {
        final Stock stock = s;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (stock == null) {
                    return;
                }
                if (hasStock(stock)) {
                    if (!isTrue) {
                        duplicateStockDialog(stock.getSymbol());
                        isTrue = true;
                    }
                    return;
                } else {
                    stockArrayList.add(stock);
                    sortStock(stockArrayList);
                    try {
                        writeJSON();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                    stockAdapter.notifyDataSetChanged();
                }
                stockAdapter.notifyDataSetChanged();

            }
        });
    }

    private boolean hasStock(Stock stock) {
        for (Stock stockInList : stockArrayList) {
            if (stockInList.getSymbol().equals(stock.getSymbol()))
                return true;
        }
        return false;
    }

    public void sortStock(ArrayList<Stock> stockArrayList){
        if(stockArrayList != null){
            Collections.sort(stockArrayList, new Comparator<Stock>() {
                @Override
                public int compare(Stock stock, Stock t1) {
                    return stock.getSymbol().compareTo(t1.getSymbol());
                }
            });
        }
    }

    public void duplicateStockDialog(String s){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.warning);
        builder.setTitle("Duplicate Stock");
        builder.setMessage(String.format("Stock Symbol %s is already displayed",s));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void symbolNotFoundDialog(String s){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Symbol Not Found: " + s);
        builder.setMessage("Data for stock symbol");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void noInternetDialog(String s){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection");
        builder.setMessage(s);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addStockDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        builder.setView(input);
        builder.setTitle("Stock Selection");
        builder.setMessage("Please enter a Stock Symbol:");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String symbolString = input.getText().toString();
                final ArrayList<Stock> stockArrayList = new NameRunnable().symbolMatch(symbolString);
                sortStock(stockArrayList);
                doOperation(symbolString, stockArrayList);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void dialog(final ArrayList<Stock> arrayList){
        final String[] items = new String[arrayList.size()];
        for(int i =0;i< arrayList.size();i++){
            items[i] = arrayList.get(i).getSymbol() + " - " + arrayList.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a dialog");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Stock stock = arrayList.get(i);
                downloadStock(stock);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void doOperation(String s, ArrayList<Stock> isMatchStockList){
        if(isMatchStockList.size() > 1){
            dialog(isMatchStockList);
        }
        else if(isMatchStockList.size() == 1){
            downloadStock(isMatchStockList.get(0));
        }
        else{
            symbolNotFoundDialog(s);
        }
        isTrue = false;
    }

    private void readJSON(){
        Stock stock;
        ArrayList<Stock> stockArrayList = new ArrayList<>();
        try{
            InputStream inputStream = openFileInput(getString(R.string.file_name));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(line);
            }
            bufferedReader.close();

            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            for(int i =0;i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String symbol = jsonObject.getString("symbol");
                String companyName = jsonObject.getString("companyName");

                String priceString = jsonObject.getString("latestPrice");
                double price = (priceString.equals("null") ? 0.0 : Double.parseDouble(priceString));

                String changeString = jsonObject.getString("change");
                double change = (changeString.equals("null") ? 0.0 : Double.parseDouble(changeString));

                String changePercentString = jsonObject.getString("changePercent");
                double changePercent = (changePercentString.equals("null") ? 0.0 : Double.parseDouble(changePercentString));
                stock = new Stock(symbol,companyName,price,change,changePercent);
                stockArrayList.add(stock);
            }
            fileStock = stockArrayList;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void writeJSON() throws IOException, JSONException{
        JSONArray jsonArray = new JSONArray();
        for(Stock stock : stockArrayList){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("symbol", stock.getSymbol());
            jsonObject.put("companyName", stock.getName());
            jsonObject.put("latestPrice", stock.getPrice());
            jsonObject.put("change", stock.getPriceChange());
            jsonObject.put("changePercent", stock.getChangePercent());
            jsonArray.put(jsonObject);
        }
        String jsonText = jsonArray.toString();
        FileOutputStream fileOutputStream = openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);
        fileOutputStream.write(jsonText.getBytes());
        fileOutputStream.close();

    }

    /*
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("symbolTextView", symbolTextView.getText().toString());
        outState.putString("priceTextView", priceTextView.getText().toString());
        outState.putString("changeTextView", changeTextView.getText().toString());
        outState.putString("nameTextView", nameTextView.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        symbolTextView.setText(savedInstanceState.getString("symbolTextView"));
        priceTextView.setText((savedInstanceState.getString("priceTextView")));
        changeTextView.setText(savedInstanceState.getString("changeTextView"));
        nameTextView.setText(savedInstanceState.getString("nameTextView"));

    }
    */
}