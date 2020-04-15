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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopTask extends AbstractTask {

    private static final String ERROR_MESSAGE = "Error occurred while terminating payara-micro";

    public static final String STOP_TASK_NAME = "microStop";

    public static final String STOP_TASK_DESCRIPTION = "Stops Payara Micro with the specified configuration.";

    private static final Logger LOG = LoggerFactory.getLogger(StopTask.class);

    private String processId;

    private boolean useUberJar;

    @Override
    public void configure(PayaraMicroExtension extension) {
        this.skip = extension.isSkip();
        this.useUberJar = extension.isUseUberJar();
        this.processId = extension.getProcessId();
    }

    @TaskAction
    public void onStop() {
        if (skip) {
            getLog().info("Stop mojo execution is skipped");
            return;
        }

        if (processId == null) {
            processId = findProcessId();
        }

        if (StringUtils.isNotEmpty(processId)) {
            killProcess(processId);
        } else {
            getLog().warn("Could not find process of running payara-micro?");
        }

    }

    String findProcessId() {
        String processId = null;
        String executorName;
        if (useUberJar) {
            executorName = getUberJarPath();
            executorName = executorName.substring(executorName.lastIndexOf(File.separator) + 1);
        } else {
            executorName = "-Dgav=" + getProjectGAV();
        }
        try {
            final Runtime re = Runtime.getRuntime();
            Process jpsProcess = re.exec("jps -v");
            InputStream inputStream = jpsProcess.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains(executorName)) {
                    String[] split = line.split(" ");
                    processId = split[0];
                }
            }
        } catch (IOException e) {
            getLog().error(ERROR_MESSAGE, e);
        }
        return processId;
    }

    private void killProcess(String processId) {
        String command = null;
        try {
            final Runtime re = Runtime.getRuntime();
            if (isUnix()) {
                command = "kill " + processId;
            } else if (isWindows()) {
                command = "taskkill /PID " + processId + " /F";
            }
            if (command == null) {
                throw new IllegalStateException("Operation system not supported!");
            }
            Process killProcess = re.exec(command);
            int result = killProcess.waitFor();
            if (result != 0) {
                getLog().error(ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException e) {
            getLog().error(ERROR_MESSAGE, e);
        }
    }

    private boolean isUnix() {
        String osName = System.getProperty("os.name");
        return osName.startsWith("Linux")
                || osName.startsWith("FreeBSD")
                || osName.startsWith("OpenBSD")
                || osName.startsWith("gnu")
                || osName.startsWith("gnu/kfreebsd")
                || osName.startsWith("netbsd")
                || osName.startsWith("Mac OS");
    }

    private boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName.startsWith("Windows CE")
                || osName.startsWith("Windows");
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public String getProcessId() {
        return processId;
    }

    public boolean isUseUberJar() {
        return useUberJar;
    }

}
