package wiseboard.repository;

import static wiseboard.input.WiseInput.ERROR_PREFIX;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import wiseboard.domain.WiseQuote;

public class WiseRepository {

    private static final String RENEWAL_JSON_ERROR = "data.json 파일 갱신에 실패했습니다.";
    private static final String CREATE_DB_DIR_ERROR = "DB 디렉토리 생성에 실패했습니다.";
    private static final String READ_LAST_ID_ERROR = "lastId.txt 파일 읽기에 실패했습니다.";
    private static final String WRITE_LAST_ID_ERROR = "lastId.txt 파일 저장에 실패했습니다.";
    private static final String READ_QUOTE_FILE_ERROR = "명언 파일 읽기에 실패했습니다: ";
    private static final String WRITE_QUOTE_FILE_ERROR = "명언 파일 저장에 실패했습니다: ";
    private static final String DELETE_QUOTE_FILE_ERROR = "명언 파일 삭제에 실패했습니다: ";
    private static final String WRONG_QUOTE_JSON_ERROR = "명언 파일의 Json 형식이 올바르지 않습니다.";

    private static final Path DB_DIR = Paths.get("db", "wiseSaying");
    private static final Path LAST_ID_FILE = DB_DIR.resolve("lastId.txt");
    private static final Path DATA_JSON_FILE = Paths.get("data.json");

    private final List<WiseQuote> quotes;
    private Integer nextId;

    public WiseRepository() {
        this.quotes = new ArrayList<>();
        EnsureDbDir();

        Integer lastId = ReadLastId();
        LoadQuotes(lastId);

        this.nextId = lastId + 1;
    }

    public WiseQuote Save(String author, String content) {
        Integer id = nextId;
        WiseQuote wiseQuote = new WiseQuote(id, author, content);

        quotes.add(wiseQuote);
        nextId++;

        WriteQuoteFile(wiseQuote);
        WriteLastId(id);

        return wiseQuote;
    }

    public WiseQuote FindById(Integer id) {
        int i = 0;

        while (i < quotes.size()) {
            WiseQuote quote = quotes.get(i);

            if (quote.id().equals(id)) {
                return quote;
            }

            i++;
        }

        return null;
    }

    public boolean DeleteById(Integer id) {
        Integer index = FindIndexById(id);

        if (index == null) {
            return false;
        }

        quotes.remove((int)index);
        DeleteQuoteFile(id);

        return true;
    }

    public boolean ReplaceById(Integer id, String author, String content) {
        Integer index = FindIndexById(id);

        if (index == null) {
            return false;
        }

        WiseQuote replaceQuote = new WiseQuote(id, author, content);
        quotes.set(index, replaceQuote);

        WriteQuoteFile(replaceQuote);

        return true;
    }

    public WiseQuote[] FindAllDesc() {
        List<WiseQuote> copy = new ArrayList<>(quotes);
        copy.sort(Comparator.comparing(WiseQuote::id).reversed());

        return copy.toArray(new WiseQuote[0]);
    }

