package no.thrapmeyer.fitbitfs;

import no.thrapmeyer.fitbitfs.fdb.FitbitHost;
import no.thrapmeyer.fitbitfs.http.FitbitHttpClient;
import no.thrapmeyer.fitbitfs.project.FitbitProjectRef;
import no.thrapmeyer.fitbitfs.sync.SyncConfig;
import no.thrapmeyer.fitbitfs.sync.SyncProject;
import no.thrapmeyer.fitbitfs.user.FitbitUser;
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

		String envName = "FITBITFS_JWT";
		String jwtEnv = System.getenv(envName);
		if (jwtEnv == null || jwtEnv.isEmpty()) {
			System.err.println("Required environment variable " + envName + " not set. Please set it to your Fitbit Studio authorization JWT");
			System.exit(1);
		}
		jwtEnv = jwtEnv.trim();



		try {
			switch (args[0]) {
				case "projects":
					commandProject(jwtEnv);
					break;

				case "whoami":
					commandWhoami(jwtEnv);
					break;

				case "init":
					if (args.length != 2) {
						printUsageAndExit();
					}
					commandInit(jwtEnv, args[1]);
					break;

				case "sync":
					commandSync(jwtEnv);
					break;

				case "hosts":
					commandHosts(jwtEnv);
					break;

				default:
					printUsageAndExit();
			}

		} catch (HttpClientErrorException e) {
			System.err.println("Error contacting Fitbit API: " + e);
		}
	}



	private static SyncProject getSyncProject(String jwt) {
		Path root = Paths.get(System.getProperty("user.dir"));
		return new SyncProject(jwt, root);
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

	private static void commandWhoami(String jwt) {
		FitbitHttpClient client = new FitbitHttpClient(jwt);
		FitbitUser user = client.getUser();
		System.out.println("Logged in as " + user.getFullName() + " <" + user.getEmail() + ">");
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

	private static void commandInit(String jwt, String projectId) throws IOException {
		SyncProject sm = getSyncProject(jwt);
		SyncConfig config = new SyncConfig();
		config.setProjectId(projectId);
		sm.initFs(config);

		System.out.println("FitbitFS initialized in directory " + sm.getRootPath() + "");

		sm.synchronize();
	}

	private static void commandSync(String jwt) throws IOException {
		SyncProject sm = getSyncProject(jwt);
		sm.synchronize();
	}

	private static void commandHosts(String jwt) throws Exception {
		FitbitHttpClient client = new FitbitHttpClient(jwt);
		List<FitbitHost> hosts = client.getHosts();

		if (hosts.isEmpty()) {
			System.out.println("No hosts found");
			return;
		}

		System.out.println("Found " + hosts.size() + " hosts\n");
		System.out.println("App hosts:");
		hosts.stream()
				.filter(FitbitHost::isAppHost)
				.forEach(host -> System.out.println(" * " + host.getId() + " – " + host.getDisplayName() + (host.isAvailable() ? " [available]" : " [not available]")));

		System.out.println("\nCompanion hosts:");
		hosts.stream()
				.filter(FitbitHost::isCompanionHost)
				.forEach(host -> System.out.println(" * " + host.getId() + " – " + host.getDisplayName() + (host.isAvailable() ? " [available]" : " [not available]")));
 	}

	private static void printUsageAndExit() {
		System.err.println("usages:\n" +
				" $ java -jar fitbitfs.jar init <projectId>\n" +
				" $ java -jar fitbitfs.jar sync\n" +
				" $ java -jar fitbitfs.jar projects\n" +
				" $ java -jar fitbitfs.jar whoami\n" +
				" $ java -jar fitbitfs.jar hosts\n");
		System.exit(1);
	}
}
