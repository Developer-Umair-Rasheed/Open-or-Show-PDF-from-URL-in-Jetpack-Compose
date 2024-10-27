package com.umairrasheed.showpdffromurl

// Importing necessary libraries for Android context, Compose UI, and coroutines
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@Composable
fun PdfView(
    url: String? = null // The URL of the PDF to be displayed
) {
    // Get the current context for file handling and displaying the PDF
    val context = LocalContext.current

    // State to track if the PDF is loading
    var isLoading by remember { mutableStateOf(true) }

    // State to hold the PDFView object
    var pdfView by remember { mutableStateOf<PDFView?>(null) }

    // This effect is triggered when the URL changes (or is first provided)
    LaunchedEffect(key1 = url) {
        // Call a function to retrieve the PDF from cache or download it from the URL
        val file = retrievePdfFromCacheOrUrl(context, url)

        // Once the file is retrieved, load it into the PDFView
        file?.let {
            withContext(Dispatchers.Main) { // Switch to the main thread to update the UI
                pdfView?.fromFile(it)
                    ?.scrollHandle(com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle(context)) // Enable scroll handle
                    ?.enableAntialiasing(true) // Improve rendering quality
                    ?.onLoad {
                        isLoading = false // Set loading state to false when the PDF is loaded
                    }
                    ?.load() // Load the PDF
            }
        }
    }

    // The UI of the PDF viewer
    Box(modifier = Modifier.fillMaxSize()) {
        // AndroidView is used to include a standard Android view in a Compose layout
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 30.dp), // Add padding to the view
            factory = {
                PDFView(context, null).also {
                    pdfView = it
                } // Initialize PDFView and store it in the state
            }
        )

        // Show a CircularProgressIndicator while the PDF is loading
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(100.dp) // Set size for the progress indicator
                    .padding(16.dp) // Add padding around the progress indicator
                    .align(Alignment.Center) // To make the circular progress at center
            )
        }
    }

    // Dispose of resources when the Composable leaves the screen
    DisposableEffect(Unit) {
        onDispose {
            pdfView?.recycle() // Recycle the PDFView to free up memory
        }
    }
}

// Function to retrieve a PDF file from the cache or download it if not cached
private suspend fun retrievePdfFromCacheOrUrl(context: Context, url: String?): File? {
    return withContext(Dispatchers.IO) { // Switch to the IO thread for network operations
        try {
            // Create or use a cache directory in the app's internal storage
            val cacheDir = File(context.cacheDir, "pdf_cache")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs() // Create the directory if it doesn't exist
            }

            // Extract the filename from the URL or use a default name
            // https://source.android.com/docs/compatibility/5.0/android-5.0-cdd.pdf
            val fileName = url?.substringAfterLast("/") ?: "temp.pdf"
            val cachedFile = File(cacheDir, fileName)
            /*
             pdf_cache
               source.android.com/docs/compatibility/5.0/android-5.0-cdd.pdf
            */

            // Check if the file is already in the cache
            if (cachedFile.exists()) {
                Log.d("PdfView", "Loading PDF from cache") // Log message for debugging
                return@withContext cachedFile // Return the cached file
            }

            // Open a connection to download the PDF from the URL
            val urlConnection: HttpURLConnection = (URL(url).openConnection() as HttpsURLConnection)

            // If the response code is 200 (OK), download the file
            if (urlConnection.responseCode == 200) {
                val inputStream = BufferedInputStream(urlConnection.inputStream) // Read the data
                val outputStream =
                    FileOutputStream(cachedFile) // This opens a file (represented by cachedFile) on your device where the data will be saved. FileOutputStream is used to write data to this file.

                // Copy the input stream to the output stream
                inputStream.copyTo(outputStream)
                outputStream.close()
                inputStream.close()

                Log.d("PdfView", "PDF cached successfully") // Log message for debugging
                return@withContext cachedFile // Return the cached file
            }
        } catch (e: Exception) {
            e.printStackTrace() // Print the exception if something goes wrong
        }
        null // Return null if the file could not be downloaded
    }
}