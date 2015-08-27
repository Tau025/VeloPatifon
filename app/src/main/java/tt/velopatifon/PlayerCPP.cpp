#include "PlayerCPP.h"
/* This is a JNI example where we use native methods to play sounds
 * using OpenSL ES. See the corresponding Java source file located at:
 *
 *   src/com/example/nativeaudio/NativeAudio/NativeAudio.java
 */

#include <assert.h>
#include <jni.h>
#include <string.h>

// for __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");
// #include <android/log.h>

// for native audio
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

// for native asset manager
#include <sys/types.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

// pre-recorded sound clips, both are 8 kHz mono 16-bit signed little endian

static const char hello[] =
#include "hello_clip.h"
;

static const char android[] =
#include "android_clip.h"
;

// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;
static SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;


// aux effect on the output mix, used by the buffer queue player
static const SLEnvironmentalReverbSettings reverbSettings =
        SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

// file descriptor player interfaces
static SLObjectItf fdPlayerObject = NULL;
static SLPlayItf fdPlayerPlay;
static SLSeekItf fdPlayerSeek;
static SLMuteSoloItf fdPlayerMuteSolo;
static SLVolumeItf fdPlayerVolume;


// synthesized sawtooth clip
#define SAWTOOTH_FRAMES 8000
static short sawtoothBuffer[SAWTOOTH_FRAMES];

// pointer and size of the next player buffer to enqueue, and number of remaining buffers
static short *nextBuffer;
static unsigned nextSize;
static int nextCount;

// playback rate (default 1x:1000)
static SLpermille playbackMinRate = 500;
static SLpermille playbackMaxRate = 2000;
static SLpermille playbackRateStepSize;
static SLPlaybackRateItf fdPlaybackRate;


// create the engine and output mix objects
void Java_com_example_nativeaudio_NativeAudio_createEngine(JNIEnv* env, jclass clazz) {
    SLresult result;

    // create engine
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // realize the engine
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get the engine interface, which is needed in order to create other objects
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // create output mix, with environmental reverb specified as a non-required interface
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, ids, req);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;


    // realize the output mix
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get the environmental reverb interface
    // this could fail if the environmental reverb effect is not available,
    // either because the feature is not present, excessive CPU load, or
    // the required MODIFY_AUDIO_SETTINGS permission was not requested and granted
    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB, &outputMixEnvironmentalReverb);
    if (SL_RESULT_SUCCESS == result) {
        result = (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
                outputMixEnvironmentalReverb, &reverbSettings);
        (void)result;
    }
    // ignore unsuccessful result codes for environmental reverb, as it is optional for this example
}

// expose the mute/solo APIs to Java for one of the 3 players

// expose the volume APIs to Java for one of the 3 players

