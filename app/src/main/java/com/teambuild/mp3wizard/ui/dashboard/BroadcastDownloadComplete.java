//package com.teambuild.mp3wizard.ui.dashboard;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.media.MediaMetadataRetriever;
//import android.net.Uri;
//import android.provider.MediaStore;
//import android.util.Log;
//
//import static android.system.OsConstants.O_RDONLY;
//
//public class BroadcastDownloadComplete extends BroadcastReceiver {
//
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (intent.getAction().equals("android.intent.action.DOWNLOAD_COMPLETE")) {
//            //addSongToMediaStore(intent);
//        }
//    }
//
//    private void addFileToMediaStore(){
////        sp<MediaMetadataRetriever> mRetriever(new MediaMetadataRetriever);
////        int fd = open(path, O_RDONLY | O_LARGEFILE);
////        status_t status;
////        if (fd < 0) {
////            // couldn't open it locally, maybe the media server can?
////            status = mRetriever->setDataSource(path);
////        } else {
////            status = mRetriever->setDataSource(fd, 0, 0x7ffffffffffffffL);
////            close(fd);
////        }
////        if (status) {
////            return MEDIA_SCAN_RESULT_ERROR;
////        }
////        Uri uri = getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
////        Log.d("test", uri.toString());
//        Uri uri = getActivity().getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
//        Log.d(TAG, uri.toString());
//    }
//}
//
//
//import android.app.DownloadManager;
//        import android.content.ActivityNotFoundException;
//        import android.content.BroadcastReceiver;
//        import android.content.Context;
//        import android.content.Intent;
//        import android.content.IntentFilter;
//        import android.net.Uri;
//        import android.os.Environment;
//        import android.webkit.CookieManager;
//        import android.webkit.DownloadListener;
//        import android.widget.Toast;
//
//        import java.util.regex.Matcher;
//        import java.util.regex.Pattern;
//
//public class MyDownloadListener implements DownloadListener {
//    private Context mContext;
//    private DownloadManager mDownloadManager;
//    private long mDownloadedFileID;
//    private DownloadManager.Request mRequest;
//
//    public MyDownloadListener(Context context) {
//        mContext = context;
//        mDownloadManager = (DownloadManager) mContext
//                .getSystemService(Context.DOWNLOAD_SERVICE);
//    }
//
//    @Override
//    public void onDownloadStart(String url, String userAgent, String
//            contentDisposition, final String mimetype, long contentLength) {
//
//        // Function is called once download completes.
//        BroadcastReceiver onComplete = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                // Prevents the occasional unintentional call. I needed this.
//                if (mDownloadedFileID == -1)
//                    return;
//                Intent fileIntent = new Intent(Intent.ACTION_VIEW);
//
//                // Grabs the Uri for the file that was downloaded.
//                Uri mostRecentDownload =
//                        mDownloadManager.getUriForDownloadedFile(mDownloadedFileID);
//                // DownloadManager stores the Mime Type. Makes it really easy for us.
//                String mimeType =
//                        mDownloadManager.getMimeTypeForDownloadedFile(mDownloadedFileID);
//                fileIntent.setDataAndType(mostRecentDownload, mimeType);
//                fileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                try {
//                    mContext.startActivity(fileIntent);
//                } catch (ActivityNotFoundException e) {
//                    Toast.makeText(mContext, "No handler for this type of file.",
//                            Toast.LENGTH_LONG).show();
//                }
//                // Sets up the prevention of an unintentional call. I found it necessary. Maybe not for others.
//                mDownloadedFileID = -1;
//            }
//        };
//        // Registers function to listen to the completion of the download.
//        mContext.registerReceiver(onComplete, new
//                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//
//        mRequest = new DownloadManager.Request(Uri.parse(url));
//        // Limits the download to only over WiFi. Optional.
//        mRequest.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
//        // Makes download visible in notifications while downloading, but disappears after download completes. Optional.
//        mRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
//        mRequest.setMimeType(mimetype);
//
//        // If necessary for a security check. I needed it, but I don't think it's mandatory.
//        String cookie = CookieManager.getInstance().getCookie(url);
//        mRequest.addRequestHeader("Cookie", cookie);
//
//        // Grabs the file name from the Content-Disposition
//        String filename = null;
//        Pattern regex = Pattern.compile("(?<=filename=\").*?(?=\")");
//        Matcher regexMatcher = regex.matcher(contentDisposition);
//        if (regexMatcher.find()) {
//            filename = regexMatcher.group();
//        }
//
