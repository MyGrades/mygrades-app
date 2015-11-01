# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/tilman/dev/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# greendao rules
-keepclassmembers class * extends de.greenrobot.dao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties

# secure preferences rules
-keep class com.tozny.crypto.android.AesCbcWithIntegrity$PrngFixes$* { *; }

# MPAndroidChart rules
-keep class com.github.mikephil.charting.** { *; }

# retrofit rules
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# eventbus rules
-keepclassmembers class ** {
    public void onEvent*(**);
}
# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# jsoup
-keeppackagenames org.jsoup.nodes

-dontwarn freemarker.**

-keep public class * extends android.support.v7.app.AppCompatActivity