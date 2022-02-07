package com.tsng.hidemyapplist

import android.content.Context
import android.os.Binder
import android.util.Log
import com.crossbowffs.remotepreferences.RemotePreferenceFile
import com.crossbowffs.remotepreferences.RemotePreferenceProvider

class XposedPreferenceProvider :
    RemotePreferenceProvider(AUTHORITY, DEFAULT_PREF_FILE_NAMEs) {
    companion object {
        private const val TAG = "XposedPreferenceProvider"
        private val DEFAULT_PREF_FILE_NAMEs = arrayOf("Scope", "Settings")
        const val AUTHORITY = "com.tsng.hidemyapplist.preferences"
    }

    private val scopePref by lazy {
        context!!.getSharedPreferences("Scope", Context.MODE_PRIVATE)
    }

    override fun onCreate(): Boolean {
        // Add templates to prefNames
        Log.d(TAG, "onCreate: Add templates to prefNames")
        val pref = context!!.getSharedPreferences("Templates", Context.MODE_PRIVATE)

        val templates = pref.getStringSet("List", emptySet())!!
        val mPrefFilesField = RemotePreferenceProvider::class.java.getDeclaredField("mPrefFiles")
        mPrefFilesField.isAccessible = true

        val prefNames = ArrayList<String>(templates.size + DEFAULT_PREF_FILE_NAMEs.size)
        prefNames.addAll(DEFAULT_PREF_FILE_NAMEs)
        for (template in templates) {
            Log.d(TAG, "onCreate: Add tpl_$template")
            prefNames.add("tpl_$template")
        }
        mPrefFilesField.set(this, RemotePreferenceFile.fromFileNames(prefNames.toTypedArray()))

        Log.d(TAG, "onCreate: super.onCreate()")
        return super.onCreate()
    }

    override fun checkAccess(prefFileName: String?, prefKey: String?, write: Boolean): Boolean {
        val callingPackageName = try {
            super.getCallingPackage()
        } catch (e:SecurityException){
            val callingUid = Binder.getCallingUid()
            context?.packageManager?.getNameForUid(callingUid)
        }

        // No write
        if (write) {
            Log.w(TAG, "$callingPackageName try to write to file $prefFileName key $prefKey")
            return false
        }

        // Can not read other apps' scope
        if (prefFileName == "Scope") {
            if (prefKey != callingPackageName) {
                Log.w(TAG, "$callingPackageName try to read scope of other packages")
                return false
            }
        }

        // Can not read settings unless BuildConfig.APPLICATION_ID or android
        if (prefFileName == "Settings") {
            if (callingPackageName != BuildConfig.APPLICATION_ID && callingPackageName != "android") {
                Log.w(TAG, "$callingPackageName try to read settings")
                return false
            }
        }

        // Can not read other apps' template
        if (prefFileName?.startsWith("tpl_") == true) {
            scopePref.getString(callingPackageName, null)?.let { requiredTemplate ->
                if (prefFileName != "tpl_$requiredTemplate") {
                    Log.w(TAG, "$callingPackageName try to read unneeded template")
                    return false
                }
            }
        }

        Log.d(TAG, "Allow $callingPackageName to read file $prefFileName key $prefKey")
        return true
    }
}