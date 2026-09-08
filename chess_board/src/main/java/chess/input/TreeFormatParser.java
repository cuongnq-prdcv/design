package chess.input;

import chess.model.Piece;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Base cho các format cấu trúc-cây dùng Jackson (JSON, YAML). Cả hai chia sẻ CÙNG shape:
 *   { "pieces": [ { "type", "side", "position" } ] }
 * và chỉ khác nhau ở ObjectMapper (JSON factory vs YAML factory) — inject qua constructor.
 *
 * MỞ RỘNG: một format cây khác (ví dụ TOML) chỉ cần truyền ObjectMapper tương ứng.
 * Chỉ TẠO model; không validate board (BoardValidator lo). Lỗi cú pháp → ChessInputException.
 */
abstract class TreeFormatParser implements PieceFormatParser {

    private final ObjectMapper mapper;

    protected TreeFormatParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public final List<Piece> parse(String raw) {
        JsonNode root;
        try {
            root = mapper.readTree(raw);
        } catch (Exception e) {
            // Bọc lỗi cú pháp của Jackson thành lỗi domain rõ ràng.
            throw new ChessInputException(formatName() + " syntax error: " + e.getMessage(), e);
        }

        if (root == null || !root.has("pieces") || !root.get("pieces").isArray()) {
            throw new ChessInputException(
                    formatName() + " must have a top-level array field 'pieces'");
        }

        List<Piece> pieces = new ArrayList<>();
        for (JsonNode node : root.get("pieces")) {
            pieces.add(new Piece(
                    PieceFields.parseType(text(node, "type")),
                    PieceFields.parseSide(text(node, "side")),
                    PieceFields.parseSquare(text(node, "position"))));
        }
        return pieces;
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            throw new ChessInputException(
                    formatName() + " piece missing required field '" + field + "': " + node);
        }
        return v.asText();
    }

    /** Tên format để đưa vào thông điệp lỗi (JSON / YAML). */
    protected abstract String formatName();
}
