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
    }
}
