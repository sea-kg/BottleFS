# BottleFS
Bottle File System

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


Архитектура:
web-server который является прослойкой для работы апи по http

API будет иметь три функции:
- upload-file
- download-file
- search-file
- full-index

API. upload-file
input parameters:
* secret-token - type string
* filename - type string
* file - type bytearray
output parameters:
* fileid - type string
* filetext - converted file to textfile (use tika)

API. download-file
input parameters:
* fileid - type string
output parameters:
* file - bytearray (use tika)

API. search-file
input parameters:
* secret-token - type string
* query - type string
* format - type string: xml, json, ruby-array, php-array
output parameters:
* list - list of fileid and filename
* count - count of result search (not more than 10 or 50 or 100)

API. full-index
input parameters:
none
output parameters:
status
remark: only from localhost and only one process!!!

API. rabbit
input parameters:
none
output parameters:
bytearray

По внутренней архитектуре:

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

- поиск по файлам
проверяется токен
получаем запрос ищем по lucene индексу и возвращаем список найденных файлов не более первых 10 или 50
в том формате в котором нас попросили, также указывая сколько всего найдено файлов по запросу
например(JSON):
{
	'count' : '3'
	'result' : [
		{ 'fileid' : '0842e519a5240a8ed129454eb4115494', 'filename' : 'ТЗ.doc' }
		{ 'fileid' : 'a681902e65722c790df8bd891cdc0eab', 'filename' : 'ТЗ.doc' }
		{ 'fileid' : '0b64b7b250a0a284c57fe39d9178cd60', 'filename' : 'ТЗ1.doc' }
	]
}

- полное переиндексирвоание
Очищаем индекс,
на все запросы по поиску файлов и по из загрузке отвечаем что заняты переиндексированием.
по выгрузке файлов тут все норм и спокойненько их отдаем.
далее после очистки индекса начинаем рекурсивно проходить по заранее заготовленным xml

- защита от нежелательных загрузок и поисков по файлам (типа только доверенным лицам можно)
при старте сервиса делается запрос доверенному серверу что "я такой то и такой стою и работаю"
на что доверенный сервис должен ответить "ок вот тебе мой индентификатор и только на него ты отвечай всех остальный посылай нахуй"
сервис запоминает его и при запросе поиска и загрузки файлов использует его для идентификации
также процедуру получения нового идентификатора лучше сделать переодически (5 минут например или 1 час)

- rabbit
кидается кубик и отдается случайный файл

По настройке и запуску:
это будет полностью веб сервер так что примерно как то так:
java -jar /usr/bin/bottle-fs-0.1.jar /etc/bottle-fs/config.xml

также для простоты можно создать deb-пакет и/или init.d скрипты (или что там в почете)

что бы перевезти и запустить сервер на другой тачке:
 - установить сервис на тачку
 - перенести файлы конфигурации,
 - перенести файлы из хранилища
 - перенести файлы индекса или запустить полное переиндексирование
и вуаля сервис уже на другой тачке

возможный пример config.xml:
<bottles>
	<bottle name="yoursite.su">
		<option name="files.index.path">/var/tmp/bottlefs/</option>
		<option name="files.path">/var/usr/share/bottlefs</option>
		<option name="files.backup.path">/var/usr/share/bottlefs</option>
		<option name="trusted.http">http://yoursite.su/bottlefs/</option>
		<option name="trusted.ip">127.0.0.1</option>
	</bottle>
</bottles>
