package exceptions

object Exceptions {
    fun validPathForAgentNotFound(name: String): RuntimeException {
        return RuntimeException("Unable to find path for agent with name '$name'!")
    }

    fun agentHasReachedWaitLimit(limit: Int): RuntimeException {
        return RuntimeException("Agent has reached the maximum allowed consecutive waiting actions! ($limit)")
    }
}
