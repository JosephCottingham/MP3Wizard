package com.teambuild.mp3wizard.audioplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.teambuild.mp3wizard.Book;
//import java.util.Random;


public class Queue {
	private List<File> queue = new ArrayList<File>();
	public Book currentBook;
	private int currentFile = -1;
	private boolean random = false;
	private List<Book> random_queue = new ArrayList<Book>();

	public File getCurrentlyPlaying() {
		return queue.get(currentFile);
	}

	public int getCurrentlyPlayingFileNum(){
		return currentFile;
	}

	public void setQueue(Book book){
		currentBook = book;
		currentFile = book.getCurrentFileAsInt()-1;
		for (int x = 1; x <= book.getFileNumAsInt(); x++){
			queue.add(new File(book.getPath(), String.format("%d.mp3", x)));
		}
	}

	public int getSizeOfQueue() {
		return queue.size();
	}
	
	public File next() {
		if (currentFile+1<queue.size() && currentFile!=-1){
			return queue.get(currentFile+1);
		}
		return null;
	}
	
	public File last() {
		if (currentFile-1>=0 && currentFile!=-1){
			return queue.get(currentFile-1);
		}
		return null;
	}
	
	public File playGet(int position) {
		currentFile = position;
		return queue.get(position);
	}
	
	public void clearQueue() {
		queue.clear();
		currentFile = -1;
	}
}
