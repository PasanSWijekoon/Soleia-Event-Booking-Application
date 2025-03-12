package com.strawhats.soleia.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user_data.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "user_profile";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PROFILE_PIC = "profile_picture";

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PROFILE_PIC + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert user data into SQLite
    public long insertUser(String name, String email, String profilePic) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PROFILE_PIC, profilePic);
        return db.insert(TABLE_NAME, null, values);
    }

    // Get user data from SQLite
    public Cursor getUserData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, new String[]{COLUMN_NAME, COLUMN_PROFILE_PIC , COLUMN_EMAIL},
                null, null, null, null, null);
    }

    // Delete user data from SQLite
    public void deleteUserData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    // Update user profile picture in SQLite
    public int updateUserProfilePicture(String profilePicUrl, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE_PIC, profilePicUrl);

        // Update the profile picture based on the user's email
        return db.update(TABLE_NAME, values, COLUMN_EMAIL + " = ?", new String[]{email});
    }

    public int updateUserName(String username, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, username);

        // Update the profile picture based on the user's email
        return db.update(TABLE_NAME, values, COLUMN_EMAIL + " = ?", new String[]{email});
    }

}