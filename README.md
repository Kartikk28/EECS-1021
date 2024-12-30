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

class AlarmClock {
    private final IODevice device;
    private final SSD1306 display;
    private final Pin ledPin;
    private final Pin buzzerPin;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d");
    private final List<String> alarmTimes = new ArrayList<>();
    private final List<Boolean> alarmTriggered = new ArrayList<>();

    public AlarmClock(String portName) throws IOException, InterruptedException {
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

    public void setAlarm(String alarmTime) {
        alarmTimes.add(alarmTime);
        alarmTriggered.add(false);
    }

    public void start() throws InterruptedException, IOException {
        while (true) {
            checkAndRunAlarms();
            displayCurrentTime();
            Thread.sleep(1000); // Update every second
        }
    }

    private void checkAndRunAlarms() throws IOException, InterruptedException {
        String currentTime = timeFormat.format(new Date());
        String currentDate = dateFormat.format(new Date());
        updateOLED(currentTime, currentDate);
        for (int i = 0; i < alarmTimes.size(); i++) {
            if (currentTime.startsWith(alarmTimes.get(i)) && !alarmTriggered.get(i)) {
                triggerAlarm();
                alarmTriggered.set(i, true);
            }
        }
    }

    private void updateOLED(String time, String date) throws IOException {
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

    private void triggerAlarm() throws IOException, InterruptedException {
        ledPin.setValue(1); // Turn on LED
        playMusicSequence();  // Play music sequence
        Thread.sleep(12000); // Ensure music plays for at least 12 seconds
        ledPin.setValue(0); // Turn off LED
        buzzerPin.setValue(0); // Ensure buzzer is off after the alarm
    }

    private void playMusicSequence() throws IOException, InterruptedException {
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

    private void displayCurrentTime() throws IOException {
        String currentTime = timeFormat.format(new Date());
        String currentDate = dateFormat.format(new Date());
        updateOLED(currentTime, currentDate);
    }

    public void stop() throws IOException {
        device.stop();
    }
}
