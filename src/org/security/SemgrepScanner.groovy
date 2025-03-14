package org.security

class SemgrepScanner implements Serializable {
    private def script
    private String targetDir
    
    SemgrepScanner(script, String targetDir = '.') {
        this.script = script
        this.targetDir = targetDir
    }
    
    def scan() {
        script.sh """
            python3 -m pip install semgrep
            semgrep scan --json --config=auto ${targetDir} > semgrep-results.json
        """
        return script.readFile('semgrep-results.json')
    }
    
    def parseResults(String jsonResults) {
        def parsedJson = script.readJSON text: jsonResults
        def findings = []
        
        parsedJson.results.each { result ->
            findings << [
                title: result.check_id,
                severity: getSeverity(result.extra.severity),
                description: "${result.extra.message}\nFound in: ${result.path}:${result.start.line}",
                date: new Date().format("yyyy-MM-dd"),
                url: result.path
            ]
        }
        
        return findings
    }
    
    private String getSeverity(String semgrepSeverity) {
        def severityMap = [
            'ERROR': 'High',
            'WARNING': 'Medium',
            'INFO': 'Low'
        ]
        return severityMap.getOrDefault(semgrepSeverity?.toUpperCase(), 'Info')
    }
} 