# replace placeholders
if [[ "$OSTYPE" == "darwin"* ]]; then
  sed -i '' -e "s/PLACEHOLDER_ACCOUNT_ID/$2/g" ../samples/${1}/build.gradle
  sed -i '' -e "s/PLACEHOLDER_APPLICATION_ID/$3/g" ../samples/${1}/build.gradle
  sed -i '' -e "s/APPCUES_APPLICATION_ID/$3/g" ../samples/${1}/src/main/AndroidManifest.xml
  sed -i '' -e "s/GOOGLE_SERVICES_KEY/$4/g" ../samples/${1}/google-services.json
else
  sed -i -e "s/PLACEHOLDER_ACCOUNT_ID/$2/g" ../samples/${1}/build.gradle
  sed -i -e "s/PLACEHOLDER_APPLICATION_ID/$3/g" ../samples/${1}/build.gradle
  sed -i -e "s/APPCUES_APPLICATION_ID/$3/g" ../samples/${1}/src/main/AndroidManifest.xml
  sed -i -e "s/GOOGLE_SERVICES_KEY/$4/g" ../samples/${1}/google-services.json
fi
