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

# MPAndroidChart rules
-keep class com.github.mikephil.charting.** { *; }
-dontwarn io.realm.**

# wnafee/vector-compat rules
-keep class com.wnafee.vector.** { *; }

# retrofit rules
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keep class de.mygrades.database.dao.** { *; }
-keep class de.mygrades.main.processor.ErrorProcessor$Error { *; }
-keep class de.mygrades.main.processor.WishProcessor$Wish { *; }
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}

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

# remove freemarker log warnings
-dontwarn freemarker.**

# remove logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# StrongHttpsClient and its support classes are totally unused, so ignore warnings
-dontwarn info.guardianproject.netcipher.**

# ignore tidy warnings
-dontwarn org.w3c.tidy.**