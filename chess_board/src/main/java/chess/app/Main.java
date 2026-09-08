package chess.app;

import chess.engine.MoveEngine;
import chess.input.BoardValidator;
import chess.input.ChessInputException;
import chess.input.ConsoleInputSource;
import chess.input.FileInputSource;
import chess.input.InputSource;
import chess.input.JsonParser;
import chess.input.NotationParser;
import chess.input.PieceFormatParser;
import chess.input.YamlParser;
import chess.model.Board;
import chess.model.Piece;
import chess.model.PieceType;
import chess.model.ResultSet;
import chess.output.ConsoleWriter;
import chess.output.JsonWriter;
import chess.output.ResultWriter;
import chess.rules.Direction;
import chess.rules.MovementRule;
import chess.rules.PawnRule;
import chess.rules.SlidingRule;
import chess.rules.SteppingRule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Composition root — nơi DUY NHẤT ráp mọi concretion. Phần còn lại NHẬN dependency, không tự new.
 *
 * Ba trục đều chọn qua registry + cờ CLI:
 *   --source console|file   (--file <path> khi source=file)
 *   --format notation|yaml|json
 *   --out    console|json
 *
 * MỞ RỘNG: thêm 1 nguồn/định dạng/format-output = thêm 1 dòng vào registry tương ứng bên dưới.
 * Lỗi input hội tụ về ChessInputException và được bắt Ở ĐÂY (in stderr + exit ≠ 0).
 */
public final class Main {

    // ---- REGISTRY: điểm mở rộng cho 3 trục. Thêm implementation = thêm 1 dòng. ----

    private static final Map<String, Supplier<InputSource>> SOURCES = Map.of(
            "console", ConsoleInputSource::new
            // "file" xử lý riêng vì cần --file <path> (xem resolveSource)
    );

    private static final Map<String, Supplier<PieceFormatParser>> PARSERS = Map.of(
            "notation", NotationParser::new,
            "yaml", YamlParser::new,
            "json", JsonParser::new
            // MỞ RỘNG: "fen", FenParser::new
    );

    private static final Map<String, Supplier<ResultWriter>> WRITERS = Map.of(
            "console", ConsoleWriter::new,
            "json", JsonWriter::new
            // MỞ RỘNG: "xml", XmlWriter::new / "text", TextWriter::new
    );

    // Bảng luật đi — thêm quân = thêm 1 dòng (engine KHÔNG đổi).
    private static final Map<PieceType, MovementRule> RULES = Map.of(
            PieceType.ROOK, new SlidingRule(Direction.ORTHOGONAL),
            PieceType.BISHOP, new SlidingRule(Direction.DIAGONAL),
            PieceType.QUEEN, new SlidingRule(Direction.ALL_8),
            PieceType.KING, new SteppingRule(Direction.ALL_8),
            PieceType.KNIGHT, new SteppingRule(Direction.KNIGHT),
            PieceType.PAWN, new PawnRule()
            // MỞ RỘNG: PieceType.ARCHBISHOP, new SlidingRule(DIAGONAL)+... (thêm enum + dòng này)
    );

    public static void main(String[] args) {
        try {
            run(args);
        } catch (ChessInputException e) {
            // ĐIỂM HỘI TỤ LỖI: mọi lỗi input/validation clear-error ra stderr, exit ≠ 0.
            System.err.println("Input error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O error while writing output: " + e.getMessage());
            System.exit(2);
        }
    }

    private static void run(String[] args) throws IOException {
        String sourceName = argValue(args, "--source", "console");
        String formatName = argValue(args, "--format", "notation");
        String outName = argValue(args, "--out", "console");

        // 1. Trục source × format ghép ở đây (ranh giới String) — mọi tổ hợp tự do.
        InputSource source = resolveSource(sourceName, args);
        PieceFormatParser parser = resolve(PARSERS, formatName, "--format");
        ResultWriter writer = resolve(WRITERS, outName, "--out");

        // 2. Pipeline: read → parse → validate → compute → write
        List<Piece> pieces = parser.parse(source.read());
        Board board = new BoardValidator().validate(pieces);
        ResultSet result = new MoveEngine(RULES).compute(board);

        StringBuilder sb = new StringBuilder();
        writer.write(result, sb);
        System.out.println(sb);
    }

    /** Source "file" cần --file <path>; các source khác lấy từ registry. */
    private static InputSource resolveSource(String name, String[] args) {
        if ("file".equals(name)) {
            String file = argValue(args, "--file", null);
            if (file == null) {
                throw new ChessInputException("--source file requires --file <path>");
            }
            return new FileInputSource(Path.of(file));
        }
        return resolve(SOURCES, name, "--source");
    }

    /** Tra registry; nếu key không tồn tại → clear error liệt kê các lựa chọn hợp lệ. */
    private static <T> T resolve(Map<String, Supplier<T>> registry, String key, String flag) {
        Supplier<T> supplier = registry.get(key);
        if (supplier == null) {
            throw new ChessInputException(
                    "Unknown " + flag + " '" + key + "'. Valid: " + registry.keySet());
        }
        return supplier.get();
    }

    /** Đọc giá trị cờ dạng "--key value"; trả default nếu không có. */
    private static String argValue(String[] args, String key, String def) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(key)) {
                return args[i + 1];
            }
        }
        return def;
    }

    private Main() {
    }
}
