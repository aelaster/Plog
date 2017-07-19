package com.lastsoft.plog.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.lastsoft.plog.R;
import com.lastsoft.plog.db.Location;

import java.util.ArrayList;
import java.util.List;

public final class LocationDialogFragment extends DialogFragment {

    int PLACE_PICKER_REQUEST = 2;

    public interface OnDialogButtonClickListener {
        void onItemClick_Location(String locationName);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            InputMethodManager inputManager = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
        }
    }

    Place thePlace;
    Location theLocation;
    View inflator = null;


    public LocationDialogFragment newInstance(int dialogType, Place thePlace, Location theLocation) {
        this.thePlace = thePlace;
        this.theLocation = theLocation;

        LocationDialogFragment frag = new LocationDialogFragment();
        Bundle args = new Bundle();
        args.putInt("dialogType", dialogType);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int dialogType = getArguments().getInt("dialogType");
             /*
             0 = confirm found place
             1 = list of saved locations, with add new on top
             2 = set place name
             3 = confirm found location
              */
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (dialogType == 0 || dialogType == 3){
            if (dialogType == 0) {
                builder.setTitle(getString(R.string.are_you_at) + thePlace.getName().toString() + "?");
            }else{
                builder.setTitle(getString(R.string.are_you_at) + theLocation.locationName + "?");
            }
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    //not there, offer them the location chooser
                    dialog.dismiss();
                    LocationDialogFragment newFragment = new LocationDialogFragment().newInstance(1, null, null);
                    newFragment.show(getActivity().getSupportFragmentManager(), "locationPicker");
                }
            })
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {

                }
            });
        }else if (dialogType == 1){
            List<Location> theLocations = Location.getAllLocations();
            final List<String> locations = new ArrayList<>();
            for (Location aLocation:theLocations){
                locations.add(aLocation.locationName);
            }
            locations.add(getString(R.string.add_new));
            locations.add(getString(R.string.none));
            builder.setTitle(R.string.choose_location)
                .setItems(locations.toArray(new CharSequence[locations.size()]), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getTargetFragment();

                        if (item == locations.size()-1) {
                            //setting blank
                            listener.onItemClick_Location(null);
                        }else if (item == locations.size()-2) {
                            //trying to add a new one
                            openPlacePicker();
                        } else {
                            //selected a location
                            //set the location to be output to the screen
                            listener.onItemClick_Location(locations.get(item));
                            //save the location in a variable, to be saved to the play
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        }else if (dialogType == 2){
            //we have to set a place name here
            LayoutInflater inflater = getActivity().getLayoutInflater();
            inflator = inflater.inflate(R.layout.dialog_location_name, null);
            builder.setView(inflator)
                    // Add action buttons
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {


                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                InputMethodManager inputManager = (InputMethodManager)
                                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                                        InputMethodManager.HIDE_NOT_ALWAYS);
                            } catch (Exception e) {
                            }
                            LocationDialogFragment.this.getDialog().dismiss();
                        }
                    });
            //then we will save it as the name for the location
            //then we will use it as the location for this play
        }

        final AlertDialog ad = builder.create();
            ad.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                final ListView lv = ad.getListView(); //this is a ListView with your "buds" in it
                if (lv != null) {
                    lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            String selectedFromList = (String) (lv.getItemAtPosition(position));
                            if (!selectedFromList.equals(getString(R.string.add_new)) && !selectedFromList.equals(getString(R.string.add_new))) {
                                Location useMe = Location.findLocationByName(selectedFromList);
                                useMe.delete();
                            }
                            LocationDialogFragment.this.getDialog().dismiss();
                            return true;
                        }
                    });
                }
            }
        });
        return ad;
    }

    @Override
    public void onStart()
    {
        final int dialogType = getArguments().getInt("dialogType");
        super.onStart();    //super.onStart() is where dialog.show() is actually called on the underlying dialog, so we have to do it after this point
        AlertDialog d = (AlertDialog)getDialog();
        if(d != null)
        {
            Button positiveButton = (Button) d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    OnDialogButtonClickListener listener = (OnDialogButtonClickListener) getTargetFragment();
                    Boolean wantToCloseDialog = false;

                    if (dialogType == 0 || dialogType == 3){
                        //set this
                        dismiss();
                        Location useMe;
                        if (dialogType == 0) {
                            useMe = Location.findLocationByName(thePlace.getName().toString());
                        }else{
                            useMe = theLocation;
                        }
                        listener.onItemClick_Location(useMe.locationName);
                    }else if (dialogType == 2){
                        EditText edit = (EditText) inflator.findViewById(R.id.locationName);
                        String text = edit.getText().toString();

                        //check to see if this name already exists
                        Location checker = Location.findLocationByName(text);
                        if (checker != null) {
                            //this name already exists.  toast the user and let them try it again
                            Toast.makeText(getActivity(), getString(R.string.location_name_exists), Toast.LENGTH_LONG).show();
                            edit.selectAll();
                        }else{
                            try {
                                InputMethodManager inputManager = (InputMethodManager)
                                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                                        InputMethodManager.HIDE_NOT_ALWAYS);
                            } catch (Exception e) {
                            }
                           dismiss();
                            //then we will save it as the name for the new location
                            Location addMe = new Location(text, thePlace.getAddress().toString(), thePlace.getId(), thePlace.getLatLng().latitude, thePlace.getLatLng().longitude);
                            addMe.save();
                            //then we will use it as the location for this play
                            listener.onItemClick_Location(addMe.locationName);
                            //set the location to be output to the screen
                        }


                    }

                    if(wantToCloseDialog)
                        dismiss();
                }
            });
        }
    }

    public void openPlacePicker(){
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
        }catch (Exception ignored){

        }
    }
}