# aDoctor 

**aDoctor** is an Android Studio plugin for code smell detection and refactoring. It is able to fix 6 Android-specific design flaws related to energy consumption:

- *Durable Wakelock*
- *Early Resource Binding*
- *Inefficient Data Structure*
- *Internal Setter*
- *Leaking Thread*
- *Member Ignoring Method*

aDoctor is the outcome of the research conducted in the Software Engineering Lab @ University of Salerno, Italy.

## Installation ad execution

### Install from Jetbrains Plugin Repository

Follow these steps to install the plugin in Android Studio for production use:

1. Open Android Studio
2. Go into *File>Settings...>Plugins>Marketplace*
3. Type "aDoctor"
4. Install it
5. Restart Android Studio
6. Open the Android project you wish to analyze
7. Go to *Refactor>ADoctor* to launch the plugin

### Install from disk

Follow these steps to install the plugin in Android Studio for production use:

1. Clone the repository
2. Open the project with IntelliJ IDEA
3. Build the project just to be sure everything is alright. Ensure you have IntelliJ Platform SDK installed
4. Go to *Build>Prepare Plugin Module \'aDoctor\ for deployment'*. This will generate a zip file in the project root
5. Copy the zip file whenever you want
6. Open Android Studio
7. Go into *File>Settings...>Plugins* 
8. Click the gear icon and *Install Plugin from Disk...*
9. Select the zip file
10. Restart Android Studio
11. Open the Android project you wish to analyze
12. Go to *Refactor>ADoctor* to launch the plugin

### Get source code and run in sandbox

Follow these steps to build the source code and run the plugin in a sandbox:

1. Clone the repository
2. Open the project with IntelliJ IDEA
3. Build the project (with the IDE) just to be sure everything is alright. Ensure you have IntelliJ Platform SDK installed
4. Run the project (with the IDE) though the run configuration *Run aDoctor*: this will run IntelliJ IDEA sandbox
5. Open the project you wish to analyze
6. Go to *Refactor>ADoctor* to launch the plugin
