package no.thrapmeyer.fitbitfs.util;

public class AnsiColor {
	private static final String ANSI_RESET = "\u001B[0m";
	private static final String ANSI_BLACK = "\u001B[30m";
	private static final String ANSI_RED = "\u001B[31m";
	private static final String ANSI_GREEN = "\u001B[32m";
	private static final String ANSI_YELLOW = "\u001B[33m";
	private static final String ANSI_BLUE = "\u001B[34m";
	private static final String ANSI_PURPLE = "\u001B[35m";
	private static final String ANSI_CYAN = "\u001B[36m";
	private static final String ANSI_WHITE = "\u001B[37m";

	public static String black(String text) { return ANSI_BLACK + text + ANSI_RESET; }
	public static String green(String text) { return ANSI_GREEN + text + ANSI_RESET; }
	public static String red(String text) { return ANSI_RED + text + ANSI_RESET; }
	public static String yellow(String text) { return ANSI_YELLOW + text + ANSI_RESET; }
	public static String blue(String text) { return ANSI_BLUE + text + ANSI_RESET; }
	public static String purple(String text) { return ANSI_PURPLE + text + ANSI_RESET; }
	public static String cyan(String text) { return ANSI_CYAN + text + ANSI_RESET; }
	public static String white(String text) { return ANSI_WHITE + text + ANSI_RESET; }
}
