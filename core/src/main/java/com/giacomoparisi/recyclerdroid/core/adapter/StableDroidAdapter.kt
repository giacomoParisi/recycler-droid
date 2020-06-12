package com.giacomoparisi.recyclerdroid.core.adapter

import com.giacomoparisi.recyclerdroid.core.DroidAdapter
import com.giacomoparisi.recyclerdroid.core.StableDroidItem
import com.giacomoparisi.recyclerdroid.core.ViewHolderFactory

open class StableDroidAdapter<T : StableDroidItem>(
        vararg factories: ViewHolderFactory<T>
) : DroidAdapter<T>(*factories) {



    init {

        this.setHasStableIds(true)

    }

    override fun getItemId(position: Int): Long = getItems()[position].stableId(position)

}