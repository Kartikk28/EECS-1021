# OLED Clock with Multiple Alarms ⏰

This project is a real-time clock with multiple alarm functionality, designed to integrate Java programming with Arduino hardware. The clock displays the current time and date on an OLED screen and triggers alarms with an LED and buzzer at user-defined times.

## Features
- Real-Time Clock: Displays time and date on an OLED screen with a blinking colon effect.
- Multiple Alarms: Allows users to set multiple alarms via the console.
- Hardware Integration: Uses Firmata protocol to control an LED and buzzer connected to Arduino.
- JUnit Tests: Includes test cases for validating alarm setup and logic.

## Hardware Requirements
- Arduino Board
- OLED Display (I2C)
- LED (connected to pin 4)
- Buzzer (connected to pin 5)

## How It Works
1. Users input alarm times in `HH:mm:ss` format.
2. The program checks alarms in real-time and triggers them with visual and audio notifications.
3. Proper clean-up ensures the program stops the hardware safely.
