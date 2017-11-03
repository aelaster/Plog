package com.lastsoft.plog.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.lastsoft.plog.R;

public final class DeleteGroupDialogFragment extends DialogFragment {

    public interface OnDialogButtonClickListener {
        void onPositiveClick(long groupId);
        void onNegativeClick();
    }

    public static DeleteGroupDialogFragment newInstance(long groupId) {
        DeleteGroupDialogFragment frag = new DeleteGroupDialogFragment();
        Bundle args = new Bundle();
        args.putLong("groupId", groupId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final long groupId = getArguments().getLong("groupId");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete);
        builder.setMessage(R.string.confirm_delete_group);
        builder.setPositiveButton(
                R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getActivity();
                        listener.onPositiveClick(groupId);
                        dialog.dismiss();
                    }
                }
        );

        builder.setNegativeButton(
                R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getActivity();
                        listener.onNegativeClick();
                        dialog.dismiss();
                    }
                }
        );
        return builder.create();
    }
}