#!/usr/bin/env bash

if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
	openssl aes-256-cbc -K $encrypted_e3bbf89066f0_key -iv $encrypted_e3bbf89066f0_iv -in ci/codesigning.asc.enc -out ci/codesigning.asc -d
	gpg --fast-import ci/codesigning.asc

	mvn deploy -P ossrh --settings ci/mvnsettings.xml
fi