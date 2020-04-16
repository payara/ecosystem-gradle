/*
 *
 * Copyright (c) 2018-2020 Payara Foundation and/or its affiliates. All rights reserved.
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

import static fish.payara.gradle.plugins.micro.Configuration.JAR_EXTENSION;
import static fish.payara.gradle.plugins.micro.Configuration.MICROBUNDLE_EXTENSION;
import static fish.payara.gradle.plugins.micro.Configuration.MICRO_ARTIFACTID;
import static fish.payara.gradle.plugins.micro.Configuration.MICRO_GROUPID;
import static fish.payara.gradle.plugins.micro.Configuration.MICRO_THREAD_NAME;
import static fish.payara.gradle.plugins.micro.Configuration.WAR_EXTENSION;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.bundling.War;
import org.slf4j.Logger;

public abstract class AbstractTask extends ConventionTask {

    protected boolean skip;

    protected Project project;

    protected Process microProcess;

    protected ThreadGroup threadGroup;

    public AbstractTask() {
        threadGroup = new ThreadGroup(MICRO_THREAD_NAME);
    }

    public abstract void configure(PayaraMicroExtension extension);

    public void setProject(Project project) {
        this.project = project;
    }

    @Input
    protected String getProjectGAV() {
        return (String) project.getGroup() + ":" + project.getName() + ":" + (String) project.getVersion();
    }

    @Input
    public boolean isSkip() {
        return skip;
    }

    protected Optional<String> getPayaraMicroPath(String version) {
        DependencyHandler dependencyHandler = project.getDependencies();
        Dependency dependency = dependencyHandler.create(MICRO_GROUPID + ":" + MICRO_ARTIFACTID + ":" + version);

        return project.getConfigurations()
                .detachedConfiguration(dependency)
                .getResolvedConfiguration()
                .getFiles()
                .stream()
                .findAny()
                .map(File::getAbsolutePath);
    }

    @Internal
    protected War getWarTask() {
        return (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
    }
    
    @Internal
    protected Optional<File> getWarArchive() {
        if (getWarTask().getArchiveFile().isPresent()) {
            return Optional.of(getWarTask().getArchiveFile().get().getAsFile());
        } else {
            return Optional.empty();
        }
    }

    @Internal
    protected String getWarPath() {
        return getWarArchive()
                .map(File::getAbsolutePath)
                .orElseThrow(() -> new IllegalStateException("WarArchive not found"));
    }

    @Internal
    protected String getExplodedWarPath() {
        return getWarArchive()
                .map(File::getParent)
                .orElseThrow(() -> new IllegalStateException("WarArchive not found"))
                + File.separator 
                + FilenameUtils.getBaseName(getWarArchive().map(File::getName).get());
    }

    @OutputFile
    protected File getUberJar() {
        return new File(getUberJarPath());
    }

    @Internal
    protected String getUberJarPath() {
        return getWarTask()
                .getArchiveFile()
                .get()
                .getAsFile()
                .getAbsolutePath()
                .replace("." + WAR_EXTENSION, "-" + MICROBUNDLE_EXTENSION + "." + JAR_EXTENSION);
    }

    protected void copy(InputStream in, OutputStream out) throws IOException {
        try {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            out.close();
            in.close();
        }
    }

    protected void closeMicroProcess() {
        if (microProcess != null) {
            try {
                microProcess.exitValue();
            } catch (IllegalThreadStateException e) {
                microProcess.destroy();
                getLog().info("Terminated payara-micro.");
            }
        }
    }

    @Internal
    public abstract Logger getLog();
    
}
