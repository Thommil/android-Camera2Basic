#include "com_thommil_animalsgo_cv_OpenCVProcessor.h"

#include <opencv2/core.hpp>


extern "C"
{
    JNIEXPORT void JNICALL Java_com_thommil_animalsgo_cv_OpenCVProcessor_validateCapture(JNIEnv *env, jobject thisObj, jobject capture)
    {
        jclass cls = env->GetObjectClass(capture);
        jfieldID fieldId = env->GetFieldID(cls, "validationState", "I");
        env->SetIntField(capture, fieldId, 0x02);
    }
}

