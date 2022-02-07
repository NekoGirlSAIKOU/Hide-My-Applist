package com.tsng.hidemyapplist.xposed.hooks;

import android.content.Intent;
import android.content.pm.ModuleInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.VersionedPackage;
import android.os.Build;

import com.tsng.hidemyapplist.xposed.hooks.method.CheckPermissionHook;
import com.tsng.hidemyapplist.xposed.hooks.method.GetApplicationEnabledSettingHook;
import com.tsng.hidemyapplist.xposed.hooks.method.GetApplicationInfoHook;
import com.tsng.hidemyapplist.xposed.hooks.method.GetInstalledApplicationsHook;
import com.tsng.hidemyapplist.xposed.hooks.method.GetInstalledModulesHook;
import com.tsng.hidemyapplist.xposed.hooks.method.GetInstalledPackagesHook;
import com.tsng.hidemyapplist.xposed.hooks.method.GetPackageGidsHook;
import com.tsng.hidemyapplist.xposed.hooks.method.GetPackageInfoHook;
import com.tsng.hidemyapplist.xposed.hooks.method.GetPackageUidHook;
import com.tsng.hidemyapplist.xposed.hooks.method.GetPackagesHoldingPermissionsHook;
import com.tsng.hidemyapplist.xposed.hooks.method.ProcessBuilderStartHook;
import com.tsng.hidemyapplist.xposed.hooks.method.QueryIntentActivitiesHook;

import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class EnhancedIndividualHooksJava implements IXposedHookLoadPackage {
    void apiHook(XC_LoadPackage.LoadPackageParam lpparam){
        Class ApplicationPackageManagerClass = XposedHelpers.findClass("android.app.ApplicationPackageManager",lpparam.classLoader);
        XposedHelpers.findAndHookMethod(
                ApplicationPackageManagerClass,
                "getInstalledApplications",
                int.class,  // flags
                new GetInstalledApplicationsHook()
        );
        XposedHelpers.findAndHookMethod(
                ApplicationPackageManagerClass,
                "getInstalledPackages",
                int.class,  // flags
                new GetInstalledPackagesHook()
        );
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            XposedHelpers.findAndHookMethod(
                    ApplicationPackageManagerClass,
                    "getInstalledModules",
                    int.class,  // flags
                    new GetInstalledModulesHook()
            );
        }
    }

    void intentQueryHook(XC_LoadPackage.LoadPackageParam lpparam){
        Class ApplicationPackageManagerClass = XposedHelpers.findClass("android.app.ApplicationPackageManager",lpparam.classLoader);

        XposedHelpers.findAndHookMethod(
                ApplicationPackageManagerClass,
                "queryIntentActivities",
                Intent.class,
                int.class,
                new QueryIntentActivitiesHook()
        );
    }

    void idDetectionHook(XC_LoadPackage.LoadPackageParam lpparam){
        Class ApplicationPackageManagerClass = XposedHelpers.findClass("android.app.ApplicationPackageManager",lpparam.classLoader);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            XposedHelpers.findAndHookMethod(
                    ApplicationPackageManagerClass,
                    "getPackageUid",
                    String.class,
                    int.class,
                    new GetPackageUidHook()
            );
            XposedHelpers.findAndHookMethod(
                    ApplicationPackageManagerClass,
                    "getPackageGids",
                    String.class,
                    int.class,
                    new GetPackageGidsHook()
            );
        }
        XposedHelpers.findAndHookMethod(
                ApplicationPackageManagerClass,
                "getPackageGids",
                String.class,
                new GetPackageGidsHook()
        );
    }

    void infoDetectionHook(XC_LoadPackage.LoadPackageParam lpparam){
        Class ApplicationPackageManagerClass = XposedHelpers.findClass("android.app.ApplicationPackageManager",lpparam.classLoader);

        XposedBridge.hookAllMethods(
                ApplicationPackageManagerClass,
                "getPackageInfo",
                new GetPackageInfoHook()
        );
/*        XposedHelpers.findAndHookMethod(
                ApplicationPackageManagerClass,
                "getPackageInfo",
                String.class,
                int.class,
                new GetPackageInfoHook()
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            XposedHelpers.findAndHookMethod(
                    ApplicationPackageManagerClass,
                    "getPackageInfo",
                    VersionedPackage.class,
                    int.class,
                    new GetPackageInfoHook()
            );
        }*/
        XposedBridge.hookAllMethods(
                ApplicationPackageManagerClass,
                "getApplicationInfo",
                new GetApplicationInfoHook()
        );
/*        XposedHelpers.findAndHookMethod(
                ApplicationPackageManagerClass,
                "getApplicationInfo",
                String.class,
                int.class,
                new GetApplicationInfoHook()
        );*/
        XposedHelpers.findAndHookMethod(
                ApplicationPackageManagerClass,
                "getApplicationEnabledSetting",
                String.class,   // packageName
                new GetApplicationEnabledSettingHook()
        );
    }

    void permissionDetectionHook(XC_LoadPackage.LoadPackageParam lpparam){
        Class ApplicationPackageManagerClass = XposedHelpers.findClass("android.app.ApplicationPackageManager",lpparam.classLoader);

        XposedBridge.hookAllMethods(
                ApplicationPackageManagerClass,
                "getPackagesHoldingPermissions",
                new GetPackagesHoldingPermissionsHook()
        );
/*        XposedHelpers.findAndHookMethod(
                ApplicationPackageManagerClass,
                "getPackagesHoldingPermissions",
                String[].class,
                int.class,
                new GetPackagesHoldingPermissionsHook()
        );*/

        XposedHelpers.findAndHookMethod(
                ApplicationPackageManagerClass,
                "checkPermission",
                String.class,   // permName
                String.class,   // packageName
                new CheckPermissionHook()
        );
    }

    void shellDetectionsHook(XC_LoadPackage.LoadPackageParam lpparam){
        XposedHelpers.findAndHookMethod(
                ProcessBuilder.class,
                "start",
                new ProcessBuilderStartHook()
        );
    }
}
