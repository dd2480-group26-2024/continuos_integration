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
import java.util.Collections;
import org.apache.maven.shared.invoker.*;

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
    
    public static boolean runTests(String directoryPath){
        System.out.println("\n\nPRINTING THE PATH:"+directoryPath+"\n\n");
        File path = new File("");
        InvocationRequest request = new DefaultInvocationRequest();
        request.setBaseDirectory( path );
        request.setBatchMode( true );  // sets batch mode so that the terminal doesn't stall and ask for input
        request.setGoals( Collections.singletonList( "install" ) );

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(path);
        try{
            InvocationResult result = invoker.execute( request );

        }catch(MavenInvocationException e){
            e.printStackTrace();
            return false;

        }
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
        runTests("~/Programming/continuous_integration");
        //Server server = new Server(8026);
        //server.setHandler(new ContinuousIntegration());
        //server.start();
        //server.join();
    }
}
