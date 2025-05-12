# # defectdojo_uploader.py
# import requests
# import argparse
# import os
# import sys
# from datetime import date, timedelta

# # --- Вспомогательные функции ---
# def make_api_request(method, url, api_key, **kwargs):
#     """Отправляет запрос к API DefectDojo и обрабатывает ответ."""
#     headers = {'Authorization': f'Token {api_key}', 'Content-Type': 'application/json'}
#     try:
#         # Увеличиваем таймаут для запросов, особенно для загрузки файлов
#         timeout = kwargs.pop('timeout', 300) # 5 минут по умолчанию
#         response = requests.request(method, url, headers=headers, timeout=timeout, **kwargs)
#         response.raise_for_status()
#         # Для методов GET, которые могут вернуть пустой список, а не ошибку
#         if method.upper() == 'GET' and response.status_code == 200:
#              # Проверка на случай пустого ответа, который не является JSON
#             if response.text:
#                 try:
#                     return response.json()
#                 except requests.exceptions.JSONDecodeError:
#                     print(f"Предупреждение: Ответ API не является JSON для GET {url}: {response.text[:100]}...")
#                     return None # Или другое значение по умолчанию
#             else:
#                 return None # Пустой ответ

#         # Для POST/PUT/PATCH и других
#         if response.status_code in [200, 201]: # Успех или Создано
#              try:
#                 return response.json()
#              except requests.exceptions.JSONDecodeError:
#                 # Успешный ответ без тела JSON (например, при загрузке файла иногда возвращается только статус)
#                 print(f"Информация: Успешный ответ {response.status_code} без JSON для {method.upper()} {url}.")
#                 return {"status_code": response.status_code, "message": "Success without JSON body"}
#         return None # Для других успешных статусов без тела

#     except requests.exceptions.Timeout:
#         print(f"Ошибка API запроса ({method.upper()} {url}): Таймаут ({timeout} сек)")
#         return None
#     except requests.exceptions.RequestException as e:
#         print(f"Ошибка API запроса ({method.upper()} {url}): {e}")
#         if hasattr(e, 'response') and e.response is not None:
#             print(f"Ответ сервера: Status={e.response.status_code}, Body={e.response.text[:500]}")
#         return None

# def get_or_create_product(base_url, api_key, product_name):
#     """Получает ID продукта по имени или создает новый продукт."""
#     # Кодируем имя продукта для URL
#     encoded_product_name = requests.utils.quote(product_name)
#     product_list_url = f"{base_url}/api/v2/products/?name={encoded_product_name}"
#     print(f"Поиск продукта '{product_name}' по URL: {product_list_url}")
#     response_data = make_api_request('GET', product_list_url, api_key)

#     if response_data and response_data.get('results'):
#         product_id = response_data['results'][0]['id']
#         print(f"Продукт '{product_name}' найден. ID: {product_id}")
#         return product_id
#     elif response_data is not None: # Запрос успешен, но results пуст
#         print(f"Продукт '{product_name}' не найден. Создание нового...")
#         product_create_url = f"{base_url}/api/v2/products/"
#         payload = {
#             "name": product_name,
#             "description": f"Автоматически созданный продукт для {product_name}",
#             "prod_type": 1 # По умолчанию 'Research and Development' - ID 1
#         }
#         create_response = make_api_request('POST', product_create_url, api_key, json=payload)
#         if create_response and create_response.get('id'):
#             product_id = create_response['id']
#             print(f"Продукт '{product_name}' успешно создан. ID: {product_id}")
#             return product_id
#         else:
#             print(f"Ошибка: Не удалось создать продукт '{product_name}'. Ответ API: {create_response}")
#             return None
#     else: # Ошибка при запросе GET
#          print(f"Ошибка при поиске продукта '{product_name}'.")
#          return None


# def get_or_create_engagement(base_url, api_key, product_id, engagement_name):
#     """Получает ID тестирования по имени в продукте или создает новое."""
#      # Кодируем имя тестирования для URL
#     encoded_engagement_name = requests.utils.quote(engagement_name)
#     engagement_list_url = f"{base_url}/api/v2/engagements/?product={product_id}&name={encoded_engagement_name}"
#     print(f"Поиск тестирования '{engagement_name}' в продукте ID {product_id}...")
#     response_data = make_api_request('GET', engagement_list_url, api_key)

#     if response_data and response_data.get('results'):
#         engagement_id = response_data['results'][0]['id']
#         print(f"Тестирование '{engagement_name}' найдено. ID: {engagement_id}")
#         return engagement_id
#     elif response_data is not None: # Запрос успешен, но results пуст
#         print(f"Тестирование '{engagement_name}' не найдено. Создание нового...")
#         engagement_create_url = f"{base_url}/api/v2/engagements/"
#         today = date.today()
#         # Устанавливаем target_end на неделю позже для CI/CD тестирований
#         target_end = today + timedelta(days=7)
#         payload = {
#             "name": engagement_name,
#             "product": product_id,
#             "target_start": today.strftime("%Y-%m-%d"),
#             "target_end": target_end.strftime("%Y-%m-%d"),
#             "active": True,
#             "status": "In Progress", # Статус "В процессе"
#             "engagement_type": "CI/CD" # Тип тестирования
#         }
#         create_response = make_api_request('POST', engagement_create_url, api_key, json=payload)
#         if create_response and create_response.get('id'):
#             engagement_id = create_response['id']
#             print(f"Тестирование '{engagement_name}' успешно создано. ID: {engagement_id}")
#             return engagement_id
#         else:
#             print(f"Ошибка: Не удалось создать тестирование '{engagement_name}'. Ответ API: {create_response}")
#             return None
#     else: # Ошибка при запросе GET
#          print(f"Ошибка при поиске тестирования '{engagement_name}'.")
#          return None

