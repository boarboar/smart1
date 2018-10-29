package com.boar.smartserver.db

import java.util.HashMap

class MapDbSensor(val map: MutableMap<String, Any?>) {
    var _id: Long by map
    var description: String by map

    constructor(id: Long, description: String)
            : this(HashMap()) {
        this._id = id
        this.description = description
    }
}