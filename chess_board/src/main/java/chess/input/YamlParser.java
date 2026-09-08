package chess.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * FORMAT: YAML. Shape:
 *   pieces:
 *     - { type: KING, side: WHITE, position: E2 }
 * Chỉ khác JSON ở ObjectMapper (YAMLFactory).
 */
public final class YamlParser extends TreeFormatParser {

    public YamlParser() {
        super(new ObjectMapper(new YAMLFactory()));
    }

    @Override
    protected String formatName() {
        return "YAML";
    }
}
