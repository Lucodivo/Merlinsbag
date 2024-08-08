package com.inasweaterpoorlyknit.core.ui

val topDrawables = arrayOf(
  R.drawable.army_jacket,
  R.drawable.denim_jacket,
  R.drawable.shirt,
)

val bottomDrawables = arrayOf(
  R.drawable.pants
)

val shoeDrawables = arrayOf(
  R.drawable.shoe,
  R.drawable.boot,
)

val accessoryDrawables = arrayOf(
  R.drawable.wallet,
  R.drawable.hat,
  R.drawable.phone,
)

val etcDrawables = arrayOf(
  R.drawable.cat,
  R.drawable.guitar,
  R.drawable.guy,
  R.drawable.kid,
)

val placeholderDrawables = arrayOf(
  *topDrawables,
  *shoeDrawables,
  *bottomDrawables,
  *accessoryDrawables,
  *etcDrawables,
)
val repeatedPlaceholderDrawables = arrayListOf(
  *placeholderDrawables,
  *placeholderDrawables,
  *placeholderDrawables,
  *placeholderDrawables,
  *placeholderDrawables,
)
