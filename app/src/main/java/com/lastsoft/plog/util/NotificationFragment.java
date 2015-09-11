package com.lastsoft.plog.util;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;

import com.lastsoft.plog.R;



public class NotificationFragment extends DialogFragment {
    public NotificationFragment newInstance(int notificationId) {
        NotificationFragment frag = new NotificationFragment();
        Bundle args = new Bundle();
        args.putInt("notificationId", notificationId);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        final int notificationId = getArguments().getInt("notificationId");
            /*
            0 = didn't add a player to a play
            1 = database exported
            2 = cannot log into bgg - creds
             */

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        switch (notificationId) {
            case 0:
                builder.setTitle(R.string.error);
                builder.setMessage(R.string.notify_0_text)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dismiss();
                            }
                        });
                break;
            case 1:
                builder.setTitle(R.string.db_exported);
                builder.setMessage(getString(R.string.db_exported_1) + Environment.getExternalStorageDirectory().getAbsolutePath() + "/SRX_export.db" + getString(R.string.db_exported_2))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dismiss();
                            }
                        });
                break;
            case 2:
                builder.setTitle(R.string.error);
                builder.setMessage(getString(R.string.error_bgg_login))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dismiss();
                            }
                        });
                break;
        }
        return builder.create();
    }
}