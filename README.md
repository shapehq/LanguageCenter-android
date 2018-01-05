# Language Center

<h1>Introduction</h1>
Language center is an easy method for translating static strings in our apps.<br>
Simply add the module to your project


<h1>init</h1> 
Init framework with:

    LanguageCenter.with(Context context);

& these string resources needed by the framework to function:

    <string name="language_center_user_name">vigo</string>
    <string name="language_center_password">5Xvz66JsKH</string>
    <string name="language_center_base_url">https://language.novasa.com/vigo/api/v1/</string>

<b>OR SIMPLY USE:</b>

    LanguageCenter.with(Context context, String appName, String baseUrl, String userName, String password)
    
    

<h1>Define your strings</h1>
In your strings.xml define two strings for every text:

    <string name="sign_up_phone_title">Let\'s get started</string>
    <string name="sign_up_phone_title_key">sign_up.phone_title</string>

The first string is the default-text while the second string is the key used in the Language Center database.

<h1>Getting the translations</h1>
To get a translation you simply use the following code:

    LanguageCenter.getInstance().getTranslation(R.string.sign_up_phone_title_key, R.string.sign_up_phone_title)

Or you can use the Language Center TextView, EditText and Button.<br>
Example:

    <com.novasa.languagecenter.view.LanguageCenterTextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/sign_up_phone_title"
        app:transKey="@string/sign_up_phone_title_key"/>



