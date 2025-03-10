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
			docker exec dvna sed -i 's/jessie/buster/g' /etc/apt/sources.list
                        docker exec dvna apt-get update -y
                        docker exec dvna apt-get install -y --force-yes python3 python3-pip
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
                script {
                    // Отправляем результаты в DefectDojo через API
                    def defectdojo_api_url = 'http://51.250.13.203:8080/api/key-v2'
                    def api_key = '7e01b91b9f5a1dcb109f9c205e85143786ad6f52'
                    def engagement_id = '1'  // ID вовлечения в DefectDojo
                    def product_id = '1' // ID продукта в DefectDojo
                    
                    def jsonPayload = readFile('semgrep-results.json')
                    
                    // Преобразуем результат Semgrep в формат, понятный DefectDojo
                    def defects = parseSemgrepResults(jsonPayload)
                    
                    // Отправляем данные в DefectDojo
                    defects.each { defect ->
                        sh """
                        curl -X POST ${defectdojo_api_url}finding/ --header "Authorization: Bearer ${api_key}" --header "Content-Type: application/json" --data '{
                            "engagement": "${engagement_id}",
                            "product": "${product_id}",
                            "title": "${defect.title}",
                            "severity": "${defect.severity}",
                            "description": "${defect.description}",
                            "date_detected": "${defect.date}",
                            "url": "${defect.url}"
                        }'
                        """
                    }
                }
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

def parseSemgrepResults(String jsonResults) {
    def results = readJSON text: jsonResults
    def defects = []
    results.each { result ->
        defects << [
            title: result.title,
            severity: result.severity,
            description: result.description,
            date: result.date,
            url: result.url
        ]
    }
    return defects
}
