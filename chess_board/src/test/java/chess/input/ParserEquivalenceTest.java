package chess.input;

import chess.model.Piece;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 3 format cùng nội dung → CÙNG một model. Chứng minh format độc lập, downstream mù format. */
class ParserEquivalenceTest {

    @Test
    void allFormatsProduceSameModel() {
        List<Piece> fromNotation = new NotationParser().parse("W: KE2, QD1\nB: PA7");
        List<Piece> fromJson = new JsonParser().parse("""
                { "pieces": [
                  { "type": "KING",  "side": "WHITE", "position": "E2" },
                  { "type": "QUEEN", "side": "WHITE", "position": "D1" },
                  { "type": "PAWN",  "side": "BLACK", "position": "A7" }
                ] }
                """);
        List<Piece> fromYaml = new YamlParser().parse("""
                pieces:
                  - { type: KING,  side: WHITE, position: E2 }
                  - { type: QUEEN, side: WHITE, position: D1 }
                  - { type: PAWN,  side: BLACK, position: A7 }
                """);

        assertEquals(fromNotation, fromJson);
        assertEquals(fromJson, fromYaml);
    }
}
