package wiseboard.input;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import wiseboard.view.WiseOutput;

public class WiseInputPersistenceTest {

    private static final Path DB_DIR = Paths.get("db", "wiseSaying");
    private static final Path LAST_ID_FILE = DB_DIR.resolve("lastId.txt");
    private static final Path DATA_JSON_FILE = Paths.get("data.json");

    @AfterEach
    void Cleanup() {
        DeleteIfExists(DATA_JSON_FILE);
        DeleteDirectoryRecursively(DB_DIR);
    }

    @Test
    void 등록_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        옛날통닭 두마리
                        오상민
                        등록
                        콜라는 펩시로
                        오상민
                        종료
                        """
        );

        assertThat(result.exception()).isNull();

        assertThat(Files.exists(DB_DIR)).isTrue();
        assertThat(Files.exists(DB_DIR.resolve("1.json"))).isTrue();
        assertThat(Files.exists(DB_DIR.resolve("2.json"))).isTrue();
        assertThat(Files.exists(LAST_ID_FILE)).isTrue();

        String lastId = ReadText(LAST_ID_FILE);
        assertThat(lastId).isEqualTo("2");

        String json1 = ReadText(DB_DIR.resolve("1.json"));
        assertThat(json1).contains("\"id\": 1");
        assertThat(json1).contains("\"content\": \"옛날통닭 두마리\"");
        assertThat(json1).contains("\"author\": \"오상민\"");

        String json2 = ReadText(DB_DIR.resolve("2.json"));
        assertThat(json2).contains("\"id\": 2");
        assertThat(json2).contains("\"content\": \"콜라는 펩시로\"");
        assertThat(json2).contains("\"author\": \"오상민\"");
    }

    @Test
    void 로드_테스트() {
        AppResult firstRun = RunWiseInput(
                """
                        등록
                        옛날통닭 두마리
                        오상민
                        등록
                        콜라는 펩시로
                        오상민
                        종료
                        """
        );

        assertThat(firstRun.exception()).isNull();

        AppResult secondRun = RunWiseInput(
                """
                        목록
                        종료
                        """
        );

        assertThat(secondRun.exception()).isNull();

        String out = secondRun.output();
        assertThat(out).contains("번호 / 작가 / 명언");
        assertThat(out).contains("----------------------");

        Integer idx2 = IndexOf(out, "2 / 오상민 / 콜라는 펩시로");
        Integer idx1 = IndexOf(out, "1 / 오상민 / 옛날통닭 두마리");

        assertThat(idx2).isNotNull();
        assertThat(idx1).isNotNull();
        assertThat(idx2).isLessThan(idx1);
    }

    @Test
    void 수정_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        옛날통닭 두마리
                        오상민
                        수정?id=1
                        옛날통닭 한마리
                        오상민
                        종료
                        """
        );

        assertThat(result.exception()).isNull();

        String json1 = ReadText(DB_DIR.resolve("1.json"));
        assertThat(json1).contains("\"id\": 1");
        assertThat(json1).contains("\"content\": \"옛날통닭 한마리\"");
        assertThat(json1).contains("\"author\": \"오상민\"");
    }

    @Test
    void 삭제_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        옛날통닭 두마리
                        오상민
                        등록
                        콜라는 펩시로
                        오상민
                        삭제?id=1
                        종료
                        """
        );

        assertThat(result.exception()).isNull();

        assertThat(Files.exists(DB_DIR.resolve("1.json"))).isFalse();
        assertThat(Files.exists(DB_DIR.resolve("2.json"))).isTrue();

        String lastId = ReadText(LAST_ID_FILE);
        assertThat(lastId).isEqualTo("2");
    }

    @Test
    void 빌드_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        옛날통닭 두마리
                        오상민
                        등록
                        콜라는 펩시로
                        오상민
                        삭제?id=1
                        수정?id=2
                        콜라는 코카콜라
                        홍길동
                        빌드
                        종료
                        """
        );

        assertThat(result.exception()).isNull();

        String out = result.output();
        assertThat(out).contains("data.json 파일의 내용이 갱신되었습니다.");

        assertThat(Files.exists(DATA_JSON_FILE)).isTrue();

        String dataJson = ReadText(DATA_JSON_FILE);
        assertThat(dataJson).startsWith("[");
        assertThat(dataJson).contains("\"id\": 2");
        assertThat(dataJson).contains("\"content\": \"콜라는 코카콜라\"");
        assertThat(dataJson).contains("\"author\": \"홍길동\"");
        assertThat(dataJson).doesNotContain("\"id\": 1");
    }

    private AppResult RunWiseInput(String input) {
        PrintStream originalOut = System.out;
        java.io.InputStream originalIn = System.in;

        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        PrintStream testOut = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);

        ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

        Throwable thrown = null;

        try {
            System.setOut(testOut);
            System.setIn(testIn);

            WiseOutput wiseOutput = new WiseOutput();
            WiseInput wiseInput = new WiseInput(wiseOutput);
            wiseInput.Start();
        } catch (Throwable t) {
            thrown = t;
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }

        String outText = outputBuffer.toString(StandardCharsets.UTF_8);
        return new AppResult(outText, thrown);
    }

    private Integer IndexOf(String text, String token) {
        int idx = text.indexOf(token);

        if (idx < 0) {
            return null;
        }

        return idx;
    }

    private String ReadText(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void DeleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private void DeleteDirectoryRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                if (Files.isDirectory(p)) {
                    DeleteDirectoryRecursively(p);
                    continue;
                }

                DeleteIfExists(p);
            }
        } catch (IOException ignored) {
        }

        try {
            Files.deleteIfExists(dir);
        } catch (IOException ignored) {
        }
    }

    private record AppResult(String output, Throwable exception) {
    }
}