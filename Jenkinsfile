@Library('JenkinsSecurity@main') _  

pipeline {
    agent any

    environment {
        DEFECTDOJO_URL = 'http://192.168.0.195:8090/api/v2/findings/'
        DEFECTDOJO_API_KEY = credentials('05390d3b4b4ce06cbbff77bcd0220543ffb7a6fc')
        ENGAGEMENT_ID = '1'
        PRODUCT_ID = '1'
    }

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

        stage('Security Scan') {
            steps {
                script {
                    def findings = runSecurityScan(
                        targetDir: '.',
                        defectDojoUrl: env.DEFECTDOJO_URL,
                        defectDojoApiKey: env.DEFECTDOJO_API_KEY,
                        engagementId: env.ENGAGEMENT_ID,
                        productId: env.PRODUCT_ID
                    )
                    
                    echo "Found ${findings.size()} security issues"
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

