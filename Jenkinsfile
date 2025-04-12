@Library('security-library') _
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Semgrep Scan') {
            steps {
                runSemgrepScan('.', 'semgrep_report.json')  // Используем библиотеку
            }
        }

        stage ('Move reports') {
            steps {
                script {
                    sh 'mv /home/kyarnk/reports/*.json $WORKSPACE/'
                }
            }
        }


        stage('Archive Report') {
            steps {
                archiveArtifacts artifacts: 'semgrep_report.json', fingerprint: true
            }
        }
    }
}
