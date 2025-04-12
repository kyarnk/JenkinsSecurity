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
        
        stage('Semgrep Scan') {
            steps {
                script {
                    // По умолчанию сканирует весь workspace
                    runSemgrepScan()
                    
                    // Или сканировать только определённую папку:
                    // runSemgrepScan("${env.WORKSPACE}/my-app")
                }
            }
        }


        // stage('Pre-build') {
        //     steps {
        //         script {
        //             // Используем вашу функцию для сканирования Semgrep
        //             runSemgrepScan("/var/lib/jenkins/juice-shop", 'semgrep_report.json')
        //         }
        //     }
        // }

        // Archive Reports: Архивация отчетов
        stage('Archive Reports') {
            steps {
                script {
                    // Архивация отчетов
                    archiveArtifacts artifacts: 'reports/*.json', fingerprint: true
                    echo 'Reports archived.'
                }
            }
        }
    }
}