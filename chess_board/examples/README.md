# Cách chạy & input mẫu

3 file dưới đây mô tả **cùng một board** ở 3 format khác nhau → chạy ra output giống hệt nhau
(bằng chứng "source × format độc lập, mọi format ra cùng model"):

- `board.notation` — compact notation
- `board.json` — JSON
- `board.yaml` — YAML

## Bước 1 — build launcher (một lần)

```bash
gradle installDist
# launcher: build/install/chess-board/bin/chess-board
```

## Bước 2 — chạy

Cú pháp: `chess-board --source <console|file> [--file <path>] --format <notation|yaml|json> --out <console|json>`

```bash
APP=build/install/chess-board/bin/chess-board

# notation từ file, in console
"$APP" --source file --file examples/board.notation --format notation --out console

# JSON từ file, in JSON
"$APP" --source file --file examples/board.json --format json --out json

# YAML từ stdin (paste/pipe), in console
cat examples/board.yaml | "$APP" --source console --format yaml --out console
```

## Mặc định

- `--source console`, `--format notation`, `--out console` nếu không truyền.
- `--source file` bắt buộc kèm `--file <path>`.

## Thử nghiệm lỗi (đều exit code ≠ 0, in "Input error: ..." ra stderr)

```bash
"$APP" --source file --file khong-ton-tai.json --format json      # file thiếu
echo '{ bad'      | "$APP" --source console --format json          # JSON hỏng
printf 'W: KE2, RE2\n' | "$APP" --source console --format notation # trùng ô
printf 'W: XE2\n'      | "$APP" --source console --format notation # piece letter lạ
```

## Ghi chú

- Nên chạy qua **launcher** (`installDist`) thay vì `gradle run` khi cần **stdin**, vì Gradle daemon
  không truyền stdin ổn định. `gradle run --args="..."` vẫn tốt cho input từ file.
