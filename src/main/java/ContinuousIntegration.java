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

 
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.stream.Collectors;

import java.util.Collections;
import org.apache.maven.shared.invoker.*;

import java.util.Date;
import java.util.HashMap;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;

//Java stuff

/** 
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
*/




public class ContinuousIntegration extends AbstractHandler
{
private HttpClient httpClient;
    private static String token="";
    private static String logInfo="";

    public ContinuousIntegration(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    public ContinuousIntegration() {
        this.httpClient =HttpClient.newHttpClient() ;
    }
    public String updateGitHubStatus(boolean status, String sha, String description) {
            String status_string="";
            if(status){
                status_string= "success";
            }
            else
            {status_string="failure";}

            JSONObject requestBody = new JSONObject()
                    .put("state", status_string)
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean runTests(String directoryPath) throws Exception{
        File path = new File(directoryPath);
        InvocationRequest request = new DefaultInvocationRequest();
        request.setBaseDirectory( path );
        request.setBatchMode( true );  // sets batch mode so that the terminal doesn't stall and ask for input
        request.setGoals( Collections.singletonList( "test" ) );
        
        String MAVEN_HOME = System.getenv("MAVEN_HOME");
        if (MAVEN_HOME == null){
            String errMsg = "\nException error due to MAVEN_HOME environment variable not set \n"
                            + "Try adding it to your ~/.bashrc, ~/.profile or /etc/environment file \n"
                            + "export MAVEN_HOME=<path> \n"
                            + "tip: you can find the path by entering \"mvn --version\"\n";

            throw new Exception(errMsg);
        }

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File (MAVEN_HOME));
        try{
            InvocationResult result = invoker.execute( request );

        }catch(MavenInvocationException e){
            e.printStackTrace();
            return false;
        }
        return true;

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

                logInfo+=tempLine+"\n";
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

    

	/**
	 * Sends an email notification to the committer's email account when a commit occurs.
	 *
	 * @param requestData   A HashMap containing request data obtained from the processRequestData method.
	 *                      It should contain the following key-value pairs:
	 *                          - "commit_id": The ID of the commit.
	 *                          - "clone_url": The URL of the repository where the commit occurred.
	 *                          - "email": The email address of the committer.
	 * @param testStatus A boolean indicating the status of the test process.
	 *                      True indicates a successful compilation, false indicates a failure.
	 * @return true if the email notification was successfully sent; otherwise, false.
	 */
	public boolean sendEmailNotification(HashMap<String, String> requestData, boolean testStatus) {
		final String username = "group26kth@gmail.com";
		final String password = "tlsf nrys dquv mpce ";

		// SMTP server settings (for Gmail)
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		// Create a Session object
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			String headCommitId = requestData.get("commit_id");
			String repoURL = requestData.get("clone_url");
			String toUser = requestData.get("email");
			String commitMessage = requestData.get("commit_message");

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("group26kth@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toUser));
			message.setSubject("Current state update");

			if (testStatus) {
				message.setText("The latest commit resulted in: SUCCESS \n" + "Commit Id: " + headCommitId + "\n" + "Commit message: " + commitMessage);
			} else if (!testStatus) {
				message.setText("The latest commit resulted in: FAILURE\n" + "Commit Id: " + headCommitId + "\n" + "Commit message: " + commitMessage);
			} else {
				message.setText("Error, issue unknown" + "Commit Id: " + headCommitId + "\n" + "Commit message: " + commitMessage);
			}
			Transport.send(message);
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return false;
	}

	
	// Extract data from GitHub's request and return a HashMap<String, String> with the data required for the CI server
	public HashMap<String,String> processRequestData(HttpServletRequest request){
		JSONObject requestBody = new JSONObject(request.getParameter("payload"));
		HashMap<String,String> map = new HashMap<>();		
		if(!requestBody.has("head_commit")){
			map.put("error", "no head_commit in the request payload");
			return map;
		}		
		map.put("repo_name", requestBody.getJSONObject("repository").getString("name")); 
		map.put("clone_url", requestBody.getJSONObject("repository").getString("clone_url")); 
		map.put("commit_id", requestBody.getJSONObject("head_commit").getString("id")); 
		map.put("email", requestBody.getJSONObject("head_commit").getJSONObject("committer").getString("email")); 
		map.put("timestamp", requestBody.getJSONObject("head_commit").getString("timestamp")); 
		map.put("commit_message", requestBody.getJSONObject("head_commit").getString("message")); 
		return map;
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

    public void deleteDirectory(String path){
        try {
            FileUtils.deleteDirectory(new File(path));            
        } catch (IOException e) {
            System.err.println("An error occurred during directory deletion: " + e.getMessage());
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
        
        HashMap<String, String> data = processRequestData(request);
        if(data.containsKey("error")){
            return;
        }
        String repo_path = "./" + data.get("repo_name"); 
        // Clone repo and checkout
        cloneAndCheckout(data.get("clone_url"), data.get("commit_id"), repo_path);
        
        // Compile and run tests
        boolean compileStatus = compileMavenProject(repo_path);
        if(compileStatus == false){            
        }    
        boolean testStatus;    
        try{
            testStatus = runTests(repo_path);
        }catch(Exception e){
            e.printStackTrace();
            deleteDirectory(repo_path);
            return;
        }
        Date buildDate = new Date();
        

        // Set github status, email notification and build history
        sendEmailNotification(data, testStatus);
        
        updateGitHubStatus(testStatus, data.get("commit_id"), "CI server status");
        
        saveToBuildHistory(data.get("commit_id"), logInfo, buildDate.toString());
        
        deleteDirectory(repo_path);

        response.getWriter().println("CI job done");
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
