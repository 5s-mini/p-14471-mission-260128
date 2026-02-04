package wiseboard.view;

import wiseboard.domain.WiseQuote;

public class WiseOutput {

    private static final String APP_TITLE = "== 명언 앱 ==";
    private static final String COMMAND_PROMPT = "명령) ";
    private static final String QUOTE_PROMPT = "명언 : ";
    private static final String AUTHOR_PROMPT = "작가 : ";
    private static final String LIST_HEADER = "번호 / 작가 / 명언";

    private static final String REGISTERED_OUTPUT = "번 명언이 등록되었습니다.";
    private static final String DELETED_OUTPUT = "번 명언이 삭제되었습니다.";
    private static final String NOT_FOUND_OUTPUT = "번 명언은 존재하지 않습니다.";
    private static final String EXISTING_CONTENT_PREFIX = "명언(기존) : ";
    private static final String EXISTING_AUTHOR_PREFIX = "작가(기존) : ";
    private static final String EMPTY_LIST_OUTPUT = "등록된 명언이 없습니다.";
    private static final String RENEWAL_JSON_OUTPUT = "data.json 파일의 내용이 갱신되었습니다.";

    public void AppTitle() {
        System.out.println(APP_TITLE);
    }

    public void CommandPrompt() {
        System.out.print(COMMAND_PROMPT);
    }

    public void QuotePrompt() {
        System.out.print(QUOTE_PROMPT);
    }

    public void AuthorPrompt() {
        System.out.print(AUTHOR_PROMPT);
    }

    public void ListHeader() {
        System.out.println(LIST_HEADER);
        System.out.println("----------------------");
    }

    public void Registered(Integer id) {
        System.out.println(id + REGISTERED_OUTPUT);
    }

    public void Deleted(Integer id) {
        System.out.println(id + DELETED_OUTPUT);
    }

    public void NotFound(Integer id) {
        System.out.println(id + NOT_FOUND_OUTPUT);
    }

    public void ModifyExistingContent(String content) {
        System.out.println(EXISTING_CONTENT_PREFIX + content);
    }

    public void ModifyExistingAuthor(String author) {
        System.out.println(EXISTING_AUTHOR_PREFIX + author);
    }

    public void RenewalJson() {
        System.out.println(RENEWAL_JSON_OUTPUT);
    }

    public void ListRows(WiseQuote[] quotes) {
        int i = 0;
        int n = quotes.length;

        while (i < n) {
            WiseQuote quote = quotes[i];
            System.out.println(quote.id() + " / " + quote.author() + " / " + quote.content());
            i++;
        }
    }

    public void EmptyList() {
        System.out.println(EMPTY_LIST_OUTPUT);
    }
}