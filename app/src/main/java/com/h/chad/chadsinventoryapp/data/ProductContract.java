package com.h.chad.chadsinventoryapp.data;

import android.provider.BaseColumns;

/**
 * Created by chad on 2/14/2017.
 * this file will create the database
 */

public class ProductContract {
    //To prevent someone from accidentally createing the ProductContrct class
    private ProductContract(){}

    /**
     * Inner class that defines the values for the database table
     * Table: products
     *   -_ID       INTEGER
     *   -name      TEXT
     *   -price     INTEGER
     *   -quantity  INTEGER
     *   -photo     BLOB
     *   -supplier  INTEGER
     */
    public final static class ProductEntry implements BaseColumns{
        //name of the table
        public final static String TABLE_NAME = "products";
        //ID of each product    TYPE: INTEGER
        public final static String _ID = BaseColumns._ID;
        //name of product       TYPE: TEXT
        public final static String PRODUCT_NAME = "name";
        //Product Description   TYPE: TEXT
        public final static String PRODUCT_DESCRIPTION ="description";
        //price of the product in cents  TYPE: INTEGER
         public final static String PRODUCT_PRICE = "price";
        //photograph of product         TYPE: BLOB
        public final static String PRODUCT_PHOTO = "photo";
        //quantity available    TYPE: INTEGER
        public final static String PRODUCT_QUANTITY = "quantity";
        /* suppliers Possiblilites are
         * 1: Wholesale coffee hut
         * 2: Bean Barn
         * 3: South America Farmer
         * TYPE: INTEGER
         */
        public final static String PRODUCT_SUPPLIER = "supplier";

        //Suppliers possible value
        public static final int COFFEE_HUT = 0;
        public static final int BEAN_BARN = 1;
        public static final int SOUTH_AMERICA_FARMER = 2;

    }


}
