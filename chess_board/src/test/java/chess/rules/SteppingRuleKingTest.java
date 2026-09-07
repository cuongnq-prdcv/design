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

class SteppingRuleKingTest {

    private static Square sq(String s) {
        return new Square(s.charAt(0) - 'A', s.charAt(1) - '1');
    }

    private final SteppingRule king = new SteppingRule(Direction.ALL_8);

    @Test
    void kingInCenterHasEightMoves() {
        Piece k = new Piece(PieceType.KING, Side.WHITE, sq("D4"));
        Board board = new Board(List.of(k));

        List<?> moves = king.movesFor(k, board);
        assertEquals(8, moves.size());
    }

    @Test
    void kingInCornerIsClampedToBoard() {
        Piece k = new Piece(PieceType.KING, Side.WHITE, sq("A1"));
        Board board = new Board(List.of(k));

        Set<String> targets = king.movesFor(k, board).stream()
                .map(m -> m.target().toString())
                .collect(Collectors.toSet());

        // góc A1 chỉ còn 3 ô: B1, A2, B2
        assertEquals(Set.of("B1", "A2", "B2"), targets);
    }

    @Test
    void kingDoesNotEnterFriendlyButCapturesEnemy() {
        Piece k = new Piece(PieceType.KING, Side.WHITE, sq("D4"));
        Piece friendly = new Piece(PieceType.PAWN, Side.WHITE, sq("D5"));
        Piece enemy = new Piece(PieceType.PAWN, Side.BLACK, sq("E5"));
        Board board = new Board(List.of(k, friendly, enemy));

        var moves = king.movesFor(k, board);
        Set<String> targets = moves.stream()
                .map(m -> m.target().toString())
                .collect(Collectors.toSet());

        assertFalse(targets.contains("D5")); // cùng phe: không vào
        assertTrue(targets.contains("E5"));  // địch: ăn được

        boolean e5IsCapture = moves.stream()
                .filter(m -> m.target().toString().equals("E5"))
                .findFirst().orElseThrow().capture();
        assertTrue(e5IsCapture);
    }
}
