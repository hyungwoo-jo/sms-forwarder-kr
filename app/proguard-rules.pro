-optimizationpasses 5
-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
#-dontskipnonpubliclibraryclassmembers
-dontoptimize
#-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes *Annotation*
-ignorewarnings

#-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt
-keepattributes SourceFile,LineNumberTable

-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.**
#-keep public class com.android.vending.licensing.ILicensingService

#-keep public class * extends android.support.v4.app.Fragment

-dontwarn com.android.JsInterface.**
-keep class com.android.JsInterface.** {*; }

-dontwarn org.apache.**
-keep class org.apache.**{ *; }

-keep public class * extends android.view.View {
  public <init>(android.content.Context);
  public <init>(android.content.Context, android.util.AttributeSet);
  public <init>(android.content.Context, android.util.AttributeSet, int);
  public void set*(...);
 }

-keepclasseswithmembernames class * {
  native <methods>;
}

-keepclasseswithmembers class * {
  public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclassmembers class * extends android.app.Activity {
  public void *(android.view.View);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembers enum * {
      public static **[] values();
      public static ** valueOf(java.lang.String);
}

-keepclassmembers class * {
      public void *ButtonClicked(android.view.View);
}

-keep class **.R$* {*;}

-dontwarn android.support.**


-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# zxing
-dontwarn com.google.zxing.**
-keep class com.google.zxing.**{*;}

-keep class microsoft.aspnet.signalr.** { *; }

#-dontoptimize
#-dontpreverify
#-dontwarn cn.jpush.**
#-keep class cn.jpush.** { *; }
#-dontwarn cn.jiguang.**
#-keep class cn.jiguang.** { *; }

-keepattributes *DatabaseField*
-keepattributes *DatabaseTable*
-keepattributes *SerializedName*
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }

#XHttp2
-keep class com.xuexiang.xhttp2.model.** { *; }
-keep class com.xuexiang.xhttp2.cache.model.** { *; }
-keep class com.xuexiang.xhttp2.cache.stategy.**{*;}
-keep class com.xuexiang.xhttp2.annotation.** { *; }

#okhttp
-dontwarn com.squareup.okhttp3.**
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**
-dontwarn javax.annotation.**

-keepattributes Signature
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod
-keep class org.xz_sale.entity.**{*;}
-keep class com.google.gson.** {*;}
-keep class com.google.**{*;}
#-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions

# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
#-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
#    rx.internal.util.atomic.LinkedQueueNode producerNode;
#}
#-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
#    rx.internal.util.atomic.LinkedQueueNode consumerNode;
#}

-dontwarn okio.**
-dontwarn javax.annotation.**

# fastjson
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }
-keepattributes Signature

# xpage
-keep class com.xuexiang.xpage.annotation.** { *; }
-keep class com.xuexiang.xpage.config.** { *; }

# xaop
-keep @com.xuexiang.xaop.annotation.* class * {*;}
-keep @org.aspectj.lang.annotation.* class * {*;}
-keep class * {
    @com.xuexiang.xaop.annotation.* <fields>;
    @org.aspectj.lang.annotation.* <fields>;
}
-keepclassmembers class * {
    @com.xuexiang.xaop.annotation.* <methods>;
    @org.aspectj.lang.annotation.* <methods>;
}

# xrouter
-keep public class com.xuexiang.xrouter.routes.**{*;}
-keep class * implements com.xuexiang.xrouter.facade.template.ISyringe{*;}
-keep interface * implements com.xuexiang.xrouter.facade.template.IProvider
-keep class * implements com.xuexiang.xrouter.facade.template.IProvider

# xvideo
-keep class com.xuexiang.xvideo.jniinterface.** { *; }

# xipc
-keep @com.xuexiang.xipc.annotation.* class * {*;}
-keep class * {
    @com.xuexiang.xipc.annotation.* <fields>;
}
-keepclassmembers class * {
    @com.xuexiang.xipc.annotation.* <methods>;
}

-keep class com.xuexiang.xui.widget.edittext.materialedittext.** { *; }

-keep class com.gyf.cactus.entity.* {*;}

-keep class cn.ppps.forwarder.core.http.entity.** {*;}
-keep class cn.ppps.forwarder.database.entity.** {*;}
-keep class cn.ppps.forwarder.entity.** {*;}
-keep class cn.ppps.forwarder.server.model.** {*;}

# javax.mail
-dontwarn com.sun.**
-dontwarn javax.mail.**
-dontwarn javax.activation.**
-keep class com.sun.** { *;}
-keep class javax.mail.** { *;}
-keep class javax.activation.** { *;}
-keep class com.smailnet.emailkit.** { *;}
-keep class cn.ppps.forwarder.utils.mail.** {*;}
-keep class com.gitee.xuankaicat.kmnkt.** {*;}
-keep class org.eclipse.paho.client.** {*;}

-keep public class com.xuexiang.xrouter.routes.**{*;}
-keep class * implements com.xuexiang.xrouter.facade.template.ISyringe{*;}
-keep interface * implements com.xuexiang.xrouter.facade.template.IProvider
-keep class * implements com.xuexiang.xrouter.facade.template.IProvider
-dontwarn com.xuexiang.xrouter.**

-dontwarn com.alipay.sdk.**
-dontwarn com.android.org.conscrypt.**
-dontwarn java.awt.image.**
-dontwarn javax.lang.model.**
-dontwarn javax.naming.**
-dontwarn javax.naming.directory.**

# This is generated automatically by the Android Gradle plugin.
-dontwarn org.joda.convert.**
-dontwarn org.slf4j.impl.**

# MultiLanguages
-keep class com.hjq.language.** {*;}

-keep class gatewayapps.crondroid.** { *; }
-keep class net.redhogs.cronparser.** { *; }

-ignorewarnings
-dontwarn **