    public void BuildDataJson() {
        List<WiseQuote> copy = new ArrayList<>(quotes);
        copy.sort(Comparator.comparing(WiseQuote::id));

        String json = ToDataJson(copy);

        try {
            Files.writeString(DATA_JSON_FILE, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(ERROR_PREFIX + RENEWAL_JSON_ERROR);
        }
    }

    private Integer FindIndexById(Integer id) {
        int i = 0;

        while (i < quotes.size()) {
            WiseQuote quote = quotes.get(i);

            if (quote.id().equals(id)) {
                return i;
            }

            i++;
        }

        return null;
    }

    private void EnsureDbDir() {
        try {
            Files.createDirectories(DB_DIR);
        } catch (IOException e) {
            throw new IllegalArgumentException(ERROR_PREFIX + CREATE_DB_DIR_ERROR);
        }
    }

    private Integer ReadLastId() {
        if (!Files.exists(LAST_ID_FILE)) {
            return 0;
        }

        try {
            String text = Files.readString(LAST_ID_FILE, StandardCharsets.UTF_8).trim();

            if (text.isEmpty()) {
                return 0;
            }

            return Integer.valueOf(text);
        } catch (IOException | NumberFormatException e) {
            throw new IllegalArgumentException(ERROR_PREFIX + READ_LAST_ID_ERROR);
        }
    }

    private void WriteLastId(Integer id) {
        try {
            Files.writeString(LAST_ID_FILE, String.valueOf(id), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(ERROR_PREFIX + WRITE_LAST_ID_ERROR);
        }
    }

    private void LoadQuotes(Integer lastId) {
        Integer i = 1;

        while (i <= lastId) {
            Path quoteFile = DB_DIR.resolve(i + ".json");

            if (Files.exists(quoteFile)) {
                WiseQuote quote = ReadQuoteFile(quoteFile);

                if (quote != null) {
                    quotes.add(quote);
                }
            }

            i++;
        }

        quotes.sort(Comparator.comparing(WiseQuote::id));
    }

    private WiseQuote ReadQuoteFile(Path path) {
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            return ParseQuoteJson(json);
        } catch (IOException e) {
            throw new IllegalArgumentException(ERROR_PREFIX + READ_QUOTE_FILE_ERROR + path.getFileName());
        }
    }

    private void WriteQuoteFile(WiseQuote quote) {
        Path quoteFile = DB_DIR.resolve(quote.id() + ".json");
        String json = ToQuoteJson(quote);

        try {
            Files.writeString(quoteFile, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(ERROR_PREFIX + WRITE_QUOTE_FILE_ERROR + quote.id());
        }
    }

    private void DeleteQuoteFile(Integer id) {
        Path quoteFile = DB_DIR.resolve(id + ".json");

        try {
            Files.deleteIfExists(quoteFile);
        } catch (IOException e) {
            throw new IllegalArgumentException(ERROR_PREFIX + DELETE_QUOTE_FILE_ERROR + id);
        }
    }

    private String ToQuoteJson(WiseQuote quote) {
        return "{\n"
                + "  \"id\": " + quote.id() + ",\n"
                + "  \"content\": \"" + quote.content() + "\",\n"
                + "  \"author\": \"" + quote.author() + "\"\n"
                + "}\n";
    }

    private WiseQuote ParseQuoteJson(String json) {
        Integer id = ParseIntField(json, "\"id\":");
        String content = ParseStringField(json, "\"content\":");
        String author = ParseStringField(json, "\"author\":");

        if (id == null || content == null || author == null) {
            throw new IllegalArgumentException(ERROR_PREFIX + WRONG_QUOTE_JSON_ERROR);
        }

        return new WiseQuote(id, author, content);
    }

    private Integer ParseIntField(String json, String key) {
        int index = json.indexOf(key);

        if (index < 0) {
            return null;
        }

        int start = index + key.length();

        while (start < json.length() && IsWhitespace(json.charAt(start))) {
            start++;
        }

        int end = start;

        while (end < json.length() && IsDigit(json.charAt(end))) {
            end++;
        }

        if (start == end) {
            return null;
        }

        return Integer.valueOf(json.substring(start, end));
    }

    private String ParseStringField(String json, String key) {
        int idx = json.indexOf(key);

        if (idx < 0) {
            return null;
        }

        int start = idx + key.length();

        while (start < json.length() && IsWhitespace(json.charAt(start))) {
            start++;
        }

        if (start >= json.length() || json.charAt(start) != '\"') {
            return null;
        }

        start++;

        int end = json.indexOf('\"', start);

        if (end < 0) {
            return null;
        }

        return json.substring(start, end);
    }

    private boolean IsWhitespace(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }

    private boolean IsDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private String ToDataJson(List<WiseQuote> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        int i = 0;

        while (i < list.size()) {
            WiseQuote quote = list.get(i);

            sb.append("  {\n");
            sb.append("    \"id\": ").append(quote.id()).append(",\n");
            sb.append("    \"content\": \"").append(quote.content()).append("\",\n");
            sb.append("    \"author\": \"").append(quote.author()).append("\"\n");
            sb.append("  }");

            if (i < list.size() - 1) {
                sb.append(",");
            }

            sb.append("\n");
            i++;
        }

        sb.append("]\n");
        return sb.toString();
    }
}