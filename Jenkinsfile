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
                script {
                    runSemgrepScan('/home/kyarnk/juice-shop', 'semgrep_report.json')

                    // Выводим результат в консоль
                    def report = readFile('semgrep_report.json')
                    echo "Semgrep Scan Results: \n${report}"
                }
            }
        }

        stage('OWASP ZAP Scan') {
            steps {
                script {
                    runZAPScan('https://juiceshop.kyarnk.ru', 'zap_report.json')
                    
                }
            }
        }

        stage('Nuclei Scan') {
            steps {
                script {
                    runNucleiScan('https://juiceshop.kyarnk.ru', 'nuclei_report.json')
                }
            }
        }

        stage('Archive Report') {
            steps {
                archiveArtifacts artifacts: ['semgrep_report.json, zap-reports/zap_report.json, nuclei-reports/nuclei_report.json'], fingerprint: true
            }
        }
    }
}



