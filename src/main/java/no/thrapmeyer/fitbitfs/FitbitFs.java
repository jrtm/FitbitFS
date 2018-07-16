package no.thrapmeyer.fitbitfs;

import no.thrapmeyer.fitbitfs.sync.SyncConfig;
import no.thrapmeyer.fitbitfs.sync.SyncProject;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FitbitFs {

	public static void main(String[] args) throws Exception {
		Path root = Paths.get(System.getProperty("user.dir"));

		if (args.length == 0) {
			printUsageAndExit();
		}

		SyncProject sm = new SyncProject(root);

		try {

			switch (args[0]) {
				case "init":
					if (args.length != 3) {
						printUsageAndExit();
					}
					String projectId = args[1];
					String jwt = args[2];

					SyncConfig config = new SyncConfig();
					config.setProjectId(projectId);
					config.setJwt(jwt);
					sm.initFs(config);

					System.out.println("FitbitFS initialized in directory " + root + "");

				case "sync":
					sm.synchronize();
					break;

				default:
					printUsageAndExit();
			}

		} catch (HttpClientErrorException e) {
			System.err.println("Error contacting Fitbit API: " + e);
		}
	}

	private static void printUsageAndExit() {
		System.err.println("usages:\n $ java -jar fitbitfs.jar init <projectId> <jwt>\n $ java -jar fitbitfs.jar sync");
		System.exit(1);
	}
}
