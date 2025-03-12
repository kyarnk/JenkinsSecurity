// src/SemgrepUtils.groovy
def installSemgrep(dockerContainer) {
    sh """
    if ! docker exec ${dockerContainer} which semgrep > /dev/null; then
        echo "Installing Semgrep..."
        docker exec ${dockerContainer} apt-get update -y
        docker exec ${dockerContainer} apt-get install -y --force-yes python3 python3-pip
        docker exec ${dockerContainer} pip3 install semgrep
    fi
    """
}

def runSemgrep(dockerContainer, config, outputFile) {
    sh """
    echo "Running Semgrep scan..."
    docker exec ${dockerContainer} semgrep --config=${config} --json --disable-nosem --verbose --exclude=node_modules /app > ${outputFile}
    """
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
