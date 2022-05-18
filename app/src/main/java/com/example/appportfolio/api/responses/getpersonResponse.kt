package com.example.appportfolio.api.responses

import com.example.appportfolio.data.entities.Person

data class getpersonResponse (
    val resultCode:Int,
    val persons:List<Person>

        )