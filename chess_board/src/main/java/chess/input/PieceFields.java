package chess.input;

import chess.model.PieceType;
import chess.model.Side;
import chess.model.Square;

/**
 * Helper chuyển đổi GIÁ TRỊ ĐƠN LẺ (chuỗi → enum/Square) dùng chung cho các parser.
 *
 * Đây KHÔNG phải validate board (off-board/trùng ô là việc của BoardValidator). Ở đây chỉ:
 * - map tên type/side → enum (ném lỗi rõ nếu không nhận ra),
 * - map "E2" → Square (ném lỗi rõ nếu sai định dạng/ngoài bàn).
 *
 * Đặt chung một chỗ để 3 parser (notation/yaml/json) KHÔNG lặp logic chuyển đổi & thông điệp lỗi.
 */
final class PieceFields {

    private PieceFields() {
    }

    static PieceType parseType(String raw) {
        try {
            return PieceType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ChessInputException("Unknown piece type: '" + raw + "'", e);
        }
    }

    static Side parseSide(String raw) {
        try {
            return Side.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ChessInputException("Unknown side: '" + raw + "'", e);
        }
    }

    /** "E2" → Square(file=4, rank=1). Ném ChessInputException nếu sai định dạng/ngoài bàn. */
    static Square parseSquare(String raw) {
        String s = raw == null ? "" : raw.trim().toUpperCase();
        if (s.length() != 2) {
            throw new ChessInputException("Invalid position: '" + raw + "' (expect like 'E2')");
        }
        int file = s.charAt(0) - 'A';
        int rank = s.charAt(1) - '1';
        if (!Square.onBoard(file, rank)) {
            throw new ChessInputException("Position off-board: '" + raw + "'");
        }
        return new Square(file, rank);
    }
}
