package exceptions

data class ReachedWaitLimitException(val limit: Int) : RuntimeException()