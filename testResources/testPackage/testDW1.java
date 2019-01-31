package testPackage;

public class testDW1 {
    public void foo() {
        PowerManager.WakeLock wakeLock1 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "wakelock1");
        wakeLock1.acquire();

        PowerManager.WakeLock wakeLock2 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "wakelock2");
        wakeLock2.acquire();
    }

    public void foo2() {
        PowerManager.WakeLock wakeLock3 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "wakelock3");
        wakeLock3.acquire();
        wakeLock3.release();
    }

    public void foo3() {
        PowerManager.WakeLock wakeLock4 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "wakelock4");
        wakeLock4.acquire();
    }
}