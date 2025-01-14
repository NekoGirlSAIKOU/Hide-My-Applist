#include <jni.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <android/log.h>

#include "linux_syscall_support.h"

int syscall_result;
jint syscall_detect(int func) {
    jint result = (syscall_result == 0) ? func == 0 : -1;
    syscall_result = 0;
    return result;
}

void signal_handler(int sig) {
    syscall_result = -1;
    __android_log_print(ANDROID_LOG_INFO, "[HMA Detections]", "[INFO] Syscall was denied");
}

JNIEXPORT jintArray JNICALL
Java_com_tsng_hidemyapplist_ui_DetectionActivity_00024DetectionTask_nativeFile(JNIEnv *env, jobject thiz, jstring path) {
    const char *cpath = (*env)->GetStringUTFChars(env, path, NULL);
    const jsize sz = 6;
    jint results[sz];
    struct stat buf;
    struct kernel_stat buf_s;
    signal(SIGSYS, signal_handler);
    results[0] = (access(cpath, F_OK) == 0);
    results[1] = (stat(cpath, &buf) == 0);
    results[2] = (fstat(open(cpath, O_PATH), &buf) == 0);
    results[3] = syscall_detect(sys_stat(cpath, &buf_s));
    results[4] = syscall_detect(sys_fstat(sys_open(cpath, O_PATH, 0), &buf_s));
    jintArray ret = (*env)->NewIntArray(env, sz);
    (*env)->SetIntArrayRegion(env, ret, 0, sz, results);
    return ret;
}