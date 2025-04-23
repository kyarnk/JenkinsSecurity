@Library('security-library') _

pipeline {
    agent any

    environment {
        HOME_DIR       = '/home/kyarnk'  // Указываем свой путь к домашней директории
        SOURCE_PATH    = '/home/kyarnk/JenkinsSecurity'  // Путь к исходным файлам
        WORKSPACE_PATH = '/var/lib/jenkins/workspace/user-test'  // Рабочая директория
        IMAGE_NAME     = 'bkimminich/juice-shop' // Выбираем нужный нам image, если он уже в DockerHub или будет создан
        TARGET_URL     = 'https://juice-shop.kyarnk.ru' // Ссылка https для работы сканера

        // DefectDojo Environments
        DEFECTDOJO_URL        = 'http://51.250.92.214:8080'
        DEFECTDOJO_API_KEY    = credentials('defect-dojo_api_key')
        DEFECTDOJO_PRODUCT    = 'Juice Shop' 
        DEFECTDOJO_ENGAGEMENT = 'Initial Security Scan'
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
                runSCAScan(IMAGE_NAME, 'syft_report.json', 'grype_report.json', HOME_DIR, WORKSPACE_PATH)
            }
        }

        stage('Run Docker Container') {
            steps {
                script {
                    // Поднимаем контейнер в фоновом режиме
                    sh """
                        docker run -d --name juice-shop-container -p 3000:3000 ${IMAGE_NAME}
                    """
                }
            }
        }

        stage('DAST Scan (ZAP)') {
            steps {
                runZAPScan(TARGET_URL, 'zap_report.json', HOME_DIR)
            }
        }

        stage('DAST Scan (Nuclei)') {
            steps {
                runNucleiScan(TARGET_URL, 'nuclei_report.json', HOME_DIR)
            }
        }

        stage('Send Reports to DefectDojo') {
            steps {
                script {
                    uploadToDefectDojo('semgrep_report.json', 'Semgrep JSON', DEFECTDOJO_PRODUCT, DEFECTDOJO_ENGAGEMENT, HOME_DIR)
                    uploadToDefectDojo('kics_report.json', 'KICS', DEFECTDOJO_PRODUCT, DEFECTDOJO_ENGAGEMENT, HOME_DIR)
                    uploadToDefectDojo('syft_report.json', 'SBOM', DEFECTDOJO_PRODUCT, DEFECTDOJO_ENGAGEMENT, HOME_DIR)
                    uploadToDefectDojo('grype_report.json', 'Grype', DEFECTDOJO_PRODUCT, DEFECTDOJO_ENGAGEMENT, HOME_DIR)
                    uploadToDefectDojo('zap_report.json', 'ZAP Scan', DEFECTDOJO_PRODUCT, DEFECTDOJO_ENGAGEMENT, HOME_DIR)
                    uploadToDefectDojo('nuclei_report.json', 'Nuclei Scan', DEFECTDOJO_PRODUCT, DEFECTDOJO_ENGAGEMENT, HOME_DIR)
                }
            }
        }

        stage('Move Reports') {
            steps {
                script {
                    sh 'cp ${HOME_DIR}/reports/kics_report.json/results.json ${WORKSPACE}/kics_report.json'
                    sh 'mv ${HOME_DIR}/reports/semgrep_report.json ${WORKSPACE}/'
                    sh 'mv ${HOME_DIR}/reports/syft_report.json ${WORKSPACE}/'
                    sh 'mv ${HOME_DIR}/reports/grype_report.json ${WORKSPACE}/'
                    sh 'mv ${HOME_DIR}/reports/zap_report.json ${WORKSPACE}/'
                    sh 'mv ${HOME_DIR}/reports/nuclei_report.json ${WORKSPACE}/'
                }
            }
        }

        stage('Archive Report') {
            steps {
                archiveArtifacts artifacts: 'semgrep_report.json, kics_report.json, syft_report.json, grype_report.json, zap_report.json, nuclei_report.json', fingerprint: true
            }
        }
    }
}