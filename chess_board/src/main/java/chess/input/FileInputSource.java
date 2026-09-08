package chess.input;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * SOURCE: đọc toàn bộ nội dung một file thành String.
 * Không biết file chứa notation/YAML/JSON — đó là việc của parser.
 */
public final class FileInputSource implements InputSource {

    private final Path path;

    public FileInputSource(Path path) {
        this.path = path;
    }

    @Override
    public String read() {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            // Bọc lỗi I/O thành lỗi domain rõ ràng (đề: "clear error").
            throw new ChessInputException("Cannot read input file: " + path, e);
        }
    }
}
