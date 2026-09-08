package chess.input;

import chess.model.Piece;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Source × format trực giao: JSON text đến từ một InputSource bất kỳ vẫn parse được bằng
 * JsonParser qua đúng mắt xích `parser.parse(source.read())`.
 */
class SourceFormatOrthogonalTest {

    @Test
    void anyFormatFromAnySource() {
        String jsonText = """
                { "pieces": [ { "type": "KING", "side": "WHITE", "position": "E2" } ] }
                """;
        // "console" source (bơm stream) mang JSON — chứng minh source không ràng buộc format
        InputSource source = new ConsoleInputSource(
                new ByteArrayInputStream(jsonText.getBytes(StandardCharsets.UTF_8)));
        PieceFormatParser parser = new JsonParser();

        List<Piece> pieces = parser.parse(source.read());
        assertEquals(1, pieces.size());
        assertEquals("E2", pieces.get(0).position().toString());
    }
}
