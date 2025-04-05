@Library('security-library') _

pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Debug') {
            steps {
                script {
                    sh 'pwd'
                    echo "NODE_NAME = ${env.NODE_NAME}"
                    echo "WORKSPACE = ${env.WORKSPACE}"
                    sh 'ls -lah'
                }
            }
        }

        // Pre-build: Анализ кода
        stage('Pre-build') {
            steps {
                script {
                    // Используем вашу функцию для сканирования Semgrep
                    runSemgrepScan("/var/lib/jenkins/juice-shop", 'semgrep_report.json')
                }
            }
        }

        // Archive Reports: Архивация отчетов
        stage('Archive Reports') {
            steps {
                script {
                    // Архивация отчетов
                    archiveArtifacts artifacts: '$HOME/reports/*.json', fingerprint: true
                    echo 'Reports archived.'
                }
            }
        }
    }
}