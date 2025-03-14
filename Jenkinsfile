@Library('JenkinsSecurity@main') _  

pipeline {
    agent any

    environment {
        DEFECTDOJO_URL = 'http://192.168.0.195:8090/api/v2/findings/'
        DEFECTDOJO_API_KEY = credentials('defectdojo-api-key')
        ENGAGEMENT_ID = '1'
        PRODUCT_ID = '1'
    }

    options {
        timeout(time: 1, unit: 'HOURS')
        ansiColor('xterm')
    }

    stages {
        stage('Checkout Code') {
            steps {
                script {
                    try {
                        checkout scm: [
                            $class: 'GitSCM',
                            branches: [[name: 'main']],
                            userRemoteConfigs: [[
                                url: 'https://github.com/kyarnk/JenkinsSecurity.git',
                                credentialsId: 'github-credentials'
                            ]]
                        ]
                    } catch (Exception e) {
                        echo "Ошибка при checkout: ${e.message}"
                        error "Не удалось получить код из репозитория"
                    }
                }
            }
        }

        stage('Start DVNA Container') {
            steps {
                script {
                    try {
                        sh """
                        if [ -z "\$(docker ps -q -f name=dvna)" ]; then
                            echo "Starting DVNA container..."
                            docker run --name dvna -p 9090:9090 -d appsecco/dvna:sqlite
                            sleep 5
                        else
                            echo "DVNA is already running"
                        fi
                        """
                    } catch (Exception e) {
                        echo "Ошибка при запуске контейнера: ${e.message}"
                        error "Не удалось запустить DVNA контейнер"
                    }
                }
            }
        }

        stage('Security Scan') {
            steps {
                script {
                    try {
                        withCredentials([string(credentialsId: 'defectdojo-api-key', variable: 'DEFECTDOJO_API_KEY')]) {
                            def findings = runSecurityScan(
                                targetDir: '.',
                                defectDojoUrl: env.DEFECTDOJO_URL,
                                defectDojoApiKey: env.DEFECTDOJO_API_KEY,
                                engagementId: env.ENGAGEMENT_ID,
                                productId: env.PRODUCT_ID
                            )
                            
                            echo "Found ${findings.size()} security issues"
                        }
                    } catch (Exception e) {
                        echo "Ошибка при сканировании: ${e.message}"
                        error "Не удалось выполнить сканирование безопасности"
                    }
                }
            }
        }

        stage('Stop DVNA Container') {
            steps {
                script {
                    try {
                        sh "docker stop dvna && docker rm dvna"
                    } catch (Exception e) {
                        echo "Ошибка при остановке контейнера: ${e.message}"
                        // Не прерываем пайплайн, если не удалось остановить контейнер
                        echo "Предупреждение: Не удалось остановить контейнер DVNA"
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        failure {
            echo 'Pipeline failed! Проверьте логи для деталей.'
        }
        success {
            echo 'Pipeline успешно завершен!'
        }
    }
}

