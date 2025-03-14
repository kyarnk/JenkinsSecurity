// vars/runSemgrepScan.groovy
def call() {
    echo 'Running Semgrep Scan'
    
    SemgrepUtils.installSemgrep('dvna')
    def semgrepResults = SemgrepUtils.runSemgrep('dvna', '--config=auto')

    writeFile file: 'semgrep-results.json', text: semgrepResults
    return semgrepResults
}
