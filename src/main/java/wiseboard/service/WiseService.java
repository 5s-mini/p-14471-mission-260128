package wiseboard.service;

import wiseboard.repository.WiseRepository;

public class WiseService {

    private final WiseRepository wiseRepository;

    public WiseService(WiseRepository wiseRepository) {
        this.wiseRepository = wiseRepository;
    }

    public Integer Register(String author, String content) {
    }
}
