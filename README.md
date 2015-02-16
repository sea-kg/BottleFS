# BottleFS
Bottle File System
Status: uncompleted!

How to Build:
$ cd bottlefs
$ gradle build

Description:
Multithread web-server:
  - upload files to server
  - indexing files
  - search by content
  - download files from server

Language:
java-1.7 or java-1.8

Using Libraries:
- for conversion files to text, tika:
    http://www.apache.org/dyn/closer.cgi/tika/tika-app-1.7.jar
- for indexing and search, lucene:
    http://apache-mirror.rbc.ru/pub/apache/lucene/java/4.10.3/
    http://lucene.apache.org/core/downloads.html

Architech:
HTTP-WEB-SERVER with api.

API. will be has methods:

http://localhost:8086/upload
input parameters:
* filename - type string
* file - type bytearray
* metadata - type bytearray (json), it's data for description this file
or input parameters:
* file-http - link to http file
* metadata - type bytearray (json), it's data for description this file
output parameters:
* fileid - type string
* filetext - converted file to textfile (used tika)

http://localhost:8086/download
input parameters:
* fileid - type string
output parameters:
* file - bytearray (use tika)

http://localhost:8086/search
input parameters:
* query - type string
* format - type string: json, xml (default: json)
output parameters:
* list - list of fileid and filename
* count - count of result search (not more than 10 or 50 or 100)

http://localhost:8086/start-reindexing
input parameters:
* none
output parameters:
* status
remark: only from localhost and only one process!!!

http://localhost:8086/stop-reindexing
input parameters:
* none
output parameters:
* status

http://localhost:8086/help
retrun some information (version list of functions and another)


# How it must be work:

- Загрузка файла будет происходить через POST запрос.
проверяется токен
fileid: вычисляется по md5 (или sha-1) от содержимого файла (или может вообще что то нелепое генерим хбз пока)
далее содержимое файла складывается по правилу 255 или 65536, что бы файловая система не охренела от количества файлов
(если файл существует то бэкапим его в отдельную диреткорию и записываем новый)
рядом складывается xml файл в котором записывается информация: fileid, filename, filetext
далее этот xml отправляется на индексирование в lucene
фича: а можно еще попробовать сгенерить иконку!

- выгрузка файлов
на входе hash от файла по нему генерим месторасположение файла
ищем его если не нашли то говорим тю-тю файла если нашли то отдаем bytearray
фича: храним файлы запакованные (для лучшего сжатия) а при отдаче распаковываем сперва их а потом уже возвращаем

- search in files

Example request:
http://localhost:8086/bottle1/search-file?query=hello&page=2&format=json

Example response:
{
	'count' : '3'
	'result' : [
		{
			'fileid' : '0842e519a5240a8ed129454eb4115494', 'filename' : 'ТЗ.doc', 'metadata' : {
				'someid': '123'
			}
		}
		{ 'fileid' : 'a681902e65722c790df8bd891cdc0eab', 'filename' : 'ТЗ.doc' }
		{ 'fileid' : '0b64b7b250a0a284c57fe39d9178cd60', 'filename' : 'ТЗ1.doc' }
	]
}

- start reindexing
clean index (remove all indexes files)
на все запросы по поиску файлов и по из загрузке отвечаем что заняты переиндексированием.
по выгрузке файлов тут все норм и спокойненько их отдаем.
далее после очистки индекса начинаем рекурсивно проходить по заранее заготовленным xml

- защита от нежелательных загрузок и поисков по файлам (типа только доверенным лицам можно)
при старте сервиса делается запрос доверенному серверу что "я такой то и такой стою и работаю"
на что доверенный сервис должен ответить "ок вот тебе мой индентификатор и только на него ты отвечай всех остальный посылай нахуй"
сервис запоминает его и при запросе поиска и загрузки файлов использует его для идентификации
также процедуру получения нового идентификатора лучше сделать переодически (5 минут например или 1 час)

Configuration and run:
web server:
java -jar /usr/bin/bottlefs-all.jar /etc/bottlefs/config.d/


также для простоты можно создать deb-пакет и/или init.d скрипты (или что там в почете)

что бы перевезти и запустить сервер на другой тачке:
 - установить сервис на тачку
 - перенести файлы конфигурации,
 - перенести файлы из хранилища
 - перенести файлы индекса или запустить полное переиндексирование
и вуаля сервис уже на другой тачке

bottle2.properties:
  # base 
  name=bottle2
  port=8086

  # trusted ip-servers
  trusted.ip=127.0.0.1
  trusted.ip=89.76.1.245

  # config for file-storage and index
  files.index.path=tmp/bottlefs-02-index
  files.path=data/bottlefs-02-files

  # config for metadata
  metadata.textfields=someid,someid2,someid3
