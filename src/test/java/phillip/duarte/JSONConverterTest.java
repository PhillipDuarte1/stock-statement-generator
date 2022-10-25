package phillip.duarte;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.Test;

public class JSONConverterTest {
    JSONConverter converter = new JSONConverter("src/main/java/phillip/duarte/stock.json", "statement.html");
    File file = new File("statement.html");

    @Test
    public void testIfFileExits() {
        assertTrue(file.exists());
    }

    public void testIfFileIsNotEmpty() {
        assertTrue(file.length() != 0);
    }
}
