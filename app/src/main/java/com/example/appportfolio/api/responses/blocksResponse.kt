package com.example.appportfolio.api.responses

import com.example.appportfolio.data.entities.Block

data class blocksResponse (
    val resultCode:Int,
    val blocks:List<Block>
        )
