package chess.model;

/**
 * Một ô trên bàn cờ 8x8.
 * file: 0..7 tương ứng A..H; rank: 0..7 tương ứng 1..8.
 * Model thuần — không I/O, không luật đi.
 */
public record Square(int file, int rank) {

    public Square {
        if (!onBoard(file, rank)) {
            throw new IllegalArgumentException(
                    "Square off-board: file=" + file + " rank=" + rank);
        }
    }

    /** Kiểm tra toạ độ có nằm trong bàn cờ không (dùng trước khi tạo Square). */
    public static boolean onBoard(int file, int rank) {
        return file >= 0 && file < 8 && rank >= 0 && rank < 8;
    }

    /** Ký hiệu bàn cờ, ví dụ (4,1) -> "E2". */
    @Override
    public String toString() {
        return "" + (char) ('A' + file) + (char) ('1' + rank);
    }
}
