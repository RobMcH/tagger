package tagger.utility;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * Wrapper used for printing expections and strings.
 */
public class Logger {
    private Logger() {
    }

    /**
     * Prints a string aligned with a certain length to stdout.
     *
     * @param s      the string to be printed.
     * @param length the length used in printf().
     */
    public static void printString(String s, int length) {
        String alignment = "%-" + length + "s";
        System.out.printf(alignment, s);
    }

    /**
     * Prints a string to stdout.
     *
     * @param s the string to be printed.
     */
    public static void printString(String s) {
        System.out.print(s);
    }

    /**
     * Prints the stack trace of an exception.
     *
     * @param e the exception.
     */
    public static void printException(Exception e) {
        e.printStackTrace();
    }
}
