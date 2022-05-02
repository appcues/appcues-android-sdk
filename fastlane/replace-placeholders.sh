# replace placeholders
if [[ "$OSTYPE" == "darwin"* ]]; then
  sed -i '' -e "s/PLACEHOLDER_ACCOUNT_ID/$2/g" ../samples/${1}/build.gradle
  sed -i '' -e "s/PLACEHOLDER_APPLICATION_ID/$3/g" ../samples/${1}/build.gradle
  sed -i '' -e "s/APPCUES_APPLICATION_ID/$3/g" ../samples/${1}/src/main/AndroidManifest.xml
else
  sed -i -e "s/PLACEHOLDER_ACCOUNT_ID/$2/g" ../samples/${1}/build.gradle
  sed -i -e "s/PLACEHOLDER_APPLICATION_ID/$3/g" ../samples/${1}/build.gradle
  sed -i -e "s/APPCUES_APPLICATION_ID/$3/g" ../samples/${1}/src/main/AndroidManifest.xml
fi
