package de.falcon.browser.unit;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.core.app.NotificationCompat.DEFAULT_SOUND;
import static androidx.core.app.NotificationCompat.DEFAULT_VIBRATE;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONException;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import de.baumann.browser.R;
import de.falcon.browser.activity.BrowserActivity;
import de.falcon.browser.database.RecordAction;
import de.falcon.browser.objects.CustomRedirect;
import de.falcon.browser.objects.CustomRedirectsHelper;

public class BrowserUnit {

    public static final int PROGRESS_MAX = 100;
    public static final int LOADING_STOPPED = 101;  //Must be > PROGRESS_MAX !
    public static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
    public static final String URL_ENCODING = "UTF-8";
    public static final String URL_SCHEME_ABOUT = "about:";
    public static final String URL_SCHEME_MAIL_TO = "mailto:";
    private static final String SEARCH_ENGINE_GOOGLE = "https://www.google.com/search?q=";
    private static final String SEARCH_ENGINE_DUCKDUCKGO = "https://duckduckgo.com/?q=";
    private static final String SEARCH_ENGINE_STARTPAGE = "https://startpage.com/do/search?query=";
    private static final String SEARCH_ENGINE_BING = "https://www.bing.com/search?q=";
    private static final String SEARCH_ENGINE_BAIDU = "https://www.baidu.com/s?wd=";
    private static final String SEARCH_ENGINE_QWANT = "https://www.qwant.com/?q=";
    private static final String SEARCH_ENGINE_ECOSIA = "https://www.ecosia.org/search?q=";
    private static final String SEARCH_ENGINE_Metager = "https://metager.org/meta/meta.ger3?eingabe=";
    private static final String SEARCH_ENGINE_STARTPAGE_DE = "https://startpage.com/do/search?lui=deu&language=deutsch&query=";
    private static final String SEARCH_ENGINE_SEARX = "https://searx.be/?q=";
    private static final String URL_ABOUT_BLANK = "about:blank";
    private static final String URL_SCHEME_FILE = "file://";
    private static final String URL_SCHEME_HTTPS = "https://";
    private static final String URL_SCHEME_HTTP = "http://";
    private static final String URL_SCHEME_FTP = "ftp://";
    private static final String URL_SCHEME_INTENT = "intent://";

    public static boolean isURL(String url) {


        url = url.toLowerCase(Locale.getDefault());

        if (url.startsWith(URL_ABOUT_BLANK)
                || url.startsWith(URL_SCHEME_MAIL_TO)
                || url.startsWith(URL_SCHEME_FILE)
                || url.startsWith(URL_SCHEME_HTTP)
                || url.startsWith(URL_SCHEME_HTTPS)
                || url.startsWith(URL_SCHEME_FTP)
                || url.startsWith(URL_SCHEME_INTENT)) {
            return true;
        }

        String regex = "^((ftp|http|https|intent)?://)"                      // support scheme
                + "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" // ftp的user@
                + "(([0-9]{1,3}\\.){3}[0-9]{1,3}"                            // IP形式的URL -> 199.194.52.184
                + "|"                                                        // 允许IP和DOMAIN（域名）
                + "([0-9a-z_!~*'()-]+\\.)*"                                  // 域名 -> www.
                + "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\."                    // 二级域名
                + "[a-z]{2,6})"                                              // first level domain -> .com or .museum
                + "(:[0-9]{1,4})?"                                           // 端口 -> :80
                + "((/?)|"                                                   // a slash isn't required if there is no file name
                + "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(url).matches();
    }

