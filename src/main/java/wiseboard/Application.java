package wiseboard;

import wiseboard.controller.WiseController;

public class Application {
    public static void main(String[] args) {
        WiseController wiseController = new WiseController();
        wiseController.Run();
    }
}