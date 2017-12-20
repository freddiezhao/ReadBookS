# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\software\Android\sdk/tools/proguard/proguard-android.txt
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

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-ignorewarnings
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#-libraryjars libs/basicfuncsdk.jar
#-libraryjars libs/epublib-core-latest-fix0.1.jar
#-libraryjars libs/htmlcleaner-2.2.jar
#-libraryjars libs/weibosdkcore_release_v2.5.2_0410.jar
#-libraryjars libs/vdisksdk.jar
#-libraryjars libs/json-simple-1.1.1.jar
#-libraryjars libs/android-support-v4.jar
#-libraryjars libs/httpmime-4.2.5.jar
#-libraryjars libs/LingvoIntegration_2.5.2.12.jar
#-libraryjars libs/jxl.jar
#-libraryjars libs/nanohttpd-2.0.5.jar
#-libraryjars libs/nineoldandroids.jar
#-libraryjars libs/open-dictionary-api-1.2.1.jar
#-libraryjars libs/pdfparse.jar
#-libraryjars libs/xmlpull_1_1_3_1.jar

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep class * extends java.lang.Exception

-keep public class * extends android.view.View
-keep public class * extends android.os.Binder
-keep public class * extends android.widget.TabHost
-keep public class * extends android.app.Dialog
-keep public class * extends android.app.ListActivity
-keep public class * extends android.app.Fragment
-keep public class * extends android.support.v4.**

-keep public class * extends org.apache.commons.logging.LogFactory
-keep public class * extends org.apache.tools.ant.launch.Launcher
-keep public class * extends org.apache.tools.ant.launch.Locator


-keep public class org.amse.ys.zip.** {*;}
-keep public class org.vimgadgets.linebreak.** {*;}
-keep public class org.xmlpull.v1.** {*;}

#

-keep public class org.geometerplus.fbreader.** {*;}

#
-keep public class org.geometerplus.zlibrary.core.** {*;}
-keep public class org.geometerplus.zlibrary.text.** {*;}
-keep public class org.geometerplus.zlibrary.ui.android.** {*;}

-keepclassmembers class **.R$* {
  public static <fields>;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep class android.support.v4.** {*;}
-keep class com.sina.book.reader.PageWidget {*;}
-keep class com.sina.book.htmlspanner.** {*;}
-keep class com.sina.book.parser.** {*;}
-keep class com.sina.basicfunc.** {*;}
-keep class org.htmlcleaner.** {*;}
-keep class dtd.** {*;}
-keep class nl.siegmann.epublib.** {*;}
-keep class android.net.http.** {*;}
-keep class com.sina.weibo.sdk.** {*;}
-keep class com.sina.deviceidjnisdk.**{*;}
-keep class com.weibo.sdk.android.** {*;}
-keep class com.sina.sso.** {*;}
-keep class com.vdisk.** {*;}
-keep class org.json.** {*;}
-keep class jxl.** {*;}

-keepattributes SourceFile,LineNumberTable