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
        IMAGE_NAME = 'bkimminich/juice-shop'
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
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    // Передаем параметры в библиотеку
                    runKICSScan(SOURCE_PATH, 'kics_report.json', HOME_DIR, WORKSPACE_PATH)  
                }
            }
        }

        // stage('Build Docker Image') {
        //     steps {
        //         script {
        //             // Собираем Docker-образ для Juice Shop
        //             sh 'docker build -t juice-shop ${SOURCE_PATH}/juice-shop/'
        //         }
        //     }
        // }

        stage('SCA Scan (Syft & Grype)') {
            steps {
                // Запуск Syft и Grype через общий скрипт
                runScaScan(IMAGE_NAME, 'syft_report.json', 'grype_report.json', HOME_DIR, WORKSPACE_PATH)
            }
        }

        stage('Move Reports') {
            steps {
                script {
                    sh 'cp ${HOME_DIR}/reports/kics_report.json/results.json ${WORKSPACE}/kics_report.json'
                    sh 'mv ${HOME_DIR}/reports/semgrep_report.json ${WORKSPACE}/'
                    sh 'mv ${HOME_DIR}/reports/syft_report.json ${WORKSPACE}/'
                    sh 'mv ${HOME_DIR}/reports/grype_report.json ${WORKSPACE}/'

                }
            }
        }

        stage('Archive Report') {
            steps {
                archiveArtifacts artifacts: 'semgrep_report.json, kics_report.json, syft_report.json, grype_report.json', fingerprint: true
            }
        }
    }
}