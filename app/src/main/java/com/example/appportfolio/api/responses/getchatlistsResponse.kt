package com.example.appportfolio.api.responses

import com.example.appportfolio.data.entities.ChatRequests
import com.example.appportfolio.data.entities.Chatroom

data class getchatlistsResponse(
 val resultCode:Int,
 val rooms:List<Chatroom>
)