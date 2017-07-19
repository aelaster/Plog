package com.lastsoft.plog.dialogs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

import com.lastsoft.plog.AddPlayFragment;
import com.lastsoft.plog.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        String gameDate;
        if (String.valueOf(month+1).length()==1) {
            gameDate = year + "-0" + (month + 1) + "-" + day;
        }else{
            gameDate = year + "-" + (month + 1) + "-" + day;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = dateFormat.parse(gameDate);
            DateFormat outputFormatter = new SimpleDateFormat("MM/dd/yyyy");
            String output_date = outputFormatter.format(date1); // Output : 01/20/2012
            AddPlayFragment listener = (AddPlayFragment) getFragmentManager().findFragmentByTag("add_play");
            if (listener != null) {
                listener.setDate(gameDate, output_date);
            }
        }catch (ParseException ignored) {}
    }

    public static DatePickerDialogFragment newInstance(String gameDate) {
        DatePickerDialogFragment frag = new DatePickerDialogFragment();
        Bundle args = new Bundle();
        args.putString("gameDate", gameDate);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker

        final String gameDate = getArguments().getString("gameDate");
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date1 = dateFormat.parse(gameDate);

            final Calendar c = Calendar.getInstance();
            c.setTime(date1);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;

    }
}