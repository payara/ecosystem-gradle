/*
 *
 * Copyright (c) 2018 Payara Foundation and/or its affiliates. All rights reserved.
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

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.artifacts.dsl.DefaultRepositoryHandler;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.War;
import org.gradle.testfixtures.ProjectBuilder;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 *
 * @author jGauravGupta
 */
public abstract class BaseTest {

    private static final String MOCK_PROJECT_GROUP_ID = "fish.payara.gradle.plugins";
    private static final String MOCK_PROJECT_NAME = "example";
    private static final String MOCK_PROJECT_VERSION = "1.0.SNAPSHOT";

    protected Project buildProject() {
        Project project = ProjectBuilder.builder()
                .withName(MOCK_PROJECT_NAME)
                .build();
        project.setGroup(MOCK_PROJECT_GROUP_ID);
        project.setVersion(MOCK_PROJECT_VERSION);

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

    protected void bundleMicro(Project project, PayaraMicroExtension extension) {
        Task task = project.getTasks().getByName("microBundle");
        assertTrue(task instanceof BundleTask);
        BundleTask bundleTask = (BundleTask) task;

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
        Task task = project.getTasks().getByName("microStart");
        assertTrue(task instanceof StartTask);
        StartTask startTask = (StartTask) task;

        startTask.configure(extension);
        assertNotNull(startTask.decideOnWhichMicroToUse());

        task = project.getTasks().getByName("microStop");
        assertTrue(task instanceof StopTask);
        StopTask stopTask = (StopTask) task;

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
