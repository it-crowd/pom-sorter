package pl.itcrowd.pom_sorter;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Notifications {
// ------------------------------ FIELDS ------------------------------

    private static final String NOTIFICATIONS = "POM Sorter";

// -------------------------- STATIC METHODS --------------------------

    public static void inform(@NotNull String title, @NotNull String content, @Nullable Project project)
    {
        com.intellij.notification.Notifications.Bus.notify(new Notification(NOTIFICATIONS, title, content, NotificationType.INFORMATION), project);
    }

// --------------------------- CONSTRUCTORS ---------------------------

    private Notifications()
    {
    }
}
