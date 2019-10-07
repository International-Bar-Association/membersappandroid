# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\dev\andoid\sdk/tools/proguard/proguard-android.txt
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

# Global rule, required for Firebase, Gson, Retrofit, etc
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes InnerClasses

-keepclassmembers enum * { *; }

-dontwarn java.lang.invoke.**

# App
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature
-keep class com.niteworks.workforcemessaging.niteworks.profile.UpdateLocationListener

-dontwarn android.net.http.AndroidHttpClient
-dontwarn com.google.android.gms.**

# Ensure proguard ignores org.apache.http.legacy (bug in proguard reading this even if we don't reference it
# - https://code.google.com/p/android/issues/detail?id=194513)
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**
-dontwarn org.apache.http.**

# Firebase/GMS
-keep public class com.google.android.gms.* { public *; }
-keep class com.google.firebase.FirebaseApp
-dontnote com.google.android.gms.**
-dontnote com.google.firebase.messaging.**
-dontwarn com.google.firebase.messaging.**

# Only necessary if you downloaded the SDK jar directly instead of from maven.
-keep class com.shaded.fasterxml.jackson.** { *; }

# GSON
-keep class com.google.gson.FieldNamingPolicy$1
-keep class com.google.gson.LongSerializationPolicy$1
-keep class com.google.gson.Gson
-keep class com.google.gson.TypeAdapter
-dontnote com.google.gson.internal.**

# Butterknife
-keep class butterknife.** { *; }
-dontnote butterknife.internal.**
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
    @butterknife.OnClick <methods>;
    @butterknife.OnEditorAction <methods>;
    @butterknife.OnItemClick <methods>;
    @butterknife.OnItemLongClick <methods>;
    @butterknife.OnLongClick <methods>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

#-keepclassmembers class * {
#    @de.clemenskeppler.materialsearchview <fields>;
#}

-keepclassmembers class de.clemenskeppler.materialsearchview.** { *; }

# OrmLite
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
-dontwarn javax.**
-dontwarn org.slf4j.**

-keepclassmembers class * {
    public <init>(android.content.Context);
 }

-keep @com.j256.ormlite.table.DatabaseTable class * {
    @com.j256.ormlite.field.DatabaseField <fields>;
    <init>();
}

-keep class org.apache.harmony.lang.annotation.**
-dontnote com.j256.ormlite.android.DatabaseTableConfigUtil # This uses reflection to access harmony annotations



# Eventbus
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keep class org.greenrobot.eventbus.util.ThrowableFailureEvent


# Retrofit
# Retrofit does reflection on generic parameters and InnerClass is required to use Signature.
-keepattributes Signature, InnerClasses
# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**
# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit
# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.-KotlinExtensions





# Glide Library (https://github.com/bumptech/glide)
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

# Glide transformations
-keep class jp.wasabeef.glide.** { *; }
-dontwarn  jp.wasabeef.glide.**
-keepattributes Signature

-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class android.support.v8.renderscript.** { *; }

# SearchView
-keep class android.support.v7.widget.SearchView { *; }

# FFmpegMediaMetadataRetriever
-keep class wseemann.media.** { *; }

# PlugPDF
-keep class com.epapyrus.plugpdf.core.** { *; }

# OKIO
-keep class okhttp3.MediaType
-keep class okhttp3.ResponseBody
-keep class okhttp3.Response
-keep class okhttp3.Call
-keep class okhttp3.RequestBody
-keep class okhttp3.Headers
-keep class okhttp3.MultipartBody$Part
-keep class okhttp3.Headers
-keep class okhttp3.HttpUrl
-keep class okhttp3.Call$Factory
-keep class okhttp3.OkHttpClient
-keep class okio.BufferedSource
-keep class okio.Source
-keep class okio.Buffer
-keep class okio.BufferedSink
-keep class okio.ByteString
-keep class okio.ByteString
-dontnote okhttp3.internal.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# JodaTime
-keep class org.joda.time.** { *; }
-dontwarn org.joda.time.**

# JobQueue
-dontwarn com.birbit.android.jobqueue.scheduling.**

-dontwarn sun.misc.Unsafe

#materialsearchview
-dontwarn de.clemenskeppler.materialsearchview.**

# Urban Airship
-dontwarn com.urbanairship.push.notifications.**
-keep public class * extends com.urbanairship.Autopilot

#Slidinguppanel
-dontwarn com.sothree.**
-keep class com.sothree.**
-keep interface com.sothree.**
