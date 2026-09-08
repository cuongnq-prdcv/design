package chess.input;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * SOURCE: đọc toàn bộ stdin (đến EOF) thành String.
 * InputStream được inject qua constructor → test được với ByteArrayInputStream (không cần stdin thật).
 */
public final class ConsoleInputSource implements InputSource {

    private final InputStream in;

    /** Production: dùng System.in. */
    public ConsoleInputSource() {
        this(System.in);
    }

    /** Testable: bơm InputStream tuỳ ý. */
    public ConsoleInputSource(InputStream in) {
        this.in = in;
    }

    @Override
    public String read() {
        try {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ChessInputException("Cannot read from console (stdin)", e);
        }
    }
}
