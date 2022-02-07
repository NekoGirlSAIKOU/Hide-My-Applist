package com.tsng.hidemyapplist.xposed.hooks

import android.app.ActivityThread
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.ModuleInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.crossbowffs.remotepreferences.RemotePreferences
import com.tsng.hidemyapplist.BuildConfig
import com.tsng.hidemyapplist.TemplateConfig
import com.tsng.hidemyapplist.XposedPreferenceProvider
import com.tsng.hidemyapplist.utils.isSystemApp
import com.tsng.hidemyapplist.xposed.XposedEntry
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage


class EnhancedIndividualHooks : EnhancedIndividualHooksJava(),SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        private const val TAG = "EnhancedIndividualHooks"
        private val shouldHookSelf by lazy {
            prefSettings.getBoolean("HookSelf", false)
        }

        @JvmStatic
        val prefScope: SharedPreferences by lazy {
            RemotePreferences(systemContext, XposedPreferenceProvider.AUTHORITY, "Scope")
        }

        @JvmStatic
        val prefSettings: SharedPreferences by lazy {
            RemotePreferences(systemContext, XposedPreferenceProvider.AUTHORITY, "Settings")
        }

        @JvmStatic
        var prefRequiredTemplate: SharedPreferences? = null
            get() {
                if (field == null) {
                    hookedPackageName?.let { it ->
                        val templateName = prefScope.getString(it, null)
                        if (templateName != null) {
                            field = RemotePreferences(
                                    systemContext,
                                    XposedPreferenceProvider.AUTHORITY,
                                    "tpl_$templateName"
                            )
                            Log.d(TAG,"templateFileName is tpl_$templateName")
                        }
                    }
                }
                return field
            }

        @JvmStatic
        var templateConfig: TemplateConfig? = null
            get() {
                if (field == null) {
                    field = prefRequiredTemplate?.let { TemplateConfig(it) }
                }
                return field
            }

        @JvmStatic
        var hookedPackageName: String? = null

        @JvmStatic
        val systemContext: Context
            get()  = XposedEntry.systemContext


        /*Return true to hide it*/
        @JvmStatic
        fun isToHidePackage(packageName: String?): Boolean {
            if (packageName == null) {
                return false    // Keep it
            } else {
                // First see if it should be kept less strictly.
                if (templateConfig?.isToHidePackage(
                        callerName = hookedPackageName,
                        queryName = packageName,
                        /* Only less strictly when infoDetectionHook is enabled*/
                        isSystemApp = templateConfig?.applyHooks?.apiRequests == true
                    ) == true
                ) {
                    return true // hide it
                } else {
                    // Seems it should be kept so check it strictly
                    try {
                        systemContext.packageManager.getApplicationInfo(
                            packageName,
                            0
                        )
                        return false // Keep it since getApplicationInfo hook say it's OK.
                    } catch (e: PackageManager.NameNotFoundException) {
                        return true // Hide it
                    }
                }
            }
        }
    }


    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (hookedPackageName != null || lpparam?.appInfo == null || lpparam.appInfo.isSystemApp()) return
        hookedPackageName = lpparam.packageName
        Log.d(TAG,"handleLoadPackage: $hookedPackageName")
        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            if (shouldHookSelf) {
                Log.d(TAG,"Hook self")
            } else {
                Log.d(TAG,"Don't hook self")
                return
            }
        }
        if(templateConfig == null){
            return  // This app is not in list.
        }

        prefScope.registerOnSharedPreferenceChangeListener(this)

        if (templateConfig?.enableAllHooks == true){
            Log.d(TAG,"Enable all hooks")
        }

        if (templateConfig?.applyHooks?.fileDetections == true) {
            Log.d(TAG,"Enable file detection hooks")
            IndividualHooks().handleLoadPackage(lpparam)
        }

        if (templateConfig?.applyHooks?.enhancedIndividualHook == true) {
            Log.d(TAG,"Enable enhancedIndividualHook")
        } else {
            Log.d(TAG,"Disable enhancedIndividualHook")
            return
        }

        if (templateConfig?.applyHooks?.apiRequests == true) {
            Log.d(TAG,"Enable apiHook and infoDetectionHook")
            apiHook(lpparam)
            infoDetectionHook(lpparam)
        }

        if (templateConfig?.applyHooks?.idDetections == true) {
            Log.d(TAG,"Enable idDetectionHook")
            idDetectionHook(lpparam)
        }

        if (templateConfig?.applyHooks?.intentQueries == true) {
            Log.d(TAG,"Enable intentQueryHook")
            intentQueryHook(lpparam)
        }

        if (templateConfig?.applyHooks?.permissionDetections == true){
            Log.d(TAG,"Enable permissionDetectionHook")
            permissionDetectionHook(lpparam)
        }

        if (templateConfig?.applyHooks?.shellDetections == true){
            Log.d(TAG,"Enable shellDetectionsHook")
            shellDetectionsHook(lpparam)
        }
        Log.d(TAG,"All hook is done.")
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(TAG,"onSharedPreferenceChanged")
        if (sharedPreferences == prefScope){
            if (key == hookedPackageName){
                Log.d(TAG,"Refresh prefRequiredTemplate and templateConfig")
                prefRequiredTemplate = null // Refresh prefRequiredTemplate
                templateConfig = null // Refresh templateConfig
            }
        }
    }
}

/*        val systemContext = XposedHelpers.callMethod(
            XposedHelpers.callStaticMethod(
                XposedHelpers.findClass(
                    "android.app.ActivityThread",
                    lpparam.classLoader
                ), "currentActivityThread"
            ), "getSystemContext"
        ) as Context*/