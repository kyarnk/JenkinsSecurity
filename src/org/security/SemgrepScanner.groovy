package org.security

import groovy.json.JsonSlurper

class SemgrepScanner implements Serializable {
    private def script
    private String targetDir
    
    SemgrepScanner(script, String targetDir = '.', boolean isNode1 = true) {
        this.script = script
        this.targetDir = targetDir
    }
    
    def scan() {
        script.echo "Running scan inside DVNA container..."
        
        // Создаем директорию для сканирования
        script.sh """
            docker exec dvna mkdir -p /tmp/scan
            docker exec dvna ls -la /app
            docker exec dvna cp -r /app/* /tmp/scan/
        """
        
        // Запускаем сканирование
        def scanResult = script.sh(
            script: """
                docker exec dvna semgrep scan \
                    --config=auto \
                    --json \
                    /tmp/scan
            """,
            returnStdout: true
        ).trim()
        
        // Сохраняем результаты
        script.writeFile file: 'semgrep-results.json', text: scanResult
        
        return parseResults(scanResult)
    }
    
    def parseResults(String jsonResults) {
        def jsonSlurper = new JsonSlurper()
        def parsedJson = jsonSlurper.parseText(jsonResults)
        def findings = []
        
        if (parsedJson.results) {
            parsedJson.results.each { result ->
                findings << [
                    title: result.check_id,
                    severity: getSeverity(result.extra?.severity),
                    description: "${result.extra?.message ?: 'No message'}\nFound in: ${result.path}:${result.start?.line ?: 'unknown'}",
                    date: new Date().format("yyyy-MM-dd"),
                    url: result.path
                ]
            }
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