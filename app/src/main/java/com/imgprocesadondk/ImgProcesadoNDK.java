package com.imgprocesadondk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ImgProcesadoNDK extends AppCompatActivity {
    private String tag = "ImgProcesadoNDK";
    private Bitmap bitmapOriginal = null;
    private Bitmap bitmapGrises = null;
    private Bitmap bitmapSepia = null;
    private ImageView ivDisplay = null;

    static {
        System.loadLibrary("imgprocesadondk");
    }

    public native void convertirGrises(Bitmap bitmapIn, Bitmap bitmapOut);
    public native void convertirSepia(Bitmap bitmapIn, Bitmap bitmapOut);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(tag, "Imagen antes de modificar");
        ivDisplay = (ImageView) findViewById(R.id.ivDisplay);
        BitmapFactory.Options options = new BitmapFactory.Options();
        // Asegurar que la imagen tiene 24 bits de color
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmapOriginal = BitmapFactory.decodeResource(this.getResources(), R.drawable.sampleimage, options);
        if (bitmapOriginal != null) ivDisplay.setImageBitmap(bitmapOriginal);
    }

    public void onResetImagen(View v) {
        Log.i(tag, "Resetear Imagen");
        ivDisplay.setImageBitmap(bitmapOriginal);
    }

    public void onConvertirGrises(View v) {
        Log.i(tag, "Conversion a escala de grises");
        bitmapGrises = Bitmap.createBitmap(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.ARGB_8888);
        convertirGrises(bitmapOriginal, bitmapGrises);
        ivDisplay.setImageBitmap(bitmapGrises);
    }

    public void onConvertirSepia(View v) {
        Log.i(tag, "Conversion a sepia");
        bitmapSepia = Bitmap.createBitmap(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.ARGB_8888);
        convertirSepia(bitmapOriginal, bitmapSepia);
        ivDisplay.setImageBitmap(bitmapSepia);
    }
}
