package chess.input;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * FORMAT: JSON. Shape: { "pieces": [ { "type","side","position" } ] }.
 * Chỉ khác YAML ở ObjectMapper (JSON factory mặc định).
 */
public final class JsonParser extends TreeFormatParser {

    public JsonParser() {
        super(new ObjectMapper());
    }

    @Override
    protected String formatName() {
        return "JSON";
    }
}
