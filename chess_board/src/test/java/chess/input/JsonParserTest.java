package chess.input;

import chess.model.Piece;
import chess.model.PieceType;
import chess.model.Side;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonParserTest {

    private final JsonParser parser = new JsonParser();

    @Test
    void parsesValidJson() {
        String json = """
                { "pieces": [
                  { "type": "KING",  "side": "WHITE", "position": "E2" },
                  { "type": "QUEEN", "side": "WHITE", "position": "D1" },
                  { "type": "PAWN",  "side": "BLACK", "position": "A7" }
                ] }
                """;
        List<Piece> pieces = parser.parse(json);
        assertEquals(3, pieces.size());
        assertEquals(PieceType.QUEEN, pieces.get(1).type());
        assertEquals(Side.WHITE, pieces.get(1).side());
        assertEquals("D1", pieces.get(1).position().toString());
    }

    @Test
    void rejectsMalformedJson() {
        assertThrows(ChessInputException.class, () -> parser.parse("{ bad json"));
    }
}
