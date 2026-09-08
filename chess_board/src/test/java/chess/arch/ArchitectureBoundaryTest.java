package chess.arch;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FITNESS FUNCTION bảo vệ ranh giới DIP:
 * các package lõi (engine, rules, output, model) KHÔNG được import chess.input.
 * Input là chi tiết ngoại vi; lõi phải mù về "pieces đến từ đâu / định dạng gì".
 *
 * Nếu ai đó lỡ import chess.input vào lõi trong tương lai, test này sẽ đỏ.
 */
class ArchitectureBoundaryTest {

    private static final List<String> CORE_PACKAGES =
            List.of("engine", "rules", "output", "model");

    @Test
    void coreDoesNotDependOnInput() throws IOException {
        Path mainRoot = Path.of("src/main/java/chess");
        for (String pkg : CORE_PACKAGES) {
            Path dir = mainRoot.resolve(pkg);
            if (!Files.isDirectory(dir)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(dir)) {
                List<Path> offenders = files
                        .filter(p -> p.toString().endsWith(".java"))
                        .filter(ArchitectureBoundaryTest::importsInput)
                        .toList();
                assertTrue(offenders.isEmpty(),
                        "Core package '" + pkg + "' must not import chess.input; offenders: " + offenders);
            }
        }
    }

    private static boolean importsInput(Path javaFile) {
        try {
            return Files.readAllLines(javaFile).stream()
                    .anyMatch(l -> l.trim().startsWith("import chess.input"));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read " + javaFile, e);
        }
    }
}
