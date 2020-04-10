package com.teambuild.mp3wizard.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teambuild.mp3wizard.Book;
import com.teambuild.mp3wizard.PlayerActivity;
import com.teambuild.mp3wizard.R;
import com.teambuild.mp3wizard.ui.localStorageDatabase;

import java.io.File;
import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class DownloadBookAdapter extends ArrayAdapter<Book> {

    ArrayList<Book> lista;

    public DownloadBookAdapter(Context context, ArrayList<Book> books){
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
        int hour = (int)(totalSec/3600);
        int min = (int)((totalSec-(3600*hour))/60);
        int sec = (int)(totalSec-((3600*hour)+(60*min)));
        String minS = String.valueOf(min);
        String secS = String.valueOf(sec);
        if (min < 10) minS = "0" + minS;
        if (sec < 10) secS += "0" + secS;
        String time = String.format("%s:%s:%s", hour, minS, secS);
        bookTime.setText(time);
        bookDownloaded.setText("Downloaded");

        // Open player button Listerner collection

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
                localStorageDatabase db = new localStorageDatabase(getApplicationContext());
                for (int x = 1; x <= book.getFileNumAsInt(); x++) {
                    new File(getApplicationContext().getFilesDir() + File.separator + book.getTitle(), String.format("%d.mp3", x)).delete();
                }
                db.removeBook(book);
                lista.remove(position);
                DownloadBookAdapter.this.notifyDataSetChanged();
                Log.d("Remove", "onClick: ");
            }
        });


        // Return the completed view to render on screen
        return convertView;
    }
}
