mvn build-helper:parse-version versions:set -DnewVersion="`${parsedVersion.majorVersion}.`${parsedVersion.minorVersion}.`${parsedVersion.nextIncrementalVersion}"
mvn versions:commit
git add pom.xml
git -c "user.name=Github Actions" -c "user.email=zhivaev993@gmail.com" commit -m "Update build number"
