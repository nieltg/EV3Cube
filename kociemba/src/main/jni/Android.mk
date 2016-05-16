# Android.mk by @nieltg

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := kociemba

LOCAL_CFLAGS   += -std=c99
LOCAL_CPPFLAGS += -std=c++11

LOCAL_SRC_FILES := \
	coordcube.c \
	cubiecube.c \
	facecube.c  \
	search.c    \
	cache.c     \
	nieltg_kociemba_CubeSolverImpl.cpp

include $(BUILD_SHARED_LIBRARY)

