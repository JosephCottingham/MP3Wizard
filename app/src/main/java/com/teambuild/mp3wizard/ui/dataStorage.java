package com.teambuild.mp3wizard.ui;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.teambuild.mp3wizard.Book;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;



public class dataStorage {
    static String TAG = "Hello";
    public static boolean writeDownloadedListToSQL(final Book book, Context context, String userId){
        String dir = context.getFilesDir().getAbsolutePath();

        try {
            File FileDir = new File(userId + File.separator, "downloaded.txt");
            Log.d("tet", "ReadDownloadedList: " + FileDir.getAbsolutePath());
            FileDir.mkdirs();
            FileDir.createNewFile();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FileDir.getAbsolutePath(), Context.MODE_APPEND));
            Log.d(TAG, "writeDownloadedList: OutputStreamWriter Created");
            outputStreamWriter.write(book.getTitle() + System.getProperty("line.separator"));
            outputStreamWriter.close();
            FileDir = new File(userId + File.separator + book.getTitle() + File.separator + "loc.txt");
            outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FileDir.getAbsolutePath(), Context.MODE_APPEND));
            outputStreamWriter.write(book.getCurrentFile() + System.getProperty("line.separator") +
                    book.getFileNum() + System.getProperty("line.separator") + book.getLocSec() +
                    System.getProperty("line.separator"));
            outputStreamWriter.close();
            return true;
        }  catch(FileNotFoundException ex) {
            Log.d("Hello", ex.getMessage());
        }  catch(IOException ex) {
            Log.d("Hello", ex.getMessage());
        }
        return  false;
    }

    public static String[] ReadDownloadedList(Context context, String userID){
        String fullString = null;
        String dir = context.getFilesDir().getAbsolutePath();
        File FileDir = new File(dir + File.separator + userID + File.separator, "downloaded.txt");
        Log.d("tet", "ReadDownloadedList: " + FileDir.getAbsolutePath());
        try {
            FileDir.mkdir();
            FileDir.createNewFile();
            InputStream inputStream = context.openFileInput(FileDir.getAbsolutePath());
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(System.getProperty("line.separator")).append(receiveString);
                }

                inputStream.close();
                fullString = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
//        Log.d("Hello", "ReadFile: Line " + line);
//        Log.d("tet", "ReadDownloadedList: " + fullString.split(System.getProperty("line.separator")));
        if (fullString!= null) {
            Log.d("tet", "ReadDownloadedList: " + fullString.split(System.getProperty("line.separator")));
            return fullString.split(System.getProperty("line.separator"));
        }
        return new String[0];
    }


    private String[] ReadTimeData(String title, Context context, String userID){
        String line = null;
        String dir = context.getFilesDir().getAbsolutePath();
        File FileDir = new File( userID + File.separator + title + File.separator);
        String path = FileDir.getAbsolutePath();

        try {
            FileInputStream fileInputStream = new FileInputStream (new File(path + "loc.txt"));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            Log.d("Hello", ex.getMessage());
        }
        catch(IOException ex) {
            Log.d("Hello", ex.getMessage());
        }
        return line.split(System.getProperty("line.separator"));
    }

}
