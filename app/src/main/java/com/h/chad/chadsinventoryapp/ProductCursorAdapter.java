package com.h.chad.chadsinventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.h.chad.chadsinventoryapp.data.ProductContract;
import com.h.chad.chadsinventoryapp.data.ProductContract.ProductEntry;

import org.w3c.dom.Text;

import static android.R.attr.name;
import static com.h.chad.chadsinventoryapp.R.id.price;

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
    public void bindView(View view, final Context context, final Cursor cursor) {
        //get the textviews from the list_item
        TextView tvName = (TextView) view.findViewById(R.id.productName);
        TextView tvDescription = (TextView) view.findViewById(R.id.productDesctiption);
        TextView tvQuantity = (TextView) view.findViewById(R.id.quantity);
        TextView tvPrice = (TextView)view.findViewById(price);

        //get the column index for each item
        int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_NAME);
        int descriptionColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_DESCRIPTION);
        final int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_QUANTITY);
        int saleColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_PRICE);

        //get the data from the database at that column
        final String nameString = cursor.getString(nameColumnIndex);
        String descriptionString = cursor.getString(descriptionColumnIndex);
        final int quantityInt = cursor.getInt(quantityColumnIndex);
        int saleInt = cursor.getInt(saleColumnIndex);
        final int id = cursor.getInt(idColumnIndex);

        //set the text
        tvName.setText(nameString);
        tvDescription.setText(descriptionString);
        tvQuantity.setText(Integer.toString(quantityInt));

        java.math.BigDecimal formattedPrice = new java.math.BigDecimal(saleInt).movePointLeft(2);
        String paymentString = formattedPrice.toString();

        tvPrice.setText("$" + paymentString);

        final Button button = (Button) view.findViewById(R.id.saleButton);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ContentValues values = new ContentValues();
                Uri currentProductUri =
                        ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                if(quantityInt <= 0){
                   Toast.makeText(context, R.string.nothing_to_sell, Toast.LENGTH_SHORT).show();
                }else{
                    values.put(ProductEntry.PRODUCT_QUANTITY, quantityInt-1);
                    context.getContentResolver().update(
                            currentProductUri, values, null, null);
                }
            }
        });
    }

}
