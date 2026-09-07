package chess.model;

import java.util.List;

/**
 * Tập kết quả cho cả bàn cờ. Data thuần — KHÔNG có toJson()/toXml().
 * Việc định dạng thuộc về ResultWriter (tách format khỏi model).
 */
public record ResultSet(List<PieceResult> results) {
}
