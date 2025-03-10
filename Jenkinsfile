pipeline {
    agent any

    stages {
        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/kyarnk/JenkinsSecurity.git', branch: 'main'
            }
        }

        stage('Start DVNA Container') {
            steps {
                script {
                    // Проверяем запущен ли контейнер dvna, если нет — запускаем
                    sh """
                    if [ -z "\$(docker ps -q -f name=dvna)" ]; then
                        echo "Starting DVNA container..."
                        docker run --name dvna -p 9090:9090 -d appsecco/dvna:sqlite
                        sleep 5  # Даем контейнеру время на запуск
                    else
                        echo "DVNA is already running"
                    fi
                    """
                }
            }
        }

        stage('Run Semgrep Scan') {
            steps {
                script {
                    // Проверяем, установлен ли Semgrep, если нет — устанавливаем
                    sh """
                    if ! docker exec dvna which semgrep > /dev/null; then
                        echo "Installing Semgrep..."
			docker exec dvna sed -i 's/jessie/buster/g' /etc/apt/soursec.list
                        docker exec dvna apt-get update -y
                        docker exec dvna apt-get install -y python3 python3-pip
                        docker exec dvna pip3 install semgrep
                    fi
                    """

                    // Запускаем Semgrep
                    sh """
                    echo "Running Semgrep scan..."
                    docker exec dvna semgrep --config=auto --json --disable-nosem --verbose --exclude=node_modules /app > semgrep-results.json
                    """
                }
            }
        }

        stage('Publish Scan Results') {
            steps {
                archiveArtifacts artifacts: 'semgrep-results.json', fingerprint: true
            }
        }

        stage('Stop DVNA Container') {
            steps {
                script {
                    // Останавливаем и удаляем контейнер после сканирования
                    sh """
                    echo "Stopping and removing DVNA container..."
                    docker stop dvna && docker rm dvna
                    """
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline completed, cleaning up if needed"
        }
    }
}

