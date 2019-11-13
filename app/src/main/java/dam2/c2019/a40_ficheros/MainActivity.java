package dam2.c2019.a40_ficheros;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private TextView resultado;
    private static final String TAG = "MainActivity";
    private boolean hayAlmacenamientoExt = false;
    private boolean almacenamientoExtEscritura = false;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    //Layout que se asocia a view para poder hacer referencia a él en otros métodos
    private View mLayout;
    private String dirAlmacExt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.mLayout = findViewById(R.id.mainLayout);
        // Buscamos los componentes de la interfaz de usuario
        Button leerMemoriaBtn = (Button) findViewById(R.id.LeerMemoriaBtn);
        Button escribirMemBtn = (Button)findViewById(R.id.EscribirMemBtn);
        Button leerRawBtn = (Button)findViewById(R.id.leerRawBtn);
        Button escribirSDBtn = (Button)findViewById(R.id.escribirSDBtn);
        Button leerSDBtn = (Button)findViewById(R.id.leerSDBtn);
        Button leerDirBtn = (Button)findViewById(R.id.leerDirBtn);
        resultado = (TextView) findViewById(R.id.ResultadoLabel);
        // Subrayamos la etiqueta que hace de título
        TextView tituloLbl = (TextView) findViewById(R.id.tituloLbl);
        SpannableString contenido = new SpannableString(tituloLbl.getText());
        contenido.setSpan(new UnderlineSpan(), 0, contenido.length(), 0);
        tituloLbl.setText(contenido);

        // Buscamos si el almacenamiento externo está activo y si podemos escribir datos en él
        dirAlmacExt=compruebaAlmacenamientoExt();
        //Comprobamos que tenemos permiso
        if (hayAlmacenamientoExt) {
            //https://developer.android.com/training/permissions/requesting?hl=es-419
            comprobarPermisosSd();
        }
        else{
            escribirSDBtn.setEnabled(false);
            leerSDBtn.setEnabled(false);
        }
    }

    public void comprobarPermisosSd(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available,
            Snackbar.make(mLayout,
                    R.string.sd_permission_available,
                    Snackbar.LENGTH_SHORT).show();
        } else {
            // Permission is missing and must be requested.
            requestSDPermission();
        }
        // END_INCLUDE
    }

    public void requestSDPermission(){
        Log.i(TAG, "SD permission has NOT been granted. Requesting permission.");

        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            Snackbar.make(mLayout, R.string.sd_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }).show();

        } else {
            Snackbar.make(mLayout, R.string.sd_unavailable, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult.");
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Log.i(TAG, "permission granted.");
                Snackbar.make(mLayout, R.string.sd_permission_available,
                        Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                // Permission request was denied.
                Log.i(TAG, "permission denied.");
                Snackbar.make(mLayout, R.string.sd_permission_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    // Método que comprueba si el almacenamiento externo está activo y si se puede escribir en ella
    private String compruebaAlmacenamientoExt(){
        // Obtenemos el estado del almacenamiento externo del teléfono
        String estado = Environment.getExternalStorageState();
        String dirAlmac = ""; //el punto montaje de almacenamiento externo

        // La tarjeta está activa y se puede escribir en ella
        if (Environment.MEDIA_MOUNTED.equals(estado)) {
            hayAlmacenamientoExt = almacenamientoExtEscritura = true;
            resultado.append("\n\nEl teléfono dispone de almacenamiento externo conectado y se puede escribir en él.");
        } else
            // Sólo se puede leer el almacenamiento externo
            if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(estado)) {
                hayAlmacenamientoExt = true;
                almacenamientoExtEscritura = false;
                resultado.append("\n\nEl teléfono dispone de almacenamiento externo conectado pero no se puede escribir en él.");
            } else {
                // No se puede leer el almacenamiento externo
                hayAlmacenamientoExt = almacenamientoExtEscritura = false;
                resultado.append("\n\nEl teléfono no tiene ningún almacenamiento externo conectado.");
            }

        if (hayAlmacenamientoExt) {
            // Mostramos el directorio donde está el almacenamiento externo
            File dir = Environment.getExternalStorageDirectory();
            dirAlmac=dir.getAbsolutePath();
            resultado.append("\n\nDirectorio almacenamiento externo: "+dir);
        }
        return dirAlmac;
    } // end compruebaTarjetaSD

    public void leerMemoria(View v) {
        resultado.setText("");
        try
        {
            BufferedReader filein = new BufferedReader(
                    new InputStreamReader(openFileInput("fichero_interno.txt")));

            String texto = filein.readLine();
            resultado.append("- Abrimos archivo 'fichero_interno.txt'");
            resultado.append("\n\n- Leemos el contenido del fichero:\n");
            resultado.append(texto);
            filein.close();
            resultado.append("'\n\n- Cerramos el archivo");
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al leer fichero de memoria interna");
            resultado.append("Error al leer fichero en memoria interna");
        }
    }
    public void escribirMemoria(View v){
        resultado.setText("");
        try
        {
            OutputStreamWriter fileout=
                    new OutputStreamWriter(openFileOutput("fichero_interno.txt",
                            Context.MODE_PRIVATE));
            resultado.append("- Abrimos fichero 'fichero_interno.txt' para escribir datos en memoria interna");
            fileout.write("Texto escritor en memoria interna...");
            resultado.append("\n\n- Escribimos los datos");
            fileout.close();
            resultado.append("\n\n- Cerramos fichero");
        }
        catch (Exception excepcion)
        {
            Log.e("Fichero", "Error al escribir fichero en memoria interna");
            resultado.append("Error al escribir fichero en memoria interna");
        }
    }

    public void escribirSD(View v) {
        resultado.setText("");
        //Si la memoria externa está disponible y se puede escribir
        if (hayAlmacenamientoExt && almacenamientoExtEscritura) {
            try {
                // Creamos un directorio de prueba
                File directorio = new File(dirAlmacExt + "/ejemplo_ficheros");
                boolean e= directorio.mkdirs();
                resultado.append("- Creamos el directorio " + dirAlmacExt + "/ejemplo_ficheros");
                // Abrimos in fichero en el raíz de la tarjeta SD
                File fichero = new File(directorio, "prueba_sd.txt");
                OutputStreamWriter fout = new OutputStreamWriter(new FileOutputStream(fichero));
                resultado.append("\n\n- Abrimos fichero '" + dirAlmacExt + "/ejemplo_ficheros/fichero_externo.txt' para escritura en memoria externa");
                fout.write("Texto escrito en fichero en tarjeta SD...");
                resultado.append("\n\n- Escribimos los datos");
                fout.close();
                resultado.append("\n\n- Cerramos fichero");
            } catch (Exception ex) {
                Log.e("Ficheros", "Error al escribir fichero en memoria externa");
                resultado.append("Error al escribir fichero en memoria externa");
            }
        } else
            resultado.append("No hay almacenamiento externo disponible o no se puede escribir en él.");
    }

    public void leerSD(View v){
        resultado.setText("");
        try
        {
            File fichero = new File(dirAlmacExt + "/ejemplo_ficheros", "prueba_sd.txt");
            BufferedReader fin =
                    new BufferedReader(new InputStreamReader(new FileInputStream(fichero)));
            resultado.append("- Abrimos fichero '"+ dirAlmacExt + "/ejemplo_ficheros/fichero_externo.txt' para lectura de memoria externa");
            String texto = fin.readLine();
            resultado.append("\n\n- Leemos el contenido del fichero:\n");
            resultado.append(texto);
            fin.close();
            resultado.append("\n\n- Cerramos fichero");
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al leer fichero de memoria externa");
            resultado.append("Error al leer fichero de memoria externa");
        }
    }

    public void leerRaw(View v){
        resultado.setText("");
        String texto = "";
        try
        {
            InputStream ficheroraw = getResources().openRawResource(R.raw.prueba_raw);
            BufferedReader brin = new BufferedReader(new InputStreamReader(ficheroraw));
            resultado.append("- Abrimos fichero '/raw/prueba_raw.txt' para lectura de recurso aplicación");
            resultado.append("\n\n- Leemos el contenido del fichero:");
            while (true) {
                texto = brin.readLine();
                // Si ya no hay más líneas que leer hemos acabado de leer el fichero
                if (texto==null) break;
                //resultado.append("\n"+Html.fromHtml(texto));
                resultado.append("\n"+texto);
            } // end while
            ficheroraw.close();
            resultado.append("\n\n- Cerramos fichero");
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al leer fichero de recurso de aplicación");
            resultado.append("Error al leer fichero de recurso de aplicación");
        }
    }

    public void leerDirectorios(View v){
        resultado.setText("");
        try
        {
            // Leemos el directorio raíz de la aplicación
            File dir = getApplicationContext().getFilesDir();
            resultado.append("- Abrimos directorio " + dir + " para ver su contenido");
            String[] ficheros = dir.list();
            resultado.append("\n\n- Leemos el contenido del directorio:");
            for (int i=0; i<ficheros.length; i++){
                File subdir = new File("/"+ficheros[i]);
                if (subdir.isDirectory())
                    resultado.append("\n Subdirectorio: "+ficheros[i]);
                else resultado.append("\n Fichero: "+ficheros[i]);
            }
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al leer contenido directorio");
            resultado.append("Error al leer contenido directorio");
        }
    }
}





