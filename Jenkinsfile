@Library('security-library') _

pipeline {
    agent any

    environment {
        SOURCE_PATH = "${env.WORKSPACE}/juice-shop"
        REPORT_DIR  = "${env.WORKSPACE}/reports"
    }

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
                    echo "${env.WORKSPACE}"
                    sh 'ls -lah'
                }
            }
        }

        stage('Prepare') {
            steps {
                script {
                    // Создаём папку для всех отчётов
                    sh "mkdir -p ${REPORT_DIR}"
                }
            }
        }
    
        // Pre-build: Анализ кода
        stage('Pre-build') {
            steps {
                script {
                    // Сканируем исходный код (SAST)
                    runSemgrepScan(SOURCE_PATH, 'semgrep_report.json')
                    
                    // Проверка зависимостей на уязвимости (SCA)
                    runSCAScan('bkimminich/juice-shop', 'syft_report.json', 'grype_report.json')

                    // Проверка инфраструктуры как кода (IaC) для дальнейшей реализации в облаке к примеру
                    try {
                        runKICSScan('/home/JenkinsSecurity/kics-test', 'kics_report.json')
                    } catch (Exception e) {
                        echo "KICS scan failed: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }

        // Build: Сборка Docker-образа
        stage('Build') {
            steps {
                script {
                    // Сборка Docker-образа
                    sh 'docker build -t juice-shop-kyarnk:latest .'
                }
            }
        }

        // Post-build: DAST и проверка собранного образа (запущенное в тестовой среде)
        stage('Post-build') {
            steps {
                script {
                    // Динамическое тестирование с помощью OWASP ZAP и Nuclei
                    runZAPScan('https://juiceshop.kyarnk.ru', 'zap_report.json')
                    runNucleiScan('https://juiceshop.kyarnk.ru', 'nuclei_report.json')

                    // Проверка собранного Docker-образа с помощью Grype
                    runSCAScan('juice-shop', 'syft_report_after_build.json', 'grype_report_after_build.json')
                }
            }
        }

        // Deploy: Развертывание контейнера
        stage('Deploy') {
            steps {
                script {
                    // Запуск контейнера после сборки
                    sh 'docker run -d --name juice-shop -p 3000:3000 juice-shop-kyarnk:latest'
                }
            }
        }

        // Archive Reports: Архивация отчетов
        stage('Archive Reports') {
            steps {
                script {
                    // Архивация отчетов
                    archiveArtifacts artifacts: 'semgrep_report.json, zap_report.json, nuclei_report.json, syft_report.json, grype_report.json, kics_report.json, syft_report_after_build.json, grype_report_after_build.json', fingerprint: true
                    echo 'Reports archived.'
                }
            }
        }
    }
}