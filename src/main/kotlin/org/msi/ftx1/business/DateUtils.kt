package org.msi.ftx1.business

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


//val LocalDateTime.asMillis get() = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000
//val Long.asLocalDateTime get() = LocalDateTime.ofInstant(Date(this*1000).toInstant(), ZoneId.systemDefault())

val ZonedDateTime.millis get() = this.toInstant().toEpochMilli()
val ZonedDateTime.seconds get() = this.toInstant().toEpochMilli() / 1000
val Long.secondsToZonedDateTime get() = ZonedDateTime.ofInstant(Date(this*1000).toInstant(), ZoneId.systemDefault())
