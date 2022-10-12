package com.vivalkm.bitfit

class Item(
    var id : Long,
    var name: String,
    var number: Float,
    var note: String,
    var isLiked: Int
) : java.io.Serializable