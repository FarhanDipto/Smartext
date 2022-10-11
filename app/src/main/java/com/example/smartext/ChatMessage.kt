package com.example.smartext

class ChatMessage(val id: String, val text: String, val from_id: String, val to_id: String, val timestamp: Long){
    constructor(): this("", "", "", "", -1)
}