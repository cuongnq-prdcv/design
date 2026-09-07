package chess.rules;

import chess.model.Board;
import chess.model.Move;
import chess.model.Piece;

import java.util.List;

/**
 * Luật di chuyển của một quân. Tách rời khỏi Piece (model thuần).
 * Engine gọi movesFor mà KHÔNG cần biết quân cụ thể — không switch(type).
 */
public interface MovementRule {
    List<Move> movesFor(Piece piece, Board board);
}
