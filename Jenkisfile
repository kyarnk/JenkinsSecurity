pipeline {
    agent any

    stages {
        stage('Checkout Code') {
            steps {
                git url: 'https://github.com/kyarnk/JenkinsSecurity.git', branch: 'main'
            }
        }

        stage('Run Semgrep') {
            steps {
                script {
                    def workspace = pwd()
                    sh """
                    docker run --rm -v ${workspace}:/src returntocorp/semgrep semgrep --config=auto --json > semgrep-results.json
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
