package no.thrapmeyer.fitbitfs.fdb;

import java.util.ArrayList;
import java.util.List;

public class FitbitHostResponse {

	private List<FitbitHost> hosts = new ArrayList<>();

	public List<FitbitHost> getHosts() {
		return hosts;
	}

	public void setHosts(List<FitbitHost> hosts) {
		this.hosts = hosts;
	}
}
