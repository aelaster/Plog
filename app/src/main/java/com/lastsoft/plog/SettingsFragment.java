/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.lastsoft.plog;

import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.lastsoft.plog.util.NotificationFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class SettingsFragment extends PreferenceFragment {

    private Preference mImportPreference, mExportPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mExportPreference = getPreferenceManager().findPreference("export_db");
        mExportPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                exportDB();
                return false;
            }
        });
        mImportPreference =  getPreferenceManager().findPreference("import_db");
        String backupDBPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SRX_export.db";
        File backupDB = new File(backupDBPath);
        if (backupDB.exists()) {
            mImportPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    importDB();
                    return false;
                }
            });
        }else{
            mImportPreference.setEnabled(false);
        }
    }

    public void importDB(){
        try{
            File oldDb = getActivity().getDatabasePath("SRX.db");
            File newDb = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SRX_export.db");
            if (newDb.exists()) {
                if(oldDb.exists()){

                }
                else{
                    //This'll create the directories you wanna write to, so you
                    //can put the DB in the right spot.
                    oldDb.getParentFile().mkdirs();
                }
                Log.d("V1", "importing database");
                FileInputStream src_input = new FileInputStream(newDb);
                FileOutputStream dst_input = new FileOutputStream(oldDb);
                FileChannel src = src_input.getChannel();
                FileChannel dst = dst_input.getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                src_input.close();
                dst_input.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportDB(){
        try{
            File currentDB = getActivity().getDatabasePath("SRX.db");
            String backupDBPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SRX_export.db";
            File backupDB = new File(backupDBPath);

            if (currentDB.exists()) {
                FileInputStream src_input = new FileInputStream(currentDB);
                FileOutputStream dst_input = new FileOutputStream(backupDB);
                FileChannel src = src_input.getChannel();
                FileChannel dst = dst_input.getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                src_input.close();
                dst_input.close();
                mImportPreference.setEnabled(true);
            }
            notifyUser(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void notifyUser(int notificationId){
        NotificationFragment newFragment = new NotificationFragment().newInstance(notificationId);
        newFragment.show(getFragmentManager(), "notifyUser");
    }
}
