package com.teambuild.mp3wizard.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teambuild.mp3wizard.Book;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class localStorageDatabase extends SQLiteOpenHelper {
    static String TAG = "SQL";
    // This database holds all currently downloaded MP3 Stories
    private static final String DATABASE_NAME = "local_storage";
    private static final String TABLE_NAME = "table_name";

    public localStorageDatabase(Context context){
        super(context, DATABASE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        Log.d(TAG, "onCreate: Started");
        String createTable = "create table " + TABLE_NAME + "(title TEXT, currentFile INT, fileNum INT, locSec BIGINT, ID TEXT PRIMARY KEY, path TEXT)";
        Log.d(TAG, "onCreate: " + createTable);
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void clearDatabase(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase(); // gets the database
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS TABLE_NAME");
        onCreate(this.getWritableDatabase());
    }
    // TODO Change
    public boolean addBook(String title, int currentFile, int fileNum, long locSec){
        // Retreive database
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", title);
        contentValues.put("currentFile", Integer.valueOf(currentFile));
        contentValues.put("fileNum", Integer.valueOf(fileNum));
        contentValues.put("locSec", Long.valueOf(locSec));
        contentValues.put("ID", createNewID(sqLiteDatabase));
        contentValues.put("path", (getApplicationContext().getFilesDir() + File.separator + title));

        sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        return true;
    }

    public ArrayList<String> getBookTitles(){
        // retreive database
        ArrayList<String> arrayList = new ArrayList<String>();
        // create cursor to select all data
        Cursor cursor = this.getReadableDatabase().rawQuery("select title from " + TABLE_NAME, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            arrayList.add(cursor.getString(cursor.getColumnIndex("title")));
            cursor.moveToNext();
        }
        return arrayList;
    }

    public ArrayList<Book> getAllDownloadData() {
        if (!isTableExists(TABLE_NAME))
            onUpgrade(this.getReadableDatabase(), 1, 1);
        ArrayList<Book> arrayList = new ArrayList<Book>();
        Cursor cursor = this.getReadableDatabase().rawQuery("select * from " + TABLE_NAME, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Book book = new Book(cursor.getString(cursor.getColumnIndex("title")),
                            String.valueOf(cursor.getInt(cursor.getColumnIndex("fileNum"))),
                            String.valueOf(cursor.getLong(cursor.getColumnIndex("locSec"))),
                            String.valueOf(cursor.getInt(cursor.getColumnIndex("currentFile"))),
                            "downloaded");
                            book.setID(String.valueOf(cursor.getString(cursor.getColumnIndex("ID"))));
                            book.setPath(String.valueOf(cursor.getString(cursor.getColumnIndex("path"))));
                    arrayList.add(book);
                } while (cursor.moveToNext());
            }
        }
        return arrayList;
    }

    public Book getBookWithID(String ID){
        ArrayList<Book> arrayList = getAllDownloadData();
        for (int x = 0; x < arrayList.size(); x++){
            if(arrayList.get(x).getID().equals(ID)) return arrayList.get(x);
        }
        return null;
    }

    private String createNewID(SQLiteDatabase sqLiteDatabase) {
        int n = 16;
        // length is bounded by 256 Character
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        String randomString
                = new String(array, Charset.forName("UTF-8"));

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // Append first 20 alphanumeric characters
        // from the generated random String into the result
        for (int k = 0; k < randomString.length(); k++) {

            char ch = randomString.charAt(k);

            if (((ch >= 'a' && ch <= 'z')
                    || (ch >= 'A' && ch <= 'Z')
                    || (ch >= '0' && ch <= '9'))
                    && (n > 0)) {

                r.append(ch);
                n--;
            }
        }
        if (checkIfValidID(r.toString(), sqLiteDatabase))
            return r.toString();
        else
            return createNewID(sqLiteDatabase);
    }

    private boolean checkIfValidID(String testCase, SQLiteDatabase sqLiteDatabase){
        Cursor cursor = this.getReadableDatabase().rawQuery("select ID from " + TABLE_NAME, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    if(cursor.getString(cursor.getColumnIndex("ID")).equals(testCase));
                        return false;
                } while (cursor.moveToNext());
            }
        }
        return true;
    }

    private boolean isTableExists(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'";
        try (Cursor cursor = db.rawQuery(query, null)) {
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    return true;
                }
            }
            return false;
        }
    }

    public void updateCurLoc(Book book){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", book.getTitle());
        contentValues.put("currentFile", Integer.valueOf(book.getCurrentFile()));
        contentValues.put("fileNum", Integer.valueOf(book.getFileNum()));
        contentValues.put("locSec", Long.valueOf(book.getLocSec()));
        contentValues.put("ID", book.getID());
        contentValues.put("path", book.getPath());

        sqLiteDatabase.update(TABLE_NAME, contentValues, "ID = ?", new String[] {book.getID()});
        try {
            FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            String userId = mFirebaseAuth.getCurrentUser().getUid();
            mDatabase.child(userId).child(book.getTitle()).child("locSec").setValue(book.getLocSec());
        } catch (Exception e){
            Log.d("TEST", "updateCurLoc: Error: " + e.getMessage());
        }
    }

    public void removeBook(Book book){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_NAME,"ID = ?", new String[] {book.getID()});
    }
}
