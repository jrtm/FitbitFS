package no.thrapmeyer.fitbitfs.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.thrapmeyer.fitbitfs.fs.SnapshotNode;

import java.io.IOException;
import java.util.Arrays;

public class Util {

	public static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
	}


	public static void printNode(SnapshotNode rootNode) {
		printNode(0, rootNode);
	}

	private static void printNode(int lvl, SnapshotNode rootNode) {
		char[] tabs = new char[lvl * 2];
		Arrays.fill(tabs, ' ');
		String tab = new String(tabs);


		System.out.println(tab + (rootNode.isDirectory() ? "[DIR] " : "[FILE] ") + rootNode.getId() + "] { ");
		if (rootNode.getType() != null) System.out.println(tab + "  Merge: " + rootNode.getType());
		System.out.println(tab + "  Name: " + rootNode.getName());
		System.out.println(tab + "  URL: " + rootNode.toUrl());
		System.out.println(tab + "  Path: " + rootNode.toPath());
		System.out.println(tab + "  mtime: " + rootNode.getLastModifiedLocal());
		System.out.println(tab + "  ftime: " + rootNode.getLastModifiedRemote());
		if (rootNode.isDirectory()) {
			System.out.println(tab + "  Children: [");
			rootNode.getChildren().forEach(node -> printNode(lvl + 2, node));
			System.out.println(tab + "  ]");
		}
		System.out.println(tab + "}");
	}

	public static void logWithStatus(String message, Object object, IOAction action) {
		System.out.print(message + " " + String.valueOf(object) + " ... ");
		try {
			action.execute();
			System.out.println(AnsiColor.green("done"));
		} catch (IOException e) {
			System.out.println(AnsiColor.red("failed"));
			throw new RuntimeException(e);
		}
	}

	public static void logWithStatusAsync(String message, Object object, IOAction action) {
		System.out.println(message + " " + String.valueOf(object) + " ... ");
		try {
			action.execute();
			System.out.println(message + " " + String.valueOf(object) + " " + AnsiColor.green("done"));
		} catch (IOException e) {
			System.out.println(message + " " + String.valueOf(object) + " " + AnsiColor.red("failed"));
			throw new RuntimeException(e);
		}
	}

	public interface IOAction {
		void execute() throws IOException;
	}

}
