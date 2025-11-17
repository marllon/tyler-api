package com.tylerproject.util

import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.text.Charsets.UTF_8

object CryptoUtils {

    /** Gera SHA-256 hash de uma string */
    fun sha256(input: String): String {
        val bytes = input.toByteArray(UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /** Gera SHA-256 e converte para Int (para cálculo do vencedor) */
    fun sha256ToInt(input: String): Int {
        val hash = sha256(input)
        // Pega os primeiros 8 caracteres hex e converte para Int
        return hash.substring(0, 8).toLong(16).toInt().and(Int.MAX_VALUE)
    }

    /** Gera uma string aleatória hex de 64 caracteres (256 bits) Usada para entropy */
    fun generateRandomEntropy(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32) // 32 bytes = 256 bits
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Calcula número vencedor deterministicamente a partir da entropy Formula: (hash(entropy +
     * raffleId) mod totalTickets) + 1
     */
    fun calculateWinnerNumber(revealEntropy: String, raffleId: String, totalTickets: Int): Int {
        if (totalTickets <= 0) throw IllegalArgumentException("totalTickets deve ser maior que 0")

        val seed = revealEntropy + raffleId
        val hashInt = sha256ToInt(seed)
        return (hashInt % totalTickets) + 1
    }

    /** Verifica se a entropy revelada bate com o hash comprometido */
    fun verifyEntropy(revealEntropy: String, committedEntropyHash: String): Boolean {
        return sha256(revealEntropy) == committedEntropyHash
    }
}
