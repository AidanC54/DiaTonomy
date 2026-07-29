package com.example.diatonomy.util

import java.security.MessageDigest

fun sha1(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}