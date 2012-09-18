package pl.com.it_crowd.pom_sorter;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import pl.com.it_crowd.pom_sorter.ui.SettingsForm;

import javax.swing.Icon;
import javax.swing.JComponent;

public class PomSorterConfigurable implements Configurable {
// ------------------------------ FIELDS ------------------------------

    private Project project;

    private SettingsForm settingsForm;

// --------------------------- CONSTRUCTORS ---------------------------

    public PomSorterConfigurable(Project project)
    {
        this.project = project;
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    @Override
    public boolean isModified()
    {
        boolean modified = false;
        if (settingsForm != null) {
            modified = settingsForm.isModified();
        }
        return modified;
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Configurable ---------------------

    @Nls
    @Override
    public String getDisplayName()
    {
        return "Pom sorter";
    }

    @Override
    public Icon getIcon()
    {
        return null;
    }

    @Override
    public String getHelpTopic()
    {
        return null;
    }

// --------------------- Interface UnnamedConfigurable ---------------------

    @Override
    public JComponent createComponent()
    {
        settingsForm = new SettingsForm(project.getComponent(PomSorter.class));
        return settingsForm.$$$getRootComponent$$$();
    }

    @Override
    public void apply() throws ConfigurationException
    {
        if (settingsForm != null) {
            settingsForm.apply();
        }
    }

    @Override
    public void reset()
    {
        if (settingsForm != null) {
            settingsForm.reset();
        }
    }

    @Override
    public void disposeUIResources()
    {
    }
}
