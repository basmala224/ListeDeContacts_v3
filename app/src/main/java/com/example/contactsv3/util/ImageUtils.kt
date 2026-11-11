package com.example.contactsv3.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.example.contactsv3.R

/**
 * Charge une photo réduite (inSampleSize) pour éviter les OutOfMemory avec
 * les photos de la caméra. En cas d'URI illisible : image par défaut, sans plantage.
 */
object ImageUtils {

    fun afficher(view: ImageView, uriString: String?, taillePx: Int = 400) {
        if (uriString.isNullOrBlank()) {
            view.setImageResource(R.drawable.ic_person)
            return
        }
        val bmp = try {
            decoder(view.context, Uri.parse(uriString), taillePx)
        } catch (e: Exception) {
            null
        }
        if (bmp != null) view.setImageBitmap(bmp) else view.setImageResource(R.drawable.ic_person)
    }

    private fun decoder(ctx: Context, uri: Uri, taille: Int) =
        ctx.contentResolver.let { cr ->
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            if (opts.outWidth <= 0 || opts.outHeight <= 0) return@let null
            var sample = 1
            while (opts.outWidth / (sample * 2) >= taille && opts.outHeight / (sample * 2) >= taille) {
                sample *= 2
            }
            val opts2 = BitmapFactory.Options().apply { inSampleSize = sample }
            cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts2) }
        }
}
