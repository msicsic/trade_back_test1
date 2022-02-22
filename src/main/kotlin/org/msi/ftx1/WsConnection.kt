package org.msi.ftx1

import org.http4k.websocket.Websocket
import java.util.*

class WsConnection(
    val ws: Websocket,
    val id: UUID = UUID.randomUUID()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as WsConnection
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}