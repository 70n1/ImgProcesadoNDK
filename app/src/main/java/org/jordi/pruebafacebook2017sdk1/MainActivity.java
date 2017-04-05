package org.jordi.pruebafacebook2017sdk1;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 10001;
    private String tag = "MainActivity";
    private Bitmap bitmapOriginal = null;
    private Bitmap bitmapGrises = null;
    private Bitmap bitmapSepia = null;
    private ImageView ivDisplay = null;
    private static Uri fileUri = null;

    private static Context context;

    private TextView tv_login_fb;
    private Button btnCompartirFoto;
    private Button btnCompartirFotoSD;
    private Button btnEnviarMensajeFB;

    private LoginButton loginButtonOficial;
    private CallbackManager elCallbackManagerDeFacebook;

    private ShareDialog elShareDialog;


    static {
        System.loadLibrary("imgprocesadondk");
    }

    public native void convertirGrises(Bitmap bitmapIn, Bitmap bitmapOut);

    public native void convertirSepia(Bitmap bitmapIn, Bitmap bitmapOut);

    public native void marco1(Bitmap bitmapIn, Bitmap bitmapOut);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        context = this;

        Log.i(tag, "Imagen antes de modificar");
        ivDisplay = (ImageView) findViewById(R.id.ivDisplay);
        BitmapFactory.Options options = new BitmapFactory.Options();
        // Asegurar que la imagen tiene 24 bits de color
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmapOriginal = BitmapFactory.decodeResource(this.getResources(), R.drawable.sampleimage, options);
        if (bitmapOriginal != null) ivDisplay.setImageBitmap(bitmapOriginal);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        loginButtonOficial = (LoginButton) findViewById(R.id.login_button);
        loginButtonOficial.setPublishPermissions("publish_actions");

        tv_login_fb = (TextView) findViewById(R.id.tv_facebookLogin);

        btnCompartirFoto = (Button) findViewById(R.id.btnCompartirFoto);
        btnCompartirFotoSD = (Button) findViewById(R.id.btnCompartirFotoSD);

        btnEnviarMensajeFB = (Button) findViewById(R.id.btnEnviarMensajeFB);

        this.elCallbackManagerDeFacebook = CallbackManager.Factory.create();
