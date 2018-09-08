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

import static fish.payara.gradle.plugins.micro.Configuration.DEFAULT_MICRO_VERSION;
import java.util.Map;
import org.gradle.api.Project;

public class PayaraMicroExtension {

    private Boolean skip = false;

    private Boolean immediateExit = true;

    private Boolean daemon = false;

    private String javaPath = "java";

    private Boolean useUberJar = false;

    private Boolean deployWar = false;

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

    public Boolean getSkip() {
        return skip;
    }

    public void setSkip(Boolean skip) {
        this.skip = skip;
    }

    public Boolean getImmediateExit() {
        return immediateExit;
    }

    public void setImmediateExit(Boolean immediateExit) {
        this.immediateExit = immediateExit;
    }

    public Boolean getDaemon() {
        return daemon;
    }

    public void setDaemon(Boolean daemon) {
        this.daemon = daemon;
    }

    public String getJavaPath() {
        return javaPath;
    }

    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath;
    }

    public Boolean getUseUberJar() {
        return useUberJar;
    }

    public void setUseUberJar(Boolean useUberJar) {
        this.useUberJar = useUberJar;
    }

    public Boolean getDeployWar() {
        return deployWar;
    }

    public void setDeployWar(Boolean deployWar) {
        this.deployWar = deployWar;
    }

    public String getPayaraMicroAbsolutePath() {
        return payaraMicroAbsolutePath;
    }

    public void setPayaraMicroAbsolutePath(String payaraMicroAbsolutePath) {
        this.payaraMicroAbsolutePath = payaraMicroAbsolutePath;
    }

    public String getPayaraVersion() {
        return payaraVersion;
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
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

}
