package com.h.chad.chadsinventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.h.chad.chadsinventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by chad on 2/14/2017.
 * Database: chadsCoffee.db
 * Table: products
 *   -_ID       INTEGER
 *   -name      TEXT
 *   -description TEXT
 *   -price     INTEGER
 *   -quantity  INTEGER
 *   -photo     BLOB
 *   -supplier  INTEGER
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    private final static String LOG_TAG = InventoryDbHelper.class.getName();
    //Name of the database file
    private final static String DATABASE_NAME = "chadsCoffee.db";
    //Database version
    private final static int DATABASE_VERSION = 1;
    /**
     * Constructor
     * @param context of the app
     * */
    public InventoryDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    /**
     * Oncreate is called when the database is created for the first time
     *   -_ID           INTEGER
     *   -name          TEXT
     *   -description   TEXT
     *   -price         INTEGER
     *   -quantity      INTEGER
     *   -photo         BLOB
     *   -supplier      INTEGER
     * */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //Create a String that contains the SQL statement to create the pet table
        String SQL_CREATE_PRODUCTS_TABLE =
                "CREATE TABLE " + ProductEntry.TABLE_NAME +"( "          +
                ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ProductEntry.PRODUCT_NAME + " TEXT NOT NULL, "            +
                ProductEntry.PRODUCT_DESCRIPTION +" TEXT, "               +
                ProductEntry.PRODUCT_PRICE + " INTEGER NOT NULL DEFAULT 9999999, "        +
                ProductEntry.PRODUCT_QUANTITY +" INTEGER NOT NULL, "      +
                ProductEntry.PRODUCT_PHOTO + " BLOB, "                    +
                ProductEntry.PRODUCT_SUPPLIER + " INTEGER);";
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //The database is on Version 1
    }
}
