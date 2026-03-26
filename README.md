# TelemetryCenter – Rover AGR

TelemetryCenter is a desktop application developed to receive, process, and visualize real-time telemetry data from the Rover AGR system. The software acts as the main monitoring interface for the rover’s logical control computer, providing live telemetry information and video streaming.

## Overview

The application receives telemetry packets transmitted by the rover through a network connection and displays the decoded data in a graphical interface. Operators can monitor system behavior, analyze telemetry values, and view a live video stream from the rover.

This tool was developed as part of the Rover AGR project.

## Features

* Real-time telemetry reception via UDP
* Live telemetry visualization
* Graph-based telemetry monitoring
* Live camera stream using OpenCV
* Modular telemetry processing system
* Designed for rover testing and monitoring

## Requirements

* Java 17 or newer
* OpenCV native library (`opencv_java4120.dll`)
* Network connection to the rover system

## Important (OpenCV Setup)

For the application to work correctly, the OpenCV native library must be accessible to Java.

You can do this in **one of the following ways**:

### Option 1 (Recommended)

Place the OpenCV DLL in the Java **bin directory**:

Example:

```
C:\Program Files\Java\jdk-17\bin\opencv_java4120.dll
```

### Option 2

Place the DLL in the **same folder as the application JAR**:

```
TelemetryCenter.jar
opencv_java4120.dll
```

### Option 3

Run the application specifying the library path:

```
java -Djava.library.path="path_to_dll_folder" -jar TelemetryCenter.jar
```

## Running the Application

Run the program with:

```
java -jar TelemetryCenter.jar
```

## Project Purpose

TelemetryCenter was created to support the Rover AGR project by providing a reliable monitoring system capable of receiving telemetry data and displaying a live video feed from the rover.

## Authors

Rover AGR Development Team
