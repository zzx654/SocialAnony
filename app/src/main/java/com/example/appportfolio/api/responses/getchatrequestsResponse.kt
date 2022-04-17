package com.example.appportfolio.api.responses

import com.example.appportfolio.data.entities.ChatRequests

data class getchatrequestsResponse(
 val resultCode:Int,
 val requests:List<ChatRequests>
)