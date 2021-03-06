package com.teambuild.mp3wizard.repository.database.local;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.R;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class LocalSQLiteDatabase extends SQLiteOpenHelper {
    static String TAG = "SQL";
    // This database holds all currently downloaded MP3 Stories
    private static final String DATABASE_NAME = "local_storage";
    private static final String DOWNLOADED_TABLE_NAME = "downloaded_table_name";
    private static final String DOWNLOADING_TABLE_NAME = "downloading_table_name";
    @SuppressLint("RestrictedApi")
    public LocalSQLiteDatabase(){
        super(getApplicationContext(), DATABASE_NAME, null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        Log.d(TAG, "onCreate: Started");
        String createDownloadedTable = "create table " + DOWNLOADED_TABLE_NAME + "(title TEXT, currentFile INT, fileNum INT, locSec BIGINT, ID TEXT PRIMARY KEY, path TEXT)";
        Log.d(TAG, "onCreate: " + createDownloadedTable);
        db.execSQL(createDownloadedTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DOWNLOADED_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void clearDatabase(String tableName){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase(); // gets the database
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS tableName");
        onCreate(this.getWritableDatabase());
    }
    // TODO Change
    @SuppressLint("RestrictedApi")
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

        sqLiteDatabase.insert(DOWNLOADED_TABLE_NAME, null, contentValues);
        return true;
    }

    @SuppressLint("RestrictedApi")
    public boolean addBook(Book book){
        // Retreive database
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", book.getTitle());
        contentValues.put("currentFile", Integer.valueOf(book.getCurrentFile()));
        contentValues.put("fileNum", Integer.valueOf(book.getFileNum()));
        contentValues.put("locSec", Long.valueOf(book.getLocSec()));
        contentValues.put("ID", createNewID(sqLiteDatabase));
        contentValues.put("path", (getApplicationContext().getFilesDir() + File.separator + book.getTitle()));

        sqLiteDatabase.insert(DOWNLOADED_TABLE_NAME, null, contentValues);
        return true;
    }

    public ArrayList<String> getBookTitles(){
        // retreive database
        ArrayList<String> arrayList = new ArrayList<String>();
        // create cursor to select all data
        Cursor cursor = this.getReadableDatabase().rawQuery("select title from " + DOWNLOADED_TABLE_NAME, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            arrayList.add(cursor.getString(cursor.getColumnIndex("title")));
            cursor.moveToNext();
        }
        return arrayList;
    }

    public ArrayList<Book> getAllDownloadData() {
        if (!isTableExists(DOWNLOADED_TABLE_NAME))
            onUpgrade(this.getReadableDatabase(), 1, 1);
        ArrayList<Book> arrayList = new ArrayList<Book>();
        Cursor cursor = this.getReadableDatabase().rawQuery("select * from " + DOWNLOADED_TABLE_NAME, null);
        if (cursor != null) {
            if (cursor.moveToFirst() && cursor != null) {
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
        if (checkIfValidID(r.toString()))
            return r.toString();
        else
            return createNewID(sqLiteDatabase);
    }

    private boolean checkIfValidID(String testCase){
        Log.d(TAG, "checkIfValidID: Test Case: " + testCase);
        // somthing wrong here
        Cursor cursor = this.getReadableDatabase().rawQuery("select ID from " + DOWNLOADED_TABLE_NAME, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
//                    if (cursor.getString(cursor.getColumnIndex("ID"))==null)
//                        break;
                    if(cursor.getString(cursor.getColumnIndex("ID")).equals(testCase)) return false;
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

        sqLiteDatabase.update(DOWNLOADED_TABLE_NAME, contentValues, "ID = ?", new String[] {book.getID()});
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
        // Remove from SQLITE Database
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(DOWNLOADED_TABLE_NAME,"ID = ?", new String[] {book.getID()});

        // Remove Audio/Icon Files from Storage
        for (int fileNum = 1; fileNum <= book.getFileNumAsInt(); fileNum++) {
            File localAudioFile = new File(book.getPath(), String.format("%d.mp3", fileNum));
            localAudioFile.delete();
        }
        File localIconFile = new File(book.getPath(), "icon.png");
        localIconFile.delete();
    }

    public LocalListAdapterSQLITE getLocalListAdapterSQLITE(Context context) {
        return new LocalListAdapterSQLITE(context, getAllDownloadData());
    }

    public Book getBookWithTitle(String title){
        ArrayList<Book> books = getAllDownloadData();
        for (Book book : books)
            if (book.getTitle().equals(title)) return book;
        return null;
    }

    public void addDownloadingItem(String title){
        if(!isTableExists(DOWNLOADING_TABLE_NAME)){
            createDownloadingTable();
        }
        // Retreive database
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Title", title);

        sqLiteDatabase.insert(DOWNLOADING_TABLE_NAME, null, contentValues);
    }

    public void removeDownloadingItem(String title){
        // Remove from SQLITE Database
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(DOWNLOADING_TABLE_NAME,"Title = ?", new String[] {title});
    }

    public ArrayList<String> getDownloadingItems(){
        if (!isTableExists(DOWNLOADING_TABLE_NAME))
            return new ArrayList<String>();
        ArrayList<String> titles = new ArrayList<String>();
        Cursor cursor = this.getReadableDatabase().rawQuery("select * from " + DOWNLOADING_TABLE_NAME, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    titles.add(cursor.getString(cursor.getColumnIndex("Title")));
                } while (cursor.moveToNext());
            }
        }
        return titles;
    }

    private void createDownloadingTable(){
        SQLiteDatabase db = this.getReadableDatabase();
        String createCurrentlyDownloadingTable = "create table " + DOWNLOADING_TABLE_NAME + "(Title TEXT)";
        Log.d(TAG, "onCreate: " + createCurrentlyDownloadingTable);
        db.execSQL(createCurrentlyDownloadingTable);
    }

    public void removeTable(){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DOWNLOADING_TABLE_NAME);
    }
}
