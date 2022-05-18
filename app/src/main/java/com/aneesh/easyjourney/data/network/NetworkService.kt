package com.aneesh.easyjourney.data.network

import com.aneesh.easyjourney.simulator.WebSocket
import com.aneesh.easyjourney.simulator.WebSocketListener

//Responsible for WebSocket provided for all the API calls
class NetworkService {

    fun createWebSocket(webSocketListener: WebSocketListener) : WebSocket{
        return WebSocket(webSocketListener)
    }

}