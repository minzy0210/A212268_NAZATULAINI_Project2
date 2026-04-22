package com.example.a212268_nazatulaini_lab1.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.example.a212268_nazatulaini_lab1.R

val Alice = FontFamily(
    Font(R.font.alice_regular)
)
val Oswald = FontFamily(
    Font(R.font.oswald_regular),
    Font(R.font.oswald_bold, FontWeight.Bold)
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Alice,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Oswald,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Alice,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Oswald,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
    // add more TextStyles as needed
)