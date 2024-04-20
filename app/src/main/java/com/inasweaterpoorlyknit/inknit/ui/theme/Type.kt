package com.inasweaterpoorlyknit.inknit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.inasweaterpoorlyknit.inknit.R

val mouldyCheeseFamily = FontFamily(
        Font(R.font.mouldycheese_regular)
)

val amaticSCFamily = FontFamily(
        Font(R.font.amatic_sc_bold, FontWeight.Normal),
        Font(R.font.amatic_sc_regular, FontWeight.Bold)
)

val creepsterFamily = FontFamily(
        Font(R.font.creepster_regular)
)

val Typography = Typography(
        bodyLarge = TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp
        )
)