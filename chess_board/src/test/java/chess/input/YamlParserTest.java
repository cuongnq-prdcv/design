package chess.input;

import chess.model.Piece;
import chess.model.PieceType;
import chess.model.Side;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlParserTest {

    private final YamlParser parser = new YamlParser();

    @Test
    void parsesValidYaml() {
        String yaml = """
                pieces:
                  - { type: KING,   side: WHITE, position: E2 }
                  - { type: QUEEN,  side: WHITE, position: D1 }
                  - { type: PAWN,   side: BLACK, position: A7 }
                """;
        List<Piece> pieces = parser.parse(yaml);
        assertEquals(3, pieces.size());
        assertEquals(PieceType.KING, pieces.get(0).type());
        assertEquals(Side.BLACK, pieces.get(2).side());
        assertEquals("A7", pieces.get(2).position().toString());
    }

    @Test
    void rejectsMalformedYaml() {
        // thụt lề sai / cấu trúc không có 'pieces'
        assertThrows(ChessInputException.class,
                () -> parser.parse("not_pieces:\n  - broken: ["));
    }
}
