version="0.1.`git rev-list HEAD --count`"
name="bottlefs"

# remove old deb package
find ./ -name *.deb  | while read f; do  rm "$f"; done

# clear old lintian log
echo "" > "lintian.log"

rm -rf deb-pkg_create
mkdir deb-pkg_create
cp -R deb-pkg/* deb-pkg_create/

cd deb-pkg_create

find -type f | grep -re ~$ | while read f; do rm -rf "$f"; done

if [ ! -d "usr" ]; then
	mkdir "usr"  
fi

if [ ! -d "usr/bin" ]; then
	mkdir "usr/bin"  
fi

if [ ! -d "usr/share" ]; then
	mkdir "usr/share"  
fi


if [ ! -d "usr/share/doc" ]; then
	mkdir "usr/share/doc"
fi

if [ ! -d "usr/share/doc/bottlefs" ]; then
	mkdir "usr/share/doc/bottlefs"
fi

if [ ! -d "usr/share/bottlefs" ]; then
	mkdir "usr/share/bottlefs"
fi

if [ ! -d "usr/share/bottlefs/libs" ]; then
	mkdir "usr/share/bottlefs/libs"
fi

cd ../../bottlefs
	gradle makezip
	if [ ! -f "build/distributions/bottlefs.zip" ]; then
		echo "not found file .zip"
		exit;
	fi
	cp "build/distributions/bottlefs.zip" "../deb/deb-pkg_create/usr/share/bottlefs/bottlefs.zip"
cd ../deb/deb-pkg_create

cd usr/share/bottlefs/
	unzip bottlefs.zip > /dev/null
	rm bottlefs.zip
	rm LICENSE
	rm README.md
	rm run.bat
	rm run.sh
	rm -rf config.d
cd ../../../

cp ../../LICENSE usr/share/doc/bottlefs/copyright

# php-files

# must be uncommented:
# cp -R ../../php/bottlefs/* usr/share/bottlefs/

find usr/share/bottlefs/ -name *~  | while read f; do  rm "$f"; done
find usr/share/bottlefs/ -name .gitignore  | while read f; do  rm "$f"; done

# change log
echo "$name ($version) unstable; urgency=low" > usr/share/doc/bottlefs/changelog.Debian
echo "" >> usr/share/doc/bottlefs/changelog.Debian

git log --oneline | while read line
do
	echo "  * $line " >> usr/share/doc/bottlefs/changelog.Debian
done
echo "" >> usr/share/doc/bottlefs/changelog.Debian
echo " -- Evgenii Sopov <mrseakg@gmail.com> `date --rfc-2822` " >> usr/share/doc/bottlefs/changelog.Debian
echo "" >> usr/share/doc/bottlefs/changelog.Debian

gzip -9 usr/share/doc/bottlefs/changelog.Debian

# todo manual
# gzip -9 "usr/share/man/man1/bottlefs.1"

# help: https://www.debian.org/doc/manuals/maint-guide/dreq.ru.html

if [ ! -d "DEBIAN" ]; then
	mkdir "DEBIAN"  
fi

# config files
echo "/etc/bottlefs/bottles/attachments.properties" >> DEBIAN/conffiles

# control
# todo section ???

size=($(du -s ./))
size=${size[0]}
echo "Source: $name
Section: web
Priority: optional
Maintainer: Evgenii Sopov <mrseakg@gmail.com>
Depends: openjdk-7-jre
Version: $version
Installed-Size: $size
Homepage: https://github.com/sea-kg/bottlefs
Package: $name
Architecture: all
Description: Engine for search in documents
" > DEBIAN/control


# create md5sums
echo -n "" > DEBIAN/md5sums
find "." -type f | while read f; do
	md5sum "$f" >> DEBIAN/md5sums
done

find usr -type f | while read f; do  chmod 644 "$f"; done
find etc -type f | while read f; do  chmod 644 "$f"; done
find var -type f | while read f; do  chmod 644 "$f"; done
find DEBIAN -type f | while read f; do  chmod 644 "$f"; done

find usr -type d | while read d; do  chmod 755 "$d"; done
find etc -type d | while read d; do  chmod 755 "$d"; done
find var -type d | while read d; do  chmod 755 "$d"; done
find DEBIAN -type d | while read d; do  chmod 755 "$d"; done

chmod +x etc/init.d/bottlefs
chmod +x usr/share/bottlefs/bottlefs.sh
chmod +x DEBIAN/preinst
chmod +x DEBIAN/postinst
chmod +x DEBIAN/prerm
chmod +x DEBIAN/postrm

find usr -type f | while read f; do md5sum "$f"; done > DEBIAN/md5sums
find etc -type f | while read f; do md5sum "$f"; done >> DEBIAN/md5sums
find var -type f | while read f; do md5sum "$f"; done >> DEBIAN/md5sums

cd ..

echo "from deb-pkg_create"

#build
fakeroot dpkg-deb --build deb-pkg_create ./

# todo uncommneted:
rm -rf deb-pkg_create

#check
lintian *.deb > lintian.log
cat lintian.log


