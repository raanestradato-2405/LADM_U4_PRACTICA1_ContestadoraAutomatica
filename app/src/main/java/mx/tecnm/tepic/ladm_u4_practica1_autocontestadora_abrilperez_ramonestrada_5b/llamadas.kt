package mx.tecnm.tepic.ladm_u4_practica1_autocontestadora_abrilperez_ramonestrada_5b

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class llamadas: BroadcastReceiver() {

    val baseRemota = FirebaseFirestore.getInstance()
    var telephonyManager: TelephonyManager? = null
    var status = false
    var tipo = ""
    var contacto = ""


    override fun onReceive(context: Context, intent: Intent?) {

            telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            val listenerTelefono: PhoneStateListener = object : PhoneStateListener() {

                override fun onCallStateChanged(state: Int, phoneNumber: String) {
                    super.onCallStateChanged(state, phoneNumber)
                    status = false

                    when (state) {
                        TelephonyManager.CALL_STATE_RINGING -> {

                            baseRemota.collection("listaDeContactos")
                                .whereEqualTo("telefono", phoneNumber)
                                .get()
                                .addOnSuccessListener { documents ->
                                    for (document in documents) {
                                        tipo = document.getString("tipo")!!
                                        contacto = document.getString("nombre")!!
                                    }

                                    if (tipo.equals("DESEADA")) {

                                        SmsManager.getDefault().sendTextMessage(
                                            phoneNumber,
                                            null,
                                            "HOLA, POR EL MOMENTO NO ESTOY DISPONIBLE, EN CUANTO TENGA OPORTUNIDAD ME COMUNICO CONTIGO",
                                            null,
                                            null
                                        )
                                        Toast.makeText(context, "Se envio en SMS", Toast.LENGTH_LONG).show()

                                    }
                                    if (tipo.equals("NO DESEADA")) {
                                        SmsManager.getDefault().sendTextMessage(phoneNumber,
                                            null,
                                            "NO DEVOLVERE TU LLAMADA, POR FAVOR NO INSISTAS!!",
                                            null,
                                            null
                                        )


                                        var datosInsertar = hashMapOf(
                                            "nombre" to contacto,
                                            "telefono" to phoneNumber,
                                            "fecha"     to Date()

                                        )
                                        baseRemota.collection("llamadasNoDeseadas")
                                            .add(datosInsertar)
                                            .addOnSuccessListener {
                                                Toast.makeText(context,"Insercion Llamada No Deseada",Toast.LENGTH_LONG)
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context,"Error de Insercion",Toast.LENGTH_LONG)
                                            }
                                           Toast.makeText(context, "Se envio en SMS", Toast.LENGTH_LONG).show()

                                    }

                                }

                        }
                    }
                }
            }

      if(!isLitening) {
        telephonyManager!!.listen(listenerTelefono, PhoneStateListener.LISTEN_CALL_STATE)
        isLitening = true
    }
    }
    companion object{
        var isLitening = false
    }

}