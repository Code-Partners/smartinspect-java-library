#include <windows.h>
#include <jni.h>

JNIEXPORT jlong JNICALL Java_com_gurock_smartinspect_Clock_getCounter
  (JNIEnv *env, jclass c)
{
	jlong count;

	if (!QueryPerformanceCounter((LARGE_INTEGER *) &count))
	{
		count = 0; /* Indicates failure */
	}

	return count;
}

JNIEXPORT jlong JNICALL Java_com_gurock_smartinspect_Clock_getFrequency
  (JNIEnv *env, jclass c)
{
	jlong frequency;
	
	if (!QueryPerformanceFrequency((LARGE_INTEGER *) &frequency))
	{
		frequency = 0; /* Indicates failure */
	}

	return frequency;
}
