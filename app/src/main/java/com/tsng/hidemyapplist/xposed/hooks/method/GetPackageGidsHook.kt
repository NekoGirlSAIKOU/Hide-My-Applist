package com.tsng.hidemyapplist.xposed.hooks.method
// Since the first arg is packageName.
// So the procession is the same.
// Just use GetPackageUidHook() for convenient
class GetPackageGidsHook:GetPackageUidHook()