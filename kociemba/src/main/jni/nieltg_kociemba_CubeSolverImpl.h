/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class nieltg_kociemba_CubeSolverImpl */

#ifndef _Included_nieltg_kociemba_CubeSolverImpl
#define _Included_nieltg_kociemba_CubeSolverImpl
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     nieltg_kociemba_CubeSolverImpl
 * Method:    isReady
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_nieltg_kociemba_CubeSolverImpl_isReady
  (JNIEnv *, jclass);

/*
 * Class:     nieltg_kociemba_CubeSolverImpl
 * Method:    prepare
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_nieltg_kociemba_CubeSolverImpl_prepare
  (JNIEnv *, jclass, jstring);

/*
 * Class:     nieltg_kociemba_CubeSolverImpl
 * Method:    solve
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_nieltg_kociemba_CubeSolverImpl_solve
  (JNIEnv *, jclass, jstring);

/*
 * Class:     nieltg_kociemba_CubeSolverImpl
 * Method:    destroy
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_nieltg_kociemba_CubeSolverImpl_destroy
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
