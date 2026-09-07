package chess.rules;

import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;
import chess.model.Square;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Luật cho quân trượt (rook/bishop/queen): trượt theo mỗi hướng tới khi bị chặn.
 * - Dừng trước quân cùng phe (không vào, không đi qua).
 * - Dừng TẠI quân địch đầu tiên và có thể ăn (capture).
 * Khác biệt giữa các quân chỉ là DANH SÁCH HƯỚNG được inject.
 */
public final class SlidingRule implements MovementRule {

    private final List<Direction> directions;

    public SlidingRule(List<Direction> directions) {
        this.directions = List.copyOf(directions);
    }

    @Override
    public List<Move> movesFor(Piece piece, Board board) {
        List<Move> moves = new ArrayList<>();
        for (Direction d : directions) {
            int f = piece.position().file() + d.df();
            int r = piece.position().rank() + d.dr();
            while (Square.onBoard(f, r)) {
                Square target = new Square(f, r);
                Optional<Piece> occupant = board.pieceAt(target);
                if (occupant.isEmpty()) {
                    moves.add(new Move(target, false));
                } else {
                    if (occupant.get().side() != piece.side()) {
                        moves.add(new Move(target, true)); // ăn quân địch
                    }
                    break; // bị chặn (cùng phe: không vào; khác phe: ăn rồi dừng)
                }
                f += d.df();
                r += d.dr();
            }
        }
        return moves;
    }
}
