package no.thrapmeyer.fitbitfs.http;

import no.thrapmeyer.fitbitfs.project.FitbitProject;
import no.thrapmeyer.fitbitfs.project.FitbitProjectResponse;
import no.thrapmeyer.fitbitfs.project.FitbitProjectRef;
import no.thrapmeyer.fitbitfs.user.FitbitUser;
import no.thrapmeyer.fitbitfs.user.FitbitUserResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FitbitHttpClient {

	private final String API_ROOT = "https://studio.fitbit.com/api";

	private final RestTemplate client;

	private final String authorization;

	public FitbitHttpClient(String authorization) {
		this.authorization = authorization;

		this.client = new RestTemplate();
	}


	public FitbitUser getUser() {
		HttpEntity entity = new HttpEntity(getHeaders("text/plain;charset=utf-8"));
		String url = API_ROOT + "/oauth/user";
		ResponseEntity<FitbitUserResponse> response = client.exchange(url, HttpMethod.GET, entity, FitbitUserResponse.class);
		return response.getBody().getUser();
	}

	public List<FitbitProjectRef> getProjects() {
		HttpEntity entity = new HttpEntity(getHeaders("text/plain;charset=utf-8"));
		String url = API_ROOT + "/projects";
		ResponseEntity<FitbitProjectResponse> response = client.exchange(url, HttpMethod.GET, entity, FitbitProjectResponse.class);
		return response.getBody().getProjects();
	}

	public FitbitProject getProject(String projectId) {
		HttpEntity entity = new HttpEntity(getHeaders("text/plain;charset=utf-8"));
		String url = API_ROOT + "/projects/" + projectId;
		ResponseEntity<FitbitProject> response = client.exchange(url, HttpMethod.GET, entity, FitbitProject.class);
		return response.getBody();
	}

	public byte[] getFile(String projectId, String path) {
		HttpEntity entity = new HttpEntity(getHeaders("text/plain;charset=utf-8"));
		String url = API_ROOT + "/projects/" + projectId + path;
		ResponseEntity<byte[]> response = client.exchange(url, HttpMethod.GET, entity, byte[].class);
		return response.getBody();
	}

	public void deleteFile(String projectId, String path) {
		HttpEntity entity = new HttpEntity(getHeaders("text/plain;charset=utf-8"));
		String url = API_ROOT + "/projects/" + projectId + path;
		ResponseEntity<byte[]> response = client.exchange(url, HttpMethod.DELETE, entity, byte[].class);
	}

	public void updateFile(String projectId, String path, byte[] data) {
		HttpEntity entity = new HttpEntity(data, getHeaders("text/plain;charset=utf-8"));
		String url = API_ROOT + "/projects/" + projectId + path;
		ResponseEntity<byte[]> response = client.exchange(url, HttpMethod.PUT, entity, byte[].class);
	}

	private HttpHeaders getHeaders(String contentType) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authority", "studio.fitbit.com");
		headers.set("content-type", contentType);
		headers.set("user-agent", "FitbitFS");
		headers.set("authorization", authorization);
		return headers;
	}

	public void mkdir(String projectId, String path) {
		Map<String, String> payload = new HashMap<>();
		payload.put("action_type", "mkdir");

		HttpEntity entity = new HttpEntity(payload, getHeaders("application/json"));
		String url = API_ROOT + "/projects/" + projectId + path;
		client.exchange(url, HttpMethod.POST, entity, byte[].class);
	}

}
