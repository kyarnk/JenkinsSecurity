import org.security.SemgrepScanner
import org.security.DefectDojoPublisher

def call(Map config) {
    def scanner = new SemgrepScanner(this, config.targetDir ?: '.')
    def publisher = new DefectDojoPublisher(
        this,
        config.defectDojoUrl,
        config.defectDojoApiKey,
        config.engagementId,
        config.productId
    )
    
    // Запуск сканирования
    def results = scanner.scan()
    
    // Парсинг результатов
    def findings = scanner.parseResults(results)
    
    // Публикация в DefectDojo
    publisher.publishFindings(findings)
    
    return findings
} 