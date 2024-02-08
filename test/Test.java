/**
 * Test infrastructure for the Continuous Integration server
 *
 * OBS:
 * Does not currently compile, will depend on Maven and groupID
 *
 * TODO
 * Add following to the Maven build:
 *
 *
 * <dependency>
 *     <groupId>org.junit.platform</groupId>
 *     <artifactId>junit-platform-launcher</artifactId>
 *     <version>1.10.2</version>
 *     <scope>test</scope>
 * </dependency>
 *
 */

package our.project.name

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test {
       @Test
       @DisplayName("Test 1")
       void test1(){
              assertTrue(true);
       }
}
