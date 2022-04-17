package com.example.appportfolio.api.responses

import com.example.appportfolio.data.entities.Voteresult

data class getvoteresultResponse (
    val resultCode:Int,
    val voteresult:List<Voteresult>
        )