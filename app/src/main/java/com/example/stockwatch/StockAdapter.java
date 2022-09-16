package com.example.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private final ArrayList<Stock> stockArrayList;
    private final MainActivity mainActivity;

    public StockAdapter(ArrayList<Stock> stockArrayList, MainActivity mainActivity){
        this.mainActivity = mainActivity;
        this.stockArrayList = stockArrayList;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list, parent, false);
        view.setOnClickListener(mainActivity);
        view.setOnLongClickListener(mainActivity);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stockArrayList.get(position);
        String icon;
        if (stock.getPriceChange() > 0) {
            icon = "▲";
           setColor(holder, Color.GREEN);
        } else if (stock.getPriceChange() == 0) {
            icon = "";
            setColor(holder, Color.WHITE);
        } else {
            icon = "▼";
            setColor(holder, Color.RED);
        }
        holder.symbolTextView.setText(stock.getSymbol());
        holder.priceTextView.setText(String.format("%.2f",stock.getPrice()));
        holder.nameTextView.setText(stock.getName());
        holder.changeTextView.setText(String.format("%s%.2f(%.2f%%)", icon, stock.getPriceChange(), stock.getChangePercent() * 100));

    }

    private void setColor(StockViewHolder holder, int color) {
        holder.symbolTextView.setTextColor(color);
        holder.nameTextView.setTextColor(color);
        holder.priceTextView.setTextColor(color);
        holder.changeTextView.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return stockArrayList.size();
    }
}
