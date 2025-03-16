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
        timestamps()
    }

    stages {
        stage('Debug Info') {
            steps {
                script {
                    echo "Checking environment..."
                    sh """
                        echo "Workspace: \${WORKSPACE}"
                        echo "Jenkins Home: \${JENKINS_HOME}"
                        echo "Java Version:"
                        java -version
                        echo "Git Version:"
                        git --version
                        echo "Docker Version:"
                        docker --version
                        echo "Current User:"
                        whoami
                        echo "Current Directory:"
                        pwd
                        echo "Directory Contents:"
                        ls -la
                    """
                }
            }
        }

        stage('Checkout Code') {
            steps {
                script {
                    echo "Starting checkout stage..."
                    try {
                        checkout scm: [
                            $class: 'GitSCM',
                            branches: [[name: 'main']],
                            userRemoteConfigs: [[
                                url: 'https://github.com/kyarnk/JenkinsSecurity.git',
                                credentialsId: 'github-credentials'
                            ]]
                        ]
                        echo "Checkout completed successfully"
                        sh 'ls -la'
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
                    echo "Starting DVNA container stage..."
                    try {
                        sh """
                        echo "Checking Docker status..."
                        docker info
                        
                        echo "Checking existing containers..."
                        docker ps -a
                        
                        if [ -z "\$(docker ps -q -f name=dvna)" ]; then
                            echo "Starting DVNA container..."
                            docker run --name dvna -p 9090:9090 -d appsecco/dvna:sqlite
                            echo "Waiting for container to start..."
                            sleep 5
                            
                            echo "Installing Python and Semgrep in container..."
                            docker exec dvna apt-get update
                            docker exec dvna apt-get install -y --force-yes python3 python3-pip
                            docker exec dvna pip3 install semgrep
                            
                            echo "Container status:"
                            docker ps | grep dvna
                        else
                            echo "DVNA container is already running"
                            echo "Updating Python and Semgrep in container..."
                            docker exec dvna apt-get update
                            docker exec dvna apt-get install -y --force-yes python3 python3-pip
                            docker exec dvna pip3 install --upgrade semgrep
                            docker ps | grep dvna
                        fi
                        """
                    } catch (Exception e) {
                        echo "Ошибка при запуске Docker: ${e.message}"
                        error "Не удалось запустить DVNA контейнер"
                    }
                }
            }
        }

        stage('Security Scan') {
            steps {
                script {
                    echo "Starting security scan stage..."
                    try {
                        withCredentials([string(credentialsId: 'defectdojo-api-key', variable: 'DEFECTDOJO_API_KEY')]) {
                            echo "Credentials loaded successfully"
                            echo "Starting Semgrep scan in DVNA container..."
                            
                            def findings = runSecurityScan(
                                targetDir: '.',
                                defectDojoUrl: env.DEFECTDOJO_URL,
                                defectDojoApiKey: env.DEFECTDOJO_API_KEY,
                                engagementId: env.ENGAGEMENT_ID,
                                productId: env.PRODUCT_ID,
                                isNode1: true
                            )
                            
                            echo "Scan completed. Found ${findings.size()} security issues"
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
                    echo "Starting container cleanup stage..."
                    try {
                        sh """
                        echo "Stopping DVNA container..."
                        docker stop dvna
                        echo "Removing DVNA container..."
                        docker rm dvna
                        echo "Cleanup completed"
                        """
                    } catch (Exception e) {
                        echo "Ошибка при остановке контейнера: ${e.message}"
                        echo "Предупреждение: Не удалось остановить контейнер DVNA"
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished - cleaning workspace"
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

