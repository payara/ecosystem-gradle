/*
 *
 * Copyright (c) [2018-2020] Payara Foundation and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://github.com/payara/Payara/blob/master/LICENSE.txt
 * See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * The Payara Foundation designates this particular file as subject to the "Classpath"
 * exception as provided by the Payara Foundation in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package fish.payara.gradle.plugins.micro;

import static fish.payara.gradle.plugins.micro.BundleTask.BUNDLE_TASK_NAME;
import static fish.payara.gradle.plugins.micro.ExplodeWarTask.EXPLODE_TASK_NAME;
import static fish.payara.gradle.plugins.micro.StartTask.START_TASK_NAME;
import static fish.payara.gradle.plugins.micro.StopTask.STOP_TASK_NAME;
import org.awaitility.Awaitility;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.TaskInternal;
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.TaskExecuter;
import org.gradle.api.internal.tasks.TaskStateInternal;
import org.gradle.api.internal.tasks.execution.DefaultTaskExecutionContext;
import org.gradle.api.plugins.WarPlugin;
import static org.gradle.api.plugins.WarPlugin.WAR_TASK_NAME;
import org.gradle.api.tasks.bundling.War;
import org.gradle.execution.ProjectExecutionServices;
import org.gradle.testfixtures.ProjectBuilder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 *
 * @author jGauravGupta
 */
public abstract class BaseTest {

    private static final String MOCK_PROJECT_GROUP_ID = "fish.payara.gradle.plugins";
    private static final String MOCK_PROJECT_NAME = "example";
    private static final String MOCK_PROJECT_VERSION = "1.0.SNAPSHOT";

    private Project project;

    private ProjectExecutionServices executionServices;

    private PayaraMicroPlugin microPlugin;
    
    private PayaraMicroExtension extension;

    protected Project buildProject() {
        project = ProjectBuilder.builder()
                .withName(MOCK_PROJECT_NAME)
                .build();
        project.setGroup(MOCK_PROJECT_GROUP_ID);
        project.setVersion(MOCK_PROJECT_VERSION);
        executionServices = new ProjectExecutionServices((ProjectInternal) project);

        DefaultRepositoryHandler repositoryHandler = (DefaultRepositoryHandler) project.getRepositories();
        repositoryHandler.add(repositoryHandler.mavenLocal());
        repositoryHandler.add(repositoryHandler.mavenCentral());

        assertFalse(project.getPlugins().hasPlugin("java"));
        project.getPluginManager().apply("java");
        assertTrue(project.getPlugins().hasPlugin("java"));

        assertFalse(project.getPlugins().hasPlugin(WarPlugin.class));
        project.getPluginManager().apply(WarPlugin.class);
        assertTrue(project.getPlugins().hasPlugin(WarPlugin.class));

        assertFalse(project.getPlugins().hasPlugin(PayaraMicroPlugin.class));
        project.getPluginManager().apply(PayaraMicroPlugin.class);
        assertTrue(project.getPlugins().hasPlugin(PayaraMicroPlugin.class));

        return project;
    }

    protected PayaraMicroExtension buildExtension() {
        microPlugin = project.getPlugins().getPlugin(PayaraMicroPlugin.class);
        extension = microPlugin.createExtension();
        extension.setDaemon(true);
        extension.setImmediateExit(false);
        return extension;
    }

    protected void createWar() {
        War war = ((War) project.getTasks().getByName(WAR_TASK_NAME));
        execute(war);
        assertTrue(war.getArchiveFile().get().getAsFile().exists());
    }

    protected void createExplodedWar() {
        createWar();

        Task task = project.getTasks().getByName(EXPLODE_TASK_NAME);
        assertTrue(task instanceof ExplodeWarTask);
        ExplodeWarTask explodeWar = (ExplodeWarTask) task;
        microPlugin.configureExplodeWarTask();
        execute(explodeWar);
        assertTrue(explodeWar.getExplodedWarDirectory().exists());
    }
    
    protected void bundleMicro() {
        createWar();

        Task task = project.getTasks().getByName(BUNDLE_TASK_NAME);
        assertTrue(task instanceof BundleTask);
        BundleTask bundleTask = (BundleTask) task;
        bundleTask.configure(extension);
        execute(bundleTask);
        assertNotNull(bundleTask.getPayaraMicroPath(extension.getPayaraVersion()));
        assertTrue(bundleTask.getWarTask().getArchiveFile().get().getAsFile().exists());
        assertTrue(bundleTask.getUberJar().exists());
    }

    protected void bootstrapMicro() {
        Task task = project.getTasks().getByName(START_TASK_NAME);
        assertTrue(task instanceof StartTask);
        StartTask startTask = (StartTask) task;

        startTask.configure(extension);
        assertNotNull(startTask.decideOnWhichMicroToUse());

        task = project.getTasks().getByName(STOP_TASK_NAME);
        assertTrue(task instanceof StopTask);
        StopTask stopTask = (StopTask) task;

        stopTask.configure(extension);
        assertNotNull(stopTask.getProjectGAV());
        assertNull(stopTask.getProcessId());
        assertNull(stopTask.findProcessId());

        execute(startTask);
        assertNotNull(stopTask.findProcessId());

        execute(stopTask);
        Awaitility.await()
                .until(() -> stopTask.findProcessId() == null);
    }

    private void execute(Task task) {
        executionServices.get(TaskExecuter.class)
                .execute((TaskInternal) task, (TaskStateInternal) task.getState(), new DefaultTaskExecutionContext(null));
        task.getState().rethrowFailure();
    }

}
