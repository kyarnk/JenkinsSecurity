@Library('JenkinsSecurity@main') _  

import SemgrepUtils

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
                    sh """
                    if [ -z "\$(docker ps -q -f name=dvna)" ]; then
                        echo "Starting DVNA container..."
                        docker run --name dvna -p 9090:9090 -d appsecco/dvna:sqlite
                        sleep 5
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
                    def semgrepResults = runSemgrepScan()
                    writeFile file: 'semgrep-results.json', text: semgrepResults
                }
            }
        }

        stage('Publish Scan Results') {
            steps {
                script {
                    def defectdojo_api_url = 'http://192.168.0.195:8090/api/v2/findings/'
                    def api_key = '05390d3b4b4ce06cbbff77bcd0220543ffb7a6fc'
                    def engagement_id = '1'
                    def product_id = '1'

                    def jsonPayload = readFile('semgrep-results.json')
                    def defects = SemgrepUtils.parseSemgrepResults(jsonPayload)

                    defects.each { defect ->  
                        def payload = """
                        {
                            "test": "${engagement_id}",
                            "product": "${product_id}",
                            "title": "${defect.title}",
                            "severity": "${defect.severity}",
                            "description": "${defect.description}",
                            "date": "${defect.date}",
                            "file_path": "${defect.url}"
                        }
                        """
                        writeFile file: 'defect.json', text: payload

                        sh """
                        curl -X POST ${defectdojo_api_url} \
                            --header "Authorization: Token ${api_key}" \
                            --header "Content-Type: application/json" \
                            --data @defect.json
                        """
                    }
                }
            }
        }

        stage('Stop DVNA Container') {
            steps {
                script {
                    sh "docker stop dvna && docker rm dvna"
                }
            }
        }
    }
}

