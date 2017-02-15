package com.h.chad.chadsinventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.h.chad.chadsinventoryapp.data.ProductContract;
import com.h.chad.chadsinventoryapp.data.ProductContract.ProductEntry;

import static android.R.attr.name;

/**
 * Created by chad on 2/14/2017.
 * AddEditActivity allows the user to add a product
 * or edit an existing product
 */

public class AddEditActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>{
    private final static String LOG_TAG = AddEditActivity.class.getName();
    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private Uri mCurrentProductUri;
    private static final int EXISTING_PRODUCT_LOADER = 0;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        //get the intent and get the data
        //to get the associated URI and determine
        //If this is a new product or the product is being edited
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if(mCurrentProductUri == null){
            setTitle("Add a Product");
        }
        else {
            setTitle("Edit product");
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_text_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_text_description);
        mPriceEditText = (EditText) findViewById(R.id.edit_text_price);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_add_edit, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        //Adds menu items to the app bar
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.save:
                saveProducts();
                break;
            case R.id.delete:
                deleteProduct();
            break;
        }
        return true;
    }

    private void deleteProduct() {

    }

    private void saveProducts() {
        //read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        if(TextUtils.isEmpty(nameString)){
            Log.e(LOG_TAG, "NO NAME");
            return;
        }
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        if(TextUtils.isEmpty(priceString)){
            priceString = Integer.toString(99999999);
        }


        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.PRODUCT_NAME, nameString);
        values.put(ProductContract.ProductEntry.PRODUCT_DESCRIPTION, descriptionString);
        values.put(ProductContract.ProductEntry.PRODUCT_PRICE, Integer.parseInt(priceString));

        //DUMMY DATA FOR QUANTITY
        values.put(ProductEntry.PRODUCT_QUANTITY, 0);
        values.put(ProductEntry.PRODUCT_SUPPLIER, 1);

        Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
        if(newUri == null){
            Toast.makeText(this, "Failure inserting product", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "success inserting product", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int thisId, Bundle args) {
        String projection [] = {
                ProductEntry._ID,
                ProductEntry.PRODUCT_NAME,
                ProductEntry.PRODUCT_DESCRIPTION,
                ProductEntry.PRODUCT_QUANTITY,
                ProductEntry.PRODUCT_PRICE
        };
        return new android.content.CursorLoader(
                this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor == null || cursor.getCount() < 1){
            return;
        }
        if(cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_DESCRIPTION);

            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);

            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mPriceEditText.setText("");

    }
}
