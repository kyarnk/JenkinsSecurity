pipeline {
    agent any

    stages {
        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/kyarnk/JenkinsSecurity.git', branch: 'main'
            }
        }

        stage('Run Semgrep on Remote Node.js App') {
            steps {
                script {
                    def workspace = pwd()
                    sh """
                    # Проверяем, установлен ли semgrep в контейнере, и если нет, устанавливаем его
                    ssh -o StrictHostKeyChecking=no -i /var/lib/jenkins/.ssh/id_ed25519 kyarnk@192.168.0.241 '
                      if ! docker exec dvna which semgrep; then
                        echo "Semgrep not found, installing it..."
                        # Обновляем репозитории Debian до актуальных
                        docker exec dvna sed -i "s/http:\/\/deb.debian.org/http:\/\/ftp.debian.org/g" /etc/apt/sources.list
                        docker exec dvna apt-get update -y
                        # Устанавливаем Python и pip3, если их нет
                        docker exec dvna apt-get install -y python3 python3-pip
                        docker exec dvna pip3 install semgrep
                      fi
                    '

                    # Запускаем semgrep в контейнере
                    ssh -o StrictHostKeyChecking=no -i /var/lib/jenkins/.ssh/id_ed25519 kyarnk@192.168.0.241 '
                      docker exec dvna semgrep --config=auto --json --disable-nosem --verbose --exclude=node_modules /app > semgrep-results.json
                    '
                    """
                }
            }
        }

        stage('Publish Results') {
            steps {
                archiveArtifacts artifacts: 'semgrep-results.json', fingerprint: true
            }
        }
    }
}
