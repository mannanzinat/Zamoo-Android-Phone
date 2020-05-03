package com.zamoo.live.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import com.zamoo.live.db.DownloadInfo;
import com.zamoo.live.utils.Constants;
import com.zamoo.live.utils.Encrypter;
import com.zamoo.live.utils.MyAppClass;
import com.zamoo.live.utils.ToastMsg;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DownloadService extends Service {

    public static final String KEY = "sHpTEGs6R21EbatPEJlPtw==";
    public static final String ENCRYPT_SIGN = "e_";
    public static final String DECRYPT_SIGN = "d_";

    public static boolean isServiceRunning;
    public static final String ACTION_NEW_DOWNLOAD = "action_new_download";
    public static final String ACTION_CANCEL_DOWNLOAD = "action_cancel_download";
    public static final String ACTION_FINISH_DOWNLOAD = "action_finish_download";
    public static final String ACTION_CANCEL_ALL_DOWNLOAD = "action_cancel_all_download";

    private NotificationCompat.Builder notification;
    private NotificationManagerCompat notificationManager;
    private DatabaseHelper helper;
    private List<DownloadInfo> downloadInfoList = new ArrayList<>();




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        helper = new DatabaseHelper(this);
        notificationManager = NotificationManagerCompat.from(this);

        IntentFilter intentFilter = new IntentFilter(ACTION_NEW_DOWNLOAD);
        registerReceiver(startNewDownload, intentFilter);
        registerReceiver(cancelNotification, new IntentFilter(ACTION_CANCEL_DOWNLOAD));
        registerReceiver(finishingDownload, new IntentFilter(ACTION_FINISH_DOWNLOAD));
        registerReceiver(cancelAllDownload, new IntentFilter(ACTION_CANCEL_ALL_DOWNLOAD));

    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        isServiceRunning = true;

        final String url = intent.getStringExtra("url");
        final String fileName = intent.getStringExtra("fileName");
        final int notificationId = intent.getIntExtra("notificationId", 0);
        Log.d("id2:",notificationId+"");

        Intent cancelIntent = new Intent(ACTION_CANCEL_DOWNLOAD);
        cancelIntent.putExtra("notificationId", notificationId);
        PendingIntent cance1PI = PendingIntent.getBroadcast(this, 0, cancelIntent, 0);

        Intent cancelAllIntent = new Intent(ACTION_CANCEL_ALL_DOWNLOAD);
        PendingIntent cancelAllPI = PendingIntent.getBroadcast(this, 0, cancelAllIntent, 0);

        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        remoteViews.setProgressBar(R.id.progressBarOne, 100, 0, false);
        remoteViews.setTextViewText(R.id.download_amount_tv, "0");
        remoteViews.setTextViewText(R.id.file_name_tv, fileName);
        remoteViews.setTextViewText(R.id.downloaded_amount_tv, "Downloading");
        remoteViews.setOnClickPendingIntent(R.id.cancel_bt, cance1PI);

        notification = new NotificationCompat.Builder(this, MyAppClass.NOTIFICATION_CHANNEL_ID);
        notification.setSmallIcon(R.drawable.ic_stat_onesignal_default);
        notification.setOnlyAlertOnce(true);
        notification.setOngoing(true);
        notification.setPriority(NotificationCompat.PRIORITY_LOW);
        //notification.setContent(remoteViews);
        notification.setContentText("Downloading");
        notification.setContentTitle(fileName);
        notification.setProgress(100, 0, false);
        notification.addAction(R.drawable.ic_close_black_24dp, "Cancel All", cancelAllPI);
        //notification.addAction(R.drawable.ic_launcher_background, "Start", null);
        //notificationManager.notify(1, notification.build());


        startForeground(notificationId, notification.build());

        startDownLoad(url, fileName, notificationId);


        return START_NOT_STICKY;
    }

    private void startDownLoad(final String url, final String fileName, final int notificationId) {

        new Thread(new Runnable() {

            int downloadId;
            int tempCurrentProgress;

            @Override
            public void run() {


                // Enabling database for resume support even after the application is killed:
                PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                        .setDatabaseEnabled(true)
                        .setReadTimeout(40_000)
                        .setConnectTimeout(40_000)
                        .build();
                PRDownloader.initialize(getApplicationContext(), config);
                String path = Constants.DOWNLOAD_DIR + getResources().getString(R.string.app_name);
                downloadId = PRDownloader.download(url, path, fileName)
                        .build()
                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {

                            @Override
                            public void onStartOrResume() {

                                Log.d("status:", "start/resume");

                            }
                        })
                        .setOnPauseListener(new OnPauseListener() {
                            @Override
                            public void onPause() {

                            }
                        })
                        .setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel() {


                            }
                        })
                        .setOnProgressListener(new OnProgressListener() {
                            @Override
                            public void onProgress(final Progress progress) {

                                final int totalProgress = (int) ((progress.totalBytes /1024) / 1024); //mb
                                final int currentProgress = (int) ((progress.currentBytes /1024) / 1024); //mb

                                if (tempCurrentProgress != currentProgress) {
                                    updateNotification(totalProgress, currentProgress, downloadId, fileName);
                                    tempCurrentProgress = currentProgress;
                                }

                            }
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                Log.d("on Completed", "called");
                                //helper.deleteByDownloadId(downloadId);
                                completeDownload(downloadId, fileName);
                            }

                            @Override
                            public void onError(Error error) {

                                new ToastMsg(DownloadService.this).toastIconError("Download canceled cause of network connection failed.");
                                error.getConnectionException().printStackTrace();

                                // stop the service and remove notification if network error is occurred.
                                notificationManager.cancel(notificationId);
                                stopSelf();


                            }

                        });

                //Log.d("downloadId : noti", downloadId +": "+notificationId);

                DownloadInfo dIndfo = new DownloadInfo(downloadId, notificationId, fileName);

                downloadInfoList.add(dIndfo);


            }
        }).start();

    }


    public void updateNotification(final int totalProgress, final int currentProgress, final int downloadId, final String fileName) {


//        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
//        remoteViews.setTextViewText(R.id.file_name_tv, fileName);

        Intent cancelAllIntent = new Intent(ACTION_CANCEL_ALL_DOWNLOAD);
        PendingIntent cancelAllPI = PendingIntent.getBroadcast(this, 0, cancelAllIntent, 0);

        final NotificationCompat.Builder notification = new NotificationCompat.Builder(DownloadService.this, MyAppClass.NOTIFICATION_CHANNEL_ID);
        notification.setSmallIcon(R.drawable.ic_stat_onesignal_default);
        notification.setOnlyAlertOnce(true);
        notification.setOngoing(true);
        notification.setPriority(NotificationCompat.PRIORITY_LOW);
        notification.addAction(R.drawable.ic_close_black_24dp, "Cancel All", cancelAllPI);
        //notification.setContent(remoteViews);

        new Handler().post(new Runnable() {
            @Override
            public void run() {

                for (DownloadInfo downloadInfo : downloadInfoList) {

                    if (downloadId == downloadInfo.getDownloadId()) {

                        //Log.d("downloadId : noti", downloadId +": "+downloadInfo.getNotificationId());

//                        Intent cancelIntent = new Intent(ACTION_CANCEL_DOWNLOAD);
//                        cancelIntent.putExtra("notificationId", downloadInfo.getNotificationId());
//                        PendingIntent cance1PI = PendingIntent.getBroadcast(DownloadService.this, 0, cancelIntent, 0);
//
                        /*remoteViews.setTextViewText(R.id.downloaded_amount_tv, currentProgress+" MB/ "+totalProgress+" MB");
                        remoteViews.setProgressBar(R.id.progressBarn, totalProgress, currentProgress, false);*/

                        notification.setContentText(currentProgress+" MB/ "+totalProgress+" MB");
                        notification.setContentTitle(fileName);
                        notification.setProgress(totalProgress, currentProgress, false);
                        //remoteViews.setOnClickPendingIntent(R.id.cancel_bt, cance1PI);
                        notificationManager.notify(downloadInfo.getNotificationId(), notification.build());
                    }
                }
            }
        });

    }

    public void completeDownload(int downloadId, String fileName) {

        new ToastMsg(DownloadService.this).toastIconSuccess(Constants.SECURED_DOWNLOAD ? "Finishing Download ..." : "Download Completed");

//        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
//        remoteViews.setProgressBar(R.id.progressBarn, 100, 0, true);

        notification = new NotificationCompat.Builder(DownloadService.this, MyAppClass.NOTIFICATION_CHANNEL_ID);
        notification.setSmallIcon(R.drawable.ic_stat_onesignal_default);
        notification.setOngoing(true);
        notification.setPriority(NotificationCompat.PRIORITY_LOW);
        notification.setProgress(100, 0, Constants.SECURED_DOWNLOAD);
        //notification.setContent(remoteViews);

        int i = 0;
        for (DownloadInfo downloadInfo : downloadInfoList) {

            if (downloadId == downloadInfo.getDownloadId()) {
                //remoteViews.setTextViewText(R.id.downloaded_amount_tv, "Finishing Download");

                notification.setContentText(Constants.SECURED_DOWNLOAD ? "Finishing Download" : "Download Completed");
                notification.setContentTitle(fileName);
                notificationManager.notify(downloadInfo.getNotificationId(), notification.build());

                if (downloadInfoList.size() > 1) {
                    PRDownloader.cancel(downloadId);

                    if (Constants.SECURED_DOWNLOAD) {
                        // encode the video
                        encodeVideo(downloadInfo.getFileName(), downloadInfo.getNotificationId(), i);
                    } else {
                        downloadInfoList.remove(i);
                        notificationManager.cancel(downloadInfo.getNotificationId());
                    }

                } else {
                    PRDownloader.shutDown();

                    if (Constants.SECURED_DOWNLOAD) {
                        // encode the video
                        encodeVideo(downloadInfo.getFileName(), downloadInfo.getNotificationId(), i);
                    } else {
                        downloadInfoList.remove(i);
                        notificationManager.cancel(downloadInfo.getNotificationId());
                        stopSelf();
                    }
                }
            }
            i++;
        }
    }

    private void encodeVideo(String fileName, final int notificationId, final int downloadListIndex) {

        //Log.d("ev", fileName);

        String path = Constants.DOWNLOAD_DIR + getResources().getString(R.string.app_name) + File.separator + fileName;
        String path2 = Constants.DOWNLOAD_DIR + getResources().getString(R.string.app_name)
                + File.separator + ENCRYPT_SIGN + fileName;

        final File inFile = new File(path);
        final File outFile = new File(path2);
        final byte[] bytes = Base64.decode(KEY, Base64.DEFAULT);
        final Encrypter encrypter = new Encrypter(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                encrypter.encryptVideo(bytes, inFile, outFile, notificationId, downloadListIndex);
            }
        }).start();

    }

    private BroadcastReceiver startNewDownload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String url = intent.getStringExtra("url");
            String fileName = intent.getStringExtra("fileName");
            int notificationId = intent.getIntExtra("notificationId", 0);

            Intent cancelAllIntent = new Intent(ACTION_CANCEL_ALL_DOWNLOAD);
            PendingIntent cancelAllPI = PendingIntent.getBroadcast(DownloadService.this, 0, cancelAllIntent, 0);


