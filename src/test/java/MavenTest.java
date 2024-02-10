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