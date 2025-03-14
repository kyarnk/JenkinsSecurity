// src/SemgrepUtils.groovy
class SemgrepUtils {
    
    static void installSemgrep(String dockerContainer) {
        echo "Checking if Semgrep is installed in container: ${dockerContainer}"
        sh """
        if ! docker exec ${dockerContainer} which semgrep > /dev/null; then
            echo "Installing Semgrep..."
            docker exec ${dockerContainer} apt-get update -y
            docker exec ${dockerContainer} apt-get install -y --force-yes python3 python3-pip
            docker exec ${dockerContainer} pip3 install semgrep
        fi
        """
    }

    static String runSemgrep(String dockerContainer, String config) {
        echo "Running Semgrep scan..."
        return sh(script: """
        docker exec ${dockerContainer} semgrep --config=${config} --json --exclude=node_modules --exclude=venv --exclude=.git /app
        """, returnStdout: true).trim()
    }

    static List parseSemgrepResults(String jsonResults) {
        def results = readJSON text: jsonResults
        def defects = []
        
        results.results.each { result -> 
            defects << [
                title: result.extra.message,
                severity: result.extra.severity ?: "Unknown",
                description: "Check ID: ${result.check_id}\nFile: ${result.path}\nLine: ${result.start.line}",
                date: new Date().format("yyyy-MM-dd"),
                url: "N/A"
            ]
        }
        return defects
    }
}
