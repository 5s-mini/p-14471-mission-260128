package wiseboard.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import wiseboard.domain.WiseQuote;

public class WiseRepository {

    private final List<WiseQuote> quotes;
    private Integer nextId;

    public WiseRepository() {
        this.quotes = new ArrayList<>();
        this.nextId = 1;
    }

    public WiseQuote Save(String author, String content) {
        Integer id = nextId;
        WiseQuote wiseQuote = new WiseQuote(id, author, content);

        quotes.add(wiseQuote);
        nextId++;

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

        return true;
    }

    public boolean ReplaceById(Integer id, String author, String content) {
        Integer index = FindIndexById(id);

        if (index == null) {
            return false;
        }

        WiseQuote replaceQuote = new WiseQuote(id, author, content);
        quotes.set(index, replaceQuote);

        return true;
    }

    public WiseQuote[] FindAllDesc() {
        List<WiseQuote> copy = new ArrayList<>(quotes);
        copy.sort(Comparator.comparing(WiseQuote::id).reversed());

        return copy.toArray(new WiseQuote[0]);
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
}
