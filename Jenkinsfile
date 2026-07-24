pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    parameters {
        booleanParam(name: 'PUBLISH', defaultValue: false,
                description: 'Publish the verified modules to the configured Maven repository')
        string(name: 'FRAMEWORK_VERSION', defaultValue: '3.0.0-SNAPSHOT',
                description: 'Version assigned to all framework modules')
        string(name: 'REPOSITORY_URL', defaultValue: '',
                description: 'Nexus/Artifactory Maven repository URL; required only when PUBLISH=true')
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle-ci"
    }

    stages {
        stage('Environment') {
            steps {
                sh 'java -version'
                sh './gradlew --version'
            }
        }

        stage('Secret scan') {
            steps {
                sh '''
                    set -eu
                    ! grep -RInE --exclude-dir=.gradle --exclude-dir=build --exclude='*.md' \\
                      '(password|access[_-]?key|secret|token)[[:space:]]*[:=][[:space:]]*[^$<{[:space:]]+' .
                '''
            }
        }

        stage('Framework verification') {
            steps {
                sh './gradlew clean frameworkCheck build sourcesJar javadocJar -PframeworkVersion=${FRAMEWORK_VERSION}'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
                    archiveArtifacts allowEmptyArchive: true,
                            artifacts: '**/build/libs/*.jar, **/build/reports/tests/**, **/build/reports/problems/**',
                            fingerprint: true
                }
            }
        }

        stage('Consumer smoke') {
            steps {
                sh './gradlew publishToMavenLocal -PframeworkVersion=${FRAMEWORK_VERSION}'
                sh './gradlew -p examples/consumer-smoke clean test -PframeworkVersion=${FRAMEWORK_VERSION}'
            }
        }

        stage('Publish') {
            when {
                expression { return params.PUBLISH }
            }
            steps {
                script {
                    if (!params.REPOSITORY_URL?.trim()) {
                        error('REPOSITORY_URL is required when PUBLISH=true')
                    }
                }
                withCredentials([usernamePassword(
                        credentialsId: 'nexus-cukes-framework',
                        usernameVariable: 'NEXUS_USER',
                        passwordVariable: 'NEXUS_PASSWORD')]) {
                    sh '''
                        ./gradlew publish \\
                          -PframeworkVersion=${FRAMEWORK_VERSION} \\
                          -PrepositoryUrl=${REPOSITORY_URL}
                    '''
                }
            }
        }
    }

    post {
        always {
            deleteDir()
        }
    }
}
