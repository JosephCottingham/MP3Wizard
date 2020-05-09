package com.teambuild.mp3wizard.audioplayer;

import com.teambuild.mp3wizard.Book;

import java.util.List;

public interface AudioPlayerServiceInterface {
	public void configureQueueWithBook(Book book);
	public void skipToPoint(int time);
	public void play();
	public void play(int position);
	public void pause();
	public Book getCurrentQueuedBook();
}
