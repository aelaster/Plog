package com.lastsoft.plog;

import android.content.Context;

import com.lastsoft.plog.wizard.model.AbstractWizardModel;
import com.lastsoft.plog.wizard.model.CustomerInfoPage;
import com.lastsoft.plog.wizard.model.PageList;


/**
 * Created by TheFlash on 6/1/2015.
 */
public class SetupWizard_Model extends AbstractWizardModel {
        public SetupWizard_Model(Context context) {
                super(context);
        }

        @Override
        protected PageList onNewRootPageList() {
                return new PageList(
                        new CustomerInfoPage(this, "Enter your name.  You will be the first player entered.")
                                .setRequired(true), //on click we will import bgg stuff
                        new CustomerInfoPage(this, "Enter your BGG username.  Your game collection will be imported.")
                                .setRequired(true) //on click we will import bgg stuff
                );
        }
}
