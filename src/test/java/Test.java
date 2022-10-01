import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static void main(String[] args) {
        System.out.println(getLastColor("&cRed&cBlue&l"));
    }

    private static String getLastColor(String s) {
        final Matcher matcher = Pattern.compile("[&§][\\da-fr]").matcher(s);
        String last = "";

        while (matcher.find()) {
            last = matcher.group();
        }

        return last;
    }

}
