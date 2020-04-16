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

import static fish.payara.gradle.plugins.micro.BundleTask.BUNDLE_TASK_DESCRIPTION;
import static fish.payara.gradle.plugins.micro.BundleTask.BUNDLE_TASK_NAME;
import static fish.payara.gradle.plugins.micro.ExplodeWarTask.EXPLODE_TASK_DESCRIPTION;
import static fish.payara.gradle.plugins.micro.ExplodeWarTask.EXPLODE_TASK_NAME;
import static fish.payara.gradle.plugins.micro.ReloadTask.RELOAD_TASK_DESCRIPTION;
import static fish.payara.gradle.plugins.micro.ReloadTask.RELOAD_TASK_NAME;
import static fish.payara.gradle.plugins.micro.StartTask.START_TASK_DESCRIPTION;
import static fish.payara.gradle.plugins.micro.StartTask.START_TASK_NAME;
import static fish.payara.gradle.plugins.micro.StopTask.STOP_TASK_DESCRIPTION;
import static fish.payara.gradle.plugins.micro.StopTask.STOP_TASK_NAME;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.War;
  
public class PayaraMicroPlugin implements Plugin<Project> {

    public static final String PLUGIN_ID = "payaraMicro";

    public static final String PAYARA_MICRO_GROUP = "Payara Micro";

    private Project project;

    @Override
    public void apply(Project project) {
        this.project = project;
        project.getPluginManager().apply(JavaPlugin.class);
        project.getPluginManager().apply(WarPlugin.class);
        StartTask startTask = createMicroStartTask();
        StopTask stopTask = createMicroStopTask();
        BundleTask bundleTask = createMicroBundleTask();
        ReloadTask reloadTask = createMicroReloadTask();
        ExplodeWarTask explodeWarTask = createMicroExplodeWarTask();
        PayaraMicroExtension extension = createExtension();
        project.afterEvaluate(prj -> {
            startTask.configure(extension);
            stopTask.configure(extension);
            bundleTask.configure(extension);
            reloadTask.configure(extension);
            configureExplodeWarTask();
        });
    }

    PayaraMicroExtension createExtension() {
        PayaraMicroExtension ext = project.getExtensions().findByType(PayaraMicroExtension.class);
        if (ext == null) {
            ext = project.getExtensions()
                    .create(PLUGIN_ID, PayaraMicroExtension.class, project);
        }
        return ext;
    }

    private BundleTask createMicroBundleTask() {
        BundleTask task = createTask(BUNDLE_TASK_NAME, BUNDLE_TASK_DESCRIPTION, BundleTask.class);
        task.dependsOn(WarPlugin.WAR_TASK_NAME);
        return task;
    }

    private StartTask createMicroStartTask() {
        return createTask(START_TASK_NAME, START_TASK_DESCRIPTION, StartTask.class);
    }

    private StopTask createMicroStopTask() {
        return createTask(STOP_TASK_NAME, STOP_TASK_DESCRIPTION, StopTask.class);
    }

    private ReloadTask createMicroReloadTask() {
        return createTask(RELOAD_TASK_NAME, RELOAD_TASK_DESCRIPTION, ReloadTask.class);
    }

    private ExplodeWarTask createMicroExplodeWarTask() {
        return createTask(EXPLODE_TASK_NAME, EXPLODE_TASK_DESCRIPTION, ExplodeWarTask.class);
    }

    ExplodeWarTask configureExplodeWarTask() {
        ExplodeWarTask task = null;
        War war = (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
        if (war != null && war.getArchiveFile().isPresent()) {
            task = (ExplodeWarTask) project.getTasks().getByName(EXPLODE_TASK_NAME);
            task.dependsOn(war);
            task.setWarFile(war.getArchiveFile().get().getAsFile().toPath());
            task.setExplodedWarDirectory(
                    war.getArchiveFile().get().getAsFile().getParentFile().toPath().resolve(
                            FilenameUtils.getBaseName(war.getArchiveFileName().get())
                    )
            );
        }
        return task;
    }

    private <T extends Task> T createTask(String name, String description, Class<T> action) {
        T task = project.getTasks().create(name, action);
        task.setGroup(PAYARA_MICRO_GROUP);
        task.setDescription(description);
        if (task instanceof AbstractTask) {
            ((AbstractTask) task).setProject(project);
        }
        return task;
    }

}