package phillip.duarte;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JSONConverter {

    private String path;
    private String fileName;

    public JSONConverter() {
    }

    public JSONConverter(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;
    }

    public void convertJSONtoHTML() throws IOException, org.json.simple.parser.ParseException, ParseException {
        JSONParser parser = new JSONParser();
        NumberFormat format = NumberFormat.getCurrencyInstance();

        try {
            // Reads and Parses File
            Object obj = parser.parse(new FileReader(path));
            JSONArray jsonObjects = (JSONArray) obj;

            // Timestamps Stock Statement when run
            LocalDate date = LocalDate.now();
            LocalTime now = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
            String time = now.format(formatter);

            // Creates New File and Appends Data with FileWriter
            File file = new File(fileName);
            FileWriter writer = new FileWriter(file.getName(), true);
            writer.write(
                    "<!DOCTYPE html>\n<html>\n<head><link rel='stylesheet' href='style.css' />\n</head>\n<body>\n");

            // Foreach to loop through each person object
            for (Object personObject : jsonObjects) {
                JSONObject jsonObject = (JSONObject) personObject;
                List<Map> list = getNestedValue(jsonObject, "stock_trades");

                Object fName = (String) getNestedValue(jsonObject, "first_name");
                String lName = (String) getNestedValue(jsonObject, "last_name");
                String ssn = (String) getNestedValue(jsonObject, "ssn");
                String email = (String) getNestedValue(jsonObject, "email");
                String phone = (String) getNestedValue(jsonObject, "phone");
                Number accountNum = (Number) getNestedValue(jsonObject, "account_number");
                JSONArray trades = (JSONArray) getNestedValue(jsonObject, "stock_trades");
                String balanceString = (String) getNestedValue(jsonObject, "beginning_balance");

                String formattedBalance = balanceString.replaceAll("^\\$|(?:(\\.[0-9]*[1-9])|\\.)0+$", "");
                Number balance = NumberFormat.getInstance().parse(formattedBalance);

                double stockTotal = 0;
                double balanceTotal = 0;
                // For loop to calculate total shares & balance on each trade object
                for (int i = 0; i < trades.size(); i++) {
                    Map nested = list.get(i);
                    Number shares = getNestedValue(nested, "count_shares");
                    String price = getNestedValue(nested, "price_per_share");
                    String type = getNestedValue(nested, "type");
                    Number priceNum = format.parse(price);

                    String calculatedEndBalance = calculateBalance(type, balance, shares, priceNum);
                    Number endBalance = NumberFormat.getCurrencyInstance().parse(calculatedEndBalance);

                    String calculatedStock = calculateStock(type, shares);
                    Number endStock = NumberFormat.getCurrencyInstance().parse(calculatedStock);

                    stockTotal += endStock.doubleValue();
                    balanceTotal += endBalance.doubleValue();
                }

                writer.write(String.format(
                        "\n<div class='user-container'>\n<div>%s, %s</div>\n<div>%s %s</div>\n<div>%s</div>\n<div>%s</div>\n</div>%s<div>\n<span>%d</span>\n</div>\n",
                        date, time, fName, lName, ssn, email, phone, accountNum));

                // foreach loop to filewrite each property in the stock object
                for (Object tradeObject : trades) {
                    JSONObject object = (JSONObject) tradeObject;
                    String stockType = (String) getNestedValue(object, "type");
                    String stockSymbol = (String) getNestedValue(object, "stock_symbol");
                    Number shares = (Number) getNestedValue(object, "count_shares");
                    String price = (String) getNestedValue(object, "price_per_share");

                    Number priceNum = format.parse(price);

                    writer.write(String.format(
                            "\n<div>%s</div>\n<div>Symbol: %s</div>\n<div>Shares: %d</div>\n<div>Price Per Share: %s</div>\n<div>Total Balance of Shares: %s</div>\n",
                            stockType,
                            stockSymbol,
                            shares, price, calculateBalance(stockType, balance, shares, priceNum)));
                }
                writer.write(String.format("\n<div>Total Shares: $%1$,.2f</div>\n<div>Total Balance: $%2$,.2f</div>\n",
                        stockTotal, balanceTotal));
            }
            writer.write("\n</body></html>");
            writer.close();
            //Trycatch to see whether file exists
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static <N extends Number> String calculateBalance(String type, N startBalance, N shares, N pricePerShare) {
        double total = shares.doubleValue() * pricePerShare.doubleValue();
        double endBalance = type.equals("Buy") ? -startBalance.doubleValue() - total
                : startBalance.doubleValue() + total;

        DecimalFormat decFormat = new DecimalFormat("$###,###,###,###.00");
        String formattedDouble = decFormat.format(endBalance);

        return formattedDouble;
    }

    public static <N extends Number> String calculateStock(String type, N shares) {
        double totalshares = type.equals("Buy") ? shares.doubleValue() : 0;

        DecimalFormat decFormat = new DecimalFormat("$###,###,###,###.00");
        String formattedDouble = decFormat.format(totalshares);

        return formattedDouble;
    }

    public static <T> T getNestedValue(Map map, String... keys) {
        Object value = map;

        for (String key : keys) {
            value = ((Map) value).get(key);
        }

        return (T) value;
    }
}
