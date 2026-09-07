package chess.rules;

import java.util.List;

/** Một hướng/offset (df, dr) trên bàn cờ. Dùng cho cả sliding lẫn stepping. */
public record Direction(int df, int dr) {

    /** 4 hướng thẳng (rook). */
    public static final List<Direction> ORTHOGONAL = List.of(
            new Direction(1, 0), new Direction(-1, 0),
            new Direction(0, 1), new Direction(0, -1));

    /** 4 hướng chéo (bishop). */
    public static final List<Direction> DIAGONAL = List.of(
            new Direction(1, 1), new Direction(1, -1),
            new Direction(-1, 1), new Direction(-1, -1));

    /** 8 hướng (queen / king). */
    public static final List<Direction> ALL_8 = concat(ORTHOGONAL, DIAGONAL);

    /** 8 offset chữ L của knight. */
    public static final List<Direction> KNIGHT = List.of(
            new Direction(1, 2), new Direction(2, 1),
            new Direction(-1, 2), new Direction(-2, 1),
            new Direction(1, -2), new Direction(2, -1),
            new Direction(-1, -2), new Direction(-2, -1));

    private static List<Direction> concat(List<Direction> a, List<Direction> b) {
        List<Direction> combined = new java.util.ArrayList<>(a);
        combined.addAll(b);
        return List.copyOf(combined);
    }
}
