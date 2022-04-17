package com.example.appportfolio.api.responses

import com.example.appportfolio.data.entities.Block
import com.example.appportfolio.data.entities.Noti

data class blocksResponse (
    val resultCode:Int,
    val blocks:List<Block>
        )
