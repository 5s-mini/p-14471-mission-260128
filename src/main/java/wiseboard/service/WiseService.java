package wiseboard.service;

import wiseboard.domain.WiseQuote;
import wiseboard.repository.WiseRepository;

public class WiseService {

    private final WiseRepository wiseRepository;

    public WiseService(WiseRepository wiseRepository) {
        this.wiseRepository = wiseRepository;
    }

    public Integer Register(String author, String content) {
        WiseQuote quote = wiseRepository.Save(author, content);
        return quote.id();
    }

    public WiseQuote[] FindAllDesc() {
        return wiseRepository.FindAllDesc();
    }

    public boolean DeleteById(int id) {
        return wiseRepository.DeleteById(id);
    }

    public WiseQuote FindById(Integer id) {
        return wiseRepository.FindById(id);
    }

    public boolean Modify(Integer id, String author, String content) {
        return wiseRepository.ReplaceById(id, author, content);
    }

    public void Build() {
        wiseRepository.BuildDataJson();
    }
}
