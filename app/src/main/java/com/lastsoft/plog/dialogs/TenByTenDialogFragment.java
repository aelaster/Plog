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

public final class TenByTenDialogFragment extends DialogFragment {

    ArrayList<GameGroup> addedGroups;
    ArrayList<CharSequence> theYears;

    public static TenByTenDialogFragment newInstance(long gameId, int year) {
        TenByTenDialogFragment frag = new TenByTenDialogFragment();
        Bundle args = new Bundle();
        args.putLong("gameId", gameId);
        args.putInt("year", year);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final long gameId = getArguments().getLong("gameId");
        final int selectedYear = getArguments().getInt("year");

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (selectedYear == -1){
            theYears = new ArrayList<CharSequence>();

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            for (int i = year+1; i >= 2015; i--){
                theYears.add(""+i);
            }

            builder.setTitle(R.string.choose_year)
                    .setItems(theYears.toArray(new CharSequence[theYears.size()]), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            dialog.dismiss();
                            TenByTenDialogFragment newFragment = new TenByTenDialogFragment().newInstance(gameId, Integer.parseInt(theYears.get(item).toString()));
                            newFragment.show(getFragmentManager(), "tenByTenPicker");
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });
        }else{
            addedGroups = new ArrayList<>();
            List<String> gameGroupNames = new ArrayList<>();
            final Game theGame = Game.findById(Game.class, gameId);
            final List<GameGroup> gameGroups = GameGroup.listAll(GameGroup.class);

            boolean checkedItems[] = new boolean[gameGroups.size()];

            int i = 0;
            for (GameGroup group : gameGroups) {
                if (TenByTen.isGroupAdded(group, theGame, selectedYear)) {//if this group has this one checked, it's always okay to add to the dialog
                    gameGroupNames.add(group.groupName);
                    checkedItems[i] = true;
                } else {
                    List<TenByTen> tens = TenByTen.tenByTens_Group(group, selectedYear);
                    if (tens.size() < 10) {//if this group doesn't have 10 selected, we can add it.  this stops adding more than 10
                        gameGroupNames.add(group.groupName);
                    }
                }
                i++;
            }

            builder.setTitle(R.string.choose_groups)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(gameGroupNames.toArray(new CharSequence[gameGroupNames.size()]), checkedItems,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                //checkedItems[which] = isChecked;
                                GameGroup checked = gameGroups.get(which);
                                if (isChecked) {
                                    addedGroups.add(checked);
                                } else {
                                    addedGroups.remove(checked);
                                }
                            }
                        });
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    TenByTen.deleteTenByTen(gameId, selectedYear);

                    for (int i = 0; i < addedGroups.size(); i++) {
                        TenByTen addMe = new TenByTen(theGame, addedGroups.get(i), selectedYear);
                        addMe.save();
                    }
                }
            });
        }
        return builder.create();
    }
}