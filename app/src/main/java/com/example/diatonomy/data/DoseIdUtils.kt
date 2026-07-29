package com.example.diatonomy.data

import net.cacheux.nvplib.data.InsulinDose

fun InsulinDose.deriveId(serial: String): String {
    val bucketMs = 300_000L
    val roundedTime = ((time + bucketMs / 2) / bucketMs) * bucketMs
    return "$serial-$roundedTime-$units-$flags"
}