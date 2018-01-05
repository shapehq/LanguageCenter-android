package com.novasa.languagecenter.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import com.novasa.languagecenter.LanguageCenter;
import com.novasa.languagecenter.R;

/**
 * Created by martinwagner on 05/01/2018.
 */

public class LanguageCenterButton extends AppCompatButton {

    public LanguageCenterButton(Context context) {
        super(context);
    }

    public LanguageCenterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LanguageCenterButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LanguageCenterButton);

            // language center
            if (!isInEditMode() && null != LanguageCenter.getInstance()) {
                String transKey = a.getString(R.styleable.LanguageCenterButton_transKey);
                String orgText = getText().toString();
                setText(LanguageCenter.getInstance().getTranslation(transKey, orgText));
            }

            a.recycle();
        }
    }

}