// enable reverb on the buffer queue player
jboolean Java_com_example_nativeaudio_NativeAudio_enableReverb(JNIEnv* env, jclass clazz, jboolean enabled) {
    SLresult result;
    // we might not have been able to add environmental reverb to the output mix
    if (NULL == outputMixEnvironmentalReverb) {
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

// create asset audio player
jboolean Java_com_example_nativeaudio_NativeAudio_createAssetAudioPlayer(JNIEnv* env, jclass clazz, jobject assetManager, jstring filename) {
    SLresult result;

    // convert Java string to UTF-8
    const char *utf8 = (*env) -> GetStringUTFChars(env, filename, NULL);
    assert(NULL != utf8);

    // use asset manager to open asset by filename
    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
    assert(NULL != mgr);
    AAsset* asset = AAssetManager_open(mgr, utf8, AASSET_MODE_UNKNOWN);

    // release the Java string and UTF-8
    (*env)->ReleaseStringUTFChars(env, filename, utf8);

    // the asset might not be found
    if (NULL == asset) { return JNI_FALSE; }

    // open asset as file descriptor
    off_t start, length;
    int fd = AAsset_openFileDescriptor(asset, &start, &length);
    assert(0 <= fd);
    AAsset_close(asset);

    // configure audio source
    SLDataLocator_AndroidFD loc_fd = {SL_DATALOCATOR_ANDROIDFD, fd, start, length};
    SLDataFormat_MIME format_mime = {SL_DATAFORMAT_MIME, NULL, SL_CONTAINERTYPE_UNSPECIFIED};
    SLDataSource audioSrc = {&loc_fd, &format_mime};

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};

    // create audio player
    const SLInterfaceID ids[3] = {SL_IID_SEEK, SL_IID_MUTESOLO, SL_IID_VOLUME};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &fdPlayerObject, &audioSrc, &audioSnk, 3, ids, req);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // realize the player
    result = (*fdPlayerObject)->Realize(fdPlayerObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get the play interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_PLAY, &fdPlayerPlay);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get the seek interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_SEEK, &fdPlayerSeek);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get the mute/solo interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_MUTESOLO, &fdPlayerMuteSolo);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get the volume interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_VOLUME, &fdPlayerVolume);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // enable whole file looping
    result = (*fdPlayerSeek)->SetLoop(fdPlayerSeek, SL_BOOLEAN_TRUE, 0, SL_TIME_UNKNOWN);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    // get playback rate interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_PLAYBACKRATE, &fdPlaybackRate);
    assert(SL_RESULT_SUCCESS == result);

    SLuint32 capa;
    result = (*fdPlaybackRate) -> GetRateRange(fdPlaybackRate, 0, &playbackMinRate, &playbackMaxRate, &playbackRateStepSize, &capa);
    assert(SL_RESULT_SUCCESS == result);

    result = (*fdPlaybackRate) -> SetPropertyConstraints(fdPlaybackRate, SL_RATEPROP_PITCHCORAUDIO);

    if (SL_RESULT_PARAMETER_INVALID == result) {
        //        LOGD("Parameter Invalid");
    }
    if (SL_RESULT_FEATURE_UNSUPPORTED == result) {
        //        LOGD("Feature Unsupported");
    }
    if (SL_RESULT_SUCCESS == result) {
        assert(SL_RESULT_SUCCESS == result);
        //     LOGD("Success");
    }

    //     result = (*fdPlaybackRate)->SetRate(fdPlaybackRate, playbackMaxRate);
    //        assert(SL_RESULT_SUCCESS == result);

    SLpermille SLrate;
    result = (*fdPlaybackRate) -> GetRate(fdPlaybackRate, &SLrate);
    assert(SL_RESULT_SUCCESS == result);

    // enable whole file looping
    result = (*fdPlayerSeek) -> SetLoop(fdPlayerSeek, SL_BOOLEAN_FALSE, 0, SL_TIME_UNKNOWN);
    assert(SL_RESULT_SUCCESS == result);
    (void)result;

    return JNI_TRUE;
}

/*JNIEXPORT void Java_com_example_stackoverflowcode_NativeAudio_setRate(JNIEnv* env, jclass clazz, jint rate) {
    result = (*fdPlayerRate) -> SetRate(fdPlayerRate, playbackMaxRate);
               assert(SL_RESULT_SUCCESS == result);
}
*/
JNIEXPORT void Java_com_example_stackoverflowcode_NativeAudio_setRate(JNIEnv * env, jclass clazz, jint rate) {
    if (NULL != fdPlaybackRate) {
        SLresult result;
        result = (*fdPlaybackRate)->SetRate(fdPlaybackRate, rate);
        assert(SL_RESULT_SUCCESS == result);
    }
}


// set the playing state for the asset audio player
void Java_com_example_nativeaudio_NativeAudio_setPlayingAssetAudioPlayer(JNIEnv* env, jclass clazz, jboolean isPlaying) {
    SLresult result;
    // make sure the asset audio player was created
    if (NULL != fdPlayerPlay) {
        // set the player's state
        result = (*fdPlayerPlay)->SetPlayState(fdPlayerPlay, isPlaying ? SL_PLAYSTATE_PLAYING : SL_PLAYSTATE_PAUSED);
        assert(SL_RESULT_SUCCESS == result);
        (void)result;
    }
}

// shut down the native audio system
void Java_com_example_nativeaudio_NativeAudio_shutdown(JNIEnv* env, jclass clazz) {
    // destroy file descriptor audio player object, and invalidate all associated interfaces
    if (fdPlayerObject != NULL) {
        (*fdPlayerObject) -> Destroy(fdPlayerObject);
        fdPlayerObject = NULL;
        fdPlayerPlay = NULL;
        fdPlayerSeek = NULL;
        fdPlayerMuteSolo = NULL;
        fdPlayerVolume = NULL;
    }

    // destroy output mix object, and invalidate all associated interfaces
    if (outputMixObject != NULL) {
        (*outputMixObject) -> Destroy(outputMixObject);
        outputMixObject = NULL;
        outputMixEnvironmentalReverb = NULL;
    }

    // destroy engine object, and invalidate all associated interfaces
    if (engineObject != NULL) {
        (*engineObject) -> Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }
}