LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on

include /Users/thommil/Dev/Workspace/animals-go-android/lib/OpenCV-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE := cv-opencv
LOCAL_SRC_FILES := cv-opencv.cpp
include $(BUILD_SHARED_LIBRARY)
