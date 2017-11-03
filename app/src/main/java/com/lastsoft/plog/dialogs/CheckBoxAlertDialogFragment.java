package com.lastsoft.plog.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.lastsoft.plog.R;
import com.lastsoft.plog.db.Game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CheckBoxAlertDialogFragment extends DialogFragment {

    static List<String> addedExpansions = new ArrayList<>();;

    public interface OnDialogButtonClickListener {
        void onPositiveClick_CheckBoxAlert(List<String> addedExpansions);
    }

    public CheckBoxAlertDialogFragment newInstance(boolean[] checkedItems, String gameName, String[] expansions) {
        CheckBoxAlertDialogFragment frag = new CheckBoxAlertDialogFragment();
        Bundle args = new Bundle();
        args.putBooleanArray("checkedItems", checkedItems);
        args.putStringArray("expansions", expansions);
        args.putString("gameName", gameName);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String gameName = getArguments().getString("gameName");
        final boolean[] checkedItems = getArguments().getBooleanArray("checkedItems");
        final List<String> expansions = new ArrayList<>(Arrays.asList(getArguments().getStringArray("expansions")));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.choose_expansions)
            // Specify the list array, the items to be selected by default (null for none),
            // and the listener through which to receive callbacks when items are selected
            .setMultiChoiceItems(expansions.toArray(new CharSequence[expansions.size()]), checkedItems,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which,
                                            boolean isChecked) {
                            checkedItems[which] = isChecked;
                            if (isChecked){
                                addedExpansions.add(expansions.get(which));
                            }else{
                                addedExpansions.remove(expansions.get(which));
                            }
                        }
                    })
            // Set the action buttons
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getTargetFragment();
                    listener.onPositiveClick_CheckBoxAlert(addedExpansions);
                    dialog.dismiss();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        return builder.create();
    }
}