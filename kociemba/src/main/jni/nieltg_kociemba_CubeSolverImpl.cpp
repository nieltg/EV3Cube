/* nieltg_kociemba_CubeSolverImpl.c by @nieltg */

#include "nieltg_kociemba_CubeSolverImpl.h"
extern "C" {
#include "cache.h"
#include "search.h"
}

static jthrowable
glue_solve_exception (JNIEnv * env, int errcode)
{
	jclass je = env->FindClass ("nieltg/kociemba/SolveException");
	if (!je) return nullptr;
	
	jmethodID con = env->GetMethodID (je, "<init>", "(I)V");
	if (!con) return nullptr;
	
	return (jthrowable) env->NewObject (je, con, errcode);
}

JNIEXPORT jboolean JNICALL
Java_nieltg_kociemba_CubeSolverImpl_isReady (JNIEnv * env, jclass thiz)
{
	return cache_is_prepared ();
}

JNIEXPORT void JNICALL
Java_nieltg_kociemba_CubeSolverImpl_prepare (JNIEnv * env, jclass thiz,
                                             jstring j_path)
{
	const char* path = env->GetStringUTFChars (j_path, nullptr);
	cache_prepare (path);
	
	env->ReleaseStringUTFChars (j_path, path);
}

JNIEXPORT jstring JNICALL
Java_nieltg_kociemba_CubeSolverImpl_solve (JNIEnv * env, jclass thiz,
                                           jstring j_face)
{
	const char* face = env->GetStringUTFChars (j_face, nullptr);
	
	int errcode = 0;
	char* sol = solution (face, 24, 1000, 0, &errcode);
	
	env->ReleaseStringUTFChars (j_face, face);
	
	if (sol == NULL)
	{
		jthrowable th = glue_solve_exception (env, errcode);
		if (th) env->Throw (th);
		
		return nullptr;
	}
	else
	{
		jstring out = env->NewStringUTF (sol);
		free (sol);
		
		return out;
	}
}

JNIEXPORT void JNICALL
Java_nieltg_kociemba_CubeSolverImpl_destroy (JNIEnv * env, jclass thiz)
{
	cache_destroy ();
}

