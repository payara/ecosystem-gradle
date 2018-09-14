package fish.payara.gradle.plugins.micro;

import org.gradle.api.Project;
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.War;
import org.gradle.testfixtures.ProjectBuilder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jGauravGupta
 */
public abstract class BaseTest {

    protected Project buildProject() {
        Project project = ProjectBuilder.builder()
                .withName("example")
                .build();
        project.setGroup("fish.payara.gradle.plugins");
        project.setVersion("1.0.SNAPSHOT");

        DefaultRepositoryHandler repositoryHandler = (DefaultRepositoryHandler) project.getRepositories();
        repositoryHandler.add(repositoryHandler.mavenLocal());
        repositoryHandler.add(repositoryHandler.mavenCentral());
        return project;
    }

    protected PayaraMicroExtension buildExtension(Project project) {
        PayaraMicroPlugin microPlugin = new PayaraMicroPlugin();
        microPlugin.apply(project);
        PayaraMicroExtension extension = microPlugin.createExtension();
        extension.setDaemon(true);
        extension.setImmediateExit(false);
        return extension;
    }

    protected void bundlMicro(Project project, PayaraMicroExtension extension) {
        BundleTask bundleTask = (BundleTask) project.getTasks().getByName("microBundle");
        assertTrue(bundleTask instanceof BundleTask);

        bundleTask.configure(extension);

        War war = ((War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME));
        war.execute();
        assertTrue(war.getArchivePath().exists());

        bundleTask.execute();
        assertNotNull(bundleTask.getPayaraMicroPath(extension.getPayaraVersion()));
        assertTrue(bundleTask.getWar().exists());
        assertTrue(bundleTask.getUberJar().exists());
    }

    protected void bootstrapMicro(Project project, PayaraMicroExtension extension) {
        StartTask startTask = (StartTask) project.getTasks().getByName("microStart");
        assertTrue(startTask instanceof StartTask);

        startTask.configure(extension);
        assertNotNull(startTask.decideOnWhichMicroToUse());

        StopTask stopTask = (StopTask) project.getTasks().getByName("microStop");
        assertTrue(stopTask instanceof StopTask);

        stopTask.configure(extension);
        assertNotNull(stopTask.getProjectGAV());
        assertNull(stopTask.getProcessId());
        assertNull(stopTask.findProcessId());

        startTask.execute();
        assertNotNull(stopTask.findProcessId());

        stopTask.execute();
        assertNull(stopTask.findProcessId());
    }

}
