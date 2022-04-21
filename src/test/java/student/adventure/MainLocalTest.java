package student.adventure;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.Rule;
import static org.junit.Assert.assertThat;
import org.junit.contrib.java.lang.system.SystemOutRule;
import student.MyAdventureMain;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

public class MainLocalTest {

    // Reference: https://stefanbirkner.github.io/system-rules/#EnvironmentVariables
    // Reference: https://stackoverflow.com/questions/6371379/mocking-java-inputstream
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    // Mock input stream to test the quit functionality of main
    @Test
    public void testMockMainQuit() throws Exception {
        String[] args = null;
        final InputStream original = System.in;
        System.setIn(new ByteArrayInputStream("take nezuko\n go practice\nquit\n".getBytes()));
        MyAdventureMain.main(args);
        System.setIn(original);
        assertThat(systemOutRule.getLog(), CoreMatchers.containsString("You successfully quit the game."));
    }

    // Mock input stream to win the game
    @Test
    public void testMockMainWin() throws Exception {
        String[] args = null;
        final InputStream original = System.in;
        System.setIn(new ByteArrayInputStream("go practice\ngo out\ngo fight\n go Continue\n".getBytes()));
        MyAdventureMain.main(args);
        System.setIn(original);
        assertThat(systemOutRule.getLog(), CoreMatchers.containsString("Congratulation! You win the game!"));
    }
}
