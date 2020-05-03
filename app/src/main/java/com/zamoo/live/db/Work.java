package com.zamoo.live.db;

public class Work {
    int id;
    String workId;
    int downloadId;
    String fileName;
    String totalSize;
    String downloadSize;
    String downloadStatus;
    String url;
    String appCloseStatus;

    public String getAppCloseStatus() {
        return appCloseStatus;
    }

    public void setAppCloseStatus(String appCloseStatus) {
        this.appCloseStatus = appCloseStatus;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(String totalSize) {
        this.totalSize = totalSize;
    }

    public String getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(String downloadSize) {
        this.downloadSize = downloadSize;
    }

    public String getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(String downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWorkId() {
        return workId;
    }

    public void setWorkId(String workId) {
        this.workId = workId;
    }


    @Override
    public String toString() {
        return "Work{" +
                "id=" + id +
                ", workId='" + workId + '\'' +
                ", downloadId=" + downloadId +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
