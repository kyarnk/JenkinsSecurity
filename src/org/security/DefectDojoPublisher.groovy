package org.security

class DefectDojoPublisher implements Serializable {
    private def script
    private String apiUrl
    private String apiKey
    private String engagementId
    private String productId
    
    DefectDojoPublisher(script, String apiUrl, String apiKey, String engagementId, String productId) {
        this.script = script
        this.apiUrl = apiUrl
        this.apiKey = apiKey
        this.engagementId = engagementId
        this.productId = productId
    }
    
    def publishFindings(findings) {
        findings.each { finding ->
            def payload = script.writeJSON returnText: true, json: [
                test: engagementId,
                product: productId,
                title: finding.title,
                severity: finding.severity,
                description: finding.description,
                date: finding.date,
                file_path: finding.url
            ]
            
            script.writeFile file: 'finding.json', text: payload
            
            script.sh """
                curl -X POST ${apiUrl} \
                    --header 'Authorization: Token ${apiKey}' \
                    --header 'Content-Type: application/json' \
                    --data @finding.json
            """
        }
    }
} 