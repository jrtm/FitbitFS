package no.thrapmeyer.fitbitfs;

import no.thrapmeyer.fitbitfs.http.FitbitHttpClient;
import no.thrapmeyer.fitbitfs.project.FitbitProjectRef;
import no.thrapmeyer.fitbitfs.sync.SyncConfig;
import no.thrapmeyer.fitbitfs.sync.SyncProject;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FitbitFs {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			printUsageAndExit();
		}

		try {
			switch (args[0]) {
				case "projects":
					if (args.length != 2) {
						printUsageAndExit();
					}
					commandProject(args[1]);
					break;

				case "init":
					if (args.length != 3) {
						printUsageAndExit();
					}
					commandInit(args[1], args[2]);
					break;

				case "sync":
					commandSync();
					break;

				default:
					printUsageAndExit();
			}

		} catch (HttpClientErrorException e) {
			System.err.println("Error contacting Fitbit API: " + e);
		}
	}



	private static SyncProject getSyncProject() {
		Path root = Paths.get(System.getProperty("user.dir"));
		return new SyncProject(root);
	}

	private static void commandProject(String jwt) {
		FitbitHttpClient client = new FitbitHttpClient(jwt);

		List<FitbitProjectRef> projects = client.getProjects();

		if (projects == null || projects.isEmpty()) {
			System.out.println("No projects found");
		} else {
			System.out.println("Found " + projects.size() + " projects:");

			long now = System.currentTimeMillis();
			for (FitbitProjectRef project : projects) {
				String diffString = getDiffString(now, project.getLastModified().toEpochSecond() * 1000L);
				System.out.println(" * " + project.getId() + " – " + project.getName() + " (updated " + diffString + " ago)");
			}
		}
	}

	private static String getDiffString(long a, long b) {
		long diff = Math.abs(a - b);
		long seconds = diff / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;

		if (minutes == 0) {
			return "a few seconds";
		}
		if (hours == 0) {
			return minutes == 1 ? "a minute" : minutes + " minutes";
		}
		if (days == 0) {
			return hours == 1 ? "an hour" : hours + " hours";
		}
		return days == 1 ? "a day" : days + " days";
	}

	private static void commandInit(String projectId, String jwt) throws IOException {
		SyncProject sm = getSyncProject();
		SyncConfig config = new SyncConfig();
		config.setProjectId(projectId);
		config.setJwt(jwt);
		sm.initFs(config);

		System.out.println("FitbitFS initialized in directory " + sm.getRootPath() + "");

		sm.synchronize();
	}

	private static void commandSync() throws IOException {
		SyncProject sm = getSyncProject();
		sm.synchronize();
	}

	private static void printUsageAndExit() {
		System.err.println("usages:\n" +
				" $ java -jar fitbitfs.jar init <projectId> <jwt>\n" +
				" $ java -jar fitbitfs.jar sync\n" +
				" $ java -jar fitbitfs.jar project <jwt>");
		System.exit(1);
	}
}
