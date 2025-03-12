// vars/runSemgrepScan.groovy
def call() {
    echo 'Running Semgrep Scan'
    // Устанавливаем Semgrep и запускаем сканирование
    SemgrepUtils.installSemgrep('dvna')
    SemgrepUtils.runSemgrep('dvna', 'auto', 'semgrep-results.json')
    return readFile('semgrep-results.json')  // Возвращаем результаты сканирования
}