// registro un callback para saber cómo ha ido el login
        LoginManager.getInstance().registerCallback(this.elCallbackManagerDeFacebook,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
// App code
                        Toast.makeText(context, "Login onSuccess()", Toast.LENGTH_LONG).show();
                        tv_login_fb.setText("Autenticado: ");
                        Profile profile = Profile.getCurrentProfile();
                        if (profile != null) {
                            tv_login_fb.setText("Autenticado: " + profile.getName());
                        }
                        btnCompartirFoto.setEnabled(true);
                        btnCompartirFotoSD.setEnabled(true);
                        btnEnviarMensajeFB.setEnabled(true);
                        btnCompartirFoto.setEnabled(true);
                        actualizarDatosFacebook();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(context, "Login onCancel()", Toast.LENGTH_LONG).show();
                        tv_login_fb.setText("Sin autenticar.");

                        btnCompartirFoto.setEnabled(false);
                        btnCompartirFotoSD.setEnabled(false);
                        btnEnviarMensajeFB.setEnabled(false);
                        btnCompartirFoto.setEnabled(false);
                        actualizarDatosFacebook();
                    }

                    @Override
                    public void onError(FacebookException exception) {
// App code
                        Toast.makeText(context, "Login onError(): " + exception.getMessage(),
                                Toast.LENGTH_LONG).show();

                        Toast.makeText(context, "Login onCancel()", Toast.LENGTH_LONG).show();
                        tv_login_fb.setText("Error al autenticar.");
                        ;

                        btnCompartirFoto.setEnabled(false);
                        btnCompartirFotoSD.setEnabled(false);
                        btnEnviarMensajeFB.setEnabled(false);
                        btnCompartirFoto.setEnabled(false);
                        actualizarDatosFacebook();
                    }
                });
        actualizarDatosFacebook();

        this.elShareDialog = new ShareDialog(this);
        this.elShareDialog.registerCallback(this.elCallbackManagerDeFacebook, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                Toast.makeText(context, "Sharer onSuccess()", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(context, "Sharer onError(): " + error.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private AccessToken obtenerAccessToken() {
        return AccessToken.getCurrentAccessToken();
    }

    private void actualizarDatosFacebook() {
        AccessToken accessToken = this.obtenerAccessToken();
        if (accessToken == null) {
            Log.d("cuandrav.actualizarVent", "no hay sesion, deshabilito");
//
// sesion con facebook cerrada
//
            this.btnCompartirFoto.setEnabled(false);
            btnEnviarMensajeFB.setEnabled(false);
            btnCompartirFoto.setEnabled(false);
            btnCompartirFotoSD.setEnabled(false);
            this.tv_login_fb.setText("No autenticado");
            return;
        }
//
// sí hay sesión
//
        Log.d("cuandrav.actualizarVent", "hay sesion habilito");
        this.tv_login_fb.setEnabled(true);
        btnEnviarMensajeFB.setEnabled(true);
        btnCompartirFotoSD.setEnabled(true);
        btnCompartirFoto.setEnabled(true);
//
// averiguo los datos básicos del usuario acreditado
//
        Profile profile = Profile.getCurrentProfile();
        if (profile != null) {
            this.tv_login_fb.setText(profile.getName());
        }
//
// otra forma de averiguar los datos básicos:
// hago una petición con "graph api" para obtener datos del usuario acreditado
//
        /*
        this.obtenerPublicProfileConRequest_async(
// como es asíncrono he de dar un callback
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject datosJSON, GraphResponse response) {
//
// muestro los datos
//
                        String nombre= "nombre desconocido";
                        try {
                            nombre = datosJSON.getString("name");
                        } catch (org.json.JSONException ex) {
                            Log.d("cuandrav.actualizarVent", "callback de obtenerPublicProfileConRequest_async: excepcion: "
                                    + ex.getMessage());
                        } catch (NullPointerException ex) {
                            Log.d("cuandrav.actualizarVent", "callback de obtenerPublicProfileConRequest_async: excepcion: "
                                    + ex.getMessage());
                        }
                        elTextoDeBienvenida.setText("bienvenido 2017: " + nombre);
                    }
                });*/
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //todo ok
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.btnReset) {
            onResetImagen(null);
            return true;
        }
        if (id == R.id.btnConvert) {
            onConvertirGrises(null);
            return true;
        }
        if (id == R.id.btnConvertSepia) {
            onConvertirSepia(null);
            return true;
        }
        if (id == R.id.btnMarco1) {
            onMarco1(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    public void onMarco1(View v) {
        Log.i(tag, "Poner Marco1");
        bitmapGrises = Bitmap.createBitmap(bitmapOriginal.getWidth(), bitmapOriginal.getHeight(), Bitmap.Config.ARGB_8888);
        marco1(bitmapOriginal, bitmapGrises);
        ivDisplay.setImageBitmap(bitmapGrises);
    }

    public void onCapturarFoto(View view) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = Uri.fromFile(getOutputMediaFile());

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                bitmapOriginal = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
            } catch (Exception e) {
                Log.e(tag, "Excepción al acceder al fichero de la imagen capturada");
            }
            ivDisplay.setImageBitmap(bitmapOriginal);

            //ivDisplay.setImageURI(fileUri);

            //bitmapOriginal = (Bitmap) data.getExtras().get("data");
            //ivDisplay.setImageBitmap(bitmapOriginal);
        } else {
            this.elCallbackManagerDeFacebook.onActivityResult(requestCode, resultCode, data);
        }
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    }

    private boolean sePuedePublicar() {
//
// compruebo la red
//
        if (!this.hayRed()) {
            Toast.makeText(this, "¿no hay red? No puedo publicar", Toast.LENGTH_LONG).show();
            return false;
        }
//
// compruebo permisos
//
        if (!this.tengoPermisoParaPublicar()) {
            Toast.makeText(this, "¿no tengo permisos para publicar? Los pido.", Toast.LENGTH_LONG).show();
            // pedirPermisoParaPublicar();
            LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_actions"));
            return false;
        }
        return true;
    }


    private boolean tengoPermisoParaPublicar() {
        AccessToken accessToken = this.obtenerAccessToken();
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    private boolean hayRed() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
// http://stackoverflow.com/questions/15091591/post-on-facebook-wall-without-showing-dialog-on-android
// comprobar que estamos conetactos a internet, antes de hacer el login con
// facebook. Si no: da problemas.
    } // ()

    public void enviarTextoAFacebook_async(final String textoQueEnviar) {
//
// si no se puede publicar no hago nada
//
        if (!sePuedePublicar()) {
            return;
        }
//
// hago la petición a través del API Graph
//
        Bundle params = new Bundle();
        params.putString("message", textoQueEnviar);
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Toast.makeText(context, "Publicación realizada: " + textoQueEnviar, Toast.LENGTH_LONG).show();
                    }
                }
        );
        request.executeAsync();
    } // ()

    public void enviarFotoAFacebook_async(Bitmap image, String comentario) {
        Log.d("cuandrav.envFotoFBasync", "llamado");
        if (image == null) {
            Toast.makeText(this, "Enviar foto: la imagen está vacía.", Toast.LENGTH_LONG).show();
            Log.d("cuandrav.envFotoFBasync", "acabo porque la imagen es null");
            return;
        }
//
// si no se puede publicar no hago nada
//
        if (!sePuedePublicar()) {
            return;
        }
//
// convierto el bitmap a array de bytes
//
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
//image.recycle ();
        final byte[] byteArray = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
        }
