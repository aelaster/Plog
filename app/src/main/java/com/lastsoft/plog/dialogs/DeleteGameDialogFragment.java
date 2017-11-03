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

public final class DeleteGameDialogFragment extends DialogFragment {

    public interface OnDialogButtonClickListener {
        void onPositiveClick_DeleteGame(long groupId, boolean deleteBGGFlag);
    }

    public static DeleteGameDialogFragment newInstance(long gameId) {
        DeleteGameDialogFragment frag = new DeleteGameDialogFragment();
        Bundle args = new Bundle();
        args.putLong("gameId", gameId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final long gameId = getArguments().getLong("gameId");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View inflator = inflater.inflate(R.layout.dialog_delete_game_bgg, null);
        builder.setView(inflator);
        builder.setTitle(R.string.delete);
        builder.setMessage(R.string.confirm_delete_game);
        builder.setPositiveButton(
                R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CheckBox deleteBox = (CheckBox) inflator.findViewById(R.id.deleteCheckbox);
                        OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getActivity();
                        listener.onPositiveClick_DeleteGame(gameId, deleteBox.isChecked());
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