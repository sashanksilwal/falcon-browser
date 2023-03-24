package de.baumann.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import de.baumann.browser.activity.BrowserActivity;

public class ShareActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        Intent browserIntent = new Intent(this, BrowserActivity.class);
        browserIntent.putExtra(android.content.Intent.EXTRA_TEXT, sharedText);
        browserIntent.setAction("postLink");
        startActivity(browserIntent);
        finish();
    }
}