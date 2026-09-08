package chess.input;

import chess.model.Piece;
import chess.model.PieceType;
import chess.model.Side;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotationParserTest {

    private final NotationParser parser = new NotationParser();

    @Test
    void parsesValidTokens() {
        List<Piece> pieces = parser.parse("W: KE2, RA1\nB: KE8");
        assertEquals(3, pieces.size());
        assertEquals(PieceType.KING, pieces.get(0).type());
        assertEquals(Side.WHITE, pieces.get(0).side());
        assertEquals("E2", pieces.get(0).position().toString());
        assertEquals(Side.BLACK, pieces.get(2).side());
    }

    @Test
    void knightLetterIsN() {
        List<Piece> pieces = parser.parse("W: NB1");
        assertEquals(PieceType.KNIGHT, pieces.get(0).type());
        assertEquals("B1", pieces.get(0).position().toString());
    }

    @Test
    void rejectsUnknownPieceLetter() {
        assertThrows(ChessInputException.class, () -> parser.parse("W: XE2"));
    }

    @Test
    void rejectsOffBoard() {
        assertThrows(ChessInputException.class, () -> parser.parse("W: KZ9"));
    }
}
