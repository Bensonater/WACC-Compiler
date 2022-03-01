package backend.enums

enum class Condition {
    EQ,
    NE,
    GT,
    GE,
    LT,
    LE,
    CS,
    AL;

    override fun toString(): String {
        return if (this == AL) "" else name
    }
}