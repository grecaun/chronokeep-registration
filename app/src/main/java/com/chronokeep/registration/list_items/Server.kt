package com.chronokeep.registration.list_items

import java.net.InetAddress
import java.net.UnknownHostException

class Server(var name: String, var id: String, address: String?, var port: Int) {
    var address: InetAddress?

    init {
        try {
            this.address = InetAddress.getByName(address)
        } catch (ignored: UnknownHostException) {
            this.address = null
        }
    }
}