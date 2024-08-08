package com.inasweaterpoorlyknit.core.database.dao

import java.util.Date
import java.util.UUID

fun generateId() = UUID.randomUUID().toString()
fun generateTime() = Date().time