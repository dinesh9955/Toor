package com.wisam.taxi.interfaces

interface SocketListener {
    fun onSocketConnected()
    fun onSocketDisconnected()
    fun onSocketConnectionError()
    fun onSocketConnectionTimeOut()
}