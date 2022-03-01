package backend.enums

enum class Condition {
    EQ,
    NE,
    GT,
    GE,
    LT,
    LE,
    CS,
    VS,
    AL;

    override fun toString(): String {
        return if (this == AL) "" else name
    }
}