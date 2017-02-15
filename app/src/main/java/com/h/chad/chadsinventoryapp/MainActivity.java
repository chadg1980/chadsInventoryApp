package com.h.chad.chadsinventoryapp;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.h.chad.chadsinventoryapp.data.ProductContract;
import com.h.chad.chadsinventoryapp.data.ProductContract.ProductEntry;


public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final int URL_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup up the FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
           @Override
            public void onClick(View v){
               Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
               startActivity(intent);
           }
        });

    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri baseUri = ProductEntry.CONTENT_URI;
        String [] projection = {
                ProductEntry._ID,
                ProductEntry.PRODUCT_NAME,
                ProductEntry.PRODUCT_DESCRIPTION,
                ProductEntry.PRODUCT_PRICE,
                ProductEntry.PRODUCT_QUANTITY
        };

        return new CursorLoader(
                this,
                baseUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
