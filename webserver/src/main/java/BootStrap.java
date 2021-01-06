import connector.Connector;

public class BootStrap {

    public static void main(String[] args) {
        Connector connector = new Connector();
        connector.start();
    }
}
