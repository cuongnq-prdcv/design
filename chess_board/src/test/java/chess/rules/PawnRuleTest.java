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

class PawnRuleTest {

    private static Square sq(String s) {
        return new Square(s.charAt(0) - 'A', s.charAt(1) - '1');
    }

    private final PawnRule pawn = new PawnRule();

    private Set<String> targets(Piece p, Board b) {
        return pawn.movesFor(p, b).stream()
                .map(m -> m.target().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void whitePawnOnStartCanMoveOneOrTwo() {
        Piece p = new Piece(PieceType.PAWN, Side.WHITE, sq("E2"));
        Board b = new Board(List.of(p));
        assertEquals(Set.of("E3", "E4"), targets(p, b));
    }

    @Test
    void whitePawnNotOnStartMovesOneOnly() {
        Piece p = new Piece(PieceType.PAWN, Side.WHITE, sq("E3"));
        Board b = new Board(List.of(p));
        assertEquals(Set.of("E4"), targets(p, b));
    }

    @Test
    void pawnBlockedCannotMoveForward() {
        Piece p = new Piece(PieceType.PAWN, Side.WHITE, sq("E2"));
        Piece blocker = new Piece(PieceType.PAWN, Side.BLACK, sq("E3"));
        Board b = new Board(List.of(p, blocker));
        // bị chặn ngay trước mặt -> không đi thẳng, không có ăn chéo
        assertTrue(targets(p, b).isEmpty());
    }

    @Test
    void pawnTwoSquareBlockedBySecondSquare() {
        Piece p = new Piece(PieceType.PAWN, Side.WHITE, sq("E2"));
        Piece blocker = new Piece(PieceType.PAWN, Side.BLACK, sq("E4"));
        Board b = new Board(List.of(p, blocker));
        // E3 trống nhưng E4 bị chặn -> chỉ đi 1 ô
        assertEquals(Set.of("E3"), targets(p, b));
    }

    @Test
    void pawnCapturesDiagonallyNotForwardEnemy() {
        Piece p = new Piece(PieceType.PAWN, Side.WHITE, sq("E2"));
        Piece enemyLeft = new Piece(PieceType.PAWN, Side.BLACK, sq("D3"));
        Piece enemyRight = new Piece(PieceType.PAWN, Side.BLACK, sq("F3"));
        Board b = new Board(List.of(p, enemyLeft, enemyRight));

        List<Move> moves = pawn.movesFor(p, b);
        Set<String> t = moves.stream().map(m -> m.target().toString()).collect(Collectors.toSet());
        assertTrue(t.contains("D3"));
        assertTrue(t.contains("F3"));
        assertTrue(t.contains("E3")); // vẫn đi thẳng được
        assertTrue(t.contains("E4"));

        // D3/F3 phải là capture
        assertTrue(moves.stream().filter(m -> m.target().toString().equals("D3"))
                .findFirst().orElseThrow().capture());
    }

    @Test
    void pawnDoesNotCaptureFriendlyDiagonal() {
        Piece p = new Piece(PieceType.PAWN, Side.WHITE, sq("E2"));
        Piece friendly = new Piece(PieceType.PAWN, Side.WHITE, sq("D3"));
        Board b = new Board(List.of(p, friendly));
        assertFalse(targets(p, b).contains("D3"));
    }

    @Test
    void blackPawnMovesDownward() {
        Piece p = new Piece(PieceType.PAWN, Side.BLACK, sq("D7"));
        Board b = new Board(List.of(p));
        assertEquals(Set.of("D6", "D5"), targets(p, b));
    }
}
