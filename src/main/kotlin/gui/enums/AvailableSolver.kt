package gui.enums

enum class AvailableSolver(name: String) {
    CBS("CBS");

    companion object {
        fun fromName(solverName: String): AvailableSolver {
            return AvailableSolver.values().first { it.name == solverName }
        }
    }
}
