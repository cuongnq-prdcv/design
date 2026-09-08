package chess.rules;

import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;

import java.util.List;

/**
 * Phase 1
 *
 * Luật di chuyển của một quân. Tách rời khỏi Piece (model thuần).
 * Engine gọi movesFor mà KHÔNG cần biết quân cụ thể — không switch(type).
 *
 * MỞ RỘNG (thêm loại luật mới, ví dụ quân "Archbishop"):
 *   1. Nếu là kiểu trượt/nhảy sẵn có → dùng lại SlidingRule/SteppingRule với bộ Direction khác.
 *   2. Nếu luật khác hẳn (như Pawn) → tạo class mới `implements MovementRule`.
 *   3. Đăng ký vào RULES map trong Main. KHÔNG đụng MoveEngine.
 */
public interface MovementRule {
    List<Move> movesFor(Piece piece, Board board);
}
