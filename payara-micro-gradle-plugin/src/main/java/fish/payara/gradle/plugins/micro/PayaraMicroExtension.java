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

import static fish.payara.gradle.plugins.micro.Configuration.DEFAULT_MICRO_VERSION;
import static fish.payara.gradle.plugins.micro.PayaraMicroPlugin.PLUGIN_ID;
import java.util.Map;
import org.gradle.api.Project;

public class PayaraMicroExtension {

    private boolean skip;

    private boolean immediateExit = true;

    private boolean daemon;

    private String javaPath = "java";

    private boolean useUberJar;

    private boolean deployWar;

    /**
     * Use exploded artifact for deployment.
     */
    private boolean exploded;

    /**
     * Attach a debugger. If set to "true", the process will suspend and wait
     * for a debugger to attach on port 5005. If set to other value, will be
     * appended to the argLine, allowing you to configure custom debug options.
     *
     */
    private String debug;

    private String payaraMicroAbsolutePath;

    private String payaraVersion = DEFAULT_MICRO_VERSION;

    private Map<String, Object> commandLineOptions;

    private Map<String, Object> javaCommandLineOptions;

    private String processId;

    private final Project project;

    /**
     * Creates a new {@code PayaraMicroExtension} that is associated with * the
     * given {@code project}.
     *
     * @param project the project
     */
    public PayaraMicroExtension(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public boolean isSkip() {
        return Boolean.valueOf(System.getProperty(PLUGIN_ID + ".skip", String.valueOf(skip)));
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean isImmediateExit() {
        return Boolean.valueOf(System.getProperty(PLUGIN_ID + ".immediateExit", String.valueOf(immediateExit)));
    }

    public void setImmediateExit(boolean immediateExit) {
        this.immediateExit = immediateExit;
    }

    public boolean isDaemon() {
        return Boolean.valueOf(System.getProperty(PLUGIN_ID + ".daemon", String.valueOf(daemon)));
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public String getJavaPath() {
        return System.getProperty(PLUGIN_ID + ".javaPath", javaPath);
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public boolean isUseUberJar() {
        return Boolean.valueOf(System.getProperty(PLUGIN_ID + ".useUberJar", String.valueOf(useUberJar)));
    }

    public void setUseUberJar(boolean useUberJar) {
        this.useUberJar = useUberJar;
    }

    public boolean isDeployWar() {
        return Boolean.valueOf(System.getProperty(PLUGIN_ID + ".deployWar", String.valueOf(deployWar)));
    }

    public void setDeployWar(boolean deployWar) {
        this.deployWar = deployWar;
    }

    public boolean isExploded() {
        return Boolean.valueOf(System.getProperty(PLUGIN_ID + ".exploded", String.valueOf(exploded)));
    }

    public void setExploded(boolean exploded) {
        this.exploded = exploded;
    }

    public String getDebug() {
        return System.getProperty(PLUGIN_ID + ".debug", debug);
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }

    public String getPayaraMicroAbsolutePath() {
        return System.getProperty(PLUGIN_ID + ".payaraMicroAbsolutePath", payaraMicroAbsolutePath);
    }

    public void setPayaraMicroAbsolutePath(String payaraMicroAbsolutePath) {
        this.payaraMicroAbsolutePath = payaraMicroAbsolutePath;
    }

    public String getPayaraVersion() {
        return System.getProperty(PLUGIN_ID + ".payaraVersion", payaraVersion);
    }

    public void setPayaraVersion(String payaraVersion) {
        this.payaraVersion = payaraVersion;
    }

    public Map<String, Object> getCommandLineOptions() {
        return commandLineOptions;
    }

    public void setCommandLineOptions(Map<String, Object> commandLineOptions) {
        this.commandLineOptions = commandLineOptions;
    }

    public Map<String, Object> getJavaCommandLineOptions() {
        return javaCommandLineOptions;
    }

    public void setJavaCommandLineOptions(Map<String, Object> javaCommandLineOptions) {
        this.javaCommandLineOptions = javaCommandLineOptions;
    }

    public String getProcessId() {
        return System.getProperty(PLUGIN_ID + ".processId", processId);
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

}
