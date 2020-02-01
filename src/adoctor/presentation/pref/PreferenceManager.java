package adoctor.presentation.pref;

import com.intellij.ide.util.PropertiesComponent;

import java.util.ArrayList;
import java.util.List;

public class PreferenceManager {
    // There was a strange behaviour for boolean values
    private static final String TRUE = "1";
    private static final String FALSE = "0";
    private static final String DW = "dw";
    private static final String ERB = "erb";
    private static final String IDS = "ids";
    private static final String IS = "is";
    private static final String LT = "lt";
    private static final String MIM = "mim";
    private static final String STATS = "stats";

    private String pluginId;
    private PropertiesComponent propertiesComponent;

    public PreferenceManager(String pluginId) {
        this.pluginId = pluginId;
        this.propertiesComponent = PropertiesComponent.getInstance();
    }

    public List<Boolean> getSavedSelectedSmells() {
        List<Boolean> selectedSmells = new ArrayList<>();
        selectedSmells.add(propertiesComponent.getValue(pluginId + "_" + DW, TRUE).equals(TRUE));
        selectedSmells.add(propertiesComponent.getValue(pluginId + "_" + ERB, TRUE).equals(TRUE));
        selectedSmells.add(propertiesComponent.getValue(pluginId + "_" + IDS, TRUE).equals(TRUE));
        selectedSmells.add(propertiesComponent.getValue(pluginId + "_" + IS, TRUE).equals(TRUE));
        selectedSmells.add(propertiesComponent.getValue(pluginId + "_" + LT, TRUE).equals(TRUE));
        selectedSmells.add(propertiesComponent.getValue(pluginId + "_" + MIM, TRUE).equals(TRUE));
        return selectedSmells;
    }

    public void setSavedSelectedSmells(List<Boolean> selectedSmells) {
        propertiesComponent.setValue(pluginId + "_" + DW, selectedSmells.get(0) ? TRUE : FALSE);
        propertiesComponent.setValue(pluginId + "_" + ERB, selectedSmells.get(1) ? TRUE : FALSE);
        propertiesComponent.setValue(pluginId + "_" + IDS, selectedSmells.get(2) ? TRUE : FALSE);
        propertiesComponent.setValue(pluginId + "_" + IS, selectedSmells.get(3) ? TRUE : FALSE);
        propertiesComponent.setValue(pluginId + "_" + LT, selectedSmells.get(4) ? TRUE : FALSE);
        propertiesComponent.setValue(pluginId + "_" + MIM, selectedSmells.get(5) ? TRUE : FALSE);
    }

    public boolean isSavedStats() {
        return propertiesComponent.getValue(pluginId + "_" + STATS, TRUE).equals(TRUE);
    }

    public void setSavedStats(boolean stats) {
        propertiesComponent.setValue(pluginId + "_" + STATS, stats ? TRUE : FALSE);
    }
}
