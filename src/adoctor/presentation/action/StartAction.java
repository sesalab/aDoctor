package adoctor.presentation.action;

import adoctor.presentation.dialog.CoreDriver;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;

import java.io.File;

public class StartAction extends AnAction {

    /**
     * Called when aDoctor is clicked in Refactor menu of the IDE
     *
     * @param e Event fired from Plugin framework
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        //Initialization
        DataContext dataContext = e.getDataContext();
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (project != null) {
            PluginId pluginId = PluginId.getId("it.unisa.plugin.adoctor");
            IdeaPluginDescriptor pluginDescriptor = PluginManager.getPlugin(pluginId);
            if (!new File(pluginDescriptor.getPath().getAbsolutePath() + "/resources").exists()) {
                new File(pluginDescriptor.getPath().getAbsolutePath() + "/resources").mkdirs();
            }

            /*
            Editor serve nel momento in cui si riesce a trovare un modo per applicare i refactoring sull'editor
                piuttosto che direttamente sui file. Ha priorità bassa.
            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            */

            CoreDriver coreDriver = new CoreDriver(project);
            coreDriver.start();
        }
    }
}
