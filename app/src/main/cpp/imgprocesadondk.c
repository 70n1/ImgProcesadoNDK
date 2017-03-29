#include "com_imgprocesadondk_ImgProcesadoNDK.h"
#include <android/log.h>
#include <android/bitmap.h>

#define LOG_TAG "libimgprocesadondk"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef struct {
    uint8_t red;
    uint8_t green;
    uint8_t blue;
    uint8_t alpha;
} rgba;

/*Conversion a grises por pixel*/
JNIEXPORT void JNICALL Java_com_imgprocesadondk_ImgProcesadoNDK_convertirGrises
         (JNIEnv *env, jobject obj, jobject bitmapcolor, jobject bitmapgris) {
              AndroidBitmapInfo infocolor;
              void *pixelscolor;
              AndroidBitmapInfo infogris;
              void *pixelsgris;
              int ret;
              int y;
              int x;

              LOGI("convertirGrises");
              if ((ret = AndroidBitmap_getInfo(env, bitmapcolor, &infocolor)) < 0) {
                LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
                return;
              }

              if ((ret = AndroidBitmap_getInfo(env, bitmapgris, &infogris)) < 0) {
                LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
                return;
              }

              LOGI("imagen color :: ancho %d;alto %d;avance %d;formato %d;flags %d",
                infocolor.width,  infocolor.height, infocolor.stride,
                infocolor.format, infocolor.flags);

              if (infocolor.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
                LOGE("Bitmap no es formato RGBA_8888 !");
                return;
              }

            LOGI("imagen color :: ancho %d;alto %d;avance %d;formato %d;flags %d",
                infogris.width, infogris.height, infogris.stride,
                infogris.format, infogris.flags);

            if (infogris.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
                LOGE("Bitmap no es formato RGBA_8888 !");
                return;
            }

            if ((ret = AndroidBitmap_lockPixels(env, bitmapcolor, &pixelscolor))  < 0) {
                LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
            }

            if ((ret = AndroidBitmap_lockPixels(env, bitmapgris, &pixelsgris)) < 0) {
                LOGE("AndroidBitmap_lockPixels() fallo ! error=%d", ret);
            }

            // modificacion pixeles en el algoritmo de escala grises
            for (y = 0; y < infocolor.height; y++) {
                rgba *line = (rgba *) pixelscolor;
                rgba *grisline = (rgba *) pixelsgris;
                for (x = 0; x < infocolor.width; x++) {
                    float output = (line[x].red + line[x].green + line[x].blue) / 3;
                    if (output > 255) output = 255;
                    grisline[x].red = grisline[x].green = grisline[x].blue = (uint8_t) output;
                    grisline[x].alpha = line[x].alpha;
                }
                pixelscolor = (char *) pixelscolor + infocolor.stride;
                pixelsgris = (char *) pixelsgris + infogris.stride;
            }
        LOGI("unlocking pixels");
        AndroidBitmap_unlockPixels(env, bitmapcolor);
        AndroidBitmap_unlockPixels(env, bitmapgris);

}