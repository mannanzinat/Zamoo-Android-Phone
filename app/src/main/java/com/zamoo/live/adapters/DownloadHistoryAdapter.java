package com.zamoo.live.adapters;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zamoo.live.DownloadActivity;
import com.zamoo.live.R;
import com.zamoo.live.models.VideoFile;
import com.zamoo.live.service.DownloadService;
import com.zamoo.live.utils.Constants;
import com.zamoo.live.utils.Encrypter;
import com.zamoo.live.utils.Tools;

import java.io.File;
import java.util.List;

public class DownloadHistoryAdapter extends RecyclerView.Adapter<DownloadHistoryAdapter.ViewHolder> {

    private Context context;
    private List<VideoFile> videoFiles;

    public DownloadHistoryAdapter(Context context, List<VideoFile> videoFiles) {
        this.context = context;
        this.videoFiles = videoFiles;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_download_history, parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        VideoFile videoFile = videoFiles.get(position);

        holder.fileNameTv.setText(videoFile.getFileName());
        holder.fileSizeTv.setText("Size: "+Tools.byteToMb(videoFile.getTotalSpace()));
        holder.dateTv.setText(Tools.milliToDate(videoFile.getLastModified()));



    }

    @Override
    public int getItemCount() {
        Log.d("vidoe size:", videoFiles.size()+"");
        return videoFiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        TextView fileNameTv, fileSizeTv, dateTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            fileNameTv = itemView.findViewById(R.id.file_name_tv);
            fileSizeTv = itemView.findViewById(R.id.file_size_tv);
            dateTv = itemView.findViewById(R.id.date_tv);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoFile vf = videoFiles.get(getAdapterPosition());
                    String path = Constants.DOWNLOAD_DIR + context.getResources().getString(R.string.app_name) + File.separator + vf.getFileName();
                    String path2 = Constants.DOWNLOAD_DIR + context.getResources().getString(R.string.app_name) + File.separator +
                            DownloadService.DECRYPT_SIGN +vf.getFileName();

                    final File encFile = new File(path);
                    final File dFile = new File(path2);

                    String key = DownloadService.KEY;
                    final byte[] b = Base64.decode(key, Base64.DEFAULT);

                    final Encrypter encrypter = new Encrypter(context);

                    // visible progress layout
                    ((DownloadActivity)context).progressHideShowControl();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            encrypter.decryptVideo(b, encFile, dFile);
                        }
                    }).start();

                    /*VideoFile vf = videoFiles.get(getAdapterPosition());
                    String url ="/storage/emulated/0/Download/OXOO/d_e_John_Wick__Chapter_2.mp4";

                    Log.d("url:", url);


                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), "video/*");
                    context.startActivity(Intent.createChooser(intent, "Complete action using"));

                    Toast.makeText(context, "clickd", Toast.LENGTH_SHORT).show();*/
                }
            });
        }
    }
}
