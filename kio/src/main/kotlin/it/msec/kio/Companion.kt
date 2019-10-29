package it.msec.kio

fun <R, E> KIO.Companion.unit(): KIO<R, E, Unit> = justR(Unit)

