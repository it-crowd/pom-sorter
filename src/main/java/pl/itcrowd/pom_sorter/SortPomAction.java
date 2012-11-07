package pl.itcrowd.pom_sorter;

import com.intellij.lang.StdLanguages;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;

public class SortPomAction extends AnAction {
// -------------------------- OTHER METHODS --------------------------

    @Override
    public void actionPerformed(AnActionEvent event)
    {
        final Project project = event.getProject();
        if (project == null) {
            Messages.showWarningDialog("Cannot obtain project from AnActionEvent", "Invalid State");
            return;
        }
        final PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            Notifications.inform("No file selected", "Please select POM.xml first", project);
            return;
        }
        final PsiFile xmlFile = psiFile.getViewProvider().getPsi(StdLanguages.XML);
        if (xmlFile == null || !(xmlFile instanceof XmlFile)) {
            Notifications.inform("Selected file is not XmlFile", "Please select POM.xml first", project);
            return;
        }
        project.getComponent(PomSorter.class).sortFile((XmlFile) xmlFile);
    }

    @Override
    public void update(AnActionEvent e)
    {
        final Presentation presentation = e.getPresentation();
        final PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (psiFile == null) {
            presentation.setEnabledAndVisible(false);
            return;
        }
        final PsiFile xmlFile = psiFile.getViewProvider().getPsi(StdLanguages.XML);
        if (xmlFile == null || !(xmlFile instanceof XmlFile)) {
            presentation.setEnabledAndVisible(false);
            return;
        }
        presentation.setEnabledAndVisible(true);
    }
}
