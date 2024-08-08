package com.inasweaterpoorlyknit.merlinsbag.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.snapshots.StateObject

// Inefficient implementation, but delegating most of the implementation to the compose snapshot library
// devs allows us to avoid any sort of real future maintenance while allowing for a nice user interface
class SnapshotStateSet<K> private constructor(private val map: SnapshotStateMap<K, Unit> = mutableStateMapOf()) : StateObject by map, MutableSet<K> {
  override val size: Int get() = map.size
  override fun iterator() = map.keys.iterator()
  override fun add(element: K) = map.put(element, Unit) != null
  override fun remove(element: K) = map.remove(element) != null
  override fun addAll(elements: Collection<K>): Boolean {
    val keysToAdd = map.keys.filterNot { elements.contains(it) }
    return if(keysToAdd.isEmpty()){
      false
    } else {
      map.putAll(keysToAdd.associateWith{Unit})
      true
    }
  }
  override fun removeAll(elements: Collection<K>): Boolean =
    elements.fold(false){ acc, element -> remove(element) || acc }
  override fun retainAll(elements: Collection<K>): Boolean {
    val keysToRemove = map.keys.filterNot { elements.contains(it) }
    return removeAll(keysToRemove)
  }
  override fun clear() = map.clear()
  override fun isEmpty() = map.isEmpty()

  override fun contains(element: K): Boolean = map.contains(element)
  override fun containsAll(elements: Collection<K>): Boolean =
      elements.fold(true) { acc, element -> map.containsKey(element) && acc }

  companion object {
    fun <K> empty() = SnapshotStateSet<K>()
  }
}
fun <K> mutableStateSetOf() = SnapshotStateSet.empty<K>()
fun <K> mutableStateSetOf(vararg values: K) = SnapshotStateSet.empty<K>().apply { addAll(values) }
