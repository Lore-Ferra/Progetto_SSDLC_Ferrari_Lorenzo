pipeline {
    agent any

    tools {
        maven 'Maven-3.9.10'
    }

    environment {
        SONARQUBE_ENV = 'SonarQubeLocal'
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
                    }
                }
            }
        }

        stage('SCA - OWASP Dependency Check') {
            steps {
                echo "Running Software Composition Analysis with OWASP Dependency Check..."
                sh 'mvn org.owasp:dependency-check-maven:check'
            }
        }

        stage('Archiviazione Report') {
            steps {
                archiveArtifacts artifacts: 'repo_git/onlinebookstore/target/dependency-check-report.*', fingerprint: true
            }
        }
    }
}
