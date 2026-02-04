package wiseboard.input;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import wiseboard.view.WiseOutput;

public class WiseInputTest {

    @Test
    void 등록_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        옛날통닭 두마리
                        오상민
                        종료
                        """
        );

        assertThat(result.exception()).isNull();

        String out = result.output();
        assertThat(out).contains("== 명언 앱 ==");
        assertThat(out).contains("명언 : ");
        assertThat(out).contains("작가 : ");
        assertThat(out).contains("1번 명언이 등록되었습니다.");
    }

    @Test
    void 등록_명언_공백_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        
                        오상민
                        """
        );

        assertThat(result.exception()).isInstanceOf(IllegalArgumentException.class);
        assertThat(result.exception().getMessage()).contains("[ERROR]");
    }

    @Test
    void 등록_명언_특수문자_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        ?!!@!##%!
                        오상민
                        """
        );

        assertThat(result.exception()).isInstanceOf(IllegalArgumentException.class);
        assertThat(result.exception().getMessage()).contains("[ERROR]");
    }

    @Test
    void 등록_작가_공백_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        옛날통닭 두마리
                        
                        """
        );

        assertThat(result.exception()).isInstanceOf(IllegalArgumentException.class);
        assertThat(result.exception().getMessage()).contains("[ERROR]");
    }

    @Test
    void 등록_작가_특수문자_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        옛날통닭 두마리
                        오상민?!?!
                        """
        );

        assertThat(result.exception()).isInstanceOf(IllegalArgumentException.class);
        assertThat(result.exception().getMessage()).contains("[ERROR]");
    }

    @Test
    void 목록_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        옛날통닭 두마리
                        오상민
                        등록
                        콜라는 펩시로
                        오상민
                        목록
                        종료
                        """
        );

        assertThat(result.exception()).isNull();

        String out = result.output();
        assertThat(out).contains("번호 / 작가 / 명언");
        assertThat(out).contains("----------------------");

        Integer idx2 = IndexOf(out, "2 / 오상민 / 콜라는 펩시로");
        Integer idx1 = IndexOf(out, "1 / 오상민 / 옛날통닭 두마리");

        assertThat(idx2).isNotNull();
        assertThat(idx1).isNotNull();
        assertThat(idx2).isLessThan(idx1);
    }

    @Test
    void 빈_목록_테스트() {
        AppResult result = RunWiseInput(
                """
                        목록
                        종료
                        """
        );

        assertThat(result.exception()).isNull();

        String out = result.output();
        assertThat(out).contains("번호 / 작가 / 명언");
        assertThat(out).contains("----------------------");
        assertThat(out).contains("등록된 명언이 없습니다.");
    }

    @Test
    void 수정_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        옛날통닭 두마리
                        오상민
                        등록
                        콜라는 펩시로
                        오상민
                        수정?id=2
                        펩시는 제로라임으로
                        오상민
                        목록
                        종료
                        """
        );

        assertThat(result.exception()).isNull();

        String out = result.output();
        assertThat(out).contains("명언(기존) : 콜라는 펩시로");
        assertThat(out).contains("작가(기존) : 오상민");
        assertThat(out).contains("2 / 오상민 / 펩시는 제로라임으로");
        assertThat(out).doesNotContain("2 / 오상민 / 콜라는 펩시로");
    }

    @Test
    void 수정_ID_정수_테스트() {
        AppResult result = RunWiseInput(
                """
                        수정?id=끼얏호우
                        """
        );

        assertThat(result.exception()).isInstanceOf(IllegalArgumentException.class);
        assertThat(result.exception().getMessage()).contains("[ERROR]");
    }

    @Test
    void 수정_실패_존재하지않음_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        옛날통닭 두마리
                        오상민
                        수정?id=2
                        종료
                        """
        );

        assertThat(result.exception()).isNull();

        String out = result.output();
        assertThat(out).contains("2번 명언은 존재하지 않습니다.");
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
                        삭제?id=2
                        목록
                        종료
                        """
        );

        assertThat(result.exception()).isNull();

        String out = result.output();
        assertThat(out).contains("2번 명언이 삭제되었습니다.");
    }

    @Test
    void 삭제_ID_정수_테스트() {
        AppResult result = RunWiseInput(
                """
                        삭제?id=끼얏호우
                        """
        );

        assertThat(result.exception()).isInstanceOf(IllegalArgumentException.class);
        assertThat(result.exception().getMessage()).contains("[ERROR]");
    }

    @Test
    void 삭제_실패_존재하지않음_테스트() {
        AppResult result = RunWiseInput(
                """
                        등록
                        옛날통닭 두마리
                        오상민
                        삭제?id=2
                        종료
                        """
        );

        assertThat(result.exception()).isNull();

        String out = result.output();
        assertThat(out).contains("2번 명언은 존재하지 않습니다.");
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

    private record AppResult(String output, Throwable exception) {
    }
}