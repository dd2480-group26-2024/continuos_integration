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
import java.io.BufferedReader;
import java.io.InputStreamReader;

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

    public boolean runTests(String directoryPath){
        try {
            // run maven tests in cloned repo
            String command = "cd "+directoryPath+" && mvn test";
            Process testProcess = Runtime.getRuntime().exec(command);
            int exitCode = testProcess.waitFor();

        } catch (Exception e) {
            // Tests failed
            e.printStackTrace();
            return false;
        }
        // Tests succeeded
        return true;
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
