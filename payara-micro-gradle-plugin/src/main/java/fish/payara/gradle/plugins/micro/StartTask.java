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

import static fish.payara.gradle.plugins.micro.Configuration.MICRO_READY_MESSAGE;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.Objects.nonNull;
import java.util.concurrent.TimeUnit;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartTask extends AbstractTask {

    public static final String START_TASK_NAME = "microStart";

    public static final String START_TASK_DESCRIPTION = "Assembles the JavaEE app into a war and deploys it to payara-micro.";

    private static final String ERROR_MESSAGE = "Errors occurred while executing payara-micro.";

    private static final Logger LOG = LoggerFactory.getLogger(StartTask.class);

    private Boolean immediateExit;

    protected Boolean daemon;

    private String javaPath;

    private Boolean useUberJar;

    private Boolean deployWar;

    private String payaraMicroAbsolutePath;

    private String payaraVersion;

    private Map<String, Object> javaCommandLineOptions;

    private Map<String, Object> commandLineOptions;

    private Thread microProcessorThread;

    @Override
    public void configure(PayaraMicroExtension extension) {
        this.skip = extension.getSkip();
        this.immediateExit = extension.getImmediateExit();
        this.daemon = extension.getDaemon();
        this.javaPath = extension.getJavaPath();
        this.useUberJar = extension.getUseUberJar();
        this.deployWar = extension.getDeployWar();
        this.payaraMicroAbsolutePath = extension.getPayaraMicroAbsolutePath();
        this.payaraVersion = extension.getPayaraVersion();
        this.javaCommandLineOptions = extension.getJavaCommandLineOptions();
        this.commandLineOptions = extension.getCommandLineOptions();
    }

    @TaskAction
    public void onStart() {

        if (skip) {
            getLog().info("Start task execution is skipped");
            return;
        }

        final String path = decideOnWhichMicroToUse();
        microProcessorThread = new Thread(threadGroup, () -> {
            final List<String> actualArgs = new ArrayList<>();
            getLog().info("Starting payara-micro from path: " + path);
            int indice = 0;
            actualArgs.add(indice++, javaPath);
            if (javaCommandLineOptions != null) {
                for (Entry<String, Object> entry : javaCommandLineOptions.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (isNotEmpty(key) && nonNull(value) && isNotEmpty(value.toString())) {
                        if (key.startsWith("D")) {
                            String systemProperty = String.format("-%s=%s", key, value);
                            actualArgs.add(indice++, systemProperty);
                        } else {
                            String option = String.format("-%s:%s", key, value);
                            actualArgs.add(indice++, option);
                        }
                    } else if (isNotEmpty(key)) {
                        actualArgs.add(indice++, "-" + key);
                    }
                }

            }
            actualArgs.add(indice++, "-Dgav=" + getProjectGAV());
            actualArgs.add(indice++, "-jar");
            actualArgs.add(indice++, path);
            if (deployWar) {
                actualArgs.add(indice++, "--deploy");
                actualArgs.add(indice++, getWarPath());
            }
            if (commandLineOptions != null) {
                for (Entry<String, Object> entry : commandLineOptions.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (isNotEmpty(key) && nonNull(value) && isNotEmpty(value.toString())) {
                        actualArgs.add(indice++, "--" + key);
                        actualArgs.add(indice++, String.valueOf(value));
                    }
                }
            }
            System.out.println("args " + actualArgs);
            try {
                final Runtime re = Runtime.getRuntime();
                microProcess = re.exec(actualArgs.toArray(new String[actualArgs.size()]));

                if (daemon) {
                    redirectStream(microProcess.getInputStream(), System.out);
                    redirectStream(microProcess.getErrorStream(), System.err);
                } else {
                    redirectStreamToGivenOutputStream(microProcess.getInputStream(), System.out);
                    redirectStreamToGivenOutputStream(microProcess.getErrorStream(), System.err);
                }

                int exitCode = microProcess.waitFor();
                if (exitCode != 0) {
                    throw new IllegalStateException(ERROR_MESSAGE);
                }

            } catch (InterruptedException ex) {
            } catch (Exception ex) {
                throw new RuntimeException(ERROR_MESSAGE, ex);
            } finally {
                if (!daemon) {
                    closeMicroProcess();
                }
            }
        });

        final Thread shutdownHook = new Thread(threadGroup, () -> {
            if (microProcess != null && microProcess.isAlive()) {
                try {
                    microProcess.destroy();
                    microProcess.waitFor(1, TimeUnit.MINUTES);
                } catch (InterruptedException ignored) {
                } finally {
                    microProcess.destroyForcibly();
                }
            }
        });

        if (daemon) {
            microProcessorThread.setDaemon(true);
            microProcessorThread.start();

            if (!immediateExit) {
                try {
                    microProcessorThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
            microProcessorThread.run();
        }
    }

    private void redirectStream(final InputStream inputStream, final PrintStream printStream) {
        final Thread thread = new Thread(threadGroup, () -> {
            BufferedReader br;
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                br = new BufferedReader(new InputStreamReader(inputStream));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    printStream.println(line);
                    if (!immediateExit && sb.toString().contains(MICRO_READY_MESSAGE)) {
                        microProcessorThread.interrupt();
                        break;
                    }
                }
            } catch (IOException e) {
                getLog().error("Error occurred while reading stream", e);
            }
        });
        thread.setDaemon(false);
        thread.start();
    }

    private void redirectStreamToGivenOutputStream(final InputStream inputStream, final OutputStream outputStream) {
        Thread thread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {
                try {
                    copy(inputStream, outputStream);
                } catch (IOException e) {
                    getLog().error("Error occurred while reading stream", e);
                }
            }
        });
        thread.setDaemon(false);
        thread.start();
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    String decideOnWhichMicroToUse() {
        IllegalStateException exception = new IllegalStateException("Could not determine Payara Micro path. Please set it by defining either \"useUberJar\", \"payaraMicroAbsolutePath\" or \"artifactItem\" configuration options.");
        if (useUberJar) {
            String path = getUberJarPath();

            if (!Files.exists(Paths.get(path))) {
                throw new IllegalStateException("\"useUberJar\" option was set to \"true\" but detected path " + path + " does not exist. You need to execute the \"bundle\" task before using this option.");
            }

            return path;
        }

        if (payaraMicroAbsolutePath != null) {
            return payaraMicroAbsolutePath;
        }

        if (payaraVersion != null) {
            return getPayaraMicroPath(payaraVersion).orElseThrow(() -> exception);
        }

        throw exception;
    }

    public Boolean getImmediateExit() {
        return immediateExit;
    }

    public Boolean getDaemon() {
        return daemon;
    }

    public String getJavaPath() {
        return javaPath;
    }

    public Boolean getUseUberJar() {
        return useUberJar;
    }

    public Boolean getDeployWar() {
        return deployWar;
    }

    public String getPayaraMicroAbsolutePath() {
        return payaraMicroAbsolutePath;
    }

    public String getPayaraVersion() {
        return payaraVersion;
    }

    public Map<String, Object> getJavaCommandLineOptions() {
        return javaCommandLineOptions;
    }

    public Map<String, Object> getCommandLineOptions() {
        return commandLineOptions;
    }

}
