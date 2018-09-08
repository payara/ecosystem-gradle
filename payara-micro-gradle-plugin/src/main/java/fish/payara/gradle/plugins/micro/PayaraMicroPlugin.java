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

import static fish.payara.gradle.plugins.micro.BundleTask.BUNDLE_TASK_DESCRIPTION;
import static fish.payara.gradle.plugins.micro.BundleTask.BUNDLE_TASK_NAME;
import static fish.payara.gradle.plugins.micro.StartTask.START_TASK_DESCRIPTION;
import static fish.payara.gradle.plugins.micro.StartTask.START_TASK_NAME;
import static fish.payara.gradle.plugins.micro.StopTask.STOP_TASK_DESCRIPTION;
import static fish.payara.gradle.plugins.micro.StopTask.STOP_TASK_NAME;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.MavenPlugin;
import org.gradle.api.plugins.WarPlugin;

public class PayaraMicroPlugin implements Plugin<Project> {

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(MavenPlugin.class);
        project.getPluginManager().apply(WarPlugin.class);
        PayaraMicroExtension extension = createExtension();
        StartTask startTask = createMicroStartTask();
        StopTask stopTask = createMicroStopTask();
        BundleTask bundleTask = createMicroBundleTask();
        project.afterEvaluate(prj -> {
            startTask.configure(extension);
            stopTask.configure(extension);
            bundleTask.configure(extension);
        });
    }

    private PayaraMicroExtension createExtension() {
        return project.getExtensions()
                .create("payaraMicro", PayaraMicroExtension.class, project);
    }

    private BundleTask createMicroBundleTask() {
        BundleTask task = createTask(BUNDLE_TASK_NAME, BUNDLE_TASK_DESCRIPTION, BundleTask.class);
        task.dependsOn(WarPlugin.WAR_TASK_NAME);
        return task;
    }

    private StartTask createMicroStartTask() {
        StartTask task = createTask(START_TASK_NAME, START_TASK_DESCRIPTION, StartTask.class);
        task.dependsOn(WarPlugin.WAR_TASK_NAME);
        return task;
    }

    private StopTask createMicroStopTask() {
        return createTask(STOP_TASK_NAME, STOP_TASK_DESCRIPTION, StopTask.class);
    }

    private <T extends AbstractTask> T createTask(String name, String description, Class<T> action) {
        T task = project.getTasks().create(name, action);
        task.setGroup(WarPlugin.WEB_APP_GROUP);
        task.setDescription(description);
        task.setProject(project);
        return task;
    }

}
