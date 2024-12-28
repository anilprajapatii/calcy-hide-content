package com.anil.hidecontentcalcy;

public class FileItem {
    private String fileName;
    private String createdTime;
    private String fileSize;
    private String filePath;

    public FileItem(String fileName, String createdTime, String fileSize, String filePath) {
        this.fileName = fileName;
        this.createdTime = createdTime;
        this.fileSize = fileSize;
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

}
