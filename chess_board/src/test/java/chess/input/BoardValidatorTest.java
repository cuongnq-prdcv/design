package chess.input;

import chess.model.Board;
import chess.model.Piece;
import chess.model.PieceType;
import chess.model.Side;
import chess.model.Square;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BoardValidatorTest {

    private static Square sq(String s) {
        return new Square(s.charAt(0) - 'A', s.charAt(1) - '1');
    }

    private final BoardValidator validator = new BoardValidator();

    @Test
    void rejectsDuplicateSquare() {
        List<Piece> pieces = List.of(
                new Piece(PieceType.KING, Side.WHITE, sq("E2")),
                new Piece(PieceType.ROOK, Side.WHITE, sq("E2")));
        assertThrows(ChessInputException.class, () -> validator.validate(pieces));
    }

    @Test
    void acceptsValidBoard() {
        List<Piece> pieces = List.of(
                new Piece(PieceType.KING, Side.WHITE, sq("E2")),
                new Piece(PieceType.ROOK, Side.WHITE, sq("A1")));
        Board board = validator.validate(pieces);
        assertEquals(2, board.pieces().size());
    }
}
