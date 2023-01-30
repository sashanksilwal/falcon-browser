package de.baumann.browser.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import de.baumann.browser.R;
import de.baumann.browser.objects.CustomRedirect;
import de.baumann.browser.view.AdapterCustomRedirect;

public class CustomRedirectsDialog extends DialogFragment {
    AdapterCustomRedirect adapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View dialogView = View.inflate(getContext(), R.layout.custom_redirects_list, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.redirects_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdapterCustomRedirect(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        builder.setTitle(R.string.custom_redirects);
        builder.setNegativeButton(R.string.app_cancel, null);
        builder.setPositiveButton(R.string.app_ok, ((dialogInterface, i) -> {}));
        builder.setNeutralButton(R.string.create_new, ((dialogInterface, i) -> {

        }));
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dI -> {
            Button b = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            b.setOnClickListener(view -> {
                showCreateNewDialog();
            });
        });
        return dialog;
    }

    private void showCreateNewDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View dialogView = View.inflate(getContext(), R.layout.create_new_redirect, null);
        TextInputEditText source = dialogView.findViewById(R.id.source);
        TextInputEditText target = dialogView.findViewById(R.id.target);

        builder.setTitle(R.string.create_new);
        builder.setNegativeButton(R.string.app_cancel, null);
        builder.setPositiveButton(R.string.app_ok, ((dialogInterface, i) -> {
            adapter.addRedirect(new CustomRedirect(source.getText().toString(), target.getText().toString()));
        }));
        builder.setView(dialogView);

        builder.show();
    }
}
