package com.h.chad.chadsinventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.h.chad.chadsinventoryapp.data.ProductContract;
import com.h.chad.chadsinventoryapp.data.ProductContract.ProductEntry;

import org.w3c.dom.Text;

import static android.R.attr.dial;
import static android.R.attr.id;
import static android.R.attr.name;

/**
 * Created by chad on 2/14/2017.
 * AddEditActivity allows the user to add a product
 * or edit an existing product
 */

public class AddEditActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = AddEditActivity.class.getName();
    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private Uri mCurrentProductUri;
    private Spinner mSupplierSpinner;
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private int mSupplier = ProductEntry.NO_SUPPLIER;
    private TextView mSupplierDetails;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);

        //get the intent and get the data
        //to get the associated URI and determine
        //If this is a new product or the product is being edited
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle("Add a Product");
        } else {
            setTitle("Edit product");
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.edit_text_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_text_description);
        mPriceEditText = (EditText) findViewById(R.id.edit_text_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_text_quantity);
        mSupplierDetails = (TextView)findViewById(R.id.supplier_details);
        mSupplierSpinner = (Spinner) findViewById(R.id.edit_supplier_spinner);
        setupSpinner();
    }

    private void setupSpinner() {
        mSupplier = ProductEntry.NO_SUPPLIER;
        //Create an adapter for the spinner.
        //The list of options are from the String array it will use the default layout
        ArrayAdapter suppllierAdapter = ArrayAdapter.createFromResource(
                this, R.array.supplier_options, android.R.layout.simple_spinner_item);
        //Specify dropdown layout style - simple list view with 1 item per line
        suppllierAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        //Apply the adapter to the spinner
        mSupplierSpinner.setAdapter(suppllierAdapter);

        //Set the integer to the constant in ProductContract
        mSupplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                String selection = (String) adapterView.getItemAtPosition(pos);
                if (!TextUtils.isEmpty(selection)){
                    if(selection.equals(getString(R.string.coffee_hut))){
                        mSupplier = ProductEntry.COFFEE_HUT;
                        mSupplierDetails.setText(R.string.coffee_hut_details);
                    }else if(selection.equals(getString(R.string.bean_barn))){
                        mSupplier = ProductEntry.BEAN_BARN;
                        mSupplierDetails.setText(R.string.bean_barn_details);
                    }else if(selection.equals(getString(R.string.farm_direct))){
                        mSupplier = ProductEntry.FARM_DIRECT;
                        mSupplierDetails.setText(R.string.farm_direct_details);
                    }else{
                        mSupplier = ProductEntry.NO_SUPPLIER;
                        mSupplierDetails.setText(R.string.no_supplier);
                    }
                    //mSupplier = 0;
                }
            }
            //Because AdapterView is an abstract class, onNothingSelected must be defined.
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mSupplier = ProductEntry.NO_SUPPLIER;
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_edit, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Adds menu items to the app bar
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveProducts();
                finish();
                break;
            case R.id.delete:
                confirmDelete();
                break;
        }
        return true;
    }

    /**
     * comfirm delete will allow the user to double check whether they meant to
     * delete the current product.
     * confirm delete takes no input and calls deleteProduct() method if a positive response
     */
    private void confirmDelete() {
        //Create a Alert Dialog Builder and set teh message
        //for positive and negative buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete_message);
        builder.setPositiveButton(R.string.confirm_positive_delete,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        //user clicks delete, product gets deleted in the deleteProduct() method.
                        deleteProduct();
                    }
                });

        builder.setNegativeButton(R.string.confirm_negative_delete,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                });
        //create and show the Alert Dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rows_deleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rows_deleted == 0) {
                Toast.makeText(getApplicationContext(),
                        "Error Deleting Product", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Product Deleted", Toast.LENGTH_SHORT).show();
            }
        }
        finish();

    }

    private void saveProducts() {
        ContentValues values = new ContentValues();

        //read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(nameString)) {
            Log.e(LOG_TAG, "NO NAME");
            return;
        }
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        if (TextUtils.isEmpty(priceString)) {
            priceString = Integer.toString(99999999);
        }
        String quantityString = mQuantityEditText.getText().toString().trim();


        if (TextUtils.isEmpty(quantityString) || Integer.parseInt(quantityString) < 0) {
            values.put(ProductEntry.PRODUCT_QUANTITY, 0);
        } else {
            values.put(ProductEntry.PRODUCT_QUANTITY, quantityString);
        }
        values.put(ProductContract.ProductEntry.PRODUCT_NAME, nameString);
        values.put(ProductContract.ProductEntry.PRODUCT_DESCRIPTION, descriptionString);
        values.put(ProductContract.ProductEntry.PRODUCT_PRICE, Integer.parseInt(priceString));

        values.put(ProductEntry.PRODUCT_SUPPLIER, mSupplier);
        //if mCurrentProductUri is null, we are adding a new product
        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "Failure inserting product", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "success inserting product", Toast.LENGTH_SHORT).show();
            }
        }
        //if mCurrentProductUri is not null
        else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, "Failure updating product", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "success updating product", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int thisId, Bundle args) {
        String projection[] = {
                ProductEntry._ID,
                ProductEntry.PRODUCT_NAME,
                ProductEntry.PRODUCT_DESCRIPTION,
                ProductEntry.PRODUCT_QUANTITY,
                ProductEntry.PRODUCT_PRICE,
                ProductEntry.PRODUCT_SUPPLIER
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
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_DESCRIPTION);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_SUPPLIER);

            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int supplierNumber = cursor.getInt(supplierColumnIndex);

            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            //Supplier spinner switch statement
            switch (supplierNumber){
                case ProductEntry.COFFEE_HUT:
                    mSupplierSpinner.setSelection(1);
                    mSupplierDetails.setText(R.string.coffee_hut_details);
                    break;
                case ProductEntry.BEAN_BARN:
                    mSupplierSpinner.setSelection(2);
                    mSupplierDetails.setText(R.string.bean_barn_details);
                    break;
                case ProductEntry.FARM_DIRECT:
                    mSupplierSpinner.setSelection(3);
                    mSupplierDetails.setText(R.string.farm_direct_details);
                    break;
                default:
                    mSupplierSpinner.setSelection(0);
                    mSupplierDetails.setText(R.string.no_supplier);
                    break;
            }

        }
    }



    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
    }
}
