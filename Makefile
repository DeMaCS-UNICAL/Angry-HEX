.PHONY: all java plugin clean

all: java plugin

java:
	@test ! -e dev.angryhex.sh || { \
		echo "use this Makefile only via ./dev.angryhex.sh! (works only in source-staging subdirectory 'angryhex')"; \
		/bin/false; \
	}
	ant jar

plugin:
	{ \
		cd src/angrybirds-box2dplugin ; \
		./configure -C ; \
		make install ; \
	}

clean:
	ant -f build.xml clean
