package com.umairrasheed.showpdffromurl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ShowPdfScreen() {

    Scaffold(
        content = {
            // Simple box which shows the pdf
            Box(modifier = Modifier.padding(it)) {
                PdfView(
                    "https://source.android.com/docs/compatibility/5.0/android-5.0-cdd.pdf"
                )
            }
        },
    )

}