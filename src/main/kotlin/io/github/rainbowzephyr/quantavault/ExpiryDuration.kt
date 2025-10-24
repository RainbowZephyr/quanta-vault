package io.github.rainbowzephyr.quantavault

import java.time.temporal.ChronoUnit

@JvmRecord
data class ExpiryDuration(
    val value: Long,
    val unit: ChronoUnit,
)
