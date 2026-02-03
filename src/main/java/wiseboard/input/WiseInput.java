package wiseboard.input;

import java.util.Scanner;
import wiseboard.service.WiseService;
import wiseboard.view.WiseOutput;


public class WiseInput {

    private static final String FINISH_COMMAND = "종료";
    private static final String REGISTER_COMMAND = "등록";
    private static final String LIST_COMMAND = "목록";
    private static final String DELETE_PREFIX = "삭제?id=";
    private static final String MODIFY_PREFIX = "수정?id=";
    private static final String BUILD_COMMAND = "빌드";

    private static final String ERROR_PREFIX = "[ERROR] ";
    private static final String INVALID_COMMAND_ERROR = "알 수 없는 명령어입니다. 사용 가능한 명령: 등록, 목록, 삭제?id={번호}, 수정?id={번호}, 종료";
    private static final String INVALID_ID_ERROR = "id는 1 이상의 정수여야 합니다. 예) 삭제?id=1, 수정?id=2";
    private static final String BLANK_CONTENT_ERROR = "명언 내용은 공백일 수 없습니다. 한글/영문/숫자/공백만 입력 가능합니다.";
    private static final String BLANK_AUTHOR_ERROR = "작가명은 공백일 수 없습니다. 한글/영문/숫자/공백만 입력 가능합니다.";
    private static final String INVALID_CONTENT_CHAR_ERROR = "명언 내용에 허용되지 않는 문자가 포함되어 있습니다. 허용: 한글/영문/숫자/공백";
    private static final String INVALID_AUTHOR_CHAR_ERROR = "작가명에 허용되지 않는 문자가 포함되어 있습니다. 허용: 한글/영문/숫자/공백";

    private final Scanner scanner;
    private final WiseOutput wiseOutput;
    private final WiseService wiseService;

    public WiseInput(WiseOutput wiseOutput) {
        this.wiseOutput = wiseOutput;
        this.scanner = new Scanner(System.in);
    }

    public void Start() {
        wiseOutput.AppTitle();

        while (true) {
            String command = Input();

            switch (command) {
                case FINISH_COMMAND:
                    return;
                case REGISTER_COMMAND:
                    Register();
                    continue;
                case LIST_COMMAND:
                    List();
                    continue;
                case BUILD_COMMAND:
                    Build();
                    continue;
            }

            if (command.startsWith(DELETE_PREFIX)) {
                Delete(command);
                continue;
            } else if (command.startsWith(MODIFY_PREFIX)) {
                Modify(command);
                continue;
            }

            throw new IllegalArgumentException(ERROR_PREFIX + INVALID_COMMAND_ERROR);
        }
    }

    public String Input() {
        wiseOutput.CommandPrompt();
        return scanner.nextLine().trim();
    }

    private void Register() {
        wiseOutput.QuotePrompt();
        String content = scanner.nextLine().trim();

        wiseOutput.AuthorPrompt();
        String author = scanner.nextLine().trim();

        ValidateContent(content);
        ValidateAuthor(author);

        Integer id = wiseService.Register(author, content);
        wiseOutput.Registered(id);
    }

    private void List() {

    }

    private void Build() {

    }

    private void Delete(String command) {

    }

    private void Modify(String command) {

    }

    private void ValidateContent(String content) {
        if (IsBlank(content)) {
            throw new IllegalArgumentException(ERROR_PREFIX + BLANK_CONTENT_ERROR);
        }
    }

    private void ValidateAuthor(String author) {
        if (IsBlank(author)) {
            throw new IllegalArgumentException(ERROR_PREFIX + BLANK_AUTHOR_ERROR);
        }
    }

    private boolean IsBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}