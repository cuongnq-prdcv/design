package chess.input;

import chess.model.Board;
import chess.model.Piece;
import chess.model.Square;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Phase 4 — xem specs/phase-4.md (case 10-11: validation một chỗ).
 * NƠI DUY NHẤT validate BOARD từ danh sách Piece đã parse, rồi dựng Board.
 * KHÔNG lặp trong từng parser — mọi format đi qua đây.
 *
 * Kiểm tra hiện có:
 * - trùng ô (hai quân cùng một Square).
 * (off-board đã được chặn khi tạo Square trong PieceFields/Square constructor.)
 *
 * MỞ RỘNG (thêm luật board-level, ví dụ "đúng 1 vua mỗi phe", "tối đa 16 quân/phe"):
 *   thêm một bước kiểm tra Ở ĐÂY và ném ChessInputException — KHÔNG rải ra parser.
 */
public final class BoardValidator {

    public Board validate(List<Piece> pieces) {
        Set<Square> seen = new HashSet<>();
        for (Piece p : pieces) {
            if (!seen.add(p.position())) {
                throw new ChessInputException(
                        "Two pieces on the same square: " + p.position());
            }
        }
        // (checkpoint mở rộng: thêm luật board-level khác tại đây)
        return new Board(pieces);
    }
}
