#!/bin/bash

stty -echo

pushd GeofenceEditorLib
ant release
popd

ant apikey-release
rm -rf google-play-services_lib/bin
ant deploy
ant apikey-debug
rm libs/geofenceeditorlib.jar

stty echo
