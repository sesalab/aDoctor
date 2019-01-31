package testPackage;

public class testERB1 {
    private LocationManager lm;
    private LocationManager lm2;
    private LocationManager lm3;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, (LocationListener) this);
            }
        }

        lm2 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm2.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
            if (lm2.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm2.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, (LocationListener) this);
            }
        }

        lm3 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm3.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
            if (lm3.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm3.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, (LocationListener) this);
            }
        }
    }
}