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
        return scanDVNAContainer()
    }
    
    private def scanDVNAContainer() {
        script.sh """
            # Создаем временную директорию в контейнере
            docker exec dvna mkdir -p /tmp/scan
            
            # Копируем код приложения для сканирования
            docker exec dvna cp -r /app/* /tmp/scan/
            
            # Запускаем сканирование внутри контейнера
            docker exec dvna bash -c 'cd /tmp/scan && semgrep scan --json --config=auto . > /tmp/semgrep-results.json'
            
            # Копируем результаты из контейнера
            docker cp dvna:/tmp/semgrep-results.json ./semgrep-results.json
            
            # Очищаем временные файлы в контейнере
            docker exec dvna rm -rf /tmp/scan /tmp/semgrep-results.json
        """
        return script.readFile('semgrep-results.json')
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