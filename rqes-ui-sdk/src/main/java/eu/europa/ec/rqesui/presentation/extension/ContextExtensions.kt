package eu.europa.ec.rqesui.presentation.extension

import android.content.Context
import android.content.Intent
import android.net.Uri
import eu.europa.ec.rqesui.presentation.ui.container.EudiRQESContainer

fun Context.finish() {
    (this as? EudiRQESContainer)?.finish()
}

/**
 * Parses a string url and sends the Action View Intent.
 *
 * @param uri the url to parse.
 */
fun Context.openUrl(uri: Uri) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (_: Exception) {
    }
}
