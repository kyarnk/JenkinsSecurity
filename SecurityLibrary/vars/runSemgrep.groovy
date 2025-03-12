// vars/runSemgrepScan.groovy
def call() {
    echo 'Running Semgrep Scan'
    // Запуск сканирования Semgrep
    installSemgrep('dvna')
    runSemgrep('dvna', 'auto', 'semgrep-results.json')
    return readFile('semgrep-results.json')
}
