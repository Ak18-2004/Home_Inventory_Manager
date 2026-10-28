
package com.example.homeinventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "HomeInventory.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "items";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "quantity INTEGER NOT NULL CHECK(quantity BETWEEN 1 AND 5000), " +
                        "purchase_date TEXT NOT NULL)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // CREATE
    public boolean insertItem(String name, int quantity, String date) {
        if (quantity < 1 || quantity > 500) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("quantity", quantity);
        values.put("purchase_date", date);

        return db.insert(TABLE, null, values) != -1;
    }

    // READ
    public Cursor getAllItems() {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE + " ORDER BY id DESC",
                null
        );
    }

    // UPDATE
    public boolean updateItem(int id, String name,
                              int quantity, String date) {
        if (quantity < 1 || quantity > 500) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("quantity", quantity);
        values.put("purchase_date", date);

        return db.update(
                TABLE,
                values,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    // DELETE
    public boolean deleteItem(int id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                TABLE,
                "id=?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }
}
