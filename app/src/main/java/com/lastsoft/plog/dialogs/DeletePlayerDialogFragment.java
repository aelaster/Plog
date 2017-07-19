package com.lastsoft.plog.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.lastsoft.plog.R;

public final class DeletePlayerDialogFragment extends DialogFragment {

    public interface OnDialogButtonClickListener {
        void onPositiveClick_DeletePlayer(long playerId);
        void onNegativeClick_DeletePlayer();
    }

    public static DeletePlayerDialogFragment newInstance(long playerId) {
        DeletePlayerDialogFragment frag = new DeletePlayerDialogFragment();
        Bundle args = new Bundle();
        args.putLong("playerId", playerId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final long playerId = getArguments().getLong("playerId");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete);
        builder.setMessage(R.string.confirm_delete_player);
        builder.setPositiveButton(
                R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getTargetFragment();
                        listener.onPositiveClick_DeletePlayer(playerId);
                        dialog.dismiss();
                    }
                }
        );

        builder.setNegativeButton(
                R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getTargetFragment();
                        listener.onNegativeClick_DeletePlayer();
                        dialog.dismiss();
                    }
                }
        );
        return builder.create();
    }
}