pipeline {

    agent {
        label 'general-purpose'
    }
    tools {
        jdk "zulu-11"
    }
    environment {
        JAVA_HOME = tool("zulu-11")
        GRADLE_HOME = "/usr/lib/gradle/jenkinstools/gradle-8.0"
        PATH = "${GRADLE_HOME}/bin:${env.PATH}"
        MAVEN_OPTS = '-Xmx2G -Djavax.net.ssl.trustStore=${JAVA_HOME}/jre/lib/security/cacerts'
        payaraBuildNumber = "${BUILD_NUMBER}"
    }
    stages {

        stage('Build') {
            steps {
                script {
                    echo '*#*#*#*#*#*#*#*#*#*#*#*#  Building SRC  *#*#*#*#*#*#*#*#*#*#*#*#*#*#*#'
                    sh '''
                    ls -lrt
                    cd payara-micro-gradle-plugin
                    gradle clean build
                    '''
                    echo '*#*#*#*#*#*#*#*#*#*#*#*#    Built SRC   *#*#*#*#*#*#*#*#*#*#*#*#*#*#*#'
                }
            }
        }
    }
}