    public static String queryWrapper(Context context, String query) {

        if (isURL(query) || query.equals("")) {
            if (query.startsWith(URL_SCHEME_ABOUT) || query.startsWith(URL_SCHEME_MAIL_TO)) {
                return query;
            }

            if (!query.contains("://")) {
                query = URL_SCHEME_HTTPS + query;
            }
            return query;
        } else {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            String customSearchEngine = sp.getString("sp_search_engine_custom", "");

            query = query.replace("&", "%26");

            //Override UserAgent if own UserAgent is defined
            if (!sp.contains("searchEngineSwitch")) {
                //if new switch_text_preference has never been used initialize the switch
                if (customSearchEngine.equals("")) {
                    sp.edit().putBoolean("searchEngineSwitch", false).apply();
                } else {
                    sp.edit().putBoolean("searchEngineSwitch", true).apply();
                }
            }

            if (sp.getBoolean("searchEngineSwitch", false)) {
                //if new switch_text_preference has never been used initialize the switch
                return customSearchEngine + query;
            } else {
                final int i = Integer.parseInt(Objects.requireNonNull(sp.getString("sp_search_engine", "0")));
                switch (i) {
                    case 1:
                        return SEARCH_ENGINE_STARTPAGE_DE + query;
                    case 2:
                        return SEARCH_ENGINE_BAIDU + query;
                    case 3:
                        return SEARCH_ENGINE_BING + query;
                    case 4:
                        return SEARCH_ENGINE_DUCKDUCKGO + query;
                    case 5:
                        return SEARCH_ENGINE_GOOGLE + query;
                    case 6:
                        return SEARCH_ENGINE_SEARX + query;
                    case 7:
                        return SEARCH_ENGINE_QWANT + query;
                    case 8:
                        return SEARCH_ENGINE_ECOSIA + query;
                    case 9:
                        return SEARCH_ENGINE_Metager + query;
                    default:
                        return SEARCH_ENGINE_STARTPAGE + query;
                }
            }
        }
    }

    public static void clearHome(Context context) {
        RecordAction action = new RecordAction(context);
        action.open(true);
        action.clearTable(RecordUnit.TABLE_START);
        action.close();
    }

