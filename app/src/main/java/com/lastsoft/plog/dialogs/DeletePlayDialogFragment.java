package com.lastsoft.plog.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.lastsoft.plog.R;

public final class DeletePlayDialogFragment extends DialogFragment {

    public interface OnDialogButtonClickListener {
        void onPositiveClick_DeletePlay(long playId);
    }

    public static DeletePlayDialogFragment newInstance(long playId) {
        DeletePlayDialogFragment frag = new DeletePlayDialogFragment();
        Bundle args = new Bundle();
        args.putLong("playId", playId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final long playId = getArguments().getLong("playId");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete);
        builder.setMessage(R.string.confirm_delete_play);
        builder.setPositiveButton(
                R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getTargetFragment();
                        listener.onPositiveClick_DeletePlay(playId);
                        dialog.dismiss();
                    }
                }
        );

        builder.setNegativeButton(
                R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );
        return builder.create();
    }
}