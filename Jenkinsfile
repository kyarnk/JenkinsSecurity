@Library('security-library') _

pipeline {
    agent any

    environment {
        HOME_DIR       = '/home/kyarnk'  // Указываем свой путь к домашней директории
        SOURCE_PATH    = '/home/kyarnk/JenkinsSecurity'  // Путь к исходным файлам
        WORKSPACE_PATH = '/var/lib/jenkins/workspace/user-test'  // Рабочая директория
        IMAGE_NAME     = 'bkimminich/juice-shop' // Выбираем нужный нам image, если он уже в DockerHub или будет создан
        TARGET_URL     = 'https://juice-shop.kyarnk.ru' // Ссылка https для работы сканера

         // DefectDojo Environments
         DEFECTDOJO_URL        = 'http://51.250.92.214:8080' // Базовый URL, без /api/v2
         DEFECTDOJO_API_KEY_CRED_ID = 'defect-dojo_api_key' // ID креденшела Jenkins
         DEFECTDOJO_PRODUCT    = 'Juice Shop'
         DEFECTDOJO_ENGAGEMENT = "CI/CD Scan - Build ${BUILD_NUMBER}" // Динамическое имя для каждого запуска
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Semgrep Scan') {
            steps {
                // Передаем параметры в библиотеку
                runSemgrepScan(SOURCE_PATH, 'semgrep_report.json', HOME_DIR, WORKSPACE_PATH)
            }
        }

        stage('KICS Scan') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    // Передаем параметры в библиотеку
                    runKICSScan(SOURCE_PATH, 'kics_report.json', HOME_DIR, WORKSPACE_PATH)  
                }
            }
        }

        // stage('Build Docker Image') {
        //     steps {
        //         script {
        //             // Собираем Docker-образ для Juice Shop
        //             sh 'docker build -t juice-shop ${SOURCE_PATH}/juice-shop/'
        //         }
        //     }
        // }

        stage('SCA Scan (Syft & Grype)') {
            steps {
                // Запуск Syft и Grype через общий скрипт
                runSCAScan(IMAGE_NAME, 'syft_report.json', 'grype_report.json', HOME_DIR, WORKSPACE_PATH)
            }
        }

        stage('Run Docker Container') {
            steps {
                script {
                    // Поднимаем контейнер в фоновом режиме
                    sh """
                        docker run -d --name juice-shop-container -p 3000:3000 ${IMAGE_NAME}
                    """
                }
            }
        }

        stage('DAST Scan (ZAP)') {
            steps {
                runZAPScan(TARGET_URL, 'zap_report.json', HOME_DIR)
            }
        }

        stage('DAST Scan (Nuclei)') {
            steps {
                runNucleiScan(TARGET_URL, 'nuclei_report.json', HOME_DIR)
            }
        }

        stage('Move Reports') {
            steps {
                script {
                    sh 'cp ${HOME_DIR}/reports/kics_report.json/results.json ${WORKSPACE}/kics_report.json'
                    sh 'mv ${HOME_DIR}/reports/semgrep_report.json ${WORKSPACE}/'
                    sh 'mv ${HOME_DIR}/reports/syft_report.json ${WORKSPACE}/'
                    sh 'mv ${HOME_DIR}/reports/grype_report.json ${WORKSPACE}/'
                    sh 'mv ${HOME_DIR}/reports/zap_report.json ${WORKSPACE}/'
                    sh 'mv ${HOME_DIR}/reports/nuclei_report.json ${WORKSPACE}/'
                }
            }
        }

 
         // --- Интеграция с DefectDojo ---
         // ИСПРАВЛЕННЫЙ СТЕЙДЖ СБОРКИ
         stage('Build Uploader Image from Library') {
             steps {
                 script {
                     def uploaderBuildDir = "${WORKSPACE}/uploader_build"
                     try {
                         // Получаем содержимое файлов из библиотеки
                         def dockerfileContent = libraryResource 'scripts/uploader/Dockerfile'
                         def uploaderScriptContent = libraryResource 'scripts/uploader/defectdojo_uploader.py'

                         // Записываем содержимое во временные файлы в workspace
                         writeFile file: "${uploaderBuildDir}/Dockerfile", text: dockerfileContent
                         writeFile file: "${uploaderBuildDir}/defectdojo_uploader.py", text: uploaderScriptContent

                         echo "Building Docker image defectdojo-uploader:latest from context: ${uploaderBuildDir}"
                         // Собираем образ, используя директорию с файлами как контекст
                         sh "docker build -t defectdojo-uploader:latest ${uploaderBuildDir}"
                         echo "Docker image built successfully."

                     } catch (Exception e) {
                         error "Failed to build defectdojo-uploader image: ${e.getMessage()}. Check libraryResource paths and Docker setup."
                     }
                 }
             }
         }

         stage('Send Reports to DefectDojo') {
             steps {
                 script {
                     // ***** ВАЖНО: ПРОВЕРЬТЕ ЭТИ ТИПЫ В ВАШЕМ DEFECTDOJO v.2.45.1 *****
                     // Перейдите в DefectDojo -> Engagement -> Import Scan Results -> Посмотрите на выпадающий список "Scan type"
                     def reports = [
                         [name: 'semgrep_report.json', type: 'Semgrep JSON'],          // Скорее всего правильно
                         [name: 'kics_report.json',    type: 'KICS JSON'],            // Или 'KICS Scan'?
                         [name: 'syft_report.json',    type: 'CycloneDX'],           // Скорее всего правильно
                         [name: 'grype_report.json',   type: 'Grype JSON'],           // Или 'Anchore Grype'?
                         [name: 'zap_report.json',     type: 'ZAP Scan'],              // Скорее всего правильно
                         [name: 'nuclei_report.json',  type: 'Nuclei Scan']           // Или 'Nuclei JSON'?
                     ]
                     // *********************************************************************

                     reports.each { report ->
                         def reportPath = "${WORKSPACE}/${report.name}" // Отчеты теперь в WORKSPACE

                         if (fileExists(reportPath)) {
                             echo "Attempting to upload ${report.name} (Type: ${report.type})"
                             try {
                                 withCredentials([string(credentialsId: env.DEFECTDOJO_API_KEY_CRED_ID, variable: 'DD_API_KEY')]) {
                                     // Запускаем СОБРАННЫЙ контейнер
                                     sh """
                                         docker run --rm \\
                                             -v "${WORKSPACE}:/reports" \\
                                             --network host \\
                                             defectdojo-uploader:latest \\
                                             --url "${env.DEFECTDOJO_URL}" \\
                                             --key "${DD_API_KEY}" \\
                                             --product "${env.DEFECTDOJO_PRODUCT}" \\
                                             --engagement "${env.DEFECTDOJO_ENGAGEMENT}" \\
                                             --scan-type "${report.type}" \\
                                             --file "/reports/${report.name}"
                                     """
                                      echo "Upload command finished for ${report.name}."
                                 }
                             } catch (Exception e) {
                                 echo "Failed to upload ${report.name}: ${e.toString()}"
                                 currentBuild.result = 'UNSTABLE'
                             }
                         } else {
                             echo "Report file not found, skipping upload: ${reportPath}"
                         }
                     } // end reports.each
                 } // end script
             } // end steps
         } // end stage

        // stage('Send Reports to DefectDojo') {
        //     steps {
        //         script {
        //             uploadToDefectDojo(
        //                 reportName: 'semgrep_report.json',
        //                 scanType: 'Semgrep JSON',
        //                 productName: "${env.DEFECTDOJO_PRODUCT}",
        //                 engagementName: "${env.DEFECTDOJO_ENGAGEMENT}",
        //                 homeDir: "${env.WORKSPACE}"  // используем WORKSPACE, потому что туда всё переместили
        //             )

        //             uploadToDefectDojo(
        //                 reportName: 'kics_report.json',
        //                 scanType: 'KICS',
        //                 productName: "${env.DEFECTDOJO_PRODUCT}",
        //                 engagementName: "${env.DEFECTDOJO_ENGAGEMENT}",
        //                 homeDir: "${env.WORKSPACE}"
        //             )

        //             uploadToDefectDojo(
        //                 reportName: 'syft_report.json',
        //                 scanType: 'SBOM',
        //                 productName: "${env.DEFECTDOJO_PRODUCT}",
        //                 engagementName: "${env.DEFECTDOJO_ENGAGEMENT}",
        //                 homeDir: "${env.WORKSPACE}"
        //             )

        //             uploadToDefectDojo(
        //                 reportName: 'grype_report.json',
        //                 scanType: 'Grype',
        //                 productName: "${env.DEFECTDOJO_PRODUCT}",
        //                 engagementName: "${env.DEFECTDOJO_ENGAGEMENT}",
        //                 homeDir: "${env.WORKSPACE}"
        //             )

        //             uploadToDefectDojo(
        //                 reportName: 'zap_report.json',
        //                 scanType: 'ZAP Scan',
        //                 productName: "${env.DEFECTDOJO_PRODUCT}",
        //                 engagementName: "${env.DEFECTDOJO_ENGAGEMENT}",
        //                 homeDir: "${env.WORKSPACE}"
        //             )

        //             uploadToDefectDojo(
        //                 reportName: 'nuclei_report.json',
        //                 scanType: 'Nuclei Scan',
        //                 productName: "${env.DEFECTDOJO_PRODUCT}",
        //                 engagementName: "${env.DEFECTDOJO_ENGAGEMENT}",
        //                 homeDir: "${env.WORKSPACE}"
        //             )
        //         }
        //     }
        // }

        stage('Archive Report') {
            steps {
                archiveArtifacts artifacts: 'semgrep_report.json, kics_report.json, syft_report.json, grype_report.json, zap_report.json, nuclei_report.json', fingerprint: true
            }
        }
    }
}