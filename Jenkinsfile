pipeline {
    agent any

    tools {
        maven 'Maven-3.9.10'
    }

    environment {
        SONARQUBE_ENV = 'SonarQubeLocal'
        NVD_API_KEY = credentials('NVD_API_KEY')
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Cloning the repository..."
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "Building the project with Maven..."
                dir('repo_git/onlinebookstore') {
                    sh 'mvn clean install'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('repo_git/onlinebookstore') {
                    withSonarQubeEnv("${env.SONARQUBE_ENV}") {
                        sh 'mvn sonar:sonar'
                        sh 'cat target/sonar/report-task.txt || true'
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    echo "Aspetto 15 secondi per permettere a SonarQube di completare l'analisi..."
                    sleep time: 15, unit: 'SECONDS'
                }
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('SCA - OWASP Dependency Check') {
            steps {
                dir('repo_git/onlinebookstore') {
                    echo "Running Software Composition Analysis with OWASP Dependency Check..."
                    sh 'mvn org.owasp:dependency-check-maven:check -Dnvd.api.key=$NVD_API_KEY'
                }
            }
        }

        stage('Archiviazione Artefatti') {
            steps {
                archiveArtifacts artifacts: 'repo_git/onlinebookstore/target/*.war', fingerprint: true
                archiveArtifacts artifacts: 'repo_git/onlinebookstore/target/dependency-check-report.*', fingerprint: true
            }
        }

        stage('Notifica') {
            steps {
                script {
                    def msg = [
                        username: "Jenkins Bot",
                        content: ":shield: *Build completata!* Progetto `onlinebookstore` ha superato tutte le scansioni di sicurezza."
                    ]

                    def webhook = credentials('DISCORD_WEBHOOK_URL')

                    httpRequest httpMode: 'POST',
                                contentType: 'APPLICATION_JSON',
                                url: webhook,
                                requestBody: groovy.json.JsonOutput.toJson(msg)
                }
            }
        }
    }
}
