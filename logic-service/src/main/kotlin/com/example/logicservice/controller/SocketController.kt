package com.example.logicservice.controller

import com.corundumstudio.socketio.AckRequest
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIOServer
import com.corundumstudio.socketio.listener.ConnectListener
import com.corundumstudio.socketio.listener.DataListener
import com.corundumstudio.socketio.listener.DisconnectListener
import com.example.logicservice.entity.Message
import com.example.logicservice.service.LogicService
import com.example.logicservice.service.SocketService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SocketController(
   @Autowired  private val server : SocketIOServer,
   @Autowired private val socketService: SocketService,
   @Autowired private val gameService : LogicService
) {

    init {

        server.addConnectListener(onConnected())

        server.addDisconnectListener(onDisconnected())

        server.addEventListener("fire_missile", String::class.java, onChatReceived())
    }

    private fun onChatReceived(): DataListener<String> {
        return DataListener { senderClient: SocketIOClient?, data: String, _: AckRequest? ->

            try {

                val message = jacksonObjectMapper().readValue(data, Message::class.java)

                val result = gameService.shootAtPlayer(message.message)

                socketService.sendMessage(message.game, "missile_fired", senderClient!!, result)

            }catch (e : Exception){
                throw Exception(e)
            }
        }
    }

    private fun onConnected() : ConnectListener {

        return ConnectListener { client ->

            val game: String = client.handshakeData.getSingleUrlParam("game")

            val token: String = client.handshakeData.getSingleUrlParam("token")

            val shouldAdmitUserToLobby = gameService.admitPlayerIntoLobby(token)

            if(!shouldAdmitUserToLobby){
                client.disconnect()
            }

            client.joinRoom(game)

            println("client has joined game : game")
        }
    }

    private fun onDisconnected() : DisconnectListener {

        return DisconnectListener { client ->
            println(" client ${client.sessionId} has disconnected")
        }
    }
}