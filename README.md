# TEST Jenkins Security Library

Библиотека для автоматизации проверок безопасности в Jenkins pipeline.

## Возможности

- Сканирование кода с помощью Semgrep
- Публикация результатов в DefectDojo

## Использование

1. Добавьте библиотеку в Jenkins:
   - Перейдите в Jenkins > Manage Jenkins > Configure System
   - В секции "Global Pipeline Libraries" добавьте:
     - Name: JenkinsSecurity
     - Default version: main
     - Retrieval method: Modern SCM
     - Source Code Management: Git
     - Project Repository: https://github.com/kyarnk/JenkinsSecurity.git

2. Используйте в вашем Jenkinsfile:

```groovy
@Library('JenkinsSecurity@main') _

pipeline {
    agent any
    
    environment {
        DEFECTDOJO_URL = 'http://your-defectdojo-url/api/v2/findings/'
        DEFECTDOJO_API_KEY = credentials('defectdojo-api-key')
        ENGAGEMENT_ID = 'your-engagement-id'
        PRODUCT_ID = 'your-product-id'
    }
    
    stages {
        stage('Security Scan') {
            steps {
                script {
                    runSecurityScan(
                        targetDir: '.',
                        defectDojoUrl: env.DEFECTDOJO_URL,
                        defectDojoApiKey: env.DEFECTDOJO_API_KEY,
                        engagementId: env.ENGAGEMENT_ID,
                        productId: env.PRODUCT_ID
                    )
                }
            }
        }
    }
}
```

## Требования

- Jenkins с установленным Docker
- Python 3.x для Semgrep
- Доступ к DefectDojo API

## Конфигурация

Создайте следующие credentials в Jenkins:
- defectdojo-api-key: API ключ для доступа к DefectDojo
