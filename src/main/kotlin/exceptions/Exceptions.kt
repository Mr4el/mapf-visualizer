package exceptions

object Exceptions {
    fun validPathForAgentNotFoundException(name: String): RuntimeException {
        return RuntimeException("Unable to find path for agent with name '$name'!")
    }

    fun agentHasReachedWaitLimitException(limit: Int): RuntimeException {
        return RuntimeException("Agent has reached the maximum allowed consecutive waiting actions! ($limit)")
    }

    fun reachedProblemSizeLimitException(limit: Int): RuntimeException {
        return RuntimeException("Sorry, but the current grid size limit is $limit cells.")
    }

    fun importFileFormatDoesNotSupportExpectedException(): RuntimeException {
        return RuntimeException("The imported file is not in the expected format.")
    }

    fun invalidMapSizeException(): RuntimeException {
        return RuntimeException("The actual graph size does not match the specified size.")
    }

    fun missingDataWhileImportingException(): RuntimeException {
        return RuntimeException("The actual file format does not match the specified one.")
    }

    fun objectOutsideTheMapException(): RuntimeException {
        return RuntimeException("One of the objects is outside the map.")
    }

    fun obstacleAndAgentPositionConflictException(): RuntimeException {
        return RuntimeException("The agent takes the obstacle position.")
    }

    fun agentAndAgentPositionConflictException(): RuntimeException {
        return RuntimeException("An agent takes the position of another agent.")
    }

    fun invalidAgentPathException(): RuntimeException {
        return RuntimeException("Agent has an invalid path.")
    }

    fun solutionHasConflictsException(): RuntimeException {
        return RuntimeException("Solution has conflicts")
    }

    fun missingMapFileException(): RuntimeException {
        return RuntimeException("The map file was not found in the same directory.")
    }
}
