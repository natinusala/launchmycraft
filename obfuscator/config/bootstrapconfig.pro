-injars ..\files\launcherbootstrap.jar
-outjars ..\obfuscated\launcherbootstrap.jar

-libraryjars 'C:\Program Files\Java\jre1.8.0_66\lib\rt.jar'

-dontshrink
-dontoptimize
-verbose
-dontnote **
-dontwarn **
-printmapping ..\obfuscated\mapping\bootstrap.txt
-overloadaggressively

-renamesourcefileattribute SourceFile
-keepattributes

-keep class !fr.launchmycraft** {
    <fields>;
    <methods>;
}

-keep public class fr.launchmycraft.launcher.bootstrap.BootstrapCore {
    public static void main(java.lang.String[]);
}