    public static void clearCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) deleteDir(dir); }
        catch (Exception exception) {
            Log.w("browser", "Error clearing cache"); }
    }

    public static void clearCookie() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.flush();
        cookieManager.removeAllCookies(value -> {
        });
    }

    public static void clearBookmark(Context context) {
        RecordAction action = new RecordAction(context);
        action.open(true);
        action.clearTable(RecordUnit.TABLE_BOOKMARK);
        action.close();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            Objects.requireNonNull(shortcutManager).removeAllDynamicShortcuts(); }
    }

    public static void clearHistory(Context context) {
        RecordAction action = new RecordAction(context);
        action.open(true);
        action.clearTable(RecordUnit.TABLE_HISTORY);
        action.close();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);
            Objects.requireNonNull(shortcutManager).removeAllDynamicShortcuts(); }
    }

    public static void intentURL(Context context, Uri uri) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(uri);
        browserIntent.setPackage("de.baumann.browser");
        Intent chooser = Intent.createChooser(browserIntent, context.getString(R.string.menu_open_with));
        context.startActivity(chooser);
    }

    public static String redirectURL (WebView ninjaWebView, SharedPreferences sp, String url) {

        String domain = HelperUnit.domain(url);
        boolean redirect = sp.getBoolean("redirect", false);
        if (!redirect) return url;

        try {
            List<CustomRedirect> redirects = CustomRedirectsHelper.getRedirects(sp);

            for (int i = 0; i < redirects.size(); i++) {
                CustomRedirect customRedirect = redirects.get(i);
                if (domain.contains(customRedirect.getSource())) {
                    ninjaWebView.stopLoading();
                    url = url.replace(customRedirect.getSource(), customRedirect.getTarget());
                    return url;
                }
            }
        } catch (JSONException e) {
            Log.e("Redirect error", e.toString());
        }

        if (sp.getBoolean("sp_youTube_switch", false) &&
                domain.equals("youtube.com") || domain.equals("m.youtube.com")) {
            ninjaWebView.stopLoading();
            String substring = url.substring(url.indexOf("youtube.com") + 12);
            url = sp.getString("sp_youTube_string", "https://yewtu.be/") + substring;
            return url;
        }

        else if (sp.getBoolean("sp_twitter_switch", false) &&
                domain.equals("twitter.com") || domain.equals("m.twitter.com")) {
            ninjaWebView.stopLoading();
            String substring = url.substring(url.indexOf("twitter.com") + 12);
            url = sp.getString("sp_twitter_string", "https://nitter.net/") + substring;
            return url;
        }

        else if (sp.getBoolean("sp_instagram_switch", false) && (domain.equals("instagram.com"))) {
            ninjaWebView.stopLoading();
            String substring = url.substring(url.indexOf("instagram.com") + 14);
            url = sp.getString("sp_instagram_string", "https://bibliogram.pussthecat.org/") + substring;
            return url;
        }
        return url;
    }

    public static void openInBackground(Activity activity, Intent intent, String url) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        if (sp.getBoolean("sp_tabBackground", false) && !Objects.equals(intent.getPackage(), "de.baumann.browser")) {

            Intent intentP = new Intent(activity, BrowserActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intentP, FLAG_IMMUTABLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String name = "Opened Link";
                String description = "url of links opened in the background -> click to open";
                int importance = NotificationManager.IMPORTANCE_HIGH; //Important for heads-up notification
                NotificationChannel channel = new NotificationChannel("1", name, importance);
                channel.setDescription(description);
                channel.setShowBadge(true);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel); }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(activity, "1")
                    .setSmallIcon(R.drawable.icon_web)
                    .setContentTitle(activity.getString(R.string.main_menu_new_tab))
                    .setContentText(url)
                    .setAutoCancel(true)
                    .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE) //Important for heads-up notification
                    .setPriority(Notification.PRIORITY_MAX) //Important for heads-up notification
                    .setContentIntent(pendingIntent); //Set the intent that will fire when the user taps the notification

            Notification buildNotification = mBuilder.build();
            NotificationManager mNotifyMgr = (NotificationManager) activity.getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(1, buildNotification);

            activity.moveTaskToBack(true);
        }
    }

    public static void clearIndexedDB(Context context) {
        File data = Environment.getDataDirectory();

        String blob_storage = "//data//" + context.getPackageName() + "//app_webview//" + "//Default//" + "//blob_storage";
        String databases = "//data//" + context.getPackageName() + "//app_webview//" + "//Default//" + "//databases";
        String indexedDB = "//data//" + context.getPackageName() + "//app_webview//" + "//Default//" + "//IndexedDB";
        String localStorage = "//data//" + context.getPackageName() + "//app_webview//" + "//Default//" + "//Local Storage";
        String serviceWorker = "//data//" + context.getPackageName() + "//app_webview//" + "//Default//" + "//Service Worker";
        String sessionStorage = "//data//" + context.getPackageName() + "//app_webview//" + "//Default//" + "//Session Storage";
        String shared_proto_db = "//data//" + context.getPackageName() + "//app_webview//" + "//Default//" + "//shared_proto_db";
        String VideoDecodeStats = "//data//" + context.getPackageName() + "//app_webview//" + "//Default//" + "//VideoDecodeStats";
        String QuotaManager = "//data//" + context.getPackageName() + "//app_webview//" + "//Default//" + "//QuotaManager";
        String QuotaManager_journal = "//data//" + context.getPackageName() + "//app_webview//" + "//Default//" + "//QuotaManager-journal";
        String webData = "//data//" + context.getPackageName() + "//app_webview//" + "//Default//" + "//Web Data";
        String WebDataJournal = "//data//" + context.getPackageName() + "//app_webview//" + "//Default//" + "//Web Data-journal";

        final File blob_storage_file = new File(data, blob_storage);
        final File databases_file = new File(data, databases);
        final File indexedDB_file = new File(data, indexedDB);
        final File localStorage_file = new File(data, localStorage);
        final File serviceWorker_file = new File(data, serviceWorker);
        final File sessionStorage_file = new File(data, sessionStorage);
        final File shared_proto_db_file = new File(data, shared_proto_db);
        final File VideoDecodeStats_file = new File(data, VideoDecodeStats);
        final File QuotaManager_file = new File(data, QuotaManager);
        final File QuotaManager_journal_file = new File(data, QuotaManager_journal);
        final File webData_file = new File(data, webData);
        final File WebDataJournal_file = new File(data, WebDataJournal);

        BrowserUnit.deleteDir(blob_storage_file);
        BrowserUnit.deleteDir(databases_file);
        BrowserUnit.deleteDir(indexedDB_file);
        BrowserUnit.deleteDir(localStorage_file);
        BrowserUnit.deleteDir(serviceWorker_file);
        BrowserUnit.deleteDir(sessionStorage_file);
        BrowserUnit.deleteDir(shared_proto_db_file);
        BrowserUnit.deleteDir(VideoDecodeStats_file);
        BrowserUnit.deleteDir(QuotaManager_file);
        BrowserUnit.deleteDir(QuotaManager_journal_file);
        BrowserUnit.deleteDir(webData_file);
        BrowserUnit.deleteDir(WebDataJournal_file);
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : Objects.requireNonNull(children)) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }
        return dir != null && dir.delete();
    }
}