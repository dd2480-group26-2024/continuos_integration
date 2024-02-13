import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.json.JSONObject;

import org.mockito.Mockito;
import javax.servlet.http.HttpServletRequest;
import java.io.StringReader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.BufferedReader;
import java.util.HashMap;
import java.io.File; 

public class MavenTest {

    @Test
    public void trueTest() {
        assertEquals(1, 1);
    }

    @Test
    public void testCloneAndCheckout(@TempDir Path tempDir) {//TempDir is a junit thing that creates a temporary directory in /tmp folder 
        // Arrange
        ContinuousIntegration ci = new ContinuousIntegration();
        String repoUrl = "https://github.com/dd2480-group26-2024/decide"; // replace with your repo URL
        String commitId = "6cdd0d9b6d2d60956e9bb4750d4f1431219a5616"; // replace with a commit id to test
        String directoryPath = tempDir.toString(); // Use JUnit's @TempDir feature to create a temporary directory

        try {
            // Act
            ci.cloneAndCheckout(repoUrl, commitId, directoryPath);

            // Assert
            Path repoPath = tempDir.resolve(".git"); // Path to the .git directory
            assertTrue(Files.exists(repoPath), "Repository .git directory should exist after cloning");

            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            try (Repository repository = repositoryBuilder.setGitDir(repoPath.toFile()).readEnvironment().findGitDir().build();
                RevWalk revWalk = new RevWalk(repository)) {
                ObjectId lastCommitId = repository.resolve(commitId);
                RevCommit revCommit = revWalk.parseCommit(lastCommitId);
                System.out.println("");
                assertEquals(commitId, revCommit.getId().getName(), "The checked out commit ID should match the requested commit ID.");
            }

            //JUnit cleans up the tempdir automatically!
        } catch (Exception e) {
            fail("Test failed due to exception: " + e.getMessage());
        }
    }
	
	@Test
	public void testProcessRequestData(){
		ContinuousIntegration ci = new ContinuousIntegration();
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		// Simplified GitHub request payload
		String payload = "{\"repository\": {\"clone_url\": \"https://github.com/dd2480-group26-2024/continuous_integration.git\"},\"head_commit\": {\"id\": \"22473f129585cad9e0662860d1cc19c9d81e4081\" }}";
		JSONObject expected = new JSONObject(payload);
		Mockito.when(request.getParameter("payload")).thenReturn(payload);
		JSONObject result = ci.processRequestData(request);
		assertTrue(result.similar(expected));
	}
    @Test
    public void testUpdateGitHubStatus() throws Exception {

        HttpClient httpClientMock = mock(HttpClient.class);
        HttpResponse<Object> httpResponseMock = mock(HttpResponse.class);

        when(httpResponseMock.body()).thenReturn("{\"state\": \"success\"}");
        when(httpClientMock.send(any(HttpRequest.class), any())).thenReturn(httpResponseMock);

        ContinuousIntegration ci = new ContinuousIntegration(httpClientMock);

        String result = ci.updateGitHubStatus("success", "abc123", "Passed");

        assertEquals("success", result);

        verify(httpClientMock).send(any(HttpRequest.class), any());
    }

    @Test
    public void test_compile_project_true() {
        try {
            ContinuousIntegration ci = new ContinuousIntegration();

            // Try to compile the correct project
            boolean comp=ci.compileMavenProject("src/test/TestMavenProject/mvnProjectCorrect");
            assertTrue(comp);

        } catch (Exception e) {
            fail("Test failed due to exception: " + e.getMessage());
        }
    }
    @Test
    public void test_compile_project_false() {
        try {
            ContinuousIntegration ci = new ContinuousIntegration();

            // Try to compile the incorrect project
            boolean comp=ci.compileMavenProject("src/test/TestMavenProject/mvnProjectIncorrect");
            assertFalse(comp);

        } catch (Exception e) {
            fail("Test failed due to exception: " + e.getMessage());
        }
    }
	
	@TempDir
	Path buildHistDir;
	@Test
	public void testSaveToBuildHistory(){
		Path buildDir = buildHistDir.resolve("builds");
		Path templatePath = buildHistDir.resolve("builds/_template.html");
		Path indexPath = buildHistDir.resolve("index.html");
		try {
			Files.createDirectories(buildDir);
            Files.createFile(templatePath);
            Files.createFile(indexPath);
			byte[] indexHtml = ("<!doctype html> <html lang=\"en\">  <head>   <meta charset=\"UTF-8\">   <title>Build History</title>  </head>  <body>   <h1>Build History</h1>   <br>  </body> </html>").getBytes();
			byte[] templateHtml = ("<!doctype html> <html lang=\"en\">  <head>   <meta charset=\"UTF-8\">   <title>Build History</title>  </head>  <body>   <h1>Build Information</h1>   <p>Commit Hash: $commit_id</p>   <p>Build Date: $build_date</p>   <h1>Build Logs</h1>   <div> 	<textarea rows=\"30\" wrap=\"off\" style=\"width: 90%; overflow-x: auto;\">$build_logs</textarea>   </div>  </body> </html> ").getBytes();
			Files.write(indexPath, indexHtml);
			Files.write(templatePath, templateHtml);
        } catch (IOException e) {
			fail("Test failed due to exception: " + e.getMessage());
        }
		ContinuousIntegration ci = new ContinuousIntegration();
		assertTrue(ci.saveToBuildHistory("COMMIT_ID", "Some logs\n on multiple\n lines", "2024-02-13", buildHistDir.toString()));
	}
}