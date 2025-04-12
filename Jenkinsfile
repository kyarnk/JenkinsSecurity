@Library('security-library') _

pipeline {
    agent {
        docker {
            image 'semgrep/semgrep'
        }
    }

    environment {
        REPORT_DIR = 'reports'
        OUTPUT_FILE = 'semgrep_report.json'
    }

    stages {
        stage('Prepare') {
            steps {
                sh 'mkdir -p ${REPORT_DIR}'
            }
        }

        stage('Semgrep Scan') {
            steps {
                sh """
                    semgrep --config=auto . -o ${REPORT_DIR}/${OUTPUT_FILE}
                """
            }
        }

        stage('Archive Report') {
            steps {
                archiveArtifacts artifacts: "${REPORT_DIR}/*.json", fingerprint: true
            }
        }
    }
}
