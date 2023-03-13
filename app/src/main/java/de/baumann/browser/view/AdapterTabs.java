package de.baumann.browser.view;

import static de.baumann.browser.database.RecordAction.BOOKMARK_ITEM;
import static de.baumann.browser.database.RecordAction.STARTSITE_ITEM;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.LinkedList;
import java.util.List;

import de.baumann.browser.R;
import de.baumann.browser.activity.BrowserActivity;
import de.baumann.browser.browser.AlbumController;
import de.baumann.browser.browser.BrowserContainer;
import de.baumann.browser.browser.BrowserController;
import de.baumann.browser.database.FaviconHelper;
import de.baumann.browser.database.Record;
import de.baumann.browser.database.RecordAction;
import de.baumann.browser.unit.HelperUnit;
import de.baumann.browser.unit.RecordUnit;

class AdapterTabs {

    private final Context context;
    private final AlbumController albumController;

    private View albumView;
    private TextView albumTitle;
    private TextView albumUrl;
    private BrowserController browserController;
    private MaterialCardView albumCardView;

    AdapterTabs(Context context, AlbumController albumController, BrowserController browserController) {
        this.context = context;
        this.albumController = albumController;
        this.browserController = browserController;
        initUI();
    }

    View getAlbumView() {
        return albumView;
    }

    void setAlbumTitle(String title, String url) {
        albumTitle.setText(title);
        albumUrl.setText(url);
    }

    void setBrowserController(BrowserController browserController) {
        this.browserController = browserController;
    }

    @SuppressLint("InflateParams")
    private void initUI() {
        albumView = LayoutInflater.from(context).inflate(R.layout.item_list, null, false);
        albumCardView = albumView.findViewById(R.id.albumCardView);
        albumTitle = albumView.findViewById(R.id.titleView);
        albumUrl = albumView.findViewById(R.id.dateView);

        ImageView albumClose = albumView.findViewById(R.id.iconView);
        albumClose.setVisibility(View.VISIBLE);
        albumClose.setOnClickListener(view -> {
            browserController.removeAlbum(albumController);
            if (BrowserContainer.size() < 2) { browserController.hideOverview();}
        });

        albumCardView.setOnLongClickListener(v -> {

            String title = albumTitle.getText().toString();
            String url = albumUrl.getText().toString();

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            View dialogView = View.inflate(context, R.layout.dialog_menu, null);

            TextView menuTitle = dialogView.findViewById(R.id.menuTitle);
            menuTitle.setText(url);
            menuTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            menuTitle.setSingleLine(true);
            menuTitle.setMarqueeRepeatLimit(1);
            menuTitle.setSelected(true);
            menuTitle.setOnClickListener(v2 -> {
                menuTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                menuTitle.setSingleLine(true);
                menuTitle.setMarqueeRepeatLimit(1);
                menuTitle.setSelected(true);
            });

            FaviconHelper.setFavicon(context, dialogView, albumUrl.getText().toString(), R.id.menu_icon, R.drawable.icon_image_broken);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            dialog.show();
            HelperUnit.setupDialog(context, dialog);

            GridItem item_01 = new GridItem( context.getString(R.string.menu_share_link), 0);
            GridItem item_02 = new GridItem( context.getString(R.string.menu_closeTab), 0);

            final List<GridItem> gridList = new LinkedList<>();
            gridList.add(gridList.size(), item_01);
            gridList.add(gridList.size(), item_02);

            GridView menu_grid = dialogView.findViewById(R.id.menu_grid);
            GridAdapter gridAdapter = new GridAdapter(context, gridList);
            menu_grid.setAdapter(gridAdapter);
            gridAdapter.notifyDataSetChanged();
            menu_grid.setOnItemClickListener((parent, view, position, id) -> {
                dialog.cancel();
                switch (position) {
                    case 0:
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, url);
                        context.startActivity(Intent.createChooser(sharingIntent, (context.getString(R.string.menu_share_link))));
                        break;
                    case 1:
                        browserController.removeAlbum(albumController);
                        if (BrowserContainer.size() < 2) { browserController.hideOverview();}
                        break;
                }
            });
            return false;
        });
    }

    public void activate() {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorSecondaryContainer, typedValue, true);
        int color = typedValue.data;

        context.getTheme().resolveAttribute(R.attr.colorError, typedValue, true);
        int color2 = typedValue.data;

        albumCardView.setCardBackgroundColor(color);
        albumTitle.setTypeface(null, Typeface.BOLD);
        albumUrl.setTypeface(null, Typeface.BOLD);
        albumTitle.setTextColor(color2);
        albumUrl.setTextColor(color2);
        albumView.setOnClickListener(view -> {
            albumCardView.setCardBackgroundColor(color);
            browserController.hideOverview();
        });
    }

    void deactivate() {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorSurfaceVariant, typedValue, true);
        int color = typedValue.data;

        context.getTheme().resolveAttribute(R.attr.colorOnSurfaceVariant, typedValue, true);
        int color2 = typedValue.data;

        albumCardView.setCardBackgroundColor(color);
        albumTitle.setTypeface(null, Typeface.NORMAL);
        albumUrl.setTypeface(null, Typeface.NORMAL);
        albumTitle.setTextColor(color2);
        albumUrl.setTextColor(color2);
        albumView.setOnClickListener(view -> {
            browserController.showAlbum(albumController);
            browserController.hideOverview();
        });
    }
}