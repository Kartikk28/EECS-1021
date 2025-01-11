import org.firmata4j.IODevice;
import org.firmata4j.I2CDevice;
import org.firmata4j.Pin;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.SSD1306;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class OLEDClockWithMultipleAlarms {
    private static IODevice device;
    private static SSD1306 display;
    private static Pin ledPin;
    private static Pin buzzerPin;
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d");
    static List<String> alarmTimes = new ArrayList<>();
    private static List<Boolean> alarmTriggered = new ArrayList<>();

    public static void main(String[] args) {
        setupAlarms();

        try {
            initializeHardware();
            while (true) {
                checkAndRunAlarms();
                Thread.sleep(1000); // Update every second
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanUp();
        }
    }

    static void setupAlarms() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter alarm times (HH:mm:ss), type 'done' when finished:");
        while (true) {
            String input = scanner.nextLine();
            if ("done".equalsIgnoreCase(input)) {
                break;
            }
            alarmTimes.add(input);13
            alarmTriggered.add(false);
        }
        scanner.close();
    }

    private static void initializeHardware() throws IOException, InterruptedException {
        String portName = "/dev/cu.usbserial-0001"; // Adjust this to your Arduino's port
        device = new FirmataDevice(portName);
        device.start();
        device.ensureInitializationIsDone();

        I2CDevice i2cDevice = device.getI2CDevice((byte) 0x3C);
        display = new SSD1306(i2cDevice, SSD1306.Size.SSD1306_128_64);
        display.init();

        ledPin = device.getPin(4);
        ledPin.setMode(Pin.Mode.OUTPUT);

        buzzerPin = device.getPin(5);
        buzzerPin.setMode(Pin.Mode.PWM);
    }

    private static void checkAndRunAlarms() throws IOException, InterruptedException {
        String currentTime = timeFormat.format(new Date());
        String currentDate = dateFormat.format(new Date());
        updateOLED(currentTime, currentDate);
        for (int i = 0; i < alarmTimes.size(); i++) {
            if (currentTime.startsWith(alarmTimes.get(i)) && !alarmTriggered.get(i)) { // Only check HH:mm:ss
                triggerAlarm();
                alarmTriggered.set(i, true);
            }
        }
    }

    private static void updateOLED(String time, String date) throws IOException {
        display.clear();
        display.getCanvas().setTextsize(2);

        // Time display with blinking colon
        boolean showColon = (System.currentTimeMillis() / 1000) % 2 == 0;
        String displayTime = showColon ? time : time.replace(':', ' ');
        display.getCanvas().setCursor(10, 10);
        display.getCanvas().write(displayTime);

        // Date and day display
        display.getCanvas().setTextsize(1);
        display.getCanvas().setCursor(10, 40);
        display.getCanvas().write(date);

        display.display();
    }

    private static void triggerAlarm() throws IOException, InterruptedException {
        ledPin.setValue(1); // Turn on LED
        playMusicSequence();  // Play music sequence
        Thread.sleep(12000); // Ensure music plays for at least 12 seconds
        ledPin.setValue(0); // Turn off LED
        buzzerPin.setValue(0); // Ensure buzzer is off after the alarm
    }

    private static void playMusicSequence() throws IOException, InterruptedException {
        int[] frequencies = {440, 494, 523, 587, 659}; // Frequencies for musical notes A4, B4, C5, D5, E5
        int[] durations = {300, 300, 300, 300, 300}; // Durations in milliseconds for each tone
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;

        while (elapsedTime < 12000) { // 12 seconds in milliseconds
            for (int i = 0; i < frequencies.length; i++) {
                buzzerPin.setValue(frequencies[i]);
                Thread.sleep(durations[i]);
                buzzerPin.setValue(0);  // Stop the tone between notes
                Thread.sleep(50);  // Short pause between notes
            }
            elapsedTime = System.currentTimeMillis() - startTime;
        }
    }

    private static void cleanUp() {
        try {
            device.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
