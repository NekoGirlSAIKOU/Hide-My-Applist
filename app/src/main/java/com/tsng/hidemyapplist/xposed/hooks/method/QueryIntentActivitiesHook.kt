package com.tsng.hidemyapplist.xposed.hooks.method

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.util.Log
import com.tsng.hidemyapplist.xposed.hooks.EnhancedIndividualHooks
import de.robv.android.xposed.XC_MethodHook

class QueryIntentActivitiesHook:XC_MethodHook() {
    companion object{
        private const val TAG = "QueryIntentActivitiesHook"
    }

    override fun afterHookedMethod(param: MethodHookParam?) {
        super.afterHookedMethod(param)
        if (param != null){
            val result = param.result as MutableList<ResolveInfo>
            param.result = result.filter {
                !EnhancedIndividualHooks.isToHidePackage(it.activityInfo.packageName)
            }.toMutableList()
        }
    }
}