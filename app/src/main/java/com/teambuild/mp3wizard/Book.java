package com.teambuild.mp3wizard;

public class Book {
    private String title;
    private String fileNum;
    private String currentFile;
    private String locSec;
    private String downloaded;

    public Book(){
    }

    public Book(String title, String fileNum, String locSec, String currentFile, String downloaded) {
        this.title = title;
        this.fileNum = fileNum;
        this.locSec = locSec;
        this.currentFile = currentFile;
        this.downloaded = downloaded;
    }

    public String getTitle() {
        return title;
    }

    public String getFileNum() {
        return fileNum;
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public String getLocSec() {
        return locSec;
    }

    public String getDownloaded(){
        return downloaded;
    }
    public void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }

    public void setLocSec(String locSec) {
        this.locSec = locSec;
    }

    public void setDownloaded(String downloaded){
        this.downloaded = downloaded;
    }
}
