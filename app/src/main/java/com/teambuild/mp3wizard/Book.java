package com.teambuild.mp3wizard;


public class Book {
    private String title;
    private String fileNum;
    private String currentFile;
    private String locSec;
    private String downloaded;
    private String ID;
    private String path;
    private String duration;

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

    public int getFileNumAsInt(){
        return Integer.parseInt(fileNum);
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public int getCurrentFileAsInt(){
        return Integer.parseInt(currentFile);
    }

    public String getLocSec() {
        return locSec;
    }

    public int getLocSecAsInt(){
        return Integer.parseInt(locSec);
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

    public String getAsString(){
        return (title + " " + fileNum + " " + currentFile + " " + locSec);
    }

    public String getID(){
        return ID;
    }

    public String getPath(){
        return path;
    }

    public void setID(String ID){
        this.ID = ID;
    }

    public void setPath(String path){
        this.path = path;
    }

}
