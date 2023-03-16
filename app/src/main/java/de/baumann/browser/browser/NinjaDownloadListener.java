package de.baumann.browser.browser;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import de.baumann.browser.R;
import de.baumann.browser.unit.BackupUnit;
import de.baumann.browser.unit.HelperUnit;
import de.baumann.browser.view.GridAdapter;
import de.baumann.browser.view.GridItem;

public class NinjaDownloadListener implements DownloadListener {
    private final Context context;
    private final WebView webView;

    public NinjaDownloadListener(Context context, WebView webView) {
        super();
        this.context = context;
        this.webView = webView;

    }

    @Override
    public void onDownloadStart(final String url, String userAgent, final String contentDisposition, final String mimeType, long contentLength) {

        // Create a background thread that has a Looper
        HandlerThread handlerThread = new HandlerThread("HandlerThread");
        handlerThread.start();
        // Create a handler to execute tasks in the background thread.
        Handler backgroundHandler = new Handler(handlerThread.getLooper());
        Message msg = backgroundHandler.obtainMessage();
        webView.requestFocusNodeHref(msg);
        final String[] msgString = {(String) msg.getData().get("url")};

        String filename = URLUtil.guessFileName(msgString[0], null, null);
        String text = context.getString(R.string.dialog_title_download) + " - " + filename;

        GridItem item_01 = new GridItem(context.getString(R.string.app_ok), R.drawable.icon_check);
        GridItem item_02 = new GridItem( context.getString(R.string.menu_share_link), R.drawable.icon_link);
        GridItem item_03 = new GridItem( context.getString(R.string.menu_save_as), R.drawable.icon_save_as);
        GridItem item_04 = new GridItem( context.getString(R.string.app_cancel), R.drawable.icon_close);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(R.string.app_warning);
        builder.setMessage(text);
        builder.setIcon(R.drawable.icon_alert);

        View dialogView = View.inflate(context, R.layout.dialog_menu, null);
        builder.setView(dialogView);
        CardView cardView = dialogView.findViewById(R.id.albumCardView);
        cardView.setVisibility(View.GONE);
        AlertDialog dialog = builder.create();
        dialog.show();
        HelperUnit.setupDialog(context, dialog);

        Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);
        GridView menu_grid = dialogView.findViewById(R.id.menu_grid);
        final List<GridItem> gridList = new LinkedList<>();
        gridList.add(gridList.size(), item_01);
        gridList.add(gridList.size(), item_02);
        gridList.add(gridList.size(), item_03);
        gridList.add(gridList.size(), item_04);
        GridAdapter gridAdapter = new GridAdapter(context, gridList);
        menu_grid.setAdapter(gridAdapter);
        gridAdapter.notifyDataSetChanged();
        menu_grid.setOnItemClickListener((parent, view, position, id) -> {
            dialog.cancel();
            switch (position) {
                case 0:
                    try {
                        Activity activity = (Activity) context;
                        if (msgString[0] == null) {
                            msgString[0] = url;
                        }
                        assert msgString[0] != null;
                        if (msgString[0].startsWith("data:")) {
                            DataURIParser dataURIParser = new DataURIParser(msgString[0]);
                            if (BackupUnit.checkPermissionStorage(context)) {
                                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
                                FileOutputStream fos = new FileOutputStream(file);
                                fos.write(dataURIParser.getImagedata()); }
                            else BackupUnit.requestPermission(activity); }
                        else {
                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(msgString[0]));
                            request.setMimeType(mimeType);
                            //------------------------COOKIE!!------------------------
                            String cookies = CookieManager.getInstance().getCookie(msgString[0]);
                            request.addRequestHeader("cookie", cookies);
                            //------------------------COOKIE!!------------------------
                            request.setDescription(context.getString(R.string.dialog_title_download));
                            request.setTitle(filename);
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                            DownloadManager dm = (DownloadManager) activity.getSystemService(DOWNLOAD_SERVICE);
                            assert dm != null;
                            if (BackupUnit.checkPermissionStorage(context)) dm.enqueue(request);
                            else BackupUnit.requestPermission(activity); }}
                    catch (Exception e) {
                        System.out.println("Error Downloading File: " + e);
                        Toast.makeText(context, context.getString(R.string.app_error) + e.toString().substring(e.toString().indexOf(":")), Toast.LENGTH_LONG).show();
                        e.printStackTrace();}
                    break;
                case 1:
                    try {
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, msgString[0]);
                        context.startActivity(Intent.createChooser(sharingIntent, (context.getString(R.string.menu_share_link)))); }
                    catch (Exception e) {
                        System.out.println("Error Downloading File: " + e);
                        Toast.makeText(context, context.getString(R.string.app_error) + e.toString().substring(e.toString().indexOf(":")), Toast.LENGTH_LONG).show();
                        e.printStackTrace();}
                    break;
                case 2:
                    Activity activity = (Activity) context;
                    HelperUnit.saveAs(activity, msgString[0], filename, dialog);
                    break;
                case 3:
                    break;
            }
        });
    }
}
