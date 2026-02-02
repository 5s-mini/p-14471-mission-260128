package controller;

import input.WiseInput;
import view.WiseOutput;

public class WiseController {

    private final WiseInput wiseInput;

    public  WiseController() {
        WiseOutput wiseOutput = new WiseOutput();
        this.wiseInput = new WiseInput(wiseOutput);
    }

    public void Run() {
        wiseInput.Start();
    }
}