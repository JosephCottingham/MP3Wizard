package com.teambuild.mp3wizard.repository.database.local;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.PlayerActivity;
import com.teambuild.mp3wizard.R;

import java.io.File;
import java.util.ArrayList;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class LocalListAdapterSQLITE extends ArrayAdapter<Book> {

    ArrayList<Book> lista;

    public LocalListAdapterSQLITE(Context context, ArrayList<Book> books){
        super(context, 0, books);
        lista = books;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Book book = lista.get(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.book_info_downloads, parent, false);
        }
        // Lookup view for data population
        LinearLayout pane = new LinearLayout(getContext());
        ImageView bookIcon = (ImageView) convertView.findViewById(R.id.bookIcon);
        TextView bookTitle = (TextView) convertView.findViewById(R.id.bookTitle);
        TextView bookFile = (TextView) convertView.findViewById(R.id.bookFile);
        TextView bookTime = (TextView) convertView.findViewById(R.id.bookTime);
        TextView bookDownloaded = (TextView) convertView.findViewById(R.id.bookDownloaded);
        final Button removeBookBtn = (Button) convertView.findViewById(R.id.bookRemoveBtn);
        // Populate the data into the template view using the data object
        bookTitle.setText(book.getTitle());
        bookFile.setText(String.format("File %s / %s", book.getCurrentFile(), book.getFileNum()));
        Log.d("ListView", "getView: CurrentFile: " + book.getCurrentFile());
        Log.d("ListView", "getView: Hour: " + book.getFileNumAsInt());

        int totalSec = book.getLocSecAsInt();
        int hour = (totalSec/3600);
        int min = ((totalSec-(3600*hour))/60);
        int sec = (totalSec-((3600*hour)+(60*min)));
        String minS = String.valueOf(min);
        String secS = String.valueOf(sec);
        if (min < 10) minS = "0" + minS;
        if (sec < 10) secS += "0" + secS;
        String time = String.format("%s:%s:%s", hour, minS, secS);
        bookTime.setText(time);
        bookDownloaded.setText("Downloaded");

        // Open player button Listerner collection
        ((ImageView) convertView.findViewById(R.id.bookIcon)).setImageBitmap(BitmapFactory.decodeFile(new File(book.getPath(), "icon.png").getAbsolutePath()));
        bookIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Player", "onTouch: Pane Touched");

                Intent playerIntent = new Intent(getContext(), PlayerActivity.class);

                Bundle b = new Bundle();
                b.putString("bookID", book.getID());
                playerIntent.putExtras(b);
                getContext().startActivity(playerIntent);
            }
        });
        
        bookTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Player", "onTouch: Pane Touched");

                Intent playerIntent = new Intent(getContext(), PlayerActivity.class);

                Bundle b = new Bundle();
                b.putString("bookID", book.getID());
                playerIntent.putExtras(b);
                getContext().startActivity(playerIntent);
            }
        });
        bookFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Player", "onTouch: Pane Touched");

                Intent playerIntent = new Intent(getContext(), PlayerActivity.class);

                Bundle b = new Bundle();
                b.putString("bookID", book.getID());
                playerIntent.putExtras(b);
                getContext().startActivity(playerIntent);
            }
        });
        bookTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Player", "onTouch: Pane Touched");

                Intent playerIntent = new Intent(getContext(), PlayerActivity.class);

                Bundle b = new Bundle();
                b.putString("bookID", book.getID());
                playerIntent.putExtras(b);
                getContext().startActivity(playerIntent);
            }
        });
        bookDownloaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Player", "onTouch: Pane Touched");

                Intent playerIntent = new Intent(getContext(), PlayerActivity.class);

                Bundle b = new Bundle();
                b.putString("bookID", book.getID());
                playerIntent.putExtras(b);
                getContext().startActivity(playerIntent);
            }
        });

        removeBookBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                removeBookBtn.setClickable(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(false);
                builder.setTitle("Removing Content");
                builder.setMessage("You are about to delete content from your device, All Content from \"" + book.getTitle() + "\" will be removed");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LocalSQLiteDatabase db = new LocalSQLiteDatabase();
                                db.removeBook(book);
                                lista.remove(position);
                                LocalListAdapterSQLITE.this.notifyDataSetChanged();
                                Log.d("Remove", "onClick: ");
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeBookBtn.setClickable(true);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


        // Return the completed view to render on screen
        return convertView;
    }
}
