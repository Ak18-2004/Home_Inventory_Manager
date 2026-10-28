
package com.example.homeinventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    EditText etName, etQuantity, etDate;
    Button btnSave;
    ListView listItems;

    DatabaseHelper dbHelper;
    ArrayList<String> itemList;
    ArrayList<Integer> itemIds;
    ArrayAdapter<String> adapter;

    int selectedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);
        listItems = findViewById(R.id.listItems);

        dbHelper = new DatabaseHelper(this);

        itemList = new ArrayList<>();
        itemIds = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                itemList
        );

        listItems.setAdapter(adapter);

        btnSave.setOnClickListener(v -> saveItem());

        listItems.setOnItemClickListener(
                (parent, view, position, id) -> showItemOptions(position)
        );

        loadItems();
    }

    // CREATE OR UPDATE
    private void saveItem() {
        String name = etName.getText().toString().trim();
        String quantityText =
                etQuantity.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (name.isEmpty() || quantityText.isEmpty()
                || date.isEmpty()) {
            Toast.makeText(this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;

        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Enter a valid quantity",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Quantity validation: minimum 1, maximum 500
        if (quantity < 1 || quantity > 500) {
            Toast.makeText(this,
                    "Quantity must be between 1 and 5000",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Basic date format validation
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this,
                    "Enter date as YYYY-MM-DD",
                    Toast.LENGTH_LONG).show();
            return;
        }

        boolean success;

        if (selectedId == -1) {
            success = dbHelper.insertItem(name, quantity, date);
        } else {
            success = dbHelper.updateItem(
                    selectedId, name, quantity, date
            );
        }

        if (success) {
            Toast.makeText(this,
                    selectedId == -1
                            ? "Item added successfully"
                            : "Item updated successfully",
                    Toast.LENGTH_SHORT).show();

            clearFields();
            loadItems();
        } else {
            Toast.makeText(this,
                    "Operation failed",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // READ
    private void loadItems() {
        itemList.clear();
        itemIds.clear();

        Cursor cursor = dbHelper.getAllItems();

        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                );

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                int quantity = cursor.getInt(
                        cursor.getColumnIndexOrThrow("quantity")
                );

                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow("purchase_date")
                );

                itemIds.add(id);

                itemList.add(
                        "Item: " + name
                                + "\nQuantity: " + quantity
                                + "\nPurchase Date: " + date
                                + "\nTap to Edit or Delete"
                );
            }
        } finally {
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    // EDIT OR DELETE MENU
    private void showItemOptions(int position) {
        int id = itemIds.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Choose Action")
                .setItems(new String[]{"Edit", "Delete"},
                        (dialog, which) -> {
                            if (which == 0) {
                                editItem(position);
                            } else {
                                confirmDelete(id);
                            }
                        })
                .show();
    }

    // UPDATE: LOAD EXISTING VALUES
    private void editItem(int position) {
        int id = itemIds.get(position);

        Cursor cursor = dbHelper.getReadableDatabase()
                .rawQuery(
                        "SELECT * FROM items WHERE id=?",
                        new String[]{String.valueOf(id)}
                );

        try {
            if (cursor.moveToFirst()) {
                selectedId = id;

                etName.setText(cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                ));

                etQuantity.setText(String.valueOf(
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("quantity")
                        )
                ));

                etDate.setText(cursor.getString(
                        cursor.getColumnIndexOrThrow("purchase_date")
                ));

                btnSave.setText("Update Item");
            }
        } finally {
            cursor.close();
        }
    }

    // DELETE WITH CONFIRMATION
    private void confirmDelete(int id) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage(
                        "Are you sure you want to delete this item?"
                )
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (dbHelper.deleteItem(id)) {
                        Toast.makeText(this,
                                "Item deleted successfully",
                                Toast.LENGTH_SHORT).show();

                        if (selectedId == id) {
                            clearFields();
                        }

                        loadItems();
                    } else {
                        Toast.makeText(this,
                                "Delete failed",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    // CLEAR FORM
    private void clearFields() {
        etName.setText("");
        etQuantity.setText("");
        etDate.setText("");

        selectedId = -1;
        btnSave.setText("Add Item");
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
