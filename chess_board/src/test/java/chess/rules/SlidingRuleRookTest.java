package chess.rules;

import chess.model.Board;
import chess.model.Move;
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

class SlidingRuleRookTest {

    private static Square sq(String s) {
        return new Square(s.charAt(0) - 'A', s.charAt(1) - '1');
    }

    private final SlidingRule rook = new SlidingRule(Direction.ORTHOGONAL);

    @Test
    void rookOnEmptyBoardReachesEdges() {
        Piece r = new Piece(PieceType.ROOK, Side.WHITE, sq("A1"));
        Board board = new Board(List.of(r));

        Set<String> targets = rook.movesFor(r, board).stream()
                .map(m -> m.target().toString())
                .collect(Collectors.toSet());

        // dọc A1: A2..A8 (7), ngang A1: B1..H1 (7) = 14 nước
        assertEquals(14, targets.size());
        assertTrue(targets.contains("A8"));
        assertTrue(targets.contains("H1"));
    }

    @Test
    void rookStopsBeforeFriendlyAndDoesNotEnter() {
        Piece r = new Piece(PieceType.ROOK, Side.WHITE, sq("A1"));
        Piece friendly = new Piece(PieceType.KING, Side.WHITE, sq("A4"));
        Board board = new Board(List.of(r, friendly));

        Set<String> targets = rook.movesFor(r, board).stream()
                .map(m -> m.target().toString())
                .collect(Collectors.toSet());

        // dọc chỉ tới A2, A3 (dừng trước A4 cùng phe)
        assertTrue(targets.contains("A2"));
        assertTrue(targets.contains("A3"));
        assertFalse(targets.contains("A4"));
        assertFalse(targets.contains("A5"));
    }

    @Test
    void rookCapturesFirstEnemyThenStops() {
        Piece r = new Piece(PieceType.ROOK, Side.WHITE, sq("A1"));
        Piece enemy = new Piece(PieceType.ROOK, Side.BLACK, sq("A4"));
        Board board = new Board(List.of(r, enemy));

        List<Move> moves = rook.movesFor(r, board);
        Set<String> targets = moves.stream()
                .map(m -> m.target().toString())
                .collect(Collectors.toSet());

        assertTrue(targets.contains("A2"));
        assertTrue(targets.contains("A3"));
        assertTrue(targets.contains("A4"));   // ăn được
        assertFalse(targets.contains("A5"));  // dừng sau khi ăn

        Move captureMove = moves.stream()
                .filter(m -> m.target().toString().equals("A4"))
                .findFirst().orElseThrow();
        assertTrue(captureMove.capture());
    }
}