# # --- Основная функция загрузки ---
# def upload_report(base_url, api_key, product_id, engagement_id, scan_type, report_file):
#     """Загружает конкретный отчет в указанное тестирование."""
#     if not os.path.exists(report_file):
#         print(f"Ошибка: Файл отчета не найден: {report_file}")
#         return False # Возвращаем False вместо выхода, чтобы пайплайн мог продолжить

#     import_scan_url = f"{base_url}/api/v2/import-scan/"
#     # Используем только Authorization заголовок для этого эндпоинта
#     headers = {'Authorization': f'Token {api_key}'}
#     files = None
#     try:
#         files = {'file': (os.path.basename(report_file), open(report_file, 'rb'))}
#         data = {
#             'engagement': engagement_id, # Используем ID тестирования
#             'scan_type': scan_type,
#             'active': 'true',
#             'verified': 'true', # Помечаем уязвимости как проверенные
#             'skip_duplicates': 'true',
#              # 'auto_create_context': 'true' # Больше не нужно, так как создаем явно
#              'close_old_findings': 'true' # Закрывать уязвимости, не найденные в этом скане
#         }

#         print(f"Загрузка отчета '{os.path.basename(report_file)}' типа '{scan_type}' в тестирование ID {engagement_id}...")
#         # Используем data=data и files=files для multipart/form-data
#         response_data = make_api_request('POST', import_scan_url, api_key, files=files, data=data, timeout=600) # Увеличим таймаут до 10 минут

#         if response_data:
#             print(f"Отчет '{os.path.basename(report_file)}' успешно обработан DefectDojo.")
#             # Печатаем детали, если они есть
#             if isinstance(response_data, dict):
#                  print(f"Детали импорта: {response_data}")
#             return True
#         else:
#             print(f"Ошибка при импорте отчета '{os.path.basename(report_file)}'. Проверьте логи API запроса выше.")
#             return False
#     finally:
#         # Убедимся, что файл закрыт, даже если была ошибка
#         if files and 'file' in files and files['file'][1] and not files['file'][1].closed:
#             files['file'][1].close()
#             print(f"Файл '{os.path.basename(report_file)}' закрыт.")


# # --- Точка входа ---
# if __name__ == "__main__":
#     parser = argparse.ArgumentParser(description='Загрузчик отчетов сканирования в DefectDojo с созданием продукта/тестирования.')
#     parser.add_argument('--url', required=True, help='URL экземпляра DefectDojo (например, http://defectdojo.example.com)')
#     parser.add_argument('--key', required=True, help='API ключ DefectDojo')
#     parser.add_argument('--product', required=True, help='Имя продукта в DefectDojo')
#     parser.add_argument('--engagement', required=True, help='Имя тестирования (engagement) в DefectDojo')
#     parser.add_argument('--scan-type', required=True, help='Тип сканирования (например, "Semgrep JSON", "KICS JSON", "ZAP Scan")')
#     parser.add_argument('--file', required=True, help='Путь к файлу отчета сканирования')

#     args = parser.parse_args()

#     # Убираем /api/v2 если он есть, чтобы использовать базовый URL
#     base_api_url = args.url.rstrip('/').replace('/api/v2', '')

#     # 1. Получить или создать Продукт
#     product_id = get_or_create_product(base_api_url, args.key, args.product)
#     if not product_id:
#         print("Критическая ошибка: Не удалось получить или создать Продукт. Завершение.")
#         sys.exit(1)

#     # 2. Получить или создать Тестирование
#     engagement_id = get_or_create_engagement(base_api_url, args.key, product_id, args.engagement)
#     if not engagement_id:
#         print("Критическая ошибка: Не удалось получить или создать Тестирование. Завершение.")
#         sys.exit(1)

#     # 3. Загрузить отчет
#     success = upload_report(base_api_url, args.key, product_id, engagement_id, args.scan_type, args.file)

#     if not success:
#         # Выход с ошибкой, если загрузка не удалась, чтобы Jenkins мог это отловить
#         print(f"Загрузка отчета {args.file} не удалась.")
#         sys.exit(1)
#     else:
#         print(f"Скрипт успешно завершен для отчета {args.file}.") 

import requests
import sys

file_name = sys.argv[1]
scan_type = ''

if file_name == 'gitleaks.json':
    scan_type = 'Gitleaks Scan'
elif file_name == 'semgrep.json':
    scan_type = 'Semgrep JSON Report'


headers = {
        'Authorization': 'Token b6042dd7fe6395dc49140f24f99a5a9c65f1e9de'
        }

url = 'http://defectdojo.ru/api/v2/import-scan/'

data = {
        'active': True,
        'verified': True,
        'scan_type': scan_type,
        'minimum_serevity': 'Low',
        'engagement': 5
        }

files = {
        'file': open(file_name, 'rb')
        }

response = requests.post(url, headers=headers, data=data, files=files)

if response.status_code == 201:
    print('Scan results imported successfully')
else:
    print(f'Failed to import scan results: {response.content}')




#How to run >
# pip3 install requests
# python3 upload_reports.py gitleaks.json
# python3 upload_reports.py semgrep.json
