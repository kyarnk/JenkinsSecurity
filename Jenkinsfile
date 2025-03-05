pipeline {
    agent any

    environment {
        REMOTE_SERVER = "192.168.0.241" 
        REMOTE_DOCKER_CONTAINER_NAME = "dvna"
        NODE_APP_PATH = "/app" 
    }

    stages {
        stage('Run Semgrep on Remote Node.js App') {
            steps {
                script {
                    // Установим SSH соединение и запускаем команду на удалённом сервере
                    sh """
                    ssh -o StrictHostKeyChecking=no -i ~/.ssh/id_ed25519 kyarnk@${REMOTE_SERVER} "
                        docker exec ${REMOTE_DOCKER_CONTAINER_NAME} semgrep --config=auto --json --disable-nosem --verbose --exclude=node_modules ${NODE_APP_PATH} > semgrep-results.json
                    "
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
