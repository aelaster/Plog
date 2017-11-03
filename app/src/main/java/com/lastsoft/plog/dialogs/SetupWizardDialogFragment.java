package com.lastsoft.plog.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.lastsoft.plog.R;

public final class SetupWizardDialogFragment extends DialogFragment {

    public interface OnDialogButtonClickListener {
        void onPositiveClick_SetupWizard();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.submit_confirm_message);
        builder.setPositiveButton(
                R.string.submit_confirm_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getTargetFragment();
                        listener.onPositiveClick_SetupWizard();
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