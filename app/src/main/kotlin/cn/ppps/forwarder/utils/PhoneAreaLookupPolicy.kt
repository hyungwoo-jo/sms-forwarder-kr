package cn.ppps.forwarder.utils

/** Keeps the opt-in check ahead of any phone-area network request. */
object PhoneAreaLookupPolicy {
    fun resolve(enabled: Boolean, fallback: String, lookup: () -> String): String =
        if (enabled) lookup() else fallback
}
