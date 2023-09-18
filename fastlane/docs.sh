#!/bin/sh

repoName="appcues-android-sdk"
initialBranch=$(git rev-parse --abbrev-ref HEAD)

# read the properties file
while IFS= read -r line; do
    key="${line%% =*}"
    value="${line#*= }"
    eval "$key"="'$value'"
done < ./appcues/appcues.properties
versionMajor=$VERSION_MAJOR
versionMinor=$VERSION_MINOR
versionPatch=$VERSION_PATCH
versionClassifier=$VERSION_CLASSIFIER

# construct the version string
version="$versionMajor.$versionMinor.$versionPatch"
if [ ! -z "$versionClassifier" ]
then
    version="$version-$versionClassifier"
fi

# check that we're on the `main` branch
if [ $initialBranch != 'main' ]
then
    read -r -p "Current branch: $initialBranch. Documentation should usually be updated from the main branch. Are you sure you want to continue? [y/N] " response
    case "$response" in
        [yY][eE][sS]|[yY])
            ;;
        *)
            exit 1
            ;;
    esac
fi

gradle dokkaHtml

cd ./appcues/build/dokka/html
zip -r ../../../../docs.zip ./*
cd ../../../../
git checkout gh-pages
# removing existing docs to ensure no old files linger
rm -rf docs
mkdir docs
cd docs
unzip -o ../docs.zip
rm ../docs.zip

git add .
git commit -m "📝 Update compiled docs for $version"
git push

git checkout $initialBranch