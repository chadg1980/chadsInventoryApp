package com.h.chad.chadsinventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.h.chad.chadsinventoryapp.data.ProductContract;
import com.h.chad.chadsinventoryapp.data.ProductContract.ProductEntry;

import org.w3c.dom.Text;

import static android.R.attr.name;

/**
 * Created by chad on 2/15/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {
    //Constructor
    public ProductCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }
    //NewView
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);

    }
    //Bind View
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //get the textviews from the list_item
        TextView tvName = (TextView) view.findViewById(R.id.productName);
        TextView tvDescription = (TextView) view.findViewById(R.id.productDesctiption);
        TextView tvQuantity = (TextView) view.findViewById(R.id.quantity);
        TextView tvPrice = (TextView)view.findViewById(R.id.price);

        //get the column index for each item
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_NAME);
        int descriptionColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_DESCRIPTION);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_QUANTITY);
        int saleColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_PRICE);

        //get the data from the database at that column
        String nameString = cursor.getString(nameColumnIndex);
        String descriptionString = cursor.getString(descriptionColumnIndex);
        int quantityInt = cursor.getInt(quantityColumnIndex);
        int saleInt = cursor.getInt(saleColumnIndex);

        //set the text
        tvName.setText(nameString);
        tvDescription.setText(descriptionString);
        tvQuantity.setText(Integer.toString(quantityInt));
        tvPrice.setText(Integer.toString(saleInt));
    }
}
