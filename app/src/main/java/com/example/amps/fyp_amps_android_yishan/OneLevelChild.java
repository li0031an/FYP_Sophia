package com.example.amps.fyp_amps_android_yishan;

import java.util.ArrayList;

public class OneLevelChild {
    String error_code;
    String error_message;
    ArrayList<Folder> folderList;

    public String getError_code() {
        return error_code;
    }

    public String getError_message() {
        return error_message;
    }

    public ArrayList<Folder> getFolderList() {
        return folderList;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

    public void setFolderList(ArrayList<Folder> folderList) {
        this.folderList = folderList;
    }

}
