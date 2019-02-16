public class Button {
    private Consumer<String> onClickConsumer;
    private String buttonName;
    
    public Button(Consumer<String> onClickConsumer,
                  String buttonName) {
        this.onClickConsumer = onClickConsumer;
        this.buttonName = buttonName;
    }

    public void click() {
        onClickConsumer.accept(buttonName);
    }

    public static void main(String[] args) {
        Button button = new Button(
                name -> System.out.println("Button " + name + " was clicked"),
                "SEARCH"
        );

        button.click();
    }
}
