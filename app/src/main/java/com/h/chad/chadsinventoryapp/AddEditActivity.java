package com.h.chad.chadsinventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Picture;
import android.icu.math.BigDecimal;
import android.icu.text.NumberFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.h.chad.chadsinventoryapp.data.ProductContract;
import com.h.chad.chadsinventoryapp.data.ProductContract.ProductEntry;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import static android.R.attr.bitmap;
import static android.R.attr.button;
import static android.R.attr.dial;
import static android.R.attr.id;
import static android.R.attr.name;
import static android.R.attr.onClick;
import static android.R.attr.order;
import static android.R.attr.previewImage;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;
import static com.h.chad.chadsinventoryapp.R.id.price;
import static com.h.chad.chadsinventoryapp.R.id.quantity;
import static com.h.chad.chadsinventoryapp.R.id.saleButton;

/**
 * Created by chad on 2/14/2017.
 * AddEditActivity allows the user to add a product
 * or edit an existing product
 */

public class AddEditActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = AddEditActivity.class.getName();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private Uri mCurrentProductUri;
    private Spinner mSupplierSpinner;
    private int mSupplier = ProductEntry.NO_SUPPLIER;
    private TextView mSupplierDetails;
    private String mSupplierEmailAddress;
    private Button mOrderBbutton;
    private String[] addresses;
    private Button mTakeAPicture;
    private ImageView mProductImage;
    private Bitmap mBitmapForDatabase;
    private boolean mHasImage;
    private Button mDetailSale;
    private Button mDetailShipment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);

        //get the intent and get the data
        //to get the associated URI and determine
        //If this is a new product or the product is being edited
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        mOrderBbutton = (Button) findViewById(R.id.order_from_supplier);
        mDetailSale = (Button) findViewById(R.id.detail_sale);
        mDetailShipment = (Button) findViewById(R.id.detail_shipment);
        if (mCurrentProductUri == null) {
            setTitle("Add a Product");
            mOrderBbutton.setEnabled(false);
            mDetailSale.setEnabled(false);
            mDetailShipment.setEnabled(false);
        } else {
            setTitle("Edit product");
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mPriceEditText = (EditText) findViewById(R.id.edit_text_price);
        mNameEditText = (EditText) findViewById(R.id.edit_text_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_text_description);
        mQuantityEditText = (EditText) findViewById(R.id.edit_text_quantity);
        mSupplierDetails = (TextView) findViewById(R.id.supplier_details);
        mSupplierSpinner = (Spinner) findViewById(R.id.edit_supplier_spinner);
        mTakeAPicture = (Button) findViewById(R.id.addPhoto);
        mProductImage = (ImageView) findViewById(R.id.productImage);
        mBitmapForDatabase = null;
        mHasImage = false;
        setupSpinner();
        takePicture();
        orderProductButton();
        madeASale();
        shipmentReceived();

    }


    //Shipment Received Button, adds 1 to quantity when button is pressed.
    private void shipmentReceived() {
        mDetailShipment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = mQuantityEditText.getText().toString().trim();
                if (TextUtils.isEmpty(quantityString)) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.add_manually), Toast.LENGTH_SHORT).show();
                } else {
                    int quantityInt = Integer.parseInt(quantityString);

                    ContentValues values = new ContentValues();
                    quantityInt += 1;
                    quantityString = Integer.toString(quantityInt);
                    values.put(ProductEntry.PRODUCT_QUANTITY, quantityString);
                    getContentResolver().update(mCurrentProductUri, values, null, null);
                }
            }
        });
    }

    //Made a sale subtracts one from the inventory of the current product.
    private void madeASale() {
        mDetailSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String quantityString = mQuantityEditText.getText().toString().trim();
                int quantityInt = Integer.parseInt(quantityString);
                if (quantityInt <= 0 || TextUtils.isEmpty(quantityString)) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.nothing_to_sell), Toast.LENGTH_SHORT).show();
                } else {
                    ContentValues values = new ContentValues();
                    quantityInt -= 1;
                    quantityString = Integer.toString(quantityInt);
                    values.put(ProductEntry.PRODUCT_QUANTITY, quantityString);
                    if (mCurrentProductUri == null) {
                        getContentResolver().insert(
                                ProductEntry.CONTENT_URI, values);
                    } else {
                        getContentResolver().update(mCurrentProductUri, values, null, null);
                    }
                }
            }
        });
    }


    private void takePicture() {

        mTakeAPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
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
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.coffee_hut))) {
                        mSupplier = ProductEntry.COFFEE_HUT;
                        mSupplierDetails.setText(R.string.coffee_hut_details);
                        mSupplierEmailAddress = getString(R.string.coffee_hut_email);
                        mOrderBbutton.setEnabled(true);
                    } else if (selection.equals(getString(R.string.bean_barn))) {
                        mSupplier = ProductEntry.BEAN_BARN;
                        mSupplierDetails.setText(R.string.bean_barn_details);
                        mSupplierEmailAddress = getString(R.string.bean_barn_email);
                        mOrderBbutton.setEnabled(true);
                    } else if (selection.equals(getString(R.string.farm_direct))) {
                        mSupplier = ProductEntry.FARM_DIRECT;
                        mSupplierDetails.setText(R.string.farm_direct_details);
                        mSupplierEmailAddress = getString(R.string.farm_direct_email);
                        mOrderBbutton.setEnabled(true);
                    } else {
                        mSupplier = ProductEntry.NO_SUPPLIER;
                        mSupplierDetails.setText(R.string.no_supplier);
                        mSupplierEmailAddress = null;
                        mOrderBbutton.setEnabled(false);
                    }
                    addresses = new String[]{mSupplierEmailAddress};
                }
            }

            //Because AdapterView is an abstract class, onNothingSelected must be defined.
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mSupplier = ProductEntry.NO_SUPPLIER;
                mOrderBbutton.setEnabled(false);
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
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
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

        int dollars = 0;
        int cents = 0;
        //If no price is entered, a very high price is a placeholder so the
        //product doesn't get sold for no money
        if (TextUtils.isEmpty(priceString)) {
            values.put(ProductEntry.PRODUCT_PRICE, 9999999);
        } else if (priceString.contains(".")) {
            String[] formatPrice = priceString.split("\\.", -1);
            if (TextUtils.isEmpty(formatPrice[0])) {
                Log.v(LOG_TAG, "decimal, but no dollars");
            } else {
                dollars = 100 * Integer.parseInt(formatPrice[0]);
            }

            if (TextUtils.isEmpty(formatPrice[1])) {
                Log.v(LOG_TAG, "decimal, but no cents");
            } else {
                cents = Integer.parseInt(formatPrice[1]);
                if (formatPrice[1].length() == 1) {
                    cents *= 10;
                }
            }

            dollars = cents + dollars;
            values.put(ProductEntry.PRODUCT_PRICE, dollars);
        } else {
            values.put(ProductEntry.PRODUCT_PRICE, Integer.parseInt(priceString));
        }

        String quantityString = mQuantityEditText.getText().toString().trim();
        if (TextUtils.isEmpty(quantityString) || Integer.parseInt(quantityString) < 0) {
            values.put(ProductEntry.PRODUCT_QUANTITY, 0);
        } else {
            values.put(ProductEntry.PRODUCT_QUANTITY, quantityString);
        }

        mSupplierEmailAddress = getSupplierEmail(mSupplier);
        values.put(ProductEntry.PRODUCT_NAME, nameString);
        values.put(ProductEntry.PRODUCT_DESCRIPTION, descriptionString);


        values.put(ProductEntry.PRODUCT_SUPPLIER_EMAIL, mSupplierEmailAddress);
        values.put(ProductEntry.PRODUCT_SUPPLIER, mSupplier);

        if (mHasImage) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            mBitmapForDatabase.compress(Bitmap.CompressFormat.JPEG, 0, stream);
            values.put(ProductEntry.PRODUCT_PHOTO, stream.toByteArray());
        }

        //if mCurrentProductUri is null, we are adding a new product
        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(
                    ProductEntry.CONTENT_URI, values);
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

    //getSupplierEmail chooses the appropriate email for the supplier selected in the spinner
    private String getSupplierEmail(int mSupplier) {
        switch (mSupplier) {
            case ProductEntry.COFFEE_HUT:
                return getString(R.string.coffee_hut_email);
            case ProductEntry.BEAN_BARN:
                return getString(R.string.bean_barn_email);
            case ProductEntry.FARM_DIRECT:
                return getString(R.string.farm_direct_email);
            default:
                return "";
        }
    }

    //When the Order Button is clicked, it sends informational to the order Button method
    private void orderProductButton() {
        final String subject = "Placing a new order";
        addresses = new String[]{mSupplierEmailAddress};
        mOrderBbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                orderButton(addresses, subject);
            }
        });
    }

    //OrderButton takes the address and subject to mail to, and starts an intent
    //to email the supplier.
    private void orderButton(final String[] mailTo, final String subject) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, mailTo);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
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
                ProductEntry.PRODUCT_SUPPLIER,
                ProductEntry.PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.PRODUCT_PHOTO
        };
        return new CursorLoader(
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
            int supplierEmailColumnIndex =
                    cursor.getColumnIndex(ProductEntry.PRODUCT_SUPPLIER_EMAIL);
            int productPhotoColumnIndex = cursor.getColumnIndex(ProductEntry.PRODUCT_PHOTO);

            mSupplierEmailAddress = cursor.getString(supplierEmailColumnIndex);

            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            long price = cursor.getLong(priceColumnIndex);
            java.math.BigDecimal formattedPrice = new java.math.BigDecimal(price).movePointLeft(2);
            String paymentString = formattedPrice.toString();

            int quantity = cursor.getInt(quantityColumnIndex);

            int supplierNumber = cursor.getInt(supplierColumnIndex);
            byte[] productPhoto = cursor.getBlob(productPhotoColumnIndex);
            if (productPhoto != null) {
                mHasImage = true;
                mBitmapForDatabase = BitmapFactory.decodeByteArray(productPhoto, 0, productPhoto.length);
            }

            mProductImage.setImageBitmap(mBitmapForDatabase);
            mNameEditText.setText(name);
            mDescriptionEditText.setText(description);
            mPriceEditText.setText(paymentString);
            mQuantityEditText.setText(Integer.toString(quantity));
            //Supplier spinner switch statement
            switch (supplierNumber) {
                case ProductEntry.COFFEE_HUT:
                    mSupplierSpinner.setSelection(1);
                    mSupplierDetails.setText(R.string.coffee_hut_details);
                    mSupplierEmailAddress = getString(R.string.coffee_hut_email);
                    break;
                case ProductEntry.BEAN_BARN:
                    mSupplierSpinner.setSelection(2);
                    mSupplierDetails.setText(R.string.bean_barn_details);
                    mSupplierEmailAddress = getString(R.string.bean_barn_email);
                    break;
                case ProductEntry.FARM_DIRECT:
                    mSupplierSpinner.setSelection(3);
                    mSupplierDetails.setText(R.string.farm_direct_details);
                    mSupplierEmailAddress = getString(R.string.farm_direct_email);
                    break;
                default:
                    mSupplierSpinner.setSelection(0);
                    mSupplierDetails.setText(R.string.no_supplier);
                    mSupplierEmailAddress = null;
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
        mSupplierSpinner.setSelection(0);
    }

    //Take a picture
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mBitmapForDatabase = (Bitmap) extras.get("data");
            mHasImage = true;
            mProductImage.setImageBitmap(mBitmapForDatabase);
        }
    }


}
