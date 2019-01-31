package testPackage;

public class testNoSmell {
    public void foo() {
        PowerManager.WakeLock wakeLock4 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "wakelock4");
        wakeLock4.acquire();
        wakeLock4.release();
    }
}