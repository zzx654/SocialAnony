package com.example.appportfolio.api.responses

import com.example.appportfolio.data.entities.RoomProfile

data class getRoomProfilesResponse (
    val resultCode:Int,
    val profiles:List<RoomProfile>
        )