package com.lastsoft.plog.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.lastsoft.plog.R;
import com.lastsoft.plog.db.Game;
import com.lastsoft.plog.db.GameGroup;
import com.lastsoft.plog.db.TenByTen;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class GameChooserDialogFragment extends DialogFragment {

    public interface OnDialogButtonClickListener {
        void onItemSelected_GameChooser(String gameName, String bggID, String bggCollectionID, boolean addToCollection, boolean deleteFromBGG);
        void onNegativeClick_GameChooser(long gameId);
    }

    ArrayList<GameGroup> addedGroups;
    ArrayList<CharSequence> theYears;

    public static GameChooserDialogFragment newInstance(ArrayList<String> theGames, ArrayList<String> theItems, ArrayList<String> theIDs, boolean addToCollection, long gameId) {
        GameChooserDialogFragment frag = new GameChooserDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("theGames", theGames);
        args.putStringArrayList("theItems", theItems);
        args.putStringArrayList("theIDs", theIDs);
        args.putBoolean("addToCollection", addToCollection);
        args.putLong("gameId", gameId);
        frag.setArguments(args);
        frag.setCancelable(false);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final long gameId = getArguments().getLong("gameId");
        final ArrayList<String> theIDs = getArguments().getStringArrayList("theIDs");
        final ArrayList<String> theGames = getArguments().getStringArrayList("theGames");
        final ArrayList<String> theItems = getArguments().getStringArrayList("theItems");
        final boolean addToCollection = getArguments().getBoolean("addToCollection");;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setCancelable(false);
        builder.setTitle(R.string.choose_version)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setItems(theItems.toArray(new CharSequence[theItems.size()]),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String BGGID = theIDs.get(i);
                                String gameName = theGames.get(i);
                                if (gameId >= 0) {
                                    Game updateMe = Game.findById(Game.class, gameId);
                                    updateMe.gameName = gameName;
                                    updateMe.save();
                                }
                                OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getActivity();
                                listener.onItemSelected_GameChooser(gameName, BGGID, "", addToCollection, false);
                                dialogInterface.dismiss();
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (gameId >= 0) {
                            OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getActivity();
                            listener.onNegativeClick_GameChooser(gameId);
                            dialog.dismiss();
                        }
                    }
                });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (gameId >= 0) {
                    OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getActivity();
                    listener.onNegativeClick_GameChooser(gameId);
                    dialogInterface.dismiss();
                }
            }
        });
        return builder.create();
    }
}