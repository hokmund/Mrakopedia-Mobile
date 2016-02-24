package com.randomname.mrakopedia.ui.settings.Feedback;

import com.randomname.mrakopedia.ui.RxBaseFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.randomname.mrakopedia.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import codetail.graphics.drawables.LollipopDrawablesCompat;

/**
 * Created by vgrigoryev on 24.02.2016.
 */
public class FeedbackFragment extends RxBaseFragment {
    private static final String TAG = FeedbackFragment.class.getSimpleName();

    @Bind(R.id.editTxtYourNameFeedbackFragment)
    EditText editTxtYourNameFeedbackFragment;
    @Bind(R.id.editTxtYourMessageFeedbackFragment)
    EditText editTxtYourMessageFeedbackFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.feedback_fragment, null);
        ButterKnife.bind(this, view);

        return view;
    }

    @OnClick(R.id.btnSendMessageFeedbackFragment)
    public void sendMessage() {
        if (editTxtYourMessageFeedbackFragment.getText().toString().length() <= 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.feedback_message_must_be_not_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        sendFeedback();
    }

    private void sendFeedback() {
        String name = editTxtYourNameFeedbackFragment.getText().toString();
        String message = editTxtYourMessageFeedbackFragment.getText().toString();

        try {
            startActivity(createEmailIntent(
                    "slayerru@yandex.ru",
                    "Отзыв на приложение Мракопедия. Отправитель: " + name,
                    message + "\n\n\n" + getSystemInfo(),
                    getString(R.string.feedback_message_is_sending)
            ));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), getResources().getString(R.string.email_client_not_installed), Toast.LENGTH_SHORT).show();
        }


    }


    private Intent createEmailIntent(final String toEmail,
                                     final String subject,
                                     final String message,
                                     final String chooserTitle
    )
    {
        Intent sendTo = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode(toEmail) +
                "?subject=" + Uri.encode(subject) +
                "&body=" + Uri.encode(message);
        Uri uri = Uri.parse(uriText);
        sendTo.setData(uri);

        List<ResolveInfo> resolveInfos =
                getActivity().getPackageManager().queryIntentActivities(sendTo, 0);

        // Emulators may not like this check...
        if (!resolveInfos.isEmpty())
        {
            return sendTo;
        }

        // Nothing resolves send to, so fallback to send...
        Intent send = new Intent(Intent.ACTION_SEND);

        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_EMAIL,
                new String[] { toEmail });
        send.putExtra(Intent.EXTRA_SUBJECT, subject);
        send.putExtra(Intent.EXTRA_TEXT, message);

        return Intent.createChooser(send, chooserTitle);
    }


    private String getSystemInfo() {
        String sdkVersion =  "Android SDK: " + Build.VERSION.SDK_INT + " (" + Build.VERSION.RELEASE + ")\n";

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        String screenSize = "Screen resolution: " + metrics.heightPixels + "x" + metrics.widthPixels + "\n";

        String appVersion = "App version: ";
        try {
            appVersion += getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName + "\n";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            appVersion += "@null\n";
        }

        return sdkVersion + screenSize + appVersion;
    }

    @Override
    public void onConnectedToInternet() {

    }
}
