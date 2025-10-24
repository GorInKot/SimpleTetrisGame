package com.example.simpletetrisgame.helper

fun array2dOfByte(
    sizeOuter: Int, sizeInner: Int
): Array<ByteArray> = Array(sizeOuter) {
    ByteArray(sizeInner)
}