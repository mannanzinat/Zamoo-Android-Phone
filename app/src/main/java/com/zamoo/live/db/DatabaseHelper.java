package com.zamoo.live.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "work_db";

    public static final String TABLE_NAME = "work";

    public static final String COLUMN_ID = "id";
        public static final String WORK_ID = "work_id";
        public static final String DOWNLOAD_ID = "download_id";
        public static final String FILE_NAME = "file_name";
        public static final String TOTAL_SIZE = "total_size";
        public static final String DOWNLOAD_SIZE = "download_size";
        public static final String DOWNLOAD_STATUS = "download_status";
        public static final String URL = "url";
        public static final String APP_CLOSE_STATUS = "app_close_statuss";

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + WORK_ID + " TEXT,"
                    + DOWNLOAD_ID + " INTEGER,"
                    + TOTAL_SIZE + " TEXT,"
                    + DOWNLOAD_SIZE + " TEXT,"
                    + DOWNLOAD_STATUS + " TEXT,"
                    + URL + " TEXT,"
                    + APP_CLOSE_STATUS + " TEXT,"
                    + FILE_NAME + " TEXT)";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        // Create tables again
        onCreate(db);
    }



    public long insertWork(Work work) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(WORK_ID, work.getWorkId());
        values.put(DOWNLOAD_ID, work.getDownloadId());
        values.put(FILE_NAME, work.getFileName());
        values.put(URL, work.getUrl());
        values.put(WORK_ID, work.getWorkId());
        values.put(APP_CLOSE_STATUS, work.getAppCloseStatus());

        // insert row
        long id = db.insert(TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public int updateWork(Work work) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TOTAL_SIZE, work.getTotalSize());
        values.put(DOWNLOAD_SIZE, work.getDownloadSize());
        values.put(DOWNLOAD_STATUS, work.getDownloadStatus());
        values.put(APP_CLOSE_STATUS, work.getAppCloseStatus());

        Log.d("workId 2:", work.getWorkId());

        // updating row
        return db.update(TABLE_NAME, values, WORK_ID + " = ?",
                new String[]{work.getWorkId()});
    }

    public void deleteByDownloadId(int downloadId) {
        String sql = "delete from work where download_id="+downloadId;

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(sql);
    }

    public Work getWorkByDownloadId(int downloadId) {
        String sql = "select * from work where download_id="+downloadId;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        Work work = new Work();
        if (cursor.moveToFirst()) {

            work.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            work.setWorkId(cursor.getString(cursor.getColumnIndex(WORK_ID)));
            work.setDownloadId(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_ID)));
            work.setFileName(cursor.getString(cursor.getColumnIndex(FILE_NAME)));
            work.setTotalSize(cursor.getString(cursor.getColumnIndex(TOTAL_SIZE)));
            work.setDownloadSize(cursor.getString(cursor.getColumnIndex(DOWNLOAD_SIZE)));
            work.setDownloadStatus(cursor.getString(cursor.getColumnIndex(DOWNLOAD_STATUS)));
            work.setUrl(cursor.getString(cursor.getColumnIndex(URL)));
            work.setAppCloseStatus(cursor.getString(cursor.getColumnIndex(APP_CLOSE_STATUS)));
        }

        return work;

    }

    public List<Work> getAllWork() {
        List<Work> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Work work = new Work();
                work.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                work.setWorkId(cursor.getString(cursor.getColumnIndex(WORK_ID)));
                work.setDownloadId(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_ID)));
                work.setFileName(cursor.getString(cursor.getColumnIndex(FILE_NAME)));
                work.setTotalSize(cursor.getString(cursor.getColumnIndex(TOTAL_SIZE)));
                work.setDownloadSize(cursor.getString(cursor.getColumnIndex(DOWNLOAD_SIZE)));
                work.setDownloadStatus(cursor.getString(cursor.getColumnIndex(DOWNLOAD_STATUS)));
                work.setUrl(cursor.getString(cursor.getColumnIndex(URL)));
                work.setAppCloseStatus(cursor.getString(cursor.getColumnIndex(APP_CLOSE_STATUS)));
                notes.add(work);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return notes;
    }

    public void deleteNote(Work work) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?",
                new String[]{String.valueOf(work.getWorkId())});
        db.close();
    }

    public void deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

}
