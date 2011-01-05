# Dashboard's Makefile

LEIN=lein
PROJECT=dashboard
DEFAULT_SITE=default
VERSION=$(shell cat .version)

test:
	$(LEIN) test

repl:
	$(LEIN) repl

start:
	$(LEIN) daemon start $(DEFAULT_SITE)

stop:
	$(LEIN) daemon stop $(DEFAULT_SITE)

restart:
	$(LEIN) daemon stop $(DEFAULT_SITE)
	$(LEIN) daemon start $(DEFAULT_SITE)

run: restart
	$(LEIN) daemon check $(DEFAULT_SITE)

dist: test
	git tag -a -f -m "Making release $(VERSION)" rel-$(VERSION)
	git archive --prefix=$(PROJECT)-$(VERSION)/ rel-$(VERSION) | bzip2 > ../$(PROJECT)-$(VERSION).tar.bz2

deploy: dist
	scp ../$(PROJECT)-$(VERSION).tar.bz2 deploy.sh $(DEPLOYTO):
	ssh $(DEPLOYTO) sh ./deploy.sh $(PROJECT)-$(VERSION)
	./increment_version.py .version
	git add .version
	git commit -m "Version update"

### Local variables: ***
### compile-command:"make" ***
### tab-width: 2 ***
### End: ***
