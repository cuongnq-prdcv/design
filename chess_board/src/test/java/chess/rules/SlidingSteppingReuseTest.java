package chess.rules;

import chess.model.Board;
import chess.model.Piece;
import chess.model.PieceType;
import chess.model.Side;
import chess.model.Square;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Bishop/Queen dùng lại SlidingRule; Knight dùng lại SteppingRule — chỉ khác bộ hướng. */
class SlidingSteppingReuseTest {

    private static Square sq(String s) {
        return new Square(s.charAt(0) - 'A', s.charAt(1) - '1');
    }

    private Set<String> targets(MovementRule rule, Piece p, Board b) {
        return rule.movesFor(p, b).stream()
                .map(m -> m.target().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void bishopSlidesDiagonalsOnly() {
        MovementRule bishop = new SlidingRule(Direction.DIAGONAL);
        Piece b = new Piece(PieceType.BISHOP, Side.WHITE, sq("C1"));
        Board board = new Board(List.of(b));
        Set<String> t = targets(bishop, b, board);
        assertTrue(t.contains("A3"));
        assertTrue(t.contains("H6"));
        assertFalse(t.contains("C2")); // không đi thẳng
    }

    @Test
    void queenIsRookPlusBishop() {
        MovementRule queen = new SlidingRule(Direction.ALL_8);
        Piece q = new Piece(PieceType.QUEEN, Side.WHITE, sq("D4"));
        Board board = new Board(List.of(q));
        Set<String> t = targets(queen, q, board);
        // trên bàn trống, queen ở D4 có 27 nước (13 sliding thẳng + 14 chéo)
        assertEquals(27, t.size());
    }

    @Test
    void knightJumpsEightLShapes() {
        MovementRule knight = new SteppingRule(Direction.KNIGHT);
        Piece n = new Piece(PieceType.KNIGHT, Side.WHITE, sq("D4"));
        Board board = new Board(List.of(n));
        assertEquals(8, targets(knight, n, board).size());
    }

    @Test
    void knightJumpsOverBlockers() {
        MovementRule knight = new SteppingRule(Direction.KNIGHT);
        Piece n = new Piece(PieceType.KNIGHT, Side.WHITE, sq("B1"));
        // chèn quân xung quanh — knight vẫn nhảy qua
        Piece block1 = new Piece(PieceType.PAWN, Side.WHITE, sq("B2"));
        Piece block2 = new Piece(PieceType.PAWN, Side.WHITE, sq("C2"));
        Board board = new Board(List.of(n, block1, block2));
        Set<String> t = targets(knight, n, board);
        assertTrue(t.contains("A3"));
        assertTrue(t.contains("C3"));
        assertTrue(t.contains("D2"));
    }
}
