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


�����������:
web-server ������� �������� ���������� ��� ������ ��� �� http

API ����� ����� ��� �������:
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

�� ���������� �����������:

- �������� ����� ����� ����������� ����� POST ������.
����������� �����
fileid: ����������� �� md5 (��� sha-1) �� ����������� ����� (��� ����� ������ ��� �� ������� ������� ��� ����)
����� ���������� ����� ������������ �� ������� 255 ��� 65536, ��� �� �������� ������� �� �������� �� ���������� ������
(���� ���� ���������� �� ������� ��� � ��������� ���������� � ���������� �����)
����� ������������ xml ���� � ������� ������������ ����������: fileid, filename, filetext
����� ���� xml ������������ �� �������������� � lucene
����: � ����� ��� ����������� ��������� ������!

- �������� ������
�� ����� hash �� ����� �� ���� ������� ����������������� �����
���� ��� ���� �� ����� �� ������� ��-�� ����� ���� ����� �� ������ bytearray
����: ������ ����� ������������ (��� ������� ������) � ��� ������ ������������� ������ �� � ����� ��� ����������

- ����� �� ������
����������� �����
�������� ������ ���� �� lucene ������� � ���������� ������ ��������� ������ �� ����� ������ 10 ��� 50
� ��� ������� � ������� ��� ���������, ����� �������� ������� ����� ������� ������ �� �������
��������(JSON):
{
	'count' : '3'
	'result' : [
		{ 'fileid' : '0842e519a5240a8ed129454eb4115494', 'filename' : '��.doc' }
		{ 'fileid' : 'a681902e65722c790df8bd891cdc0eab', 'filename' : '��.doc' }
		{ 'fileid' : '0b64b7b250a0a284c57fe39d9178cd60', 'filename' : '��1.doc' }
	]
}

- ������ ������������������
������� ������,
�� ��� ������� �� ������ ������ � �� �� �������� �������� ��� ������ �������������������.
�� �������� ������ ��� ��� ���� � ������������ �� ������.
����� ����� ������� ������� �������� ���������� ��������� �� ������� ������������� xml

- ������ �� ������������� �������� � ������� �� ������ (���� ������ ���������� ����� �����)
��� ������ ������� �������� ������ ����������� ������� ��� "� ����� �� � ����� ���� � �������"
�� ��� ���������� ������ ������ �������� "�� ��� ���� ��� �������������� � ������ �� ���� �� ������� ���� ��������� ������� �����"
������ ���������� ��� � ��� ������� ������ � �������� ������ ���������� ��� ��� �������������
����� ��������� ��������� ������ �������������� ����� ������� ������������ (5 ����� �������� ��� 1 ���)

- rabbit
�������� ����� � �������� ��������� ����

�� ��������� � �������:
��� ����� ��������� ��� ������ ��� ��� �������� ��� �� ���:
java -jar /usr/bin/bottle-fs-0.1.jar /etc/bottle-fs/config.xml

����� ��� �������� ����� ������� deb-����� �/��� init.d ������� (��� ��� ��� � ������)

��� �� ��������� � ��������� ������ �� ������ �����:
 - ���������� ������ �� �����
 - ��������� ����� ������������,
 - ��������� ����� �� ���������
 - ��������� ����� ������� ��� ��������� ������ ������������������
� ����� ������ ��� �� ������ �����

��������� ������ config.xml:
<bottles>
	<bottle name="yoursite.su">
		<option name="files.index.path">/var/tmp/bottlefs/</option>
		<option name="files.path">/var/usr/share/bottlefs</option>
		<option name="files.backup.path">/var/usr/share/bottlefs</option>
		<option name="trusted.http">http://yoursite.su/bottlefs/</option>
		<option name="trusted.ip">127.0.0.1</option>
	</bottle>
</bottles>
