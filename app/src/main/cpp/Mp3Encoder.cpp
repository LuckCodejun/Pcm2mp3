//
// Created by meetpast on 2020/4/8.
//

#include "com_meetpast_pcm2mp3_Mp3Encoder.h"
#include "mp3_encoder.h"
#include <jni.h>

Mp3Encoder *encoder = NULL;

extern "C" JNIEXPORT jint JNICALL Java_com_meetpast_pcm2mp3_Mp3Encoder_init
        (JNIEnv *env, jobject jobj, jstring pcmPathParam, jint audioChannelsParam, jint bitRateParam, jint sampleRateParam, jstring mp3PahParam){
    const char* pcmPath = env->GetStringUTFChars(pcmPathParam,NULL);
    const char* mp3Path = env->GetStringUTFChars(mp3PahParam,NULL);
    encoder = new Mp3Encoder();
    int ret = encoder->lint(pcmPath,
                            mp3Path,
                            sampleRateParam,
                            audioChannelsParam,
                            bitRateParam);
    env->ReleaseStringUTFChars(mp3PahParam, mp3Path);
    env->ReleaseStringUTFChars(pcmPathParam, pcmPath);
    return ret;
}

extern "C" JNIEXPORT void JNICALL Java_com_meetpast_pcm2mp3_Mp3Encoder_encode
(JNIEnv *, jobject){
    encoder->Encode();
}

extern "C" JNIEXPORT void JNICALL Java_com_meetpast_pcm2mp3_Mp3Encoder_destroy
(JNIEnv *, jobject){
    encoder->Destory();
}