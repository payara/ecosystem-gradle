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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BundleTask extends AbstractTask {

    private static final String ERROR_MESSAGE = "Error occurred while bundling payara-micro";

    public static final String BUNDLE_TASK_NAME = "microBundle";

    public static final String BUNDLE_TASK_DESCRIPTION = "Bundles payara-micro with the attached project's artifact into uber jar";

    private static final Logger LOG = LoggerFactory.getLogger(BundleTask.class);

    private String javaPath;

    private String payaraVersion;

    @Override
    public void configure(PayaraMicroExtension extension) {
        this.skip = extension.isSkip();
        this.javaPath = extension.getJavaPath();
        this.payaraVersion = extension.getPayaraVersion();
    }

    @TaskAction
    public void onBundle() {
        if (skip) {
            getLog().info("Bundle task execution is skipped");
            return;
        }
        Optional<String> pathOpt = getPayaraMicroPath(payaraVersion);
        if (!pathOpt.isPresent()) {
            getLog().info("payara-micro path not found");
            return;
        }

        final List<String> actualArgs = new ArrayList<>();
        getLog().info("Bundling payara-micro from path: " + pathOpt.get());
        int indice = 0;
        actualArgs.add(indice++, javaPath);
        actualArgs.add(indice++, "-Dgav=" + getProjectGAV());
        actualArgs.add(indice++, "-jar");
        actualArgs.add(indice++, pathOpt.get());
        actualArgs.add(indice++, "--deploy");
        actualArgs.add(indice++, getWarPath());
        actualArgs.add(indice++, "--outputUberJar");
        actualArgs.add(indice++, getUberJarPath());

        final Runtime re = Runtime.getRuntime();
        try {
            microProcess = re.exec(actualArgs.toArray(new String[actualArgs.size()]));
            copy(microProcess.getInputStream(), System.out);
            copy(microProcess.getErrorStream(), System.err);

            int exitCode = microProcess.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException(ERROR_MESSAGE);
            }
        } catch (InterruptedException | IOException ex) {
            getLog().error(ERROR_MESSAGE, ex);
        } finally {
            closeMicroProcess();
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public String getPayaraVersion() {
        return payaraVersion;
    }

    public String getJavaPath() {
        return javaPath;
    }

}
