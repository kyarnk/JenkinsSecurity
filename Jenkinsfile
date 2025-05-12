@Library('security-library') _

pipeline {
    agent any

    environment {
        HOME_DIR       = '/home/kyarnk'  // Указываем свой путь к домашней директории
        SOURCE_PATH    = '/home/kyarnk/JenkinsSecurity'  // Путь к исходным файлам
        WORKSPACE_PATH = '/var/lib/jenkins/workspace/user-test'  // Рабочая директория
        IMAGE_NAME     = 'bkimminich/juice-shop' // Выбираем нужный нам image, если он уже в DockerHub или будет создан
        TARGET_URL     = 'https://juice-shop.kyarnk.ru' // Ссылка https для работы сканера

        //  // DefectDojo Environments
        //  DEFECTDOJO_URL        = 'http://51.250.92.214:8080' // Базовый URL, без /api/v2
        //  DEFECTDOJO_API_KEY_CRED_ID = 'defect-dojo_api_key' // ID креденшела Jenkins
        //  DEFECTDOJO_PRODUCT    = 'Juice Shop'
        //  DEFECTDOJO_ENGAGEMENT = "CI/CD Scan - Build ${BUILD_NUMBER}" // Динамическое имя для каждого запуска
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }


        stage('Upload to DD') {
            steps {
                script {
                    sh 'docker run -d --name upload-dd docker.io/zhuzha/upload-to-dd:newest'
                    sh 'docker logs upload-dd'
                }
            }
        }

//         stage('Semgrep Scan') {
//             steps {
//                 // Передаем параметры в библиотеку
//                 runSemgrepScan(SOURCE_PATH, 'semgrep_report.json', HOME_DIR, WORKSPACE_PATH)
//             }
//         }

//         stage('KICS Scan') {
//             steps {
//                 catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
//                     // Передаем параметры в библиотеку
//                     runKICSScan(SOURCE_PATH, 'kics_report.json', HOME_DIR, WORKSPACE_PATH)  
//                 }
//             }
//         }

//         // stage('Build Docker Image') {
//         //     steps {
//         //         script {
//         //             // Собираем Docker-образ для Juice Shop
//         //             sh 'docker build -t juice-shop ${SOURCE_PATH}/juice-shop/'
//         //         }
//         //     }
//         // }

//         stage('SCA Scan (Syft & Grype)') {
//             steps {
//                 // Запуск Syft и Grype через общий скрипт
//                 runSCAScan(IMAGE_NAME, 'syft_report.json', 'grype_report.json', HOME_DIR, WORKSPACE_PATH)
//             }
//         }

//         stage('Run Docker Container') {
//             steps {
//                 script {
//                     // Поднимаем контейнер в фоновом режиме
//                     sh """
//                         docker run -d --name juice-shop-container -p 3000:3000 ${IMAGE_NAME}
//                     """
//                 }
//             }
//         }

//         stage('DAST Scan (ZAP)') {
//             steps {
//                 runZAPScan(TARGET_URL, 'zap_report.json', HOME_DIR)
//             }
//         }

//         stage('DAST Scan (Nuclei)') {
//             steps {
//                 runNucleiScan(TARGET_URL, 'nuclei_report.json', HOME_DIR)
//             }
//         }

//         stage('Move Reports') {
//             steps {
//                 script {
//                     sh 'cp ${HOME_DIR}/reports/kics_report.json/results.json ${WORKSPACE}/kics_report.json'
//                     sh 'mv ${HOME_DIR}/reports/semgrep_report.json ${WORKSPACE}/'
//                     sh 'mv ${HOME_DIR}/reports/syft_report.json ${WORKSPACE}/'
//                     sh 'mv ${HOME_DIR}/reports/grype_report.json ${WORKSPACE}/'
//                     sh 'mv ${HOME_DIR}/reports/zap_report.json ${WORKSPACE}/'
//                     sh 'mv ${HOME_DIR}/reports/nuclei_report.json ${WORKSPACE}/'
//                 }
//             }
//         }

 
//         //  // --- Интеграция с DefectDojo (Сборка образа из WORKSPACE) ---
//         //  stage('Build Uploader Image') {
//         //      steps {
//         //          script {
//         //              // Путь к Dockerfile ОТНОСИТЕЛЬНО WORKSPACE после checkout scm
//         //              def dockerfileDir = "${WORKSPACE}/scripts/uploader"
//         //              def dockerfilePath = "${dockerfileDir}/Dockerfile"

//         //              // Используем только fileExists для проверки Dockerfile
//         //              if (fileExists(dockerfilePath)) {
//         //                  echo "Found Dockerfile at ${dockerfilePath}. Building Docker image defectdojo-uploader:latest from context: ${dockerfileDir}"
//         //                  // Собираем образ, используя директорию из checkout как контекст
//         //                  // Оборачиваем в try-catch на случай ошибок Docker
//         //                  try {
//         //                     sh "docker build -t defectdojo-uploader:latest ${dockerfileDir}"
//         //                     echo "Docker image built successfully."
//         //                  } catch (Exception e) {
//         //                     error "Failed during docker build for ${dockerfileDir}: ${e.getMessage()}"
//         //                  }
//         //              } else {
//         //                  // Если Dockerfile не найден в репозитории SCM, сборка невозможна
//         //                  error "Uploader Dockerfile not found at ${dockerfilePath}. Please add scripts/uploader/Dockerfile and defectdojo_uploader.py to your source code repository."
//         //              }
//         //          }
//         //      }
//         //  }

//         //  stage('Send Reports to DefectDojo') {
//         //      steps {
//         //          script {
//         //              // ***** ВАЖНО: ПРОВЕРЬТЕ ЭТИ ТИПЫ В ВАШЕМ DEFECTDOJO v.2.45.1 *****
//         //              def reports = [
//         //                  [name: 'semgrep_report.json', type: 'Semgrep JSON'],
//         //                  [name: 'kics_report.json',    type: 'KICS JSON'],
//         //                  [name: 'syft_report.json',    type: 'CycloneDX'],
//         //                  [name: 'grype_report.json',   type: 'Grype JSON'], // или 'Anchore Grype'?
//         //                  [name: 'zap_report.json',     type: 'ZAP Scan'],
//         //                  [name: 'nuclei_report.json',  type: 'Nuclei Scan'] // или 'Nuclei JSON'?
//         //              ]
//         //              // *********************************************************************

//         //              reports.each { report ->
//         //                  def reportPath = "${WORKSPACE}/${report.name}"

//         //                  if (fileExists(reportPath)) {
//         //                      echo "Attempting to upload ${report.name} (Type: ${report.type})"
//         //                      try {
//         //                          withCredentials([string(credentialsId: env.DEFECTDOJO_API_KEY_CRED_ID, variable: 'DD_API_KEY')]) {
//         //                              // Запускаем ЛОКАЛЬНО СОБРАННЫЙ контейнер
//         //                              sh """
//         //                                  docker run --rm \\
//         //                                      -v "${WORKSPACE}:/reports" \\
//         //                                      --network host \\
//         //                                      defectdojo-uploader:latest \\
//         //                                      --url "${env.DEFECTDOJO_URL}" \\
//         //                                      --key "${DD_API_KEY}" \\
//         //                                      --product "${env.DEFECTDOJO_PRODUCT}" \\
//         //                                      --engagement "${env.DEFECTDOJO_ENGAGEMENT}" \\
//         //                                      --scan-type "${report.type}" \\
//         //                                      --file "/reports/${report.name}"
//         //                              """
//         //                               echo "Upload command finished for ${report.name}."
//         //                          }
//         //                      } catch (Exception e) {
//         //                          echo "Failed to upload ${report.name}: ${e.toString()}"
//         //                          currentBuild.result = 'UNSTABLE'
//         //                      }
//         //                  } else {
//         //                      echo "Report file not found, skipping upload: ${reportPath}"
//         //                  }
//         //              }
//         //          }
//         //      }
//         //  }

//         stage('Archive Report') {
//             steps {
//                 archiveArtifacts artifacts: 'semgrep_report.json, kics_report.json, syft_report.json, grype_report.json, zap_report.json, nuclei_report.json', fingerprint: true
//             }
//         }
//     }
// }