//
// hago la petición a traves del Graph API
//
        Bundle params = new Bundle();
        params.putByteArray("source", byteArray); // bytes de la imagen
        params.putString("caption", comentario); // comentario
// si se quisiera publicar una imagen de internet: params.putString("url", "{image-url}");
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/photos",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Toast.makeText(context, "" + byteArray.length + " Foto enviada: " + response.toString(), Toast.LENGTH_LONG).show();
//textoConElMensaje.setText(response.toString());
                    }
                }
        );
        request.executeAsync();
    } // ()

    public void onEnviarMensajeFB(View view) {
        enviarTextoAFacebook_async("Texto de prueba para compartir en facebook (AMARTIN)");
    }

    public void onCompartirFoto(View view) {
        enviarFotoAFacebook_async(((BitmapDrawable) ivDisplay.getDrawable()).getBitmap(), "Enviando foto a Facebook (AMARTIN)");
    }

    private void publicarFotoConShareDialog() {
        // https://developers.facebook.com/docs/android/share -> Using the Share Dialog
        if (!puedoUtilizarShareDialogParaPublicarFoto()) {
            return;
        }

        // // cojo una imagen directamente de los recursos
        // para publicarla //
        //

        //Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.com_facebook_logo);
        Bitmap image = ((BitmapDrawable) ivDisplay.getDrawable()).getBitmap();
        // // monto la petición //
        SharePhoto photo = new SharePhoto.Builder().setBitmap(image).build();
        SharePhotoContent content = new SharePhotoContent.Builder().addPhoto(photo).build();
        this.elShareDialog.show(content);
    } // ()

    private boolean puedoUtilizarShareDialogParaPublicarFoto() {
        return ShareDialog.canShow(SharePhotoContent.class);
    }


    public void onCompartirFotoSD(View view) {
        publicarFotoConShareDialog();
    }
}
