package com.lastsoft.plog;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class RightClickDialogFragment extends
        DialogFragment {
    private Activity callingActivity;
    private int callingFragment;
    private long callingID;

    public RightClickDialogFragment(int type, long id) {
        // Required empty public constructor
        callingFragment = type;
        callingID = id;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_task);
        if (callingFragment == 0){//PlaysFragment
            builder.setItems(R.array.play_rightclick, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {//edit
                        //call a function in the activity that passes the playID

                    } else if (which == 1) {//delete
                        //just do the delete here.
                        //TO-DO: add confirm
                    }
                }
            });
        }
        return builder.create();
    }


}
