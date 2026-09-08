package chess.input;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileInputSourceTest {

    @Test
    void readsFileContent() throws Exception {
        Path tmp = Files.createTempFile("board", ".txt");
        try {
            Files.writeString(tmp, "W: KE2, RA1");
            String content = new FileInputSource(tmp).read();
            assertEquals("W: KE2, RA1", content);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void missingFileFails() {
        InputSource src = new FileInputSource(Path.of("/no/such/file-xyz.txt"));
        assertThrows(ChessInputException.class, src::read);
    }

    @Test
    void consoleReadsStdinStream() {
        InputSource src = new ConsoleInputSource(
                new ByteArrayInputStream("W: KE2".getBytes(StandardCharsets.UTF_8)));
        assertEquals("W: KE2", src.read());
    }
}
