# EV3Cube

A robot for solving 3x3 Rubik's Cube using LEGO MINDSTORMS EV3 and an Android Phone.

> How about replacing color sensor with an Android phone?  
> I think it will improve overall speed.
>
> &ndash;Daniel, 2016

This project needs several periperals to work:

- **An Android device**  
  Color input and as solving processor.

- **LEGO MINDSTORMS EV3**  
  Interact physically with the Rubik's Cube.

## Getting Started

### EV3 Robot

#### Assembly

Assemble LEGO MINDSTORMS EV3 into a Rubik's solver robot.

Robot can be built by following [Mindcub3r's building instructions](http://mindcuber.com/mindcub3r/mindcub3r.html).  
Replace **color sensor part** in the instructions with **phone stand**.

#### Software

Upload **EV3-part program** to LEGO MINDSTORMS EV3.

1. Connect LEGO MINDSTORM EV3 with computer.
2. Open `misc/Rubiks Solver.ev3` with [EV3 software](https://www.lego.com/en-us/mindstorms/downloads/download-software).
3. Upload the program.

### Android

1. Open project with [Android Studio](https://developer.android.com/studio/index.html).
2. Connect Android phone with computer.
3. Build project and install on Android phone.

## Credits

This project uses following components:

- [Herbert Kociemba's two-phase algorithm in Python and C](https://github.com/muodov/kociemba/commit/dfe63f8a16f02c7d0455246ad5fb39ad0b66cab6)

- [OpenCV 3.1.0 for Android SDK](http://opencv.org/platforms/android.html)

This project is written based on:

- RoboLiterate's robotcomms for opening Bluetooth communication to an EV3 brick (lower protocol)

- EV3Messenger for sending and for receiving messages from/to an EV3 brick (higher protocol)
