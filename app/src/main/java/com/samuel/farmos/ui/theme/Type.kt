package com.samuel.farmos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.samuel.farmos.R

private val FarmOsFontFamily = FontFamily(
    Font(R.font.roboto_variable, weight = FontWeight.Normal)
)

/** Use bundled Roboto so a device's custom display font does not change the app UI. */
val Typography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = FarmOsFontFamily),
        displayMedium = base.displayMedium.copy(fontFamily = FarmOsFontFamily),
        displaySmall = base.displaySmall.copy(fontFamily = FarmOsFontFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = FarmOsFontFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = FarmOsFontFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = FarmOsFontFamily),
        titleLarge = base.titleLarge.copy(fontFamily = FarmOsFontFamily),
        titleMedium = base.titleMedium.copy(fontFamily = FarmOsFontFamily),
        titleSmall = base.titleSmall.copy(fontFamily = FarmOsFontFamily),
        bodyLarge = base.bodyLarge.copy(fontFamily = FarmOsFontFamily),
        bodyMedium = base.bodyMedium.copy(fontFamily = FarmOsFontFamily),
        bodySmall = base.bodySmall.copy(fontFamily = FarmOsFontFamily),
        labelLarge = base.labelLarge.copy(fontFamily = FarmOsFontFamily),
        labelMedium = base.labelMedium.copy(fontFamily = FarmOsFontFamily),
        labelSmall = base.labelSmall.copy(fontFamily = FarmOsFontFamily)
    )
}
