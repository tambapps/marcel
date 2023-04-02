package com.tambapps.marcel.android.marshell.util

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback

/**
 * List allowing to listen to updates. Note that this list is NOT thread safe and should always be
 * modified from the main thread
 */
class ListenableList<T>: AbstractMutableList<T>() {

  private val list  = mutableListOf<T>()
  override val size: Int
    get() = list.size

  private val callbacks = mutableListOf<ListUpdateCallback>()

  fun registerCallback(callback: ListUpdateCallback) {
    callbacks.add(callback)
  }

  fun unregisterCallback(callback: ListUpdateCallback): Boolean {
    return callbacks.remove(callback)
  }


  override fun clear() {
    val previousSize = this.size
    list.clear()
    if (previousSize > 0) {
      callbacks.forEach {
        it.onRemoved(0, previousSize)
      }
    }
  }

  override fun addAll(elements: Collection<T>): Boolean {
    val startIndex = 0
    list.addAll(elements)
    callbacks.forEach {
      it.onInserted(startIndex, elements.size)
    }
    return true
  }

  override fun addAll(index: Int, elements: Collection<T>): Boolean {
    list.addAll(elements)
    callbacks.forEach {
      it.onInserted(index, elements.size)
    }
    return true
  }

  override fun add(index: Int, element: T) {
    list.add(index, element)
    callbacks.forEach {
      it.onInserted(index, 1)
    }
  }

  override fun add(element: T): Boolean {
    list.add(element)
    callbacks.forEach {
      it.onInserted(size - 1, 1)
    }
    return true
  }

  override fun get(index: Int) = list.get(index)

  override fun isEmpty() = list.isEmpty()

  override fun removeAt(index: Int): T {
    val t = list.removeAt(index)
    callbacks.forEach {
      it.onRemoved(index, 1)
    }
    return t
  }

  override fun set(index: Int, element: T): T {
    val t = list.set(index, element)
    callbacks.forEach {
      it.onChanged(index, 1, null)
    }
    return t
  }

  override fun retainAll(elements: Collection<T>): Boolean {
    val previousList = this.toList()
    if (!list.retainAll(elements)) return false
    val diffs = DiffUtil.calculateDiff(DiffCallback(previousList))
    callbacks.forEach {
      diffs.dispatchUpdatesTo(it)
    }
    return true
  }

  override fun removeAll(elements: Collection<T>): Boolean {
    val previousList = this.toList()
    if (!list.removeAll(elements)) return false
    val diffs = DiffUtil.calculateDiff(DiffCallback(previousList))
    callbacks.forEach {
      diffs.dispatchUpdatesTo(it)
    }
    return true
  }

  private inner class DiffCallback(private val oldList: List<T>): DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = this@ListenableList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
      oldList.getOrNull(oldItemPosition) == this@ListenableList.getOrNull(newItemPosition)

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
      areItemsTheSame(oldItemPosition, newItemPosition)
  }

  override fun remove(element: T): Boolean {
    val i = indexOf(element)
    if (i < 0) return false
    list.removeAt(i)
    callbacks.forEach {
      it.onRemoved(i, 1)
    }
    return true
  }

  override fun lastIndexOf(element: T) = list.lastIndexOf(element)

  override fun indexOf(element: T) = list.indexOf(element)

  override fun containsAll(elements: Collection<T>) = list.containsAll(elements)

  override fun contains(element: T) = list.contains(element)

}