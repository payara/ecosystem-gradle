/*
 *
 * Copyright (c) 2020 Payara Foundation and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReloadTask extends AbstractTask {

    public static final String RELOAD_TASK_NAME = "microReload";

    public static final String RELOAD_TASK_DESCRIPTION = "Redeploys the exploded WAR file.";

    private static final Logger LOG = LoggerFactory.getLogger(ReloadTask.class);

    private static final String RELOAD_FILE = ".reload";

    @Override
    public void configure(PayaraMicroExtension extension) {
        this.skip = extension.isSkip();
    }

    @TaskAction
    public void onReload() {

        if (skip) {
            getLog().info("Reload task execution is skipped");
            return;
        }

        File explodedDir = new File(getExplodedWarPath());
        if (!explodedDir.exists()) {
            throw new IllegalStateException(String.format("explodedDir[%s] not found", explodedDir.toString()));
        }
        File reloadFile = new File(explodedDir, RELOAD_FILE);
        if (reloadFile.exists()) {
            reloadFile.setLastModified(System.currentTimeMillis());
        } else {
            try {
                reloadFile.createNewFile();
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to create .reload file " + ex.toString());
            }
        }

    }

    @Override
    public Logger getLog() {
        return LOG;
    }

}
