package com.teambuild.mp3wizard.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.PlayerActivity;
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.ui.localStorageDatabase;

import java.util.ArrayList;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class DownloadBookAdapter extends ArrayAdapter<Book> {
    public DownloadBookAdapter(Context context, ArrayList<Book> books){
        super(context, 0, books);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final Book book = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.book_info_downloads, parent, false);
        }
        // Lookup view for data population
        LinearLayout pane = new LinearLayout(getContext());
        TextView bookTitle = (TextView) convertView.findViewById(R.id.bookTitle);
        TextView bookFile = (TextView) convertView.findViewById(R.id.bookFile);
        TextView bookTime = (TextView) convertView.findViewById(R.id.bookTime);
        TextView bookDownloaded = (TextView) convertView.findViewById(R.id.bookDownloaded);
        Button removeBookBtn = (Button) convertView.findViewById(R.id.bookRemoveBtn);
        // Populate the data into the template view using the data object
        bookTitle.setText(book.getTitle());
        bookFile.setText(String.format("File %s / %s", book.getCurrentFile(), book.getFileNum()));
        Log.d("ListView", "getView: CurrentFile: " + book.getCurrentFile());
        Log.d("ListView", "getView: Hour: " + book.getFileNumAsInt());

        long totalSec = book.getLocSecAsLong();
        long hour = (totalSec%3600);
        Log.d("ListView", "getView: Hour: " + String.valueOf(hour));
        long min = ((totalSec-(3600*hour))%60);
        Log.d("ListView", "getView: min: " + String.valueOf(min));
        long sec = (totalSec-((3600*hour)+(60*min)));
        Log.d("ListView", "getView: sec: " + String.valueOf(sec));
        String time = String.format("%d:%d:%d", hour, min, sec);
        bookTime.setText(time);
        bookDownloaded.setText("Downloaded");

        // Open player button Listerner collection

        bookTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Player", "onTouch: Pane Touched");
                String[] data = new String[2];
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
                getContext().startActivity(new Intent(getContext(), PlayerActivity.class));
            }
        });
        bookTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Player", "onTouch: Pane Touched");
                getContext().startActivity(new Intent(getContext(), PlayerActivity.class));
            }
        });
        bookDownloaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Player", "onTouch: Pane Touched");
                getContext().startActivity(new Intent(getContext(), PlayerActivity.class));
            }
        });

        removeBookBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d("Remove", "onClick: ");
            }
        });


        // Return the completed view to render on screen
        return convertView;
    }
}
