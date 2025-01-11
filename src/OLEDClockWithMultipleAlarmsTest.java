import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OLEDClockWithMultipleAlarmsTest {
    private final InputStream originalSystemIn = System.in;
    private final PrintStream originalSystemOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalSystemIn);
        System.setOut(originalSystemOut);
    }

    @Test
    void testSetupAlarms() {
        String input = "10:00:00\n11:30:00\ndone\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        OLEDClockWithMultipleAlarms.setupAlarms();

        List<String> expectedAlarms = Arrays.asList("10:00:00", "11:30:00");
        assertEquals(expectedAlarms, OLEDClockWithMultipleAlarms.alarmTimes);
    }

    // Add more test cases as needed to cover other methods
}
