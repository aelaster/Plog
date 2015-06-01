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
                        new CustomerInfoPage(this, "Enter your name and your default color.  You will be the first player entered.  You can enter more by visiting \"Players\" in the swipe menu.")
                                .setRequired(true), //on click we will import bgg stuff
                        new CustomerInfoPage(this, "Enter your BGG username.  Your game collection will be synced with BGG and you'll be able to log any of your plays.")
                                .setRequired(true) //on click we will import bgg stuff
                );
        }
}
