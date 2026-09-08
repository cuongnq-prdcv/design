package chess.rules;

import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;
import chess.model.Square;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Phase 1 (King) + Phase 2 (Knight) — xem specs/phase-1.md, specs/phase-2.md.
 *
 * Luật cho quân bước 1 nhịp theo offset cố định (king/knight).
 * - Không vào ô có quân cùng phe.
 * - Vào ô trống, hoặc ăn quân địch.
 * Khác biệt giữa king và knight chỉ là DANH SÁCH OFFSET được inject.
 *
 * MỞ RỘNG: một quân "nhảy" kiểu mới = KHÔNG viết class mới, chỉ truyền bộ offset khác khi đăng ký
 * trong Main (King=ALL_8, Knight=KNIGHT). Lưu ý: quân nhảy KHÔNG bị chặn giữa đường (khác sliding).
 */
public final class SteppingRule implements MovementRule {

    private final List<Direction> offsets;

    public SteppingRule(List<Direction> offsets) {
        this.offsets = List.copyOf(offsets);
    }

    @Override
    public List<Move> movesFor(Piece piece, Board board) {
        List<Move> moves = new ArrayList<>();
        for (Direction d : offsets) {
            int f = piece.position().file() + d.df();
            int r = piece.position().rank() + d.dr();
            if (!Square.onBoard(f, r)) {
                continue;
            }
            Square target = new Square(f, r);
            Optional<Piece> occupant = board.pieceAt(target);
            if (occupant.isEmpty()) {
                moves.add(new Move(target, false));
            } else if (occupant.get().side() != piece.side()) {
                moves.add(new Move(target, true)); // ăn quân địch
            }
            // cùng phe: bỏ qua
        }
        return moves;
    }
}
