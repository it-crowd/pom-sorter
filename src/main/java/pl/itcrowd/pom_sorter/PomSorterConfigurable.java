package pl.itcrowd.pom_sorter;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import pl.itcrowd.pom_sorter.ui.SettingsForm;

import javax.swing.Icon;
import javax.swing.JComponent;

public class PomSorterConfigurable implements Configurable, ProjectComponent {
// ------------------------------ FIELDS ------------------------------

    public static final String COMPONENT_NAME = "PomSorterConfigurable";

    private Project project;

    private SettingsForm settingsForm;

// --------------------------- CONSTRUCTORS ---------------------------

    public PomSorterConfigurable(@NotNull Project project)
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


// --------------------- Interface BaseComponent ---------------------

    @Override
    public void initComponent()
    {
    }

    @Override
    public void disposeComponent()
    {
    }

// --------------------- Interface Configurable ---------------------

    @Nls
    @Override
    public String getDisplayName()
    {
        return "Pom sorter";
    }

    public Icon getIcon()
    {
        /**
         * This method is required by Configurable interface in Idea 11 and is kept for compatibility
         */
        return null;
    }

    @Override
    public String getHelpTopic()
    {
        return null;
    }

// --------------------- Interface NamedComponent ---------------------

    @NotNull
    @Override
    public String getComponentName()
    {
        return COMPONENT_NAME;
    }

// --------------------- Interface ProjectComponent ---------------------

    @Override
    public void projectOpened()
    {
    }

    @Override
    public void projectClosed()
    {
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
