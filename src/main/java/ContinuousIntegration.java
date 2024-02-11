import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
 
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.io.File;
import org.eclipse.jetty.server.handler.AbstractHandler;
import java.util.stream.Collectors;
import org.json.JSONObject;
 
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

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
	

    // Send email notfication method
    public boolean sendEmailNotification(JSONObject requestBodyJson, String webhookCommitResult){
        final String username = "group26kth@gmail.com";
        final String password = "kth26group";

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
            String headCommitId = requestBodyJson.getJSONObject("head_commit").getString("id");
            String repoURL = requestBodyJson.getJSONObject("repository").getString("clone_url");
            String toUser = requestBodyJson.getJSONObject("head_commit").getJSONObject("committer").getString("email");

            Message message = new MimeMessage(session);
            //Change here to change sender email!
            message.setFrom(new InternetAddress("group26kth@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toUser));
            message.setSubject("Current state update");

            if(webhookCommitResult.contains("BUILD SUCESS")){
                message.setText("The latest commit resulted in: SUCCESS \n");
            } else if(webhookCommitResult.contains("BUILD FAIL") || webhookCommitResult.contains("COMPILATION ERROR")){
                message.setText("The latest commit resulted in: FAILURE\n");
            } else {
                message.setText("Error, issue unknown");
            }
            Transport.send(message);
            System.out.println("Message has been sent");
            return true;
        }catch (MessagingException e){
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
