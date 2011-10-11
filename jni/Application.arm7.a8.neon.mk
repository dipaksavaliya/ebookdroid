APP_ABI := armeabi-v7a

APP_CFLAGS := -DHAVE_CONFIG_H -DTHREADMODEL=NOTHREADS -DDEBUGLVL=0 -Os -march=armv7-a -mtune=cortex-a8 -mfpu=neon -mfloat-abi=softfp

APP_MODULES := jpeg libdjvu mupdf ebookdroid