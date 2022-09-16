package com.example.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {
    TextView symbolTextView, priceTextView, changeTextView, nameTextView;

    public StockViewHolder(@NonNull View itemView) {
        super(itemView);
        symbolTextView = itemView.findViewById(R.id.symbolTextView);
        priceTextView = itemView.findViewById(R.id.priceTextView);
        changeTextView = itemView.findViewById(R.id.changeTextView);
        nameTextView = itemView.findViewById(R.id.nameTextView);
    }
}
