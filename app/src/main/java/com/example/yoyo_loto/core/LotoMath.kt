package com.example.yoyo_loto.core

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.ln

fun powBig(base: Int, exp: Int): BigInteger {
    var result = BigInteger.ONE
    var b = BigInteger.valueOf(base.toLong())
    var e = exp
    while (e > 0) {
        if (e and 1 == 1) {
            result = result.multiply(b)
        }
        b = b.multiply(b)
        e = e shr 1
    }
    return result
}

fun combBig(n: Int, k: Int): BigInteger {
    if (k < 0 || k > n) return BigInteger.ZERO
    var kk = k
    if (kk > n - kk) kk = n - kk
    var res = BigInteger.ONE
    for (i in 0 until kk) {
        res = res.multiply(BigInteger.valueOf((n - i).toLong()))
        res = res.divide(BigInteger.valueOf((i + 1).toLong()))
    }
    return res
}

fun logPow(base: Double, exp: Int): Double {
    return exp * ln(base)
}

fun powInt(base: Int, exp: Int): Int? {
    var result = 1L
    repeat(exp) {
        result *= base.toLong()
        if (result > Int.MAX_VALUE) return null
    }
    return result.toInt()
}

fun bigIntToSci(value: BigInteger, maxDigits: Int = 6): String {
    val s = value.toString()
    if (s.length <= maxDigits) return s
    val lead = s.take(maxDigits)
    val trimmed = lead.trimEnd('0')
    val mantissa = if (trimmed.isEmpty()) "1" else trimmed
    val exp = s.length - 1
    val decimals = if (mantissa.length > 1) {
        mantissa.first() + "." + mantissa.drop(1)
    } else {
        mantissa
    }
    return "$decimals" + "e$exp"
}

fun bigDecimalToSci(value: BigDecimal, maxDigits: Int = 6): String {
    val s = value.stripTrailingZeros().toPlainString()
    return if (s.length <= maxDigits) s else bigIntToSci(value.toBigInteger(), maxDigits)
}
