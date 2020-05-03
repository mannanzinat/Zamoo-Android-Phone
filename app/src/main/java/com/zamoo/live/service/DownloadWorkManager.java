package com.zamoo.live.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.zamoo.live.R;
import com.zamoo.live.db.DatabaseHelper;
import com.zamoo.live.db.Work;
import com.zamoo.live.utils.Constants;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class DownloadWorkManager extends Worker {

    public static final String START_PAUSE_ACTION = "startPause";
    public static final String START_PAUSE_STATUS = "startPauseStatus";
    public static final String START_PAUSE_FEEDBACK_STATUS = "startPauseFeedbackStatus";
    public static final String PROGRESS_RECEIVER = "progress_receiver";

    String fileName;
    int downloadId;

    Context context;

    DatabaseHelper helper;
    private boolean isDownloading;
    private String workId;

    private long downloadByte, totalByte;

    public DownloadWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {

        helper = new DatabaseHelper(context);
        workId = Constants.workId;

        Data data = getInputData();
        final String url = data.getString("url");
        final String dir = data.getString("dir");
        final String fileName = data.getString("fileName");

        Log.d("url in job service:", url);

        new Thread(new Runnable() {
            @Override
            public void run() {


                // Enabling database for resume support even after the application is killed:
                PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                        .setDatabaseEnabled(true)
                        .setReadTimeout(30_000)
                        .setConnectTimeout(30_000)
                        .build();
                PRDownloader.initialize(getApplicationContext(), config);
                String path = Constants.DOWNLOAD_DIR + context.getResources().getString(R.string.app_name);
                downloadId = PRDownloader.download(url, path, fileName)
                        .build()
                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                            @Override
                            public void onStartOrResume() {

                                isDownloading = true;

                                Intent intent = new Intent(START_PAUSE_FEEDBACK_STATUS);
                                intent.putExtra("result", RESULT_OK);
                                intent.putExtra("downloadId", downloadId);
                                intent.putExtra("status", "start");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                // set app close status false
                                Work work = helper.getWorkByDownloadId(downloadId);
                                work.setAppCloseStatus("false");
                                // save the data to the database
                                helper.updateWork(work);

                            }
                        })
                        .setOnPauseListener(new OnPauseListener() {
                            @Override
                            public void onPause() {
                                isDownloading = false;

                                Intent intent = new Intent(START_PAUSE_FEEDBACK_STATUS);
                                intent.putExtra("result", RESULT_OK);
                                intent.putExtra("downloadId", downloadId);
                                intent.putExtra("status", "pause");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


                                Work work = helper.getWorkByDownloadId(downloadId);

                                work.setDownloadSize(downloadByte+"");
                                work.setTotalSize(totalByte+"");
                                work.setDownloadStatus("paused");
                                work.setAppCloseStatus("false");
                                // save the data to the database
                                helper.updateWork(work);

                            }
                        })
                        .setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel() {
                                isDownloading = false;

                            }
                        })
                        .setOnProgressListener(new OnProgressListener() {
                                    @Override
                                    public void onProgress(Progress progress) {
                                        Intent intent = new Intent(PROGRESS_RECEIVER);
                                        intent.putExtra("result", RESULT_OK);
                                        intent.putExtra("downloadId", downloadId);
                                        intent.putExtra("currentByte", progress.currentBytes);
                                        intent.putExtra("workId", workId);
                                        intent.putExtra("totalByte", progress.totalBytes);
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                downloadByte = progress.currentBytes;
                                totalByte = progress.totalBytes;
                            }
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                helper.deleteByDownloadId(downloadId);
                            }

                            @Override
                            public void onError(Error error) {
                                error.getConnectionException().printStackTrace();
                            }

                        });

                boolean isDuplicationFound = false;
                List<Work> workList = helper.getAllWork();
                for (Work w : workList) {
                    if (w.getDownloadId() == downloadId) {
                        isDuplicationFound = true;
                    }
                }

                if (!isDuplicationFound) {
                    Work work = new Work();
                    work.setWorkId(Constants.workId);
                    work.setDownloadId(downloadId);
                    work.setFileName(fileName);
                    work.setUrl(url);
                    work.setAppCloseStatus("false");
                    long v = helper.insertWork(work);

                    Log.d("-----------", v+"");
                }

            }
        }).start();

        return Result.SUCCESS;
    }

    @Override
    public void onStopped() {
        super.onStopped();


    }
}
