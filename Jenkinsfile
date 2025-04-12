// @Library('security-library') _
// pipeline {
//     agent any

//     stages {
//         stage('Checkout') {
//             steps {
//                 checkout scm
//             }
//         }

//         stage('Semgrep Scan') {
//             steps {
//                 runSemgrepScan('.', 'semgrep_report.json')  // Используем библиотеку
//             }
//         }

//         stage ('Move reports') {
//             steps {
//                 script {
//                     sh 'mv /home/kyarnk/reports/*.json $WORKSPACE/'
//                 }
//             }
//         }


//         stage('Archive Report') {
//             steps {
//                 archiveArtifacts artifacts: 'semgrep_report.json', fingerprint: true
//             }
//         }
//     }
// }
@Library('security-library') _

pipeline {
    agent any

    environment {
        HOME_DIR = '/home/kyarnk'  // Указываем свой путь к домашней директории
        SOURCE_PATH = '/home/kyarnk/JenkinsSecurity'  // Путь к исходным файлам
        WORKSPACE_PATH = '/var/lib/jenkins/workspace/user-test'  // Рабочая директория
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Semgrep Scan') {
            steps {
                // Передаем параметры в библиотеку
                runSemgrepScan(SOURCE_PATH, 'semgrep_report.json', HOME_DIR, WORKSPACE_PATH)
            }
        }

        stage('KICS Scan') {
            steps {
                // Передаем параметры в библиотеку
                runKICSScan(SOURCE_PATH, 'kics_report.json', HOME_DIR, WORKSPACE_PATH)
            }
        }

        stage('Move Reports') {
            steps {
                script {
                    sh 'mv /home/kyarnk/reports/*.json ${WORKSPACE}/'
                }
            }
        }

        stage('Archive Report') {
            steps {
                archiveArtifacts artifacts: '**/*', fingerprint: true
            }
        }
    }
}