//            Intent cancelIntent = new Intent(ACTION_CANCEL_DOWNLOAD);
//            cancelIntent.putExtra("notificationId", notificationId);
//            PendingIntent cance1PI = PendingIntent.getBroadcast(DownloadService.this, 0, cancelIntent, 0);

//            final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
//            remoteViews.setTextViewText(R.id.file_name_tv, fileName);
//            remoteViews.setOnClickPendingIntent(R.id.cancel_bt, cance1PI);

            final NotificationCompat.Builder notification = new NotificationCompat.Builder(DownloadService.this, MyAppClass.NOTIFICATION_CHANNEL_ID);
            notification.setSmallIcon(R.drawable.ic_stat_onesignal_default);
            notification.setOnlyAlertOnce(true);
            notification.setOngoing(true);
            notification.setPriority(NotificationCompat.PRIORITY_LOW);
            //notification.setContent(remoteViews);
            notification.addAction(R.drawable.ic_close_black_24dp, "Cancel All", cancelAllPI);
            notificationManager.notify(notificationId, notification.build());

            startDownLoad(url, fileName, notificationId);



        }
    };

    private BroadcastReceiver cancelNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int notificationId = intent.getIntExtra("notificationId", 0);

            //Toast.makeText(DownloadService.this, "notificatinId: "+notificationId, Toast.LENGTH_SHORT).show();

            /*notification = new NotificationCompat.Builder(DownloadService.this, MyAppClass.NOTIFICATION_CHANNEL_ID);
            notification.setSmallIcon(R.drawable.ic_stat_onesignal_default);
            notification.setOnlyAlertOnce(true);
            notification.setOngoing(false);
            notification.setPriority(NotificationCompat.PRIORITY_LOW);
            notificationManager.notify(notificationId, notification.build());*/

        }
    };

    private BroadcastReceiver cancelAllDownload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            new ToastMsg(DownloadService.this).toastIconSuccess("Canceled All Download.");

            PRDownloader.cancelAll();
            PRDownloader.shutDown();
            notificationManager.cancelAll();
            stopSelf();

        }
    };

    private BroadcastReceiver finishingDownload = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String message = intent.getStringExtra("message");
            String originalFile = intent.getStringExtra("originalFile");
            int notificationId = intent.getIntExtra("notificationId", 0);
            int downloadListIndex = intent.getIntExtra("downloadListIndex", 0);

            // delete the original file
            String path = Constants.DOWNLOAD_DIR + getResources().getString(R.string.app_name)
                    + File.separator + originalFile;

            File file = new File(path);
            if (file.exists()) {
                file.delete();
            } else {
                Log.d("Finishing download:", "File not exist to delete");
            }

            new ToastMsg(DownloadService.this).toastIconSuccess(message);

            // also stop the service
            if (downloadInfoList.size() > 1) {
                Log.d("downloadListsize:", downloadInfoList.size()+"");
                downloadInfoList.remove(downloadListIndex);
                notificationManager.cancel(notificationId);
            } else {
                notificationManager.cancel(notificationId);
                downloadInfoList.remove(downloadListIndex);
                stopSelf();
            }

        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        unregisterReceiver(startNewDownload);
        unregisterReceiver(cancelNotification);
        unregisterReceiver(finishingDownload);
        unregisterReceiver(cancelAllDownload);
    }
}