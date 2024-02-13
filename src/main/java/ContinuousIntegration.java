import java.io.IOException;
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
import java.util.HashMap;

//Java stuff

/** 
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
*/




public class ContinuousIntegration extends AbstractHandler
{
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

        // Testing email notification
		HashMap<String, String> requestData = processRequestData(request);
		//Should use the compileMavenProject when it is fully working
		boolean compileStatus = true; // Example status, replace with your logic
		sendEmailNotification(requestData, compileStatus);

        response.getWriter().println("CI job done");

    }

	/**
	 * Sends an email notification to the committer's email account when a commit occurs.
	 *
	 * @param requestData   A HashMap containing request data obtained from the processRequestData method.
	 *                      It should contain the following key-value pairs:
	 *                          - "commit_id": The ID of the commit.
	 *                          - "clone_url": The URL of the repository where the commit occurred.
	 *                          - "email": The email address of the committer.
	 * @param compileStatus A boolean indicating the status of the compilation process.
	 *                      True indicates a successful compilation, false indicates a failure.
	 * @return true if the email notification was successfully sent; otherwise, false.
	 */
	public boolean sendEmailNotification(HashMap<String, String> requestData, boolean compileStatus) {
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
			String committMessage = requestData.get("commit_message");

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("group26kth@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toUser));
			message.setSubject("Current state update");

			if (compileStatus) {
				message.setText("The latest commit resulted in: SUCCESS \n" + headCommitId + "\n" + committMessage);
			} else if (!compileStatus) {
				message.setText("The latest commit resulted in: FAILURE\n" + headCommitId + "\n" + committMessage);
			} else {
				message.setText("Error, issue unknown" + headCommitId);
			}
			Transport.send(message);
			System.out.println("Message has been sent");
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return false;
	}

  	// Returns a String[2], the first element is the clone_url, the second is the commit id
	public String[] processRequestData(HttpServletRequest request){
		String[] reqData = new String[2];
		JSONObject requestBody = new JSONObject();
		try{
			requestBody = new JSONObject(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
		}catch(IOException e){
			e.printStackTrace();
		}
		if (requestBody.has("head_commit")) {
			reqData[0] = requestBody.getJSONObject("repository").getString("clone_url");
			reqData[1] = requestBody.getJSONObject("head_commit").getString("id");
		}
		return reqData;

	// Extract data from GitHub's request and return a JSONObject
	public JSONObject processRequestData(HttpServletRequest request){
		JSONObject requestBody = new JSONObject(request.getParameter("payload"));
		return requestBody;
	}
 
    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8026);
        server.setHandler(new ContinuousIntegration()); 
        server.start();
        server.join();
    }

  

}
