package com.habibi.quanlykhohang;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_PRODUCTS = "products";

    private static final String COL_ID = "id";
    private static final String COL_BARCODE = "barcode";
    private static final String COL_NAME = "name";
    private static final String COL_PRICE = "price";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_LOCATION = "location";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BARCODE + " TEXT UNIQUE, " +
                COL_NAME + " TEXT, " +
                COL_PRICE + " REAL, " +
                COL_QUANTITY + " INTEGER, " +
                COL_LOCATION + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    public boolean insertProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_BARCODE, product.getBarcode());
        values.put(COL_NAME, product.getName());
        values.put(COL_PRICE, product.getPrice());
        values.put(COL_QUANTITY, product.getQuantity());
        values.put(COL_LOCATION, product.getLocation());

        long result = db.insert(TABLE_PRODUCTS, null, values);
        return result != -1;
    }

    public Product getProductByBarcode(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, null,
                COL_BARCODE + "=?", new String[]{barcode},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Product product = new Product();
            product.setId(cursor.getInt(0));
            product.setBarcode(cursor.getString(1));
            product.setName(cursor.getString(2));
            product.setPrice(cursor.getDouble(3));
            product.setQuantity(cursor.getInt(4));
            product.setLocation(cursor.getString(5));
            cursor.close();
            return product;
        }
        return null;
    }

    public boolean updateInventory(String barcode, int quantityChange) {
        Product product = getProductByBarcode(barcode);
        if (product != null) {
            int newQuantity = product.getQuantity() + quantityChange;
            if (newQuantity < 0) return false;

            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_QUANTITY, newQuantity);

            int rows = db.update(TABLE_PRODUCTS, values,
                    COL_BARCODE + "=?", new String[]{barcode});
            return rows > 0;
        }
        return false;
    }

    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product();
                product.setId(cursor.getInt(0));
                product.setBarcode(cursor.getString(1));
                product.setName(cursor.getString(2));
                product.setPrice(cursor.getDouble(3));
                product.setQuantity(cursor.getInt(4));
                product.setLocation(cursor.getString(5));
                productList.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return productList;
    }
}
