/*
 *
 * Copyright (c) [2024] Payara Foundation and/or its affiliates. All rights reserved.
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

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.gradle.api.artifacts.DependencySet;

public class PayaraMicroVersionSelector {

    private static final String MAVEN_METADATA_URL = "https://repo1.maven.org/maven2/fish/payara/extras/payara-micro/maven-metadata.xml";
    private static final String GROUP_ID_JAKARTA = "jakarta.platform";
    private static final String GROUP_ID_JAVAX = "javax";
    private static final String ARTIFACT_ID_JAVAEE_WEB_API = "javaee-web-api";
    private static final String ARTIFACT_ID_JAVAEE_API = "javaee-api";

    private static final Map<Integer, String> JAKARTA_TO_PAYARA_MAP = new HashMap<>();
    private final Project project;

    public PayaraMicroVersionSelector(Project project) {
        this.project = project;
    }

    static {
        JAKARTA_TO_PAYARA_MAP.put(11, "7."); // Jakarta EE 11 -> Payara Micro 7.x
        JAKARTA_TO_PAYARA_MAP.put(10, "6."); // Jakarta EE 10 -> Payara Micro 6.x
        JAKARTA_TO_PAYARA_MAP.put(9, "6.");  // Jakarta EE 9 -> Payara Micro 6.x
        JAKARTA_TO_PAYARA_MAP.put(8, "5.");  // Jakarta EE 8 -> Payara Micro 5.x
    }

    public String fetchPayaraVersion() throws GradleException {
        String jakartaVersion = getJakartaVersion();
        String payaraMicroVersion = null;
        if (jakartaVersion != null) {
            int jakartaMajorVersion = getJakartaMajorVersion(jakartaVersion);
            if (jakartaMajorVersion != -1) {
                payaraMicroVersion = getPayaraMicroVersion(jakartaMajorVersion);
                if (payaraMicroVersion != null) {
                    project.getLogger().info("Selected Payara Micro version '" + payaraMicroVersion + "' for Jakarta EE version '" + jakartaVersion + "'.");
                } else {
                    project.getLogger().warn("Could not determine the appropriate Payara Micro version.");
                }
            } else {
                project.getLogger().warn("Invalid Jakarta EE version: " + jakartaVersion);
            }
        } else {
            payaraMicroVersion = getLatestPayaraMicroVersion();
            if (payaraMicroVersion != null) {
                project.getLogger().info("No Jakarta EE dependency found. Using latest Payara Micro version: " + payaraMicroVersion);
            } else {
                project.getLogger().warn("Could not fetch the latest Payara Micro version.");
            }
        }
        return payaraMicroVersion;
    }

    private String getJakartaVersion() {
       DependencySet dependencies = project.getConfigurations().getByName("compileClasspath").getAllDependencies();
        for (Dependency dependency : dependencies) {
            if (GROUP_ID_JAKARTA.equals(dependency.getGroup())) {
                return dependency.getVersion();
            }
            if (GROUP_ID_JAVAX.equals(dependency.getGroup())
                    && (ARTIFACT_ID_JAVAEE_API.equals(dependency.getName()) || ARTIFACT_ID_JAVAEE_WEB_API.equals(dependency.getName()))) {
                return dependency.getVersion();
            }
        }
        return null;
    }

    private int getJakartaMajorVersion(String jakartaVersion) {
        try {
            return Integer.parseInt(jakartaVersion.split("\\.")[0]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String getPayaraMicroVersion(int jakartaMajorVersion) throws GradleException {
        return fetchPayaraMicroVersion(version -> version.startsWith(JAKARTA_TO_PAYARA_MAP.get(jakartaMajorVersion)));
    }

    private String getLatestPayaraMicroVersion() throws GradleException {
        return fetchPayaraMicroVersion(version -> true);
    }

    private String fetchPayaraMicroVersion(Predicate<String> versionPredicate) throws GradleException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(MAVEN_METADATA_URL);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    String xmlContent = EntityUtils.toString(entity);
                    SAXBuilder saxBuilder = new SAXBuilder();
                    Document document = saxBuilder.build(new StringReader(xmlContent));
                    Element rootElement = document.getRootElement();
                    Element versionsElement = rootElement.getChild("versioning").getChild("versions");

                    if (versionsElement != null) {
                        List<Element> versionElements = versionsElement.getChildren("version");

                        for (int i = versionElements.size() - 1; i >= 0; i--) {
                            String version = versionElements.get(i).getText();
                            if (versionPredicate.test(version)) {
                                return version;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new GradleException("Error fetching Payara Micro version", e);
        }
        return null;
    }
}
