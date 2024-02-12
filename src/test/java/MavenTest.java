import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.json.JSONObject;

import org.mockito.Mockito;
import javax.servlet.http.HttpServletRequest;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

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
		String payload = "{\"repository\": {\"clone_url\": \"https://github.com/dd2480-group26-2024/continuous_integration.git\"},\"head_commit\": {\"id\": \"545c38c57a26677c764a657fb42f2186c34c8bac\",\"message\": \"edited the sendEmailNotification method and removed the newly added method\",\"timestamp\": \"2024-02-12T16:47:59+01:00\",\"committer\": {\"email\": \"robin.yurt@hotmail.com\",}}}";
		HashMap<String,String> expected = new HashMap<>();			
		expected.put("clone_url", "https://github.com/dd2480-group26-2024/continuous_integration.git"); 
		expected.put("commit_id", "545c38c57a26677c764a657fb42f2186c34c8bac"); 
		expected.put("email", "robin.yurt@hotmail.com"); 
		expected.put("timestamp", "2024-02-12T16:47:59+01:00"); 
		expected.put("commit_message", "edited the sendEmailNotification method and removed the newly added method"); 
		Mockito.when(request.getParameter("payload")).thenReturn(payload);
		HashMap<String,String> result = ci.processRequestData(request);
		assertTrue(result.equals(expected));
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
}