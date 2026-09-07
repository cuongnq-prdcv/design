package chess.rules;

import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;
import chess.model.Side;
import chess.model.Square;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Luật cho Pawn (đơn giản hoá — không en passant, không phong cấp):
 * - Tiến 1 ô vào ô TRỐNG (White: +rank, Black: -rank).
 * - Tiến 2 ô từ hàng xuất phát nếu CẢ HAI ô trước đều trống.
 * - Ăn CHÉO tiến 1 ô khi ô đó có quân địch.
 * Để riêng vì luật khác hẳn sliding/stepping (hướng đi != hướng ăn).
 */
public final class PawnRule implements MovementRule {

    @Override
    public List<Move> movesFor(Piece piece, Board board) {
        List<Move> moves = new ArrayList<>();

        int dir = (piece.side() == Side.WHITE) ? 1 : -1;
        int startRank = (piece.side() == Side.WHITE) ? 1 : 6; // rank2 / rank7 (0-based)
        int file = piece.position().file();
        int rank = piece.position().rank();

        // Tiến 1 ô vào ô trống
        int oneRank = rank + dir;
        if (Square.onBoard(file, oneRank)) {
            Square one = new Square(file, oneRank);
            if (board.isEmpty(one)) {
                moves.add(new Move(one, false));

                // Tiến 2 ô từ hàng xuất phát nếu ô kế tiếp cũng trống
                int twoRank = rank + 2 * dir;
                if (rank == startRank && Square.onBoard(file, twoRank)) {
                    Square two = new Square(file, twoRank);
                    if (board.isEmpty(two)) {
                        moves.add(new Move(two, false));
                    }
                }
            }
        }

        // Ăn chéo tiến 1 ô (chỉ khi có quân địch)
        for (int df : new int[]{-1, 1}) {
            int cf = file + df;
            int cr = rank + dir;
            if (!Square.onBoard(cf, cr)) {
                continue;
            }
            Square diag = new Square(cf, cr);
            Optional<Piece> occupant = board.pieceAt(diag);
            if (occupant.isPresent() && occupant.get().side() != piece.side()) {
                moves.add(new Move(diag, true));
            }
        }

        return moves;
    }
}
