import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.io.InputStreamReader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
 
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.BufferedReader;
import java.io.File;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;

import java.util.HashMap;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.apache.commons.lang3.StringUtils;

//Java stuff

/** 
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
*/




public class ContinuousIntegration extends AbstractHandler
{
private HttpClient httpClient;
    private static String token="";
    public ContinuousIntegration(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    public ContinuousIntegration() {
        this.httpClient =HttpClient.newHttpClient() ;
    }
    public String updateGitHubStatus(String status, String sha, String description) {
            JSONObject requestBody = new JSONObject()
                    .put("state", status)
                    .put("description", description);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/dd2480-group26-2024/continuous_integration/statuses/" + sha))
                    .header("Authorization", "token " + token)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
                JSONObject jsonResponse = new JSONObject(response.body());
                return jsonResponse.getString("state");
            } catch (Exception e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

    /**
     * 
     * @param repoUrl the URL of the repository
     * @param commitId the commit ID to checkout
     * @param directoryPath the path where the repo will be cloned to.
     */
    public void cloneAndCheckout(String repoUrl, String commitId, String directoryPath) {
        try {
            // Clone the repository
            Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(new File(directoryPath))
                .call();
    
            // Checkout the specific commit
            git.checkout().setName(commitId).call();
    
            git.close();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    public boolean compileMavenProject(String projectDirectory) {
        try {
            // command to compile mvn program
            String[] command = {"mvn", "clean", "compile"};
    
            // start the process
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(projectDirectory)); 
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int exitCode = process.waitFor(); 
    
            
            BufferedReader outputError = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String tempLine;
            boolean projectWorking = true;

            // Check if the exit code is 0
            if (exitCode != 0) {
                projectWorking = false;
            }
    
            while ((tempLine = outputError.readLine()) != null) {
                if (tempLine.contains("[ERROR]")) {
                    projectWorking = false;
                }
            }
            return projectWorking;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    
    
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);
		
		
		
        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        // 2nd compile the code
		
		

        response.getWriter().println("CI job done");
    }
	
	// Extract data from GitHub's request and return a JSONObject
	public JSONObject processRequestData(HttpServletRequest request){
		JSONObject requestBody = new JSONObject(request.getParameter("payload"));
		return requestBody;
	}
	
	// Save build info in a build history
	public boolean saveToBuildHistory(String commitId, String buildLogs, String buildDate, String path){
		HashMap<String, String> buildData = new HashMap<>();
		buildData.put("$commit_id", commitId);
		buildData.put("$build_date", buildDate);
		buildData.put("$build_logs", buildLogs);
		
		try{
			File input = new File(path+"/index.html");
			Document doc = Jsoup.parse(input, "UTF-8");
			Element body = doc.body();
			body.appendElement("a")
				.attr("href", "builds/"+commitId+".html")
				.text(commitId + "\t|\t" + buildDate);
			body.appendElement("br");

			Files.write(Paths.get(path+"/index.html"), doc.html().getBytes());
			Path templatePath = Paths.get(path+"/builds/_template.html");
			Path newBuildPath = Paths.get(path+"/builds/"+commitId+".html");
			String templateContent = new String(Files.readAllBytes(templatePath));
			String modifiedContent = StringUtils.replaceEach(templateContent, buildData.keySet().toArray(new String[0]), buildData.values().toArray(new String[0]));
			Files.write(newBuildPath, modifiedContent.getBytes());
		} catch(IOException e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean saveToBuildHistory(String commitId, String buildLogs, String buildDate){
		return saveToBuildHistory(commitId, buildLogs, buildDate, "build_history");
	}
	
    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
    if (args.length > 0) {
        token = args[0];
    }
    else{
        System.err.println("NO TOKEN ");
        return;
    }

        Server server = new Server(8026);
        server.setHandler(new ContinuousIntegration()); 
        server.start();
        server.join();
    }

  

}
