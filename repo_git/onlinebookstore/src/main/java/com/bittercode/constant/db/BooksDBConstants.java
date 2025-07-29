package com.bittercode.constant.db;

public final class BooksDBConstants {

    private BooksDBConstants() {
        // Previene l'istanza della classe
    }

    public static final String TABLE_BOOK = "books";

    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_BARCODE = "barcode";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_QUANTITY = "quantity";
}
