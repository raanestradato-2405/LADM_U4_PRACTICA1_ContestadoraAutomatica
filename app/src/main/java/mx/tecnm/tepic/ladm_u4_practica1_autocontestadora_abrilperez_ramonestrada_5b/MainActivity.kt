package mx.tecnm.tepic.ladm_u4_practica1_autocontestadora_abrilperez_ramonestrada_5b

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.CursorAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {

    // Permisos mensajes
    val siPermiso = 1
    val siPermisoRecibido = 2
    val siPermisoLlamada = 3
    val siPermisoEstado = 4

    //-----------------------

    var activar = false
    val baseRemota = FirebaseFirestore.getInstance()
    private var nTelephonyManager: TelephonyManager?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

       nTelephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val permission = ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE)
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_PHONE_STATE),1)
        }
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.RECEIVE_SMS), siPermisoRecibido)
        }
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.SEND_SMS), siPermiso)

        }
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_CALL_LOG), siPermisoLlamada)

        }

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_CONTACTS )!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_CONTACTS),1)

        }





        btnListaBlanca.setOnClickListener {
            insertarListaBlanca()

        }

        btnListaNegra.setOnClickListener {
            insertarListaNegra()

        }

        btnActivar.setOnClickListener {
            if(activar == false){

                Toast.makeText(this,"AUTOCONTESTADORA ACTIVADA", Toast.LENGTH_LONG).show()
                txtModo.setText("MODO AUTOCONTESTADORA: ACTIVADO ")
                activar = true

            } else{
                Toast.makeText(this,"AUTOCONTESTADORA DESACTIVADA", Toast.LENGTH_LONG).show()
                txtModo.setText("MODO AUTOCONTESTADORA: DESACTIVADO ")
                activar = false
            }
        }


    }
//---------------------------------------------------Almacenar en lista blanca -------------------------------------
    private fun insertarListaBlanca() {

        var datosInsertar = hashMapOf(
            "nombre" to txtNombre.text.toString(),
            "telefono" to txtNumero.text.toString(),
            "tipo"     to "DESEADA"

            )

        baseRemota.collection("listaDeContactos")
            .add(datosInsertar)
            .addOnSuccessListener {
                alerta("SE INSERTO CORRECTAMENTE")
            }
            .addOnFailureListener {
                mensaje("ERROR: ${it.message!!}")
            }
        txtNombre.setText("")
        txtNumero.setText("")
    }

//----------------------------------------------------------Almacena en lista negra -----------------------------------
private fun insertarListaNegra() {
    var datosInsertar = hashMapOf(
        "nombre" to txtNombre.text.toString(),
        "telefono" to txtNumero.text.toString(),
        "tipo"     to "NO DESEADA"

        )

    baseRemota.collection("listaDeContactos")
        .add(datosInsertar)
        .addOnSuccessListener {
            alerta("SE INSERTO CORRECTAMENTE")
        }
        .addOnFailureListener {
            mensaje("ERROR: ${it.message!!}")
        }
    txtNombre.setText("")
    txtNumero.setText("")

}

//----------------------------------------------------Llamadas no Deseadas ----------------------------------------------------

fun llemadasNoDeseadas(){
    if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_CALL_LOG)!= PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CALL_LOG),1)
    }
    else{
        var selection:String = CallLog.Calls.TYPE+"="+CallLog.Calls.MISSED_TYPE
        var cursor: Cursor? = null

        try {
            cursor = contentResolver.query(Uri.parse("content://call_log/calls"),null,selection,null,null)

            var nombre = ""
            var telefono = ""
            var fecha = ""

            while (cursor?.moveToNext()!!){
                nombre = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                telefono = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                fecha = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE))
            }

            var datosInsertar = hashMapOf(
                "nombre" to nombre,
                "telefono" to telefono,
                "fecha"     to Date(fecha.toLong())

            )

            baseRemota.collection("llamadasNoDeseadas")
                .add(datosInsertar)
                .addOnSuccessListener {
                    alerta("SE INSERTO CORRECTAMENTE")
                }
                .addOnFailureListener {
                    mensaje("ERROR: ${it.message!!}")
                }

        }catch (ex: Exception){
            Toast.makeText(this,ex.message,Toast.LENGTH_LONG).show()
        } finally {
            cursor?.close()
        }
    }
}


//-----------------------------------------------------------Alertas y mensajes --------------------------------------
    private fun alerta(s: String) {
        Toast.makeText(this,s, Toast.LENGTH_LONG).show()

    }

    private fun mensaje(s: String) {
        AlertDialog.Builder(this).setTitle("ATENCION")
            .setMessage(s)
            .setPositiveButton("OK"){d,i->}
            .show()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1){
            setTitle("PERMISO OTORGADO")
        }
    }



}