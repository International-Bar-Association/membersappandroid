package com.ibamembers.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.ibamembers.R;

public class IntentManager {

    public void contactIba(@NonNull Context context) {
        Resources resources = context.getResources();
        String emailAddress = resources.getString(R.string.login_contact_iba_email_address);
        String emailSubject = resources.getString(R.string.login_contact_iba_email_subject);
        String emailChooserTitle = resources.getString(R.string.login_contact_iba_email_chooser_title);

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + emailAddress));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        context.startActivity(Intent.createChooser(emailIntent, emailChooserTitle));
    }
}
