pipeline { agent any

    options {
        timestamps()
    }

    stages {
        stage('Build (Java 8)') {
            tools {
                jdk 'OpenJDK 8'
            }
            steps {
                sh './gradlew clean classes testClasses'
            }
        }
        stage('Test (Java 8)') {
            tools {
                jdk 'OpenJDK 8'
            }
            steps {
                sh './gradlew test jacocoTestReport'
            }
            post {
                always {
                    junit 'build/test-results/*/*.xml'
                    recordCoverage(tools: [[parser: 'JACOCO', pattern: '**/jacocoTestReport.xml']])
                }
            }
        }
        stage('Binary (Java 8)') {
            tools {
                jdk 'OpenJDK 8'
            }
            steps {
                sh './gradlew fatJar'
                archiveArtifacts artifacts: 'build/libs/frimgur-jar-with-dependencies.jar', fingerprint: true
            }
        }
        stage('Compatibility (Java 17)') {
            tools {
                jdk 'OpenJDK 17'
            }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh './gradlew clean test'
                }
            }
        }
        stage('Compatibility (Java 21)') {
            tools {
                jdk 'OpenJDK 21'
            }
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    sh './gradlew clean test'
                }
            }
        }
    }
}

// vi: ts=4 sw=4 et si
