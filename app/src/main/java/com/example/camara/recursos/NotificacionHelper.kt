package com.example.camara.recursos

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** Recurso nativo: notificaciones locales de stock bajo. Solo se publica si el usuario concedió el permiso. */
object NotificacionHelper {
    private const val CANAL = "stock_bajo"

    fun requierePermiso(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun tienePermiso(context: Context): Boolean =
        !requierePermiso() ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun avisarStockBajo(context: Context, nombre: String, cantidad: String) {
        if (!tienePermiso(context)) return          // integración segura: sin permiso no se notifica
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CANAL, "Stock bajo", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Stock bajo: $nombre")
            .setContentText("Quedan $cantidad unidades, bajo la existencia mínima.")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(nombre.hashCode(), notificacion)
    }
}
