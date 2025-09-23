# Basic ProGuard rules for your app

# Keep your application class
-keep public class com.bessadi.fitwod.Application { *; }

# Keep all classes in your package
-keep class com.bessadi.fitwod.** { *; }

# Keep Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# Keep View models
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep database classes
-keep class * extends android.database.sqlite.SQLiteOpenHelper { *; }

# Keep parcelable classes
-keep class * implements android.os.Parcelable { *; }

# Keep serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep annotations
-keepattributes Annotation

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep callback methods
-keepclassmembers class * {
    void on*(**);
}

# Third-party libraries
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }
-keep class androidx.** { *; }
-keep class com.github.bumptech.glide.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Annotation
-keep class com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn retrofit.**
-dontwarn org.codehaus.mojo.**