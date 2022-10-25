package phillip.duarte;

import java.io.IOException;
import java.text.ParseException;

public final class App {
    public static void main(String[] args) throws IOException, org.json.simple.parser.ParseException, ParseException {
        JSONConverter converter = new JSONConverter("src/main/java/phillip/duarte/stock.json", "statement.html");
        converter.convertJSONtoHTML();
    }
}