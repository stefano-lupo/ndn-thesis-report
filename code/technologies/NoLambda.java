public class Button {
    private OnClickRunner onClickRunner;
    private String buttonName;

    public Button(OnClickRunner onClickRunner,
                  String buttonName) {
        this.onClickRunner = onClickRunner;
        this.buttonName = buttonName;
    }

    public void click() {
        onClickRunner.runOnClick(buttonName);
    }

    public static void main(String[] args) {
        Button button = new Button(new ButtonClickLogger(), "SEARCH");
        button.click();
    }
}

interface OnClickRunner {
    void runOnClick(String buttonName);
}

class ButtonClickLogger implements OnClickRunner {
    @Override
    public void runOnClick(String buttonName) {
        System.out.println("Button " + buttonName + " was clicked.");
    }
}
