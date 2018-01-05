package com.novasa.languagecenter.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.util.AttributeSet;

import com.novasa.languagecenter.LanguageCenter;
import com.novasa.languagecenter.R;

/**
 * Created by martinwagner on 20/12/2017.
 */

public class LanguageCenterTextView extends AppCompatTextView {

    public LanguageCenterTextView(Context context) {
        super(context);
    }

    public LanguageCenterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public LanguageCenterTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private boolean mHtml;

    private void init(AttributeSet attrs){
        if (attrs != null) {
            final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LanguageCenterTextView);

            mHtml = a.getBoolean(R.styleable.LanguageCenterTextView_html, false);

            // language center
            if (!isInEditMode() && null != LanguageCenter.getInstance()) {
                String transKey = a.getString(R.styleable.LanguageCenterTextView_transKey);
                String orgText = getText().toString();
                setText(LanguageCenter.getInstance().getTranslation(transKey, orgText));
            }

            a.recycle();
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (mHtml) {
            super.setText(Html.fromHtml(text.toString()), type);
        } else {
            super.setText(text, type);
        }
    }


}
