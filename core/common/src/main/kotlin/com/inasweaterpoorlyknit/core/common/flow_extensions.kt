package com.inasweaterpoorlyknit.core.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun<T,R> Flow<List<T>>.listMap(mapFun: (T) -> R): Flow<List<R>> = map{ list -> list.map { mapFun(it) }}