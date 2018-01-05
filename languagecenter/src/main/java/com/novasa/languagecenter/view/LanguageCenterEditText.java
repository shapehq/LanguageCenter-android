package com.novasa.languagecenter.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.novasa.languagecenter.LanguageCenter;
import com.novasa.languagecenter.R;

/**
 * Created by martinwagner on 20/12/2017.
 */

public class LanguageCenterEditText extends AppCompatEditText {

    public LanguageCenterEditText(Context context) {
        super(context);
    }

    public LanguageCenterEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LanguageCenterEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LanguageCenterEditText);

            // language center
            if (!isInEditMode() && null != LanguageCenter.getInstance()) {
                String hintTransKey = a.getString(R.styleable.LanguageCenterEditText_hintTransKey);
                String orgText = getHint() != null ? getHint().toString() : null;

                if (!(orgText == null || orgText.isEmpty()) && !(hintTransKey == null || hintTransKey.isEmpty())) {
                    setHint(LanguageCenter.getInstance().getTranslation(hintTransKey, orgText));
                }
            }

            a.recycle();
        }
    }
}
