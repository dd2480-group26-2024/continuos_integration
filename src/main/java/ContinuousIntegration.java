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
	
	// Modifies the HashMap<String, String> passed as parameter. 
	// Clone url accesible with key "clone_url" and commit id with key "commit_id".
	public void processRequestData(HttpServletRequest request, HashMap<String, String> map){
		JSONObject requestBody = new JSONObject(request.getParameter("payload"));
		if (requestBody.has("head_commit")) {
			map.put("clone_url", requestBody.getJSONObject("repository").getString("clone_url"));
			map.put("commit_id", requestBody.getJSONObject("head_commit").getString("id"));
		}
		return;
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
