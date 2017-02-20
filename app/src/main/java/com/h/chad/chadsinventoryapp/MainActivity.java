package com.h.chad.chadsinventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;

import com.h.chad.chadsinventoryapp.data.ProductContract;
import com.h.chad.chadsinventoryapp.data.ProductContract.ProductEntry;

import java.util.List;

import static android.R.attr.id;
import static com.h.chad.chadsinventoryapp.R.id.fab;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final int URL_LOADER = 0;
    private ProductCursorAdapter mProductCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup up the FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                startActivity(intent);
            }
        });
        //find the listview to be populated
        ListView lvProducts = (ListView) findViewById(R.id.list_products);
        lvProducts.setEmptyView(findViewById(R.id.empty));
        //Setup the adapter to create a list for each row
        //of the Inventory Database, Products table
        mProductCursorAdapter = new ProductCursorAdapter(this, null);
        lvProducts.setAdapter(mProductCursorAdapter);

        //click listener for each item to open up the AddEditActivity
        lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long thisId) {
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                //Form the content URI that represents the specific product that was
                //clicked on, by appending the "id" (passed as input to his method) onto
                //the ProductEntry.CONTENT_URI
                Uri currentProductUri =
                        ContentUris.withAppendedId(ProductEntry.CONTENT_URI, thisId);
                //Set the URI on the datra field of the intent
                intent.setData(currentProductUri);
                //launch the activity
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(URL_LOADER, null, this);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri baseUri = ProductEntry.CONTENT_URI;
        String[] projection = {
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
        mProductCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductCursorAdapter.swapCursor(null);
    }
}
