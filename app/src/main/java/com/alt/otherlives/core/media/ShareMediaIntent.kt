package com.alt.otherlives.core.media

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Keep media URI grants on both EXTRA_STREAM and ClipData for Sharesheet targets. */
internal object ShareMediaIntent {
    fun create(context: Context, uri: Uri, mimeType: String, message: String): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, message)
            clipData = ClipData.newUri(context.contentResolver, "ALT shared media", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
}
