package backend.enums

enum class Condition {
    EQ,
    NEQ,
    GT,
    GTE,
    LT,
    LTE,
    AL;

    override fun toString(): String {
        return if (this == AL) "" else name
    }
}