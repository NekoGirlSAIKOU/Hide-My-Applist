package com.tsng.hidemyapplist

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.util.Log
import com.tsng.hidemyapplist.utils.isSystemApp
import com.tsng.hidemyapplist.xposed.hooks.EnhancedIndividualHooks
import java.io.File

class TemplateConfig(private val templatePref: SharedPreferences) {
    companion object{
        private const val TAG = "TemplateConfig"
    }

    val hideApps by lazy {
        templatePref.getStringSet("HideApps", emptySet())!!
    }
    val whiteList by lazy {
        templatePref.getBoolean("WhiteList", false)
    }
    val excludeSystemApps by lazy {
        if (whiteList){
            templatePref.getBoolean("ExcludeSystemApps", false)
        } else false
    }
    val enableAllHooks by lazy {
        templatePref.getBoolean("EnableAllHooks", false)
    }
    val applyHooks by lazy {
        if (enableAllHooks) {
            HooksConfig()
        } else {
            val hooksSet = templatePref.getStringSet("ApplyHooks", emptySet())!!
            HooksConfig(
                intentQueries = hooksSet.contains("Intent queries"),
                idDetections = hooksSet.contains("ID detections"),
                fileDetections = hooksSet.contains("File detections"),
                apiRequests = hooksSet.contains("API requests"),
                enhancedIndividualHook = hooksSet.contains("Enhanced individual hook"),
                permissionDetections = hooksSet.contains("Permission detections"),
                shellDetections = hooksSet.contains("Shell detections")
            )
        }
    }

    inner class HooksConfig(
        val intentQueries: Boolean = true,
        val idDetections: Boolean = true,
        val fileDetections: Boolean = true,
        val apiRequests: Boolean = true,
        val enhancedIndividualHook: Boolean = true,
        val permissionDetections:Boolean = true,
        val shellDetections:Boolean = true
    )


    /*
    * Return true to hide
    * */
    fun isToHidePackage(queryApplicationInfo: ApplicationInfo):Boolean{
        return isToHidePackage(
            queryName = queryApplicationInfo.packageName,
            isSystemApp = queryApplicationInfo.isSystemApp())
    }

    /*
    * Return true to hide
    * */
    fun isToHidePackage(queryName:String?,isSystemApp:Boolean):Boolean{
        if (queryName==null){
            return false
        }

        return if (whiteList){
            // White list
                if (excludeSystemApps){
                    !hideApps.contains(queryName) && !isSystemApp
                } else {
                    !hideApps.contains(queryName)
                }
        } else {
            // Black list
            hideApps.contains(queryName)
        }
    }

    /*
    * Return true to hide
    * */
    fun isToHidePackage(callerName: String?, queryApplicationInfo: ApplicationInfo):Boolean{
        return isToHidePackage(
            callerName = callerName,
            queryName = queryApplicationInfo.packageName,
            isSystemApp = queryApplicationInfo.isSystemApp()
        )
    }

    /*
    * Return true to hide
    * */
    fun isToHidePackage(callerName: String?, queryName:String?,isSystemApp:Boolean):Boolean{
        if (callerName!= null && queryName!= null && callerName in queryName){
            return false
        }
        return isToHidePackage(
            queryName = queryName,
            isSystemApp = isSystemApp)
    }


    fun isToHideFile(callerName:String?,path:String?):Boolean{
        if (path == null){
            return false
        }
        val rules = setOf(
            Regex("/storage/emulated/.*?/Android/.*?/"),
            Regex("/storage/self/primary/Android/.*?/"),
            Regex("/sdcard/Android/.*?/"),
            Regex("/data/data/"),
            Regex("/data/user/.*?/")
        )
        for (regex in rules)
            regex.find(path)?.let {
                val queryName = path.removePrefix(it.value).split("/")[0]
                // For convenience let's consider it non-system app.
                // I think this won't cause trouble.
                return isToHidePackage(
                    callerName = callerName,
                    queryName = queryName,
                    isSystemApp = false
                )
            }
        return false
    }
}