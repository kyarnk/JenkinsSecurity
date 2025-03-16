import org.security.SemgrepScanner
import org.security.DefectDojoPublisher

def call(Map config) {
    echo "Initializing security scan..."
    
    def scanner = new SemgrepScanner(
        this,
        config.targetDir ?: '.',
        config.isNode1 ?: false
    )
    def publisher = new DefectDojoPublisher(
        this,
        config.defectDojoUrl,
        config.defectDojoApiKey,
        config.engagementId,
        config.productId
    )
    
    // Запуск сканирования
    echo "Starting Semgrep scan..."
    if (config.isNode1) {
        echo "Running scan inside DVNA container..."
    } else {
        echo "Running local scan..."
    }
    
    def results = scanner.scan()
    echo "Scan completed, parsing results..."
    
    // Парсинг результатов
    def findings = scanner.parseResults(results)
    echo "Found ${findings.size()} issues"
    
    // Публикация в DefectDojo
    if (findings.size() > 0) {
        echo "Publishing findings to DefectDojo..."
        publisher.publishFindings(findings)
        echo "Findings published successfully"
    } else {
        echo "No findings to publish"
    }
    
    return findings
} 