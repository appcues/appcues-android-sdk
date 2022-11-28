#!/bin/sh

# MIT License
# 
# Copyright (c) 2021 Segment
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

# Original source: https://github.com/segmentio/analytics-swift/blob/main/release.sh

# check if `gh` tool is installed.
if ! command -v gh &> /dev/null
then
	echo "Github CLI tool is required, but could not be found."
	echo "Install it via: $ brew install gh"
	exit 1
fi

# check if `gh` tool has auth access.
# command will return non-zero if not auth'd.
authd=$(gh auth status -t)
if [[ $? != 0 ]]
then
	echo "ex: $ gh auth login"
	exit 1
fi

# check that we're on the `main` or `release` branch
branch=$(git rev-parse --abbrev-ref HEAD)
if [[ $branch != 'main' ]] && [[ $branch != release/* ]]
then
	echo "The 'main' or 'release/*' must be the current branch to make a release."
	echo "You are currently on: $branch"
	exit 1
fi

if [ -n "$(git status --porcelain)" ]
then
    echo "There are uncommited changes. Please commit and create a pull request or stash them.";
    exit 1
fi

# check that the required maven central signing and auth vars are set
if [ -z ${GPG_SIGNING_KEY+x} ]; then echo "GPG_SIGNING_KEY must be set for Maven Central publishing"; exit 1; fi
if [ -z ${GPG_SIGNING_KEY_PWD+x} ]; then echo "GPG_SIGNING_KEY_PWD must be set for Maven Central publishing"; exit 1; fi
if [ -z ${OSSRH_USERNAME+x} ]; then echo "OSSRH_USERNAME must be set for Maven Central publishing"; exit 1; fi
if [ -z ${OSSRH_PASSWORD+x} ]; then echo "OSSRH_PASSWORD must be set for Maven Central publishing"; exit 1; fi

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

echo "Appcues SDK current version: $version"

# no args, so give usage.
if [ $# -eq 0 ]
then
	echo "Release automation script"
	echo ""
	echo "Usage: $ ./release.sh <version>"
	echo "   ex: $ ./release.sh \"1.0.2\""
	exit 0
fi

newVersion="${1}"
echo "Preparing to release $newVersion..."

versionComparison=$(./fastlane/semver.sh $newVersion $version)

if [ $versionComparison != '1' ]
then
	echo "New version must be greater than previous version ($version)."
	exit 1
fi

read -r -p "Are you sure you want to release $newVersion? [y/N] " response
case "$response" in
	[yY][eE][sS]|[yY])
		;;
	*)
		exit 1
		;;
esac

# get the commits since the last release, filtering ones that aren't relevant.
changelog=$(git log --pretty=format:"- [%as] %s (%h)" $(git describe --tags --abbrev=0 @^)..@ --abbrev=7 | sed '/[🔧🎬⬆️📸✅💡📝]/d')
tempFile=$(mktemp)
echo $changelog
# write changelog to temp file.
echo "$changelog" >> $tempFile

# get the components of the version string for the properties file update
newMajorVersion=$(echo $newVersion | cut -sd. -f1)
newMinorVersion=$(echo $newVersion | cut -sd. -f2)
newPatchVersion=$(echo $newVersion | cut -sd. -f3 | cut -d- -f1)
newVersionClassifier=$(echo $newVersion | cut -sd- -f2)

# update appcues/appcues.properties
sed -i '' "/^VERSION_MAJOR /s/=.*$/= $newMajorVersion/" ./appcues/appcues.properties
sed -i '' "/^VERSION_MINOR /s/=.*$/= $newMinorVersion/" ./appcues/appcues.properties
sed -i '' "/^VERSION_PATCH /s/=.*$/= $newPatchVersion/" ./appcues/appcues.properties
sed -i '' "/^VERSION_CLASSIFIER /s/=.*$/= $newVersionClassifier/" ./appcues/appcues.properties

# commit the version change.
git commit -am "🔖 Update version to $newVersion"
git push
# gh release will make both the tag and the release itself.
gh release create $newVersion -F $tempFile -t $newVersion --target $branch

# remove the tempfile.
rm $tempFile

# publish to Maven Central
gradle appcues:publishReleasePublicationToOSSRHRepository

# compile the docs for the new version and deploy to GitHub pages
./fastlane/docs.